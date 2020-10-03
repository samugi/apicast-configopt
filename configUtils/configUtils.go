package configUtils

import (
	"bytes"
	"encoding/gob"
	"encoding/json"
	"io/ioutil"
	"os"
)

func ExtractConfigJSONFromFile(inputFilePath string) map[string][]map[string]interface{} {
	jsonFile, err := os.Open(inputFilePath)
	check(err)
	defer jsonFile.Close()
	byteValue, _ := ioutil.ReadAll(jsonFile)
	var result map[string][]map[string]interface{}
	json.Unmarshal([]byte(byteValue), &result)
	return result
}

func check(e error) {
	if e != nil {
		panic(e)
	}
}

func GetBytes(key interface{}) []byte {
	var buf bytes.Buffer
	enc := gob.NewEncoder(&buf)
	err := enc.Encode(key)
	if err != nil {
		return nil
	}
	return buf.Bytes()
}
