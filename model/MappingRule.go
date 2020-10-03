package model

type MappingRule struct {
	Id          int    `json:"id"`
	Proxy_id    int    `json:"proxy_id"`
	Http_method string `json:"http_method"`
	Pattern     string `json:"pattern"`
	Owner_id    string `json:"owner_id"`
}
