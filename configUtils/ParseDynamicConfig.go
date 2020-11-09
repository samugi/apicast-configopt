package configUtils

import (
	"encoding/json"
	"fmt"

	"github.com/samugi/apicast-configopt/model"
)

func InjectMappingRules(fullConfigBytes []byte, fixedConfig model.Configuration) (fullConfig model.DynamicConfig) {

	configType := ScanConfigType(fullConfigBytes)
	fullConfig = ExtractFullConfig(fullConfigBytes)

	switch configType {
	case ConfigFromDump:
		proxy_configs := fullConfig.FullConfigContainer["proxy_configs"]
		for index, res := range proxy_configs.([]interface{}) {
			proxy_config := res.(map[string]interface{})["proxy_config"].(map[string]interface{})
			content := proxy_config["content"].(map[string]interface{})
			proxy := content["proxy"].(map[string]interface{})
			id := proxy["id"].(float64)
			fixedProxy := fixedConfig.ProxyConfigsOuter[index].ProxyConfig.Content.Proxy
			if fixedProxy.Id != int64(id) {
				panic("Oh no: " + fmt.Sprint(id) + " != " + fmt.Sprint(fixedProxy.Id))
			}
			fullConfig.FullConfigContainer["proxy_configs"].([]interface{})[index].(map[string]interface{})["proxy_config"].(map[string]interface{})["content"].(map[string]interface{})["proxy"].(map[string]interface{})["proxy_rules"] = fixedProxy.Proxy_rules
		}
		return
	case ConfigBoot:
		var fixedConfigCB model.ConfigBoot
		for _, outer := range fixedConfig.ProxyConfigsOuter {
			fixedConfigCB.Config.Services = append(fixedConfigCB.Config.Services, outer.ProxyConfig.Content)
		}
		//==========================================
		config := fullConfig.FullConfigContainer["config"].(map[string]interface{})
		services := config["services"]
		for index, res := range services.([]interface{}) {
			proxy := res.(map[string]interface{})["proxy"].(map[string]interface{})
			id := proxy["id"].(float64)
			fixedProxy := fixedConfigCB.Config.Services[index].Proxy
			if fixedProxy.Id != int64(id) {
				panic("Oh no:  " + fmt.Sprint(id) + " != " + fmt.Sprint(fixedProxy.Id))
			}
			fullConfig.FullConfigContainer["config"].(map[string]interface{})["services"].([]interface{})[index].(map[string]interface{})["proxy"].(map[string]interface{})["proxy_rules"] = fixedProxy.Proxy_rules
		}
		return
	case ConfigConfig:
		var fixedConfigCC model.ConfigConfig
		for _, outer := range fixedConfig.ProxyConfigsOuter {
			fixedConfigCC.Services = append(fixedConfigCC.Services, outer.ProxyConfig.Content)
		}
		//==========================================
		services := fullConfig.FullConfigContainer["services"].([]interface{})
		for index, res := range services {
			proxy := res.(map[string]interface{})["proxy"].(map[string]interface{})
			id := proxy["id"].(float64)
			fixedProxy := fixedConfigCC.Services[index].Proxy
			if fixedProxy.Id != int64(id) {
				panic("Oh no:  " + fmt.Sprint(id) + " != " + fmt.Sprint(fixedProxy.Id))
			}
			fullConfig.FullConfigContainer["services"].([]interface{})[index].(map[string]interface{})["proxy"].(map[string]interface{})["proxy_rules"] = fixedProxy.Proxy_rules
		}
		return
	case ConfigSingleService:
		var fixedConfigSS model.Content
		fixedConfigSS = fixedConfig.ProxyConfigsOuter[0].ProxyConfig.Content
		//==========================================
		fixedProxy := fixedConfigSS.Proxy
		id := fullConfig.FullConfigContainer["proxy"].(map[string]interface{})["id"].(float64)
		if fixedProxy.Id != int64(id) {
			panic("Oh no:  " + fmt.Sprint(id) + " != " + fmt.Sprint(fixedProxy.Id))
		}
		fullConfig.FullConfigContainer["proxy"].(map[string]interface{})["proxy_rules"] = fixedProxy.Proxy_rules
		return
	}
	return
}

func ExtractFullConfig(byteValue []byte) model.DynamicConfig {
	result := model.DynamicConfig{}
	json.Unmarshal([]byte(byteValue), &result.FullConfigContainer)
	return result
}
