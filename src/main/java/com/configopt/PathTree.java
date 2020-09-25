package com.configopt;

public class PathTree{

    public PathNode root = null;

    public void insertMappingRule(APIcast apicast, MappingRule mappingRule){
        if(root == null)
            root = new PathNode();
        root.insert(apicast, mappingRule, 0);
    }

    public PathNode getRoot(){
        return this.root;
    }
}