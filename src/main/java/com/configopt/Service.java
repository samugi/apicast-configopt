package com.configopt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Service{
    private final List<MappingRule> productMappingRules = new ArrayList<>();
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

    public void addProductMappingRule(MappingRule mappingRule){
        mappingRule.setHost(host);
        this.productMappingRules.add(mappingRule);
    }

    public List<MappingRule> getProductMappingRules(){
        return this.productMappingRules;
    }

    @Override
    public String toString(){
        return "host: " + host + "MappingRules: " + Arrays.toString(productMappingRules.toArray());
    }
}