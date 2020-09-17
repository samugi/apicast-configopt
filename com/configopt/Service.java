package com.configopt;

import java.util.ArrayList;
import java.util.List;

public class Service{
    private final List<MappingRule> productMappingRules = new ArrayList<>();
    private String host;

    public Service(String host){
        this.host = host;
    }

    public void setHost(String host){
        this.host = host;
    }

    public String getHost(){
        return this.host;
    }

    public void addProductMappingRule(MappingRule mappingRule){
        mappingRule.setHost(host);
        this.productMappingRules.add(mappingRule);
    }

    public List<MappingRule> getProductMappingRules(){
        return this.productMappingRules;
    }
}