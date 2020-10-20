package output

import (
	"configopt/globalUtils"
	"configopt/model"
	"encoding/json"
	"fmt"
	"os"
	"sort"
)

var OutputFile string

func RewriteConfig(config model.Configuration) {
	if OutputFile != "" {
		jsonized, err := json.Marshal(&config)
		if err != nil {
			fmt.Println(err)
			return
		}
		file, error := os.Create(OutputFile)
		if error != nil {
			panic(error)
		}
		fmt.Fprint(file, string(jsonized))
		file.Close()
	}
}

func PrintIssues() {
	sort.Slice(globalUtils.Issues, func(i, j int) bool {
		return globalUtils.Issues[i].Severity < globalUtils.Issues[j].Severity
	})
	if OutputFile == "" {
		for _, issue := range globalUtils.Issues {
			fmt.Println(issue.String())
		}
		return
	}
	file, error := os.Create(OutputFile)
	if error != nil {
		panic(error)
	}
	for _, issue := range globalUtils.Issues {
		fmt.Fprint(file, issue.String())
	}
	file.Close()
}
