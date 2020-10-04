package model

type Proxy struct {
	Id          float64       `json:"id"`
	Endpoint    string        `json:"endpoint"`
	Proxy_rules []MappingRule `json:"proxy_rules"`
}
