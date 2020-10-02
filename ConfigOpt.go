package main

import (
	"fmt"
	"os"
)

const USAGE string = "usage: go run ConfigOpt.go [options...] --configuration <arg>"

func main() {
	args := os.Args[1:]
	if len(args) == 0 {
		printUsage()
	}

	fmt.Println("Hello World")
}

func printUsage() {
	fmt.Println(USAGE)
}
