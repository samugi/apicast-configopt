package com.configopt;

import java.util.ArrayList;
import java.util.List;
//import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class PathNode: a node of the PathTree. Each node holds a character that is
 * part of a mapping rule
 * 
 * routeMappings represents the list of mapping rules that the current node is
 * part of, for example:
 * 
 * `a` / \ `b` `e` / \ `c` `d`
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
    private List<PathNode> children = new ArrayList<>();// new CopyOnWriteArrayList<>();
    private PathNode parent = null;
    private List<MappingRule> routeMappings = new ArrayList<>(); // new CopyOnWriteArrayList<>();
    private List<MappingRule> mappingRulesEndingHere = new ArrayList<>(); // new CopyOnWriteArrayList<>();
    private boolean isLastBeforeDollar = false;
    private PathPiece data;
    Logger logger = Logger.getLogger(Utils.LOG_TAG);

    public PathNode() {

    }

    public PathNode(PathPiece str) {
        this.data = str;
    }

    public PathNode(PathPiece str, PathNode parent) {
        this.data = str;
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
        PathPiece tmpData = MappingRulesUtils.getNextPiece(mappingRule, index);
        if (this.getData() != null
                && !this.getData().equals(tmpData) /* || (pathSoFar != null && !pathSoFar.equals(tmpPathSoFar)) */) {
            throw new IllegalArgumentException("can't insert value on node with different data");
        }

        if (!mappingRule.forceInsertion() && !MappingRulesUtils.validateInsertion(apicast, this, mappingRule) || mappingRule.getPath().length() < index)
            return;

        this.routeMappings.add(mappingRule);
        this.setData(tmpData);
        int pathPieceLength = this.getData().toString().length();
        index += pathPieceLength -1;
        logger.log(Level.INFO, "set this node's data to: " + this.data);
        // from here we go on with the children
        if (index < mappingRule.getPath().length() - 1 ) {
            boolean foundChild = false;
            for (PathNode child : this.children) {
                if (child.getData().equals(MappingRulesUtils.getNextPiece(mappingRule, index+1))) {
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
            this.addMappingRulesEndingHere(mappingRule);
        }
    }

    /**
     * Recursively removes the characters in "path" starting from "index" from the
     * current Node and its children
     * 
     * @param path  the entire path to remove
     * @param index the index of the character that will be inserted in the current
     *              node
     */
    public void removeRecursive(MappingRule mappingRule, int index) {
        routeMappings.remove(mappingRule);
        if (this.routeMappings.size() == 0 && !this.isRoot())
            this.removeParent();
        if (index < mappingRule.getPath().length() - 1) {
            for (PathNode child : this.children) {
                if (child.getData().equals(MappingRulesUtils.getNextPiece(mappingRule, index+1)))
                    child.remove(mappingRule, index + 1);
            }
        } else {
            logger.log(Level.INFO, "Finished removing mapping rule: " + mappingRule.toString());
            this.mappingRulesEndingHere.remove(mappingRule); // useless
        }
    }

    /**
     * Removes the characters in "path" starting from "index" from the current Node
     * and its children
     * 
     * @param path  the entire path to remove
     * @param index the index of the character that will be inserted in the current
     *              node
     */
    public void remove(MappingRule mappingRule, int index) {
        PathNode node = this;
        while (index < mappingRule.getPath().length() - 1) {
            List<PathNode> children = node.getChildren();

            node.getRouteMappings().remove(mappingRule);
            if (node.routeMappings.size() == 0 && !node.isRoot())
                node.removeParent();

            for (PathNode child : children) {
                PathPiece pp = MappingRulesUtils.getNextPiece(mappingRule, index+1);
                if (child.getData().equals(pp)) {
                    node = child;
                    index += pp.toString().length();
                    continue;
                }
            }
        }

        logger.log(Level.INFO, "Finished removing mapping rule: " + mappingRule.toString());
        this.mappingRulesEndingHere.remove(mappingRule); // useless
    }

    public void setIsLastBeforeDollar(boolean isLastBeforeDollar) {
        this.isLastBeforeDollar = true;
    }

    public boolean getIsLastBeforeDollar() {
        return this.isLastBeforeDollar;
    }

    public void setParent(PathNode parent) {
        this.parent = parent;
    }

    public void addChild(PathNode child) {
        child.setParent(this);
        this.children.add(child);
    }

    public void removeChild(PathNode child) {
        child.setParent(null);
        this.children.remove(child);
    }

    public PathPiece getData() {
        return this.data;
    }

    public PathNode getParent(){
        return this.parent;
    }

    public void setData(PathPiece data) {
        this.data = data;
    }

    public boolean isRoot() {
        return (this.parent == null);
    }

    public boolean isLeaf() {
        return this.children.size() == 0;
    }

    public List<PathNode> getChildren() {
        return this.children;
    }

    public void removeParent() {
        this.parent.removeChild(this);
        this.parent = null;
    }

    public List<MappingRule> getRouteMappings() {
        return this.routeMappings;
    }

    public List<MappingRule> getMappingRulesEndingHere() {
        return this.mappingRulesEndingHere;
    }

    public void addMappingRulesEndingHere(MappingRule mr) {
        this.mappingRulesEndingHere.add(mr);
    }

    public void removeMappingRuleFromTree(MappingRule mr) {
        PathNode root = this;
        while (root.parent != null)
            root = root.parent;
        root.remove(mr, 0);
    }
}