package com.configopt;

public class PathTree{

    public PathNode root = null;

    public void insertMappingRule(MappingRule mappingRule){
        if(root == null)
            root = new PathNode();
        root.insert(mappingRule, 0);
    }
}