package com.configopt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Service{
    private final List<MappingRuleSM> productMappingRules = new ArrayList<>();
    private String host;
    private Long id;

    public Service(Long id, String host){
        this.id = id;
        this.host = host;
    }

    public String getHost(){
        return this.host;
    }

    public Long getId(){
        return this.id;
    }

    public void addProductMappingRule(MappingRuleSM mappingRule){
        mappingRule.setHost(host);
        this.productMappingRules.add(mappingRule);
    }

    public List<MappingRuleSM> getProductMappingRules(){
        return this.productMappingRules;
    }

    @Override
    public String toString(){
        return "host: " + host + "MappingRules: " + Arrays.toString(productMappingRules.toArray());
    }
}