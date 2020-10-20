package clargs

import (
	"configopt/option"
	"fmt"
	"strconv"
	s "strings"
)

func GetUsageOptions(options []*option.Option) string {
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

func PrintValues(options []*option.Option) {
	for _, opt := range options {
		fmt.Println("Option: " + opt.LongOption + ", Value: " + opt.Value() + ", ValueB: " + strconv.FormatBool(opt.ValueB()))
	}
}

func CheckArgs(args []string, options []*option.Option, usage string) {
	if len(args) == 0 {
		printUsage(usage)
		return
	}
	for i, o := range options {
		parameterValue := getParameterValue(args, o.ShortOption)
		if parameterValue == "" {
			parameterValue = getParameterValue(args, o.LongOption)
		}
		if o.Required {
			if o.HasArgs && parameterValue == "" {
				printUsage(usage)
				return
			}
			if !findOptionInArgs(o, args) {
				printUsage(usage)
				return
			}
		}
		options[i].SetValue(parameterValue)
		options[i].SetValueB(findOptionInArgs(o, args))
	}
}

func printUsage(usage string) {
	fmt.Println(usage)
}

func getParameterValue(slice []string, parameter string) string {
	for index, par := range slice {
		if s.HasPrefix(par, parameter) {
			if len(s.Split(par, "=")) > 1 {
				return s.Split(par, "=")[1]
			}
			if index < len(par) {
				return par[index:]
			}
		}
	}
	return ""
}

func findOptionInArgs(opt *option.Option, parameters []string) bool {
	for _, par := range parameters {
		par = s.Split(par, "=")[0]
		if opt.ShortOption == par || opt.LongOption == par {
			return true
		}
	}
	return false
}
