package model

import "strings"

type Issue struct {
	Severity    int
	Description string
	Rules       []MappingRule
}

func (i Issue) String() string {
	var sb strings.Builder
	sb.WriteString("Issue found " + i.getSeverityText() + " - " + i.Description + " - for mapping rules: \n")
	for _, rule := range i.Rules {
		sb.WriteString(rule.String())
		sb.WriteString("\n")
	}
	return sb.String()
}

func (i Issue) getSeverityText() string {
	severe := "[ SEVERE   ]"
	minor := "[ MINOR    ]"
	optional := "[ OPTIONAL ]"
	switch i.Severity {
	case 1:
		return severe
	case 2:
		return minor
	case 5:
		return optional
	default:
		return minor
	}
}
