package com.configopt;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Class PathNode: a node of the PathTree. Each node holds a character that is
 * part of a mapping rule
 * 
 * routeMappings represents the list of mapping rules that the current node is
 * part of, for example:
 * 
 *     `a` 
 *     / \ 
 *   `b` `e` 
 *   / \  
 * `c` `d`
 * 
 * Node `a` would have routeMappings = ["abc", "abd", "ae"] Node `b` would have
 * routeMappings = ["abc", "abd"] Node `c` would have routeMappings = ["abc"]
 * etc...
 * 
 * pathSoFar is the path that goes straight from the root until the current node
 * data is the char that is held in the Node
 * 
 */
public class PathNode {
    private List<PathNode> children = new CopyOnWriteArrayList<>();
    private PathNode parent = null;
    private List<MappingRule> routeMappings = new CopyOnWriteArrayList<>();
    private List<MappingRule> mappingRulesEndingHere = new CopyOnWriteArrayList<>();
    private String pathSoFar;
    private char data;
    Logger logger = Logger.getLogger(PathNode.class.getName());

    public PathNode() {

    }

    public PathNode(char character) {
        this.data = character;
    }

    public PathNode(char character, PathNode parent) {
        this.data = character;
        this.parent = parent;
    }

    /**
     * Recursively adds the characters in "path" starting from "index" in the
     * current Node and its children
     * 
     * @param path  the entire path to insert
     * @param index the index of the character that will be inserted in the current
     *              node
     */
    public void insert(APIcast apicast, MappingRule mappingRule, int index) {
        char tmpData = mappingRule.getPath().charAt(index);
        String tmpPathSoFar = mappingRule.getPath().substring(0, index + 1);
        if (this.data != '\u0000'
                && this.data != tmpData /* || (pathSoFar != null && !pathSoFar.equals(tmpPathSoFar)) */) {
            throw new IllegalArgumentException("can't insert value on node with different data");
        }

        if (!mappingRule.forceInsertion() && !MappingRulesUtils.validateInsertion(apicast, this, mappingRule, index))
            return;

        this.routeMappings.add(mappingRule);
        this.pathSoFar = tmpPathSoFar;
        this.data = tmpData; // here this.data is either null or it has the same value of tmpData
        logger.log(Level.INFO, "set this node's data to: " + this.data);
        // from here we go on with the children
        if (index < mappingRule.getPath().length() - 1) {
            boolean foundChild = false;
            for (PathNode child : this.children) {
                if (child.getData() == mappingRule.getPath().charAt(index + 1)) {
                    child.insert(apicast, mappingRule, index + 1);
                    foundChild = true;
                }
            }
            if (!foundChild) {
                PathNode child = new PathNode();
                this.addChild(child);
                child.insert(apicast, mappingRule, index + 1);
            }
        } else {
            logger.log(Level.INFO, "Finished adding mapping rule: " + mappingRule.toString());
            this.mappingRulesEndingHere.add(mappingRule);
        }
    }

    /**
     * Recursively removes the characters in "path" starting from "index" from the current Node and its children
     * @param path the entire path to remove
     * @param index the index of the character that will be inserted in the current node
     */
    public void remove(MappingRule mappingRule, int index) {
        routeMappings.remove(mappingRule);
        if(this.routeMappings.size() == 0)
            this.removeParent();
        if (index < mappingRule.getPath().length()-1) {
            for(PathNode child : this.children){
                if(child.getData() == mappingRule.getPath().charAt(index+1))
                    child.remove(mappingRule, index+1);
            }
        }else{
            logger.log(Level.INFO, "Finished removing mapping rule: " + mappingRule.toString());
            this.mappingRulesEndingHere.remove(mappingRule); //useless
        }
    }

    public void setParent(PathNode parent) {
        this.parent = parent;
    }

    public void addChild(PathNode child) {
        child.setParent(this);
        this.children.add(child);
    }

    public void removeChild(PathNode child){
        child.setParent(null);
        this.children.remove(child);
    }

    public char getData() {
        return this.data;
    }

    public void setData(char data) {
        this.data = data;
    }

  
    public boolean isRoot() {
        return (this.parent == null);
    }

    public boolean isLeaf() {
        return this.children.size() == 0;
    }

    public void removeParent() {
        this.parent.removeChild(this);
        this.parent = null;
    }

    public List<MappingRule> getRouteMappings(){
        return this.routeMappings;
    }

    public List<MappingRule> getMappingRulesEndingHere(){
        return this.mappingRulesEndingHere;
    }

	public void removeMappingRuleFromTree(MappingRule mr) {
        PathNode root = this;
        while(root.parent != null)
            root = root.parent;
        root.remove(mr, 0);
	}
}