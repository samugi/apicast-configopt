package main

import (
	"configopt/clargs"
	"configopt/configUtils"
	"configopt/option"
	"configopt/output"
	"fmt"
	"os"
)

var usage string

func main() {
	usage = "usage: go run ConfigOpt.go [options...] --configuration <arg>"
	optionConfig := option.New("-c", "--configuration", "JSON configuration file path", true, true)
	optionOutput := option.New("-o", "--output", "Output file for report", true, false)
	optionVerbose := option.New("-v", "--verbose", "Verbose logs", false, false)
	optionInteractive := option.New("-i", "--interactive", "Enables interactive mode", false, false)
	optionPathRoutingOnly := option.New("-pro", "--pathroutingonly", "Runs in path routing only mode. Use this if you have APICAST_PATH_ROUTING_ONLY=true", false, false)
	optionHelp := option.New("-h", "--help", "Show this help message", false, false)

	options := []*option.Option{}
	options = append(options, &optionConfig, &optionOutput, &optionVerbose, &optionInteractive, &optionPathRoutingOnly, &optionHelp)
	usage += clargs.GetUsageOptions(options)
	args := os.Args[1:]
	clargs.CheckArgs(args, options, usage)

	inputFilePath := optionConfig.Value()
	output.OutputFile = optionOutput.Value()

	JSONConfig := configUtils.ExtractConfigJSONFromFile(inputFilePath)

	proxy_configs := JSONConfig["proxy_configs"]

	for _, result := range proxy_configs {

		proxy_config := result["proxy_config"].(map[string]interface{})
		content := proxy_config["content"].(map[string]interface{})
		proxy := content["proxy"].(map[string]interface{})

		proxy_rules_arr := proxy["proxy_rules"].([]interface{})
		for k, r := range proxy_rules_arr {
			//	id := r["id"]
			mr := r.(map[string]interface{}) //FIXME boh
			fmt.Sprint(k)
			fmt.Sprint(r)
			fmt.Sprint(mr)
			//	fmt.Sprint(id)

		}

		//		for keypr, resultpr := range proxy_rules {
		//			id := resultpr["id"].(string)
		// http_method := resultpr["http_method"].(string)
		// pattern := resultpr["pattern"].(string)
		// owner_id := resultpr["owner_id"].(string)
		// proxy_id := resultpr["proxy_id"].(string)
		// fmt.Println("mapping rule: " + id + http_method + pattern + owner_id + proxy_id)
		// fmt.Sprint(keypr)
		//	}
	}

	clargs.PrintValues(options)
}
