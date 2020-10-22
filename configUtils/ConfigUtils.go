package configUtils

import (
	"bytes"
	"configopt/globalUtils"
	"configopt/model"
	"configopt/option"
	"configopt/output"
	"encoding/gob"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"os"
	"path/filepath"
	"reflect"
	"strings"
)

var OptionConfig option.Option
var OptionOutput option.Option
var OptionVerbose option.Option
var OptionInteractive option.Option
var OptionPathRoutingOnly option.Option
var OptionHelp option.Option
var OptionConfirmAll option.Option
var Mode string

const (
	ModeScan            = "SCAN"
	ModeInteractive     = "INTERACTIVE"
	ConfigFromDump      = "CONFIG_FROM_DUMP"
	ConfigBoot          = "CONFIG_BOOT"
	ConfigConfig        = "CONFIG_CONFIG"
	ConfigSingleService = "CONFIG_SINGLE_SERVICE"
	OwnerTypeProxy      = "Proxy"
	OwnerTypeBackend    = "BackendApi"
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

	configType := scanConfigType(byteValue)

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

func scanConfigType(byteVal []byte) string {
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

// func ExtractConfigJSONFromFileWithInterfaces(inputFilePath string) map[string][]map[string]interface{} {
// 	jsonFile, err := os.Open(inputFilePath)
// 	check(err)
// 	defer jsonFile.Close()
// 	byteValue, _ := ioutil.ReadAll(jsonFile)
// 	var result map[string][]map[string]interface{}
// 	json.Unmarshal([]byte(byteValue), &result)

// 	proxy_configs := result["proxy_configs"]
// 	for _, res := range proxy_configs {
// 		proxy_config := res["proxy_config"].(map[string]interface{})
// 		content := proxy_config["content"].(map[string]interface{})
// 		proxy := content["proxy"].(map[string]interface{})
// 		proxy_rules_arr := proxy["proxy_rules"].([]interface{})
// 		for k, r := range proxy_rules_arr {
// 			//	id := r["id"]
// 			mr := r.(map[string]interface{}) //FIXME boh
// 			id := ExtractFloatFromJSON(mr, "id")
// 			http_method := ExtractStringFromJSON(mr, "http_method")
// 			pattern := ExtractStringFromJSON(mr, "pattern")
// 			owner_id := ExtractStringFromJSON(mr, "owner_id")
// 			proxy_id := ExtractFloatFromJSON(mr, "proxy_id")

// 			fmt.Sprint(k)
// 			fmt.Sprint(r)
// 			fmt.Sprint(id)
// 			fmt.Sprint(proxy_id)
// 			fmt.Sprint(http_method + pattern + owner_id)
// 			//	fmt.Sprint(id)
// 		}
// 	}
// 	return result
// }

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

func ExtractFloatFromJSON(dict map[string]interface{}, key string) float64 {
	if val, ok := dict[key]; ok {
		return val.(float64)
	}
	return -1
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
		backendRules := make(map[string][]*model.MappingRule)
		for proxind := 0; proxind < len(proxyGroups[ind]); proxind++ {
			proxyPointer := proxyGroups[ind][proxind]
			rulesPnt := &((*proxyPointer).Proxy_rules)
			for pRulesInd := 0; pRulesInd < len(*rulesPnt); pRulesInd++ {
				// only add proxy rules, not backend rules
				if (*rulesPnt)[pRulesInd].Owner_type == "" || (*rulesPnt)[pRulesInd].Owner_type == OwnerTypeProxy {
					allRulesToVerify = append(allRulesToVerify, &((*rulesPnt)[pRulesInd]))
				} else if (*rulesPnt)[pRulesInd].Owner_type == OwnerTypeBackend {
					id := (*rulesPnt)[pRulesInd].Owner_id
					if _, ok := backendRules[id]; ok {
						backendRules[id] = append(backendRules[id], &((*rulesPnt)[pRulesInd]))
					} else {
						backendRules[id] = []*model.MappingRule{&((*rulesPnt)[pRulesInd])}
					}
				}
			}
		}
		//TODO progressbar

		for indexRules := 0; indexRules < len(allRulesToVerify); indexRules++ {
			validateMappingRule(allRulesToVerify[indexRules], allRulesToVerify, indexRules+1)
		}
		for k := range backendRules {
			for j := 0; j < len(backendRules[k]); j++ {
				validateMappingRule(backendRules[k][j], backendRules[k], j+1)
			}
		}
		if Mode == ModeScan {
			output.PrintIssues()
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
		}
	}
}

func validateMappingRule(rule *model.MappingRule, allRules []*model.MappingRule, index int) {
	for i := index; i < len(allRules); i++ {
		currentRule := (allRules)[i]
		severity := calculateSeverity(rule, currentRule)
		if Mode == ModeScan {
			var description string
			if rule.BrutalMatch(currentRule) || rule.CanBeOptimized(currentRule) {
				if rule.BrutalMatch(currentRule) {
					description = "one rule matches the other"
				} else if rule.CanBeOptimized(currentRule) {
					description = "rules could be optimized"
				}
				rules := []model.MappingRule{*rule, *currentRule}
				issue := model.Issue{Rules: rules, Description: description, Severity: severity}
				globalUtils.Issues = append(globalUtils.Issues, issue)
			}
		} else if Mode == ModeInteractive {
			if !(*rule).IsMarkedForDeletion && rule.BrutalMatch(currentRule) {
				keep := !OptionConfirmAll.ValueB() && requestMappingKeep(*rule, *currentRule, true)
				if !keep {
					(*currentRule).SetMarkedForDeletion(true)
				} else {
					keep2 := !OptionConfirmAll.ValueB() && requestMappingKeep(*currentRule, *rule, false)
					if !keep2 {
						(*rule).SetMarkedForDeletion(true)
					}
				}
			} else if !(*rule).IsMarkedForDeletion && rule.CanBeOptimized(currentRule) {
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
		}
	}
}

func calculateSeverity(rule1 *model.MappingRule, rule2 *model.MappingRule) (retSev int) {
	retSev = 2
	if rule1.CanBeOptimized(rule2) {
		retSev = 5
	} else if rule1.Owner_type == OwnerTypeBackend {
		retSev = 4
	} else if (rule1.Host == rule2.Host || globalUtils.PathRoutingOnly) && rule1.Proxy_id != rule2.Proxy_id {
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
	if reflect.DeepEqual(shorter, currentRule) {
		longer = rule
	} else {
		longer = currentRule
	}

	if !shorter.IsExactMatch {
		panic("optimizable not ending with $")
	}
	fmt.Println("These rules " + shorter.String() + ", " + longer.String() + " could be optimized by removing the dollar from " + shorter.String() + " (if it exists) and deleting " + longer.String() + ". Would you like to proceed?  Y/N")
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
		fmt.Println("This rule: " + rule1.String() + " collides with: " + rule2.String())
	}
	fmt.Println("Would you like to keep " + rule2.String() + "?  Y/N")
	var response string
	for {
		fmt.Scanln(&response)
		if strings.EqualFold(response, "Y") {
			return true
		} else if strings.EqualFold(response, "N") {
			return false
		}
		fmt.Println("Invalid response, would you like to keep " + rule2.String() + "? Y/N")
	}
}
