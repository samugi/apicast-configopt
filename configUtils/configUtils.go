package configUtils

import (
	"bytes"
	"configopt/model"
	"encoding/gob"
	"encoding/json"
	"io/ioutil"
	"os"
)

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
