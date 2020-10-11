package output

import (
	"configopt/globalUtils"
	"configopt/model"
	"fmt"
	"os"
	"sort"
)

var OutputFile string

func RewriteConfig(config model.Configuration) {

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
