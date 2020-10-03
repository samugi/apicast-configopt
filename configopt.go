package main

import (
	"configopt/option"
	"fmt"
	"os"
	s "strings"
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

	options := []option.Option{}
	options = append(options, optionConfig, optionOutput, optionVerbose, optionInteractive, optionPathRoutingOnly, optionHelp)

	usage += getUsageOptions(options)
	args := os.Args[1:]
	checkArgs(args, options)
	printValues(options)
}

func getUsageOptions(options []option.Option) string {
	retStr := "\n\nOptions\n"
	for _, opt := range options {
		optStr := opt.ShortOption + ", " + opt.LongOption
		retStr += optStr
		for i := 0; i < 30-len(optStr); i++ {
			retStr += " "
		}
		retStr += opt.Description
		retStr += "\n"
	}
	return retStr
}

func printValues(options []option.Option) {
	for _, opt := range options {
		fmt.Println("Option: " + opt.LongOption + ", Value: " + opt.Value())
	}
}

func checkArgs(args []string, options []option.Option) {
	if len(args) == 0 {
		printUsage()
		return
	}
	for i, o := range options {
		if o.Required {
			parameterValue := getParameterValue(args, o.ShortOption)
			if parameterValue == "" {
				parameterValue = getParameterValue(args, o.LongOption)
			}
			if o.HasArgs && parameterValue == "" {
				printUsage()
				return
			}
			if !findOptionInArgs(o, args) {
				printUsage()
				return
			}
			options[i].SetValue(parameterValue)
			fmt.Println("setting value: " + parameterValue)
		}
	}
}

func printUsage() {
	fmt.Println(usage)
}

func getParameterValue(slice []string, parameter string) string {
	for index, par := range slice {
		if s.HasPrefix(par, parameter) {
			if len(s.Split(par, "=")) > 1 {
				return s.Split(par, "=")[1]
			}
			if index+1 < len(slice) {
				return slice[index+1]
			}
		}
	}
	return ""
}

func findOptionInArgs(opt option.Option, parameters []string) bool {
	for _, par := range parameters {
		par = s.Split(par, "=")[0]
		if opt.ShortOption == par || opt.LongOption == par {
			return true
		}
	}
	return false
}
