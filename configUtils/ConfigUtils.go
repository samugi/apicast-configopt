package configUtils

import (
	"bytes"
	"encoding/gob"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"os"
	"path/filepath"
	"reflect"
	"strings"
	"time"

	"github.com/samugi/apicast-configopt/globalUtils"
	"github.com/samugi/apicast-configopt/model"

	"github.com/samugi/simple-clargs/clargs"
)

var OptionConfig clargs.Option
var OptionOutput clargs.Option
var OptionVerbose clargs.Option
var OptionInteractive clargs.Option
var OptionPathRoutingOnly clargs.Option
var OptionHelp clargs.Option
var OptionConfirmAll clargs.Option
var OptionAutoFix clargs.Option
var OptionUpdateRemote clargs.Option
var Mode string
var FullConfig model.DynamicConfig
var FullConfigBytes []byte

const (
	ModeScan            = "SCAN"
	ModeInteractive     = "INTERACTIVE"
	ModeAutoFix         = "AUTOFIX"
	AutoOptimize        = "opt"
	AutoFix             = "fix"
	ConfigFromDump      = "CONFIG_FROM_DUMP"
	ConfigBoot          = "CONFIG_BOOT"
	ConfigConfig        = "CONFIG_CONFIG"
	ConfigSingleService = "CONFIG_SINGLE_SERVICE"
)

func ExtractConfigJSONFromFileWithStructs(inputFilePath string) model.Configuration {
	var configuration model.Configuration
	absFile, err := filepath.Abs(inputFilePath)
	if err != nil {
		panic(err)
	}
	jsonFile, err := os.Open(absFile)
	check(err)
	defer jsonFile.Close()
	byteValue, _ := ioutil.ReadAll(jsonFile)

	configType := ScanConfigType(byteValue)

	FullConfig = ExtractFullConfig(byteValue)
	FullConfigBytes = byteValue

	switch configType {
	case ConfigFromDump:
		json.Unmarshal(byteValue, &configuration)
		break
	case ConfigBoot:
		var configBoot model.ConfigBoot
		json.Unmarshal(byteValue, &configBoot)
		for _, content := range configBoot.Config.Services {
			pc := model.ProxyConfig{Content: content}
			pco := model.ProxyConfigOuter{ProxyConfig: pc}
			configuration.ProxyConfigsOuter = append(configuration.ProxyConfigsOuter, pco)
		}
		break
	case ConfigConfig:
		var configConfig model.ConfigConfig
		json.Unmarshal(byteValue, &configConfig)
		for _, content := range configConfig.Services {
			pc := model.ProxyConfig{Content: content}
			pco := model.ProxyConfigOuter{ProxyConfig: pc}
			configuration.ProxyConfigsOuter = append(configuration.ProxyConfigsOuter, pco)
		}
		break
	case ConfigSingleService:
		var content model.Content
		json.Unmarshal(byteValue, &content)
		pc := model.ProxyConfig{Content: content}
		pco := model.ProxyConfigOuter{ProxyConfig: pc}
		configuration.ProxyConfigsOuter = append(configuration.ProxyConfigsOuter, pco)
		break
	}

	return configuration
}

func ScanConfigType(byteVal []byte) string {
	var objmap map[string]interface{}
	json.Unmarshal(byteVal, &objmap)
	if _, ok := objmap["config"]; ok {
		return ConfigBoot
	}
	if _, ok := objmap["services"]; ok {
		return ConfigConfig
	}
	if _, ok := objmap["proxy_configs"]; ok {
		return ConfigFromDump
	}
	if _, ok := objmap["proxy"]; ok {
		return ConfigSingleService
	}
	return ""
}

func check(e error) {
	if e != nil {
		panic(e)
	}
}

func ExtractStringFromJSON(dict map[string]interface{}, key string) string {
	if val, ok := dict[key]; ok {
		return val.(string)
	}
	return ""
}

func GetBytes(key interface{}) []byte {
	var buf bytes.Buffer
	enc := gob.NewEncoder(&buf)
	err := enc.Encode(key)
	if err != nil {
		return nil
	}
	return buf.Bytes()
}

func ValidateAllProxies(config model.Configuration) {
	proxyGroups := createProxyGroups(&config)

	for ind := 0; ind < len(proxyGroups); ind++ {
		var allRulesToVerify []*model.MappingRule
		backendRules := make(map[int64][]*model.MappingRule)
		for proxind := 0; proxind < len(proxyGroups[ind]); proxind++ {
			proxyPointer := proxyGroups[ind][proxind]
			rulesPnt := &((*proxyPointer).Proxy_rules)
			for pRulesInd := 0; pRulesInd < len(*rulesPnt); pRulesInd++ {
				// only add proxy rules, not backend rules
				if (*rulesPnt)[pRulesInd].Owner_type == nil || *(*rulesPnt)[pRulesInd].Owner_type == "" || *(*rulesPnt)[pRulesInd].Owner_type == model.OwnerTypeProxy {
					allRulesToVerify = append(allRulesToVerify, &((*rulesPnt)[pRulesInd]))
				} else if (*rulesPnt)[pRulesInd].Owner_type != nil && *(*rulesPnt)[pRulesInd].Owner_type == model.OwnerTypeBackend {
					var id int64
					if (*rulesPnt)[pRulesInd].Owner_id != nil {
						id = *(*rulesPnt)[pRulesInd].Owner_id
					}
					if _, ok := backendRules[id]; ok {
						backendRules[id] = append(backendRules[id], &((*rulesPnt)[pRulesInd]))
					} else {
						backendRules[id] = []*model.MappingRule{&((*rulesPnt)[pRulesInd])}
					}
				}
			}
		}
		for indexRules := 0; indexRules < len(allRulesToVerify); indexRules++ {
			validateMappingRule(allRulesToVerify[indexRules], allRulesToVerify, indexRules+1)
		}

		for k := range backendRules {
			for j := 0; j < len(backendRules[k]); j++ {
				validateMappingRule(backendRules[k][j], backendRules[k], j+1)
				time.Sleep(time.Millisecond)
			}
		}
		if Mode == ModeScan {
			PrintIssues()
		}
	}
}

func InitializeRules(config model.Configuration) {
	proxyConfigs := config.ProxyConfigsOuter
	for indexPC := 0; indexPC < len(proxyConfigs); indexPC++ {
		proxy := proxyConfigs[indexPC].ProxyConfig.Content.Proxy
		host := proxy.Endpoint
		rules := proxy.Proxy_rules
		for indexRules := 0; indexRules < len(rules); indexRules++ {
			rules[indexRules].Initialize(host)
			model.LongestRuleLength = model.Max(model.LongestRuleLength, len(*rules[indexRules].Pattern))
			model.LongestHostLength = model.Max(model.LongestHostLength, len(rules[indexRules].Host))
		}
	}
}

func validateMappingRule(rule *model.MappingRule, allRules []*model.MappingRule, index int) {
	if Mode == ModeAutoFix || Mode == ModeInteractive {
		index = 0 //in this case we start from the beginning of the list because these modes potentially change the rules
	}
	for i := index; i < len(allRules); i++ {
		currentRule := (allRules)[i]
		if *(rule.Id) == *(currentRule.Id) {
			continue
		}
		severity := calculateSeverity(rule, currentRule)

		if Mode == ModeScan {
			var description string

			if rule.BrutalMatch(currentRule) {
				description = globalUtils.RulesMatch
			} else if rule.CanBeOptimized(currentRule) {
				description = globalUtils.RulesOpt
			} else {
				continue
			}

			rules := []model.MappingRule{*rule, *currentRule}
			issue := model.Issue{Rules: rules, Description: description, Severity: severity}
			globalUtils.Issues = append(globalUtils.Issues, issue)

		} else if Mode == ModeInteractive {
			if rulesNotDeletedMatch(rule, currentRule) {
				keep := !OptionConfirmAll.ValueB() && requestMappingKeep(*rule, *currentRule, true)
				if !keep {
					(*currentRule).SetMarkedForDeletion(true)
				} else {
					keep2 := !OptionConfirmAll.ValueB() && requestMappingKeep(*currentRule, *rule, false)
					if !keep2 {
						(*rule).SetMarkedForDeletion(true)
					}
				}
			} else if rulesNotDeletedCanBeOptimized(rule, currentRule) {
				optimize := OptionConfirmAll.ValueB() || requestOptimization(*currentRule, *rule)
				shorter := model.GetShorter(currentRule, rule)
				var longer *model.MappingRule
				if reflect.DeepEqual(shorter, currentRule) {
					longer = rule
				} else {
					longer = currentRule
				}
				if optimize {
					if shorter.IsExactMatch {
						(*shorter).SetExactMatch(false)
					}
					(*longer).SetMarkedForDeletion(true)
				}
			}
		} else if Mode == ModeAutoFix {
			if rulesNotDeletedMatchHighSev(rule, currentRule) {
				ruleToDelete := getAutoDelete(rule, currentRule)
				(*ruleToDelete).SetMarkedForDeletion(true)
			} else if rulesNotDeletedMatchOrOptimize(rule, currentRule) {
				shorter := model.GetShorter(currentRule, rule)
				var longer *model.MappingRule
				if reflect.DeepEqual(shorter, currentRule) {
					longer = rule
				} else {
					longer = currentRule
				}

				if shorter.IsExactMatch {
					(*shorter).SetExactMatch(false)
				}
				(*longer).SetMarkedForDeletion(true)
			}
		}
	}
}

func rulesNotDeletedMatchOrOptimize(rule1 *model.MappingRule, rule2 *model.MappingRule) bool {
	return !(*rule1).IsMarkedForDeletion && !(*rule2).IsMarkedForDeletion && rule1.Matches(*rule2) && OptionAutoFix.Value() == AutoOptimize
}

func rulesNotDeletedMatchHighSev(rule1 *model.MappingRule, rule2 *model.MappingRule) bool {
	return rulesNotDeletedMatch(rule1, rule2) && calculateSeverity(rule1, rule2) == 1
}

func rulesNotDeletedMatch(rule1 *model.MappingRule, rule2 *model.MappingRule) bool {
	return !(*rule1).IsMarkedForDeletion && !(*rule2).IsMarkedForDeletion && rule1.BrutalMatch(rule2)
}

func rulesNotDeletedCanBeOptimized(rule1 *model.MappingRule, rule2 *model.MappingRule) bool {
	return !(*rule1).IsMarkedForDeletion && !(*rule2).IsMarkedForDeletion && rule1.CanBeOptimized(rule2)
}

//for now let's auto delete the longest
func getAutoDelete(rule1 *model.MappingRule, rule2 *model.MappingRule) *model.MappingRule {
	shorter := model.GetShorter(rule1, rule2)
	var longer *model.MappingRule
	if reflect.DeepEqual(shorter, rule1) {
		longer = rule2
	} else {
		longer = rule1
	}
	return longer
}

func calculateSeverity(rule1 *model.MappingRule, rule2 *model.MappingRule) (retSev int) {
	retSev = 2
	if rule1.CanBeOptimized(rule2) {
		retSev = 5
	} else if rule1.Owner_type != nil && *rule1.Owner_type == model.OwnerTypeBackend {
		retSev = 4
	} else if (rule1.Host == rule2.Host || globalUtils.PathRoutingOnly) && *rule1.Proxy_id != *rule2.Proxy_id {
		retSev = 1
	}
	return
}

func createProxyGroups(config *model.Configuration) (proxyGroups [][]*model.Proxy) {
	proxyGroupsMap := make(map[string][]*model.Proxy)
	proxyConfigs := &((*config).ProxyConfigsOuter)
	//(*proxyConfigs)[0].ProxyConfig.Content.Proxy.Proxy_rules[0].SetMarkedForDeletion(true)
	if !globalUtils.PathRoutingOnly {
		//PATH ROUTING ONLY NOT ENABLED: for each service in the config, map by host in serviceGroupsMap
		for i := 0; i < len(*proxyConfigs); i++ {
			proxyAdr := &((*proxyConfigs)[i].ProxyConfig.Content.Proxy)
			host := (*proxyAdr).Endpoint
			if _, ok := proxyGroupsMap[host]; ok {
				proxyGroupsMap[host] = append(proxyGroupsMap[host], proxyAdr)
			} else {
				proxyGroupsMap[host] = []*model.Proxy{proxyAdr}
			}
		}
		for k := range proxyGroupsMap {
			proxyGroups = append(proxyGroups, proxyGroupsMap[k])
		}
	} else {
		var proxies []*model.Proxy
		for i := 0; i < len(*proxyConfigs); i++ {
			proxyAdr := &((*proxyConfigs)[i].ProxyConfig.Content.Proxy)
			proxies = append(proxies, proxyAdr)
		}
		proxyGroups = append(proxyGroups, proxies)
	}

	_ = proxyGroups
	_ = proxyGroupsMap
	return
}

func requestOptimization(currentRule model.MappingRule, rule model.MappingRule) bool {
	shorter := model.GetShorter(&currentRule, &rule)
	var longer model.MappingRule
	if shorter.Id == currentRule.Id {
		longer = rule
	} else {
		longer = currentRule
	}

	if !shorter.IsExactMatch {
		panic("optimizable not ending with $")
	}
	fmt.Println("These rules\n" + shorter.String() + ",\n" + longer.String() + "\nCould be optimized by removing the dollar from ID:" + fmt.Sprint(*shorter.Id) + " and deleting ID:" + fmt.Sprint(*longer.Id) + ". Would you like to proceed?  Y/N")
	//reader := bufio.NewReader(os.Stdin)
	var response string
	for {
		fmt.Scanln(&response)
		if strings.EqualFold(response, "Y") {
			return true
		} else if strings.EqualFold(response, "N") {
			return false
		}
		fmt.Println("Invalid response, would you like to proceed? Y/N")
	}
}

func requestMappingKeep(rule1 model.MappingRule, rule2 model.MappingRule, ask bool) bool {
	if ask {
		fmt.Println("This rule:\n" + rule1.String() + "\ncollides with:\n" + rule2.String())
	}
	fmt.Println("Would you like to keep ID:" + fmt.Sprint(*rule2.Id) + "?  Y/N")
	var response string
	for {
		fmt.Scanln(&response)
		if strings.EqualFold(response, "Y") {
			return true
		} else if strings.EqualFold(response, "N") {
			return false
		}
		fmt.Println("Invalid response, would you like to keep ID:" + fmt.Sprint(*rule2.Id) + "? Y/N")
	}
}
