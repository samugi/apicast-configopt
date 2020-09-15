import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class PathNode: a node of the PathTree. Each node holds a character that is part of a mapping rule
 * 
 * routePaths represents the list of paths that the current node is part of, for example:
 * 
 *       `a`
 *      /   \
 *    `b`   `e`
 *    / \
 *  `c` `d`
 * 
 * Node `a` would have routePaths = ["abc", "abd", "ae"]
 * Node `b` would have routePaths = ["abc", "abd"]
 * Node `c` would have routePaths = ["abc"]
 * etc...
 * 
 * pathSoFar is the path that goes straight from the root until the current node
 * data is the char that is held in the Node 
 * 
 */
public class PathNode {
    private List<PathNode> children = new ArrayList<PathNode>();
    private PathNode parent = null;
    private List<String> routePaths = new ArrayList<>();
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
    public void insert(String path, int index) {
        char tmpData = path.charAt(index);
        String tmpPathSoFar = path.substring(0, index + 1);
        if (this.data != '\u0000' && this.data != tmpData /*|| (pathSoFar != null && !pathSoFar.equals(tmpPathSoFar))*/) {
            throw new IllegalArgumentException("can't insert value on node with different data");
        }
        if(!routePaths.contains(path))
            this.routePaths.add(path);
        pathSoFar = tmpPathSoFar;
        if ((this.isLeaf() || index == path.length()-1) && this.data != '\u0000') {
            System.out.println("Duplicate rules: " + Arrays.toString(routePaths.toArray()));
        }
        this.data = tmpData; //here this.data is either null or it has the same value of tmpData
        System.out.println("set this node's data to: " + this.data);
        //from here we go on with the children
        if (index < path.length()-1) {
            boolean foundChild = false;
            for(PathNode child : this.children){
                if(child.getData() == path.charAt(index+1)){
                    System.out.println("found existing child");
                    child.insert(path, index+1);
                    foundChild = true;
                }
            }
            if(!foundChild){
                PathNode child = new PathNode();
                this.addChild(child);
                child.insert(path, index + 1);
            }
        }else{
            System.out.println("Finished adding mapping rule with path: " + path);
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
}