package clargs

import (
	"configopt/option"
	"fmt"
	"os"
	"strconv"
	"strings"
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
		PrintUsage(usage)
		os.Exit(1)
		return
	}
	for i, o := range options {
		parameterValue := getParameterValue(args, o.ShortOption)
		if parameterValue == "" {
			parameterValue = getParameterValue(args, o.LongOption)
		}
		if o.Required {
			if o.HasArgs && parameterValue == "" {
				PrintUsage(usage)
				os.Exit(1)
				return
			}
			if !findOptionInArgs(o, args) {
				PrintUsage(usage)
				os.Exit(1)
				return
			}
		}
		options[i].SetValue(parameterValue)
		options[i].SetValueB(findOptionInArgs(o, args))
	}
}

func PrintUsage(usage string) {
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

func findOptionInArgs(opt *option.Option, parameters []string) bool {
	for _, par := range parameters {
		par = s.Split(par, "=")[0]

		var pars []string
		if len(par) > 2 && par[0] == '-' && par[1] != '-' { //this means it's a multiparam of the kind -abc
			pars = strings.Split(par[1:len(par)], "")
			pars = addHyphen(pars)
		} else {
			pars = append(pars, par)
		}

		for _, p := range pars {
			if opt.ShortOption == p || opt.LongOption == p {
				return true
			}
		}
	}
	return false
}

func addHyphen(parameters []string) []string {
	for i, str := range parameters {
		parameters[i] = "-" + str
	}
	return parameters
}
