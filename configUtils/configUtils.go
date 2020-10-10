package configUtils

import (
	"bytes"
	"configopt/model"
	"configopt/option"
	"encoding/gob"
	"encoding/json"
	"io/ioutil"
	"os"
)

var OptionConfig option.Option
var OptionOutput option.Option
var OptionVerbose option.Option
var OptionInteractive option.Option
var OptionPathRoutingOnly option.Option
var OptionHelp option.Option

func ExtractConfigJSONFromFileWithStructs(inputFilePath string) model.Configuration {
	var configuration model.Configuration
	jsonFile, err := os.Open(inputFilePath)
	check(err)
	defer jsonFile.Close()
	byteValue, _ := ioutil.ReadAll(jsonFile)
	json.Unmarshal(byteValue, &configuration)
	return configuration
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

func ValidateAllServices(config model.Configuration) {
	createServiceGroups(config)
}

func createServiceGroups(config model.Configuration) (serviceGroups [][]*model.Proxy) {
	serviceGroupsMap := make(map[string][]*model.Proxy)
	proxyConfigs := config.ProxyConfigsOuter

	if !OptionPathRoutingOnly.ValueB() {
		//PATH ROUTING ONLY NOT ENABLED: for each service in the config, map by host in serviceGroupsMap
		for _, proxyConfig := range proxyConfigs {
			proxy := proxyConfig.ProxyConfig.Content.Proxy
			host := proxy.Endpoint
			if _, ok := serviceGroupsMap[host]; ok {
				serviceGroupsMap[host] = append(serviceGroupsMap[host], &proxy)
			} else {
				serviceGroupsMap[host] = []*model.Proxy{&proxy}
			}
		}
		for _, proxies := range serviceGroupsMap {
			serviceGroups = append(serviceGroups, proxies)
		}
	} else {
		var proxies []*model.Proxy
		for _, proxyConfig := range proxyConfigs {
			proxy := proxyConfig.ProxyConfig.Content.Proxy
			proxies = append(proxies, &proxy)
		}
		serviceGroups = append(serviceGroups, proxies)
	}

	_ = serviceGroups
	_ = serviceGroupsMap
	return
}
