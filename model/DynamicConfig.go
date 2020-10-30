package model

//https://stackoverflow.com/questions/33436730/unmarshal-json-with-some-known-and-some-unknown-field-names
type DynamicConfig struct {
	FullConfigContainer map[string]interface{}
}
