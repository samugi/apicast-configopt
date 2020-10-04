package model

type MappingRule struct {
	Id          float64 `json:"id"`
	Proxy_id    float64 `json:"proxy_id"`
	Http_method string  `json:"http_method"`
	Pattern     string  `json:"pattern"`
	Owner_id    string  `json:"owner_id"`
}
