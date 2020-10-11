package main

import (
	"configopt/clargs"
	"configopt/configUtils"
	"configopt/globalUtils"
	"configopt/option"
	"configopt/output"
	"fmt"
	"os"
)

var usage = "usage: go run ConfigOpt.go [options...] --configuration <arg>"

func main() {
	configUtils.Mode = configUtils.ModeScan

	configUtils.OptionConfig = option.New("-c", "--configuration", "JSON configuration file path", true, true)
	configUtils.OptionOutput = option.New("-o", "--output", "Output file for report", true, false)
	configUtils.OptionVerbose = option.New("-v", "--verbose", "Verbose logs", false, false)
	configUtils.OptionInteractive = option.New("-i", "--interactive", "Enables interactive mode", false, false)
	configUtils.OptionPathRoutingOnly = option.New("-pro", "--pathroutingonly", "Runs in path routing only mode. Use this if you have APICAST_PATH_ROUTING_ONLY=true", false, false)
	configUtils.OptionHelp = option.New("-h", "--help", "Show this help message", false, false)

	options := []*option.Option{}
	options = append(options, &configUtils.OptionConfig, &configUtils.OptionOutput, &configUtils.OptionVerbose, &configUtils.OptionInteractive, &configUtils.OptionPathRoutingOnly, &configUtils.OptionHelp)
	usage += clargs.GetUsageOptions(options)
	args := os.Args[1:]
	clargs.CheckArgs(args, options, usage)

	inputFilePath := configUtils.OptionConfig.Value()
	output.OutputFile = configUtils.OptionOutput.Value()

	config := configUtils.ExtractConfigJSONFromFileWithStructs(inputFilePath)

	configUtils.InitializeRules(config)

	globalUtils.PathRoutingOnly = configUtils.OptionPathRoutingOnly.ValueB()

	configUtils.ValidateAllProxies(config)

	if configUtils.OptionInteractive.ValueB() {
		configUtils.Mode = configUtils.ModeInteractive
		output.RewriteConfig(config)
	}

	fmt.Sprint(config)
	//clargs.PrintValues(options)
}
