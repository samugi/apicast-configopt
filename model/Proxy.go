package model

type Proxy struct {
	Id          int64         `json:"id"`
	Endpoint    string        `json:"endpoint"`
	Proxy_rules []MappingRule `json:"proxy_rules"`
}
