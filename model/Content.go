package model

type Content struct {
	Id    int64 `json:"id"`
	Proxy Proxy `json:"proxy"`
}
