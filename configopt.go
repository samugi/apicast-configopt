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
	configUtils.OptionPathRoutingOnly = option.New("-p", "--pathroutingonly", "Runs in path routing only mode. Use this if you have APICAST_PATH_ROUTING_ONLY=true", false, false)
	configUtils.OptionHelp = option.New("-h", "--help", "Show this help message", false, false)
	configUtils.OptionAutoFix = option.New("-a", "--autofix", "Automatically fixes config. Pass value "+configUtils.AutoFix+" to just remove duplicates, "+configUtils.AutoOptimize+" to also auto-optimize", true, false)

	options := []*option.Option{}
	options = append(options, &configUtils.OptionConfig, &configUtils.OptionOutput, &configUtils.OptionVerbose, &configUtils.OptionInteractive, &configUtils.OptionPathRoutingOnly, &configUtils.OptionHelp, &configUtils.OptionAutoFix)
	usage += clargs.GetUsageOptions(options)
	args := os.Args[1:]
	clargs.CheckArgs(args, options, usage)

	inputFilePath := configUtils.OptionConfig.Value()
	output.OutputFile = configUtils.OptionOutput.Value()
	if configUtils.OptionInteractive.ValueB() {
		configUtils.Mode = configUtils.ModeInteractive
	}
	if configUtils.OptionAutoFix.ValueB() {
		switch configUtils.OptionAutoFix.Value() {
		case configUtils.AutoFix:
			configUtils.Mode = configUtils.ModeAutoFix
			break
		case configUtils.AutoOptimize:
			configUtils.Mode = configUtils.ModeAutoFix
			break
		default:
			fmt.Println("Wrong value for autofix parameter")
			clargs.PrintUsage(usage)
			os.Exit(1)
			break
		}
		if output.OutputFile == "" {
			fmt.Println("Autofix requires an output file")
			clargs.PrintUsage(usage)
			os.Exit(1)
		}
	}
	config := configUtils.ExtractConfigJSONFromFileWithStructs(inputFilePath)

	configUtils.InitializeRules(config)

	globalUtils.PathRoutingOnly = configUtils.OptionPathRoutingOnly.ValueB()

	configUtils.ValidateAllProxies(config)

	if configUtils.Mode == configUtils.ModeInteractive || configUtils.Mode == configUtils.ModeAutoFix {
		output.RewriteConfig(config)
	}

	fmt.Sprint(config)
	//clargs.PrintValues(options)
}
