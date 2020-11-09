package threescaleapi

import (
	"crypto/tls"
	"fmt"
	"net/http"
	"net/url"
	"strings"

	"github.com/samugi/apicast-configopt/model"
)

var adminPortal string
var accessToken string

func Init(rem string) {
	protocol := strings.Split(rem, "//")[0] + "//"
	accessToken = strings.Split(strings.Split(rem, "@")[0], "//")[1]
	adminPortalHost := strings.TrimSuffix(strings.Split(rem, "@")[1], "/")
	adminPortal = protocol + adminPortalHost
	http.DefaultTransport.(*http.Transport).TLSClientConfig = &tls.Config{InsecureSkipVerify: true}
}

type ProxyRuleUpdateBody struct {
	access_token string
	pattern      string
}

func UpdateProxyRule(proxyRule model.MappingRule) {
	mid := *proxyRule.Id
	pid := *proxyRule.Proxy_id
	requestUrl := adminPortal + "/admin/api/services/" + fmt.Sprint(pid) + "/proxy/mapping_rules/" + fmt.Sprint(mid) + ".xml"

	data := url.Values{}
	data.Set("access_token", accessToken)
	data.Set("pattern", *proxyRule.Pattern)

	client := &http.Client{}
	request, err := http.NewRequest("PATCH", requestUrl, strings.NewReader(data.Encode()))
	if err != nil {
		panic(err)
	}

	request.Header.Set("Accept", "*/*")
	request.Header.Set("Content-Type", "application/x-www-form-urlencoded")

	response, err := client.Do(request)

	if err != nil {
		panic(err)
	}

	defer response.Body.Close()
	// contents, err := ioutil.ReadAll(response.Body)
	// if err != nil {
	// 	log.Fatal(err)
	// }
	// fmt.Println("The calculated length is:", len(string(contents)), "for the url:", requestUrl)
	// fmt.Println("   ", response.StatusCode)
	// hdr := response.Header
	// for key, value := range hdr {
	// 	fmt.Println("   ", key, ":", value)
	// }
	// fmt.Println(contents)
}

func UpdateBackendRule(backendRule model.MappingRule) {
	mid := *backendRule.Id
	bid := *backendRule.Owner_id
	requestUrl := adminPortal + "/admin/api/backend_apis/" + fmt.Sprint(bid) + "/mapping_rules/" + fmt.Sprint(mid) + ".json"

	data := url.Values{}
	data.Set("access_token", accessToken)
	data.Set("pattern", *backendRule.Pattern)

	client := &http.Client{}
	request, err := http.NewRequest("PUT", requestUrl, strings.NewReader(data.Encode()))
	if err != nil {
		panic(err)
	}

	request.Header.Set("Accept", "*/*")
	request.Header.Set("Content-Type", "application/x-www-form-urlencoded")

	response, err := client.Do(request)

	if err != nil {
		panic(err)
	}

	defer response.Body.Close()
}

func DeleteProxyRule(proxyRule model.MappingRule) {
	mid := *proxyRule.Id
	pid := *proxyRule.Proxy_id
	requestUrl := adminPortal + "/admin/api/services/" + fmt.Sprint(pid) + "/proxy/mapping_rules/" + fmt.Sprint(mid) + ".xml"

	data := url.Values{}
	data.Set("access_token", accessToken)

	client := &http.Client{}
	request, err := http.NewRequest("DELETE", requestUrl, strings.NewReader(data.Encode()))
	if err != nil {
		panic(err)
	}

	request.Header.Set("Accept", "*/*")
	request.Header.Set("Content-Type", "application/x-www-form-urlencoded")

	response, err := client.Do(request)

	if err != nil {
		panic(err)
	}

	defer response.Body.Close()
	// contents, err := ioutil.ReadAll(response.Body)
	// if err != nil {
	// 	log.Fatal(err)
	// }
	// fmt.Println("The calculated length is:", len(string(contents)), "for the url:", requestUrl)
	// fmt.Println("   ", response.StatusCode)
	// hdr := response.Header
	// for key, value := range hdr {
	// 	fmt.Println("   ", key, ":", value)
	// }
	// fmt.Println(contents)
}

func DeleteBackendRule(backendRule model.MappingRule) {
	mid := *backendRule.Id
	bid := *backendRule.Owner_id
	requestUrl := adminPortal + "/admin/api/backend_apis/" + fmt.Sprint(bid) + "/mapping_rules/" + fmt.Sprint(mid) + ".json"

	data := url.Values{}
	data.Set("access_token", accessToken)

	client := &http.Client{}
	request, err := http.NewRequest("DELETE", requestUrl, strings.NewReader(data.Encode()))
	if err != nil {
		panic(err)
	}

	request.Header.Set("Accept", "*/*")
	request.Header.Set("Content-Type", "application/x-www-form-urlencoded")

	response, err := client.Do(request)

	if err != nil {
		panic(err)
	}

	defer response.Body.Close()
}
