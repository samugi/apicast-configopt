package main

import (
	"fmt"
	"os"
	s "strings"
)

const USAGE string = "usage: go run ConfigOpt.go [options...] --configuration <arg>"

func main() {
	args := os.Args[1:]
	if len(args) == 0 {
		printUsage()
	}

	fmt.Println(getParameterValue(args, "--configuration"))
}

func printUsage() {
	fmt.Println(USAGE)
}

func getParameterValue(slice []string, parameter string) string {
	for index, par := range slice {
		if s.Contains(par, parameter) {
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
