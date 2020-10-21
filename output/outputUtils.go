package output

import (
	"configopt/globalUtils"
	"configopt/model"
	"encoding/json"
	"fmt"
	"os"
	"path/filepath"
	"sort"
)

var OutputFile string

func RewriteConfig(config model.Configuration) {
	config = cleanConfig(config)
	if OutputFile != "" {
		jsonized, err := json.Marshal(&config)
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

func removeRule(proxyRules []model.MappingRule, i int) []model.MappingRule {
	proxyRules[i] = proxyRules[len(proxyRules)-1]
	return proxyRules[:len(proxyRules)-1]
}

func cleanConfig(config model.Configuration) model.Configuration {
	for outerIndex, proxyConfigOuter := range config.ProxyConfigsOuter {
		rules := proxyConfigOuter.ProxyConfig.Content.Proxy.Proxy_rules
		for ruleIndex, proxyRule := range rules {
			if proxyRule.IsMarkedForDeletion {
				rules = removeRule(rules, ruleIndex)
			}
		}
		config.ProxyConfigsOuter[outerIndex].ProxyConfig.Content.Proxy.Proxy_rules = rules
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
