package com.configopt;

import com.configopt.Utils.Mode;

public class TestPathNode {
    public static void main(String[]args){
        Service service1 = new Service("example.org");
        service1.addProductMappingRule(new MappingRule("GET", "/foo/bar"));
        service1.addProductMappingRule(new MappingRule("GET", "/foo/bar/test"));
        service1.addProductMappingRule(new MappingRule("GET", "/whatever"));
        service1.addProductMappingRule(new MappingRule("GET", "/fo"));

        Service service2 = new Service("example.org");
        service1.addProductMappingRule(new MappingRule("GET", "/foo/bar"));
        service1.addProductMappingRule(new MappingRule("GET", "/foo/bar/test"));
        service1.addProductMappingRule(new MappingRule("GET", "/whatever"));
        service1.addProductMappingRule(new MappingRule("GET", "/fo"));

        Utils.mode = Mode.STDOUTPUT;
        APIcast apicast = new APIcast();
        apicast.setPathRoutingOnlyEnabled(true);
        apicast.addService(service1);
        apicast.addService(service2);
        apicast.validateAllServices();
        
    }
}