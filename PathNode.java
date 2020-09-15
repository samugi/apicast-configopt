import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class PathNode: a node of the PathTree. Each node holds a character that is part of a mapping rule
 * 
 * routeMappings represents the list of mapping rules that the current node is part of, for example:
 * 
 *       `a`
 *      /   \
 *    `b`   `e`
 *    / \
 *  `c` `d`
 * 
 * Node `a` would have routeMappings = ["abc", "abd", "ae"]
 * Node `b` would have routeMappings = ["abc", "abd"]
 * Node `c` would have routeMappings = ["abc"]
 * etc...
 * 
 * pathSoFar is the path that goes straight from the root until the current node
 * data is the char that is held in the Node 
 * 
 */
public class PathNode {
    private List<PathNode> children = new ArrayList<PathNode>();
    private PathNode parent = null;
    private List<MappingRule> routeMappings = new ArrayList<>();
    private String pathSoFar;
    private char data;

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
     * Recursively adds the characters in "path" starting from "index" in the current Node and its children
     * @param path the entire path to insert
     * @param index the index of the character that will be inserted in the current node
     */
    public void insert(MappingRule mappingRule, int index) {
        char tmpData = mappingRule.getPath().charAt(index);
        String tmpPathSoFar = mappingRule.getPath().substring(0, index + 1);
        if (this.data != '\u0000' && this.data != tmpData /*|| (pathSoFar != null && !pathSoFar.equals(tmpPathSoFar))*/) {
            throw new IllegalArgumentException("can't insert value on node with different data");
        }
        if(!routeMappings.contains(mappingRule))
            this.routeMappings.add(mappingRule);
        pathSoFar = tmpPathSoFar;

        MappingRulesController.validateInsertion(this, mappingRule, index);

        this.data = tmpData; //here this.data is either null or it has the same value of tmpData
        System.out.println("set this node's data to: " + this.data);
        //from here we go on with the children
        if (index < mappingRule.getPath().length()-1) {
            boolean foundChild = false;
            for(PathNode child : this.children){
                if(child.getData() == mappingRule.getPath().charAt(index+1)){
                    System.out.println("found existing child");
                    child.insert(mappingRule, index+1);
                    foundChild = true;
                }
            }
            if(!foundChild){
                PathNode child = new PathNode();
                this.addChild(child);
                child.insert(mappingRule, index + 1);
            }
        }else{
            System.out.println("Finished adding mapping rule: " + mappingRule.toString());
        }
    }

    public void setParent(PathNode parent) {
        this.parent = parent;
    }

    public void addChild(PathNode child) {
        child.setParent(this);
        this.children.add(child);
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
        this.parent = null;
    }

    public List<MappingRule> getRouteMappings(){
        return this.routeMappings;
    }
}