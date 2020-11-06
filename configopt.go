package main

import (
	"configopt/configUtils"
	"configopt/globalUtils"
	"fmt"
	"os"

	"github.com/samugi/simple-clargs/clargs"
)

var usage = "usage: ./configopt [options...] --configuration <arg>"

func main() {
	configUtils.Mode = configUtils.ModeScan

	configUtils.OptionConfig = clargs.New("-c", "--configuration", "JSON configuration file path", true, true)
	configUtils.OptionOutput = clargs.New("-o", "--output", "Output file for report", true, false)
	configUtils.OptionVerbose = clargs.New("-v", "--verbose", "Verbose logs", false, false)
	configUtils.OptionInteractive = clargs.New("-i", "--interactive", "Enables interactive mode", false, false)
	configUtils.OptionPathRoutingOnly = clargs.New("-p", "--pathroutingonly", "Runs in path routing only mode. Use this if you have APICAST_PATH_ROUTING_ONLY=true", false, false)
	configUtils.OptionHelp = clargs.New("-h", "--help", "Show this help message", false, false)
	configUtils.OptionAutoFix = clargs.New("-a", "--autofix", "Automatically fixes config. Pass value "+configUtils.AutoFix+" to just remove duplicates, "+configUtils.AutoOptimize+" to also auto-optimize", true, false)
	configUtils.OptionUpdateRemote = clargs.New("-u", "--updateremote", "Updates the remote configuration. This only works together with interactive or autofix modes. Pass the remote like: `https://{access_token}@admin-portal.example.org` as a value for this argument.", true, false)

	options := []*clargs.Option{}
	options = append(options, &configUtils.OptionConfig, &configUtils.OptionOutput, &configUtils.OptionVerbose, &configUtils.OptionInteractive, &configUtils.OptionPathRoutingOnly, &configUtils.OptionHelp, &configUtils.OptionAutoFix, &configUtils.OptionUpdateRemote)
	args := os.Args[1:]

	//check the command line arguments
	clargs.Init(usage, options, args)
	clargs.CheckArgs()

	inputFilePath := configUtils.OptionConfig.Value()
	configUtils.OutputFile = configUtils.OptionOutput.Value()
	if configUtils.OptionInteractive.ValueB() {
		configUtils.Mode = configUtils.ModeInteractive
		if configUtils.OutputFile == "" && !configUtils.OptionUpdateRemote.ValueB() {
			fmt.Println("Interactive mode requires an output file or the --updateremote parameter")
			clargs.PrintUsage()
			os.Exit(1)
		}
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
			clargs.PrintUsage()
			os.Exit(1)
			break
		}
		if configUtils.OutputFile == "" && !configUtils.OptionUpdateRemote.ValueB() {
			fmt.Println("Autofix mode requires an output file or the --updateremote parameter")
			clargs.PrintUsage()
			os.Exit(1)
		}
	}
	if configUtils.OptionUpdateRemote.ValueB() {
		configUtils.Remote = configUtils.OptionUpdateRemote.Value()
	}
	config := configUtils.ExtractConfigJSONFromFileWithStructs(inputFilePath)

	configUtils.InitializeRules(config)

	globalUtils.PathRoutingOnly = configUtils.OptionPathRoutingOnly.ValueB()

	configUtils.ValidateAllProxies(config)

	if configUtils.Mode == configUtils.ModeInteractive || configUtils.Mode == configUtils.ModeAutoFix {
		configUtils.RewriteConfig(config)
	}

	fmt.Sprint(config)
	//clargs.PrintValues(options)
}
