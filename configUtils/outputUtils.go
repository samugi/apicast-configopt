package configUtils

import (
	"encoding/json"
	"fmt"
	"os"
	"path/filepath"
	"sort"
	"strconv"

	"github.com/samugi/apicast-configopt/globalUtils"
	"github.com/samugi/apicast-configopt/model"
	"github.com/samugi/apicast-configopt/threescaleapi"
)

var OutputFile string
var Remote string

func RewriteConfig(config model.Configuration) {
	if OptionUpdateRemote.ValueB() {
		updateRemoteMappingRules(config)
	}
	config = cleanConfig(config)
	FullConfig = InjectMappingRules(FullConfigBytes, config)
	if OutputFile != "" {
		jsonized, err := json.Marshal(&FullConfig.FullConfigContainer)
		if err != nil {
			fmt.Println(err)
			return
		}
		absFile, err := filepath.Abs(OutputFile)
		if err != nil {
			panic(err)
		}
		file, error := os.Create(absFile)
		if error != nil {
			panic(error)
		}
		fmt.Fprint(file, string(jsonized))
		file.Close()
	}
}

func updateRemoteMappingRules(config model.Configuration) {
	if Remote == "" {
		panic("Need to pass a valid admin portal URL like: https://{TOKEN}@admin-portal.example.org")
	}
	threescaleapi.Init(Remote)

	for _, proxyConfigOuter := range config.ProxyConfigsOuter {
		proxy := proxyConfigOuter.ProxyConfig.Content.Proxy
		serviceId := proxy.ServiceId
		for _, rule := range proxy.Proxy_rules {
			if rule.IsMarkedForDeletion {
				deleteMappingRule(rule, strconv.FormatInt(serviceId, 10))
			} else if rule.IsUpdated {
				updateMappingRule(rule, strconv.FormatInt(serviceId, 10))
			}
		}
	}
}

func deleteMappingRule(rule model.MappingRule, serviceId string) {
	if rule.Owner_type == nil || *rule.Owner_type == model.OwnerTypeProxy {
		threescaleapi.DeleteProxyRule(rule, serviceId)
	} else if *rule.Owner_type == model.OwnerTypeBackend {
		threescaleapi.DeleteBackendRule(rule)
	} else {
		panic("owner type not allowed")
	}
}

func updateMappingRule(rule model.MappingRule, serviceId string) {
	if rule.Owner_type == nil || *rule.Owner_type == model.OwnerTypeProxy {
		threescaleapi.UpdateProxyRule(rule, serviceId)
	} else if *rule.Owner_type == model.OwnerTypeBackend {
		threescaleapi.UpdateBackendRule(rule)
	} else {
		panic("owner type not allowed")
	}
}

// func removeRule(proxyRules []model.MappingRule, i int) []model.MappingRule {
// 	proxyRules[i] = proxyRules[len(proxyRules)-1]
// 	return proxyRules[:len(proxyRules)-1]
// }

func cleanConfig(config model.Configuration) model.Configuration {

	for outerIndex, proxyConfigOuter := range config.ProxyConfigsOuter {
		k := 0
		rules := proxyConfigOuter.ProxyConfig.Content.Proxy.Proxy_rules
		for _, proxyRule := range rules {
			//removing rules marked for deletion here
			if !proxyRule.IsMarkedForDeletion {
				rules[k] = proxyRule
				k++
			}
		}
		config.ProxyConfigsOuter[outerIndex].ProxyConfig.Content.Proxy.Proxy_rules = rules[:k]
	}
	return config
}

func PrintIssues() {
	sort.Slice(globalUtils.Issues, func(i, j int) bool {
		return globalUtils.Issues[i].Severity < globalUtils.Issues[j].Severity
	})
	if OutputFile == "" {
		for _, issue := range globalUtils.Issues {
			fmt.Println(issue.String())
		}
		return
	}
	absFile, err := filepath.Abs(OutputFile)
	if err != nil {
		panic(err)
	}
	file, error := os.Create(absFile)
	if error != nil {
		panic(error)
	}
	for _, issue := range globalUtils.Issues {
		fmt.Fprint(file, issue.String())
	}
	file.Close()
}
