package main

import (
	"configopt/clargs"
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

	fmt.Println(inputFilePath + output.OutputFile)
	clargs.PrintValues(options)
}
