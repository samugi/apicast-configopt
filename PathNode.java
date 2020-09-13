import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PathNode {
    private List<PathNode> children = new ArrayList<PathNode>();
    private PathNode parent = null;
    private List<String> paths = new ArrayList<>();
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

    public void insert(String path, int index) {
        char tmpData = path.charAt(index);
        String tmpPathSoFar = path.substring(0, index + 1);
        if (this.data != '\u0000' && this.data != tmpData || (pathSoFar != null && !pathSoFar.equals(tmpPathSoFar))) {
            throw new IllegalArgumentException("can't insert value on node with different data");
        }
        if(!paths.contains(path))
            this.paths.add(path);
        pathSoFar = tmpPathSoFar;
        if ((this.isLeaf() || index == path.length()-1) && this.data != '\u0000') {
            System.out.println("Duplicate rules: " + Arrays.toString(paths.toArray()));
        }

        this.data = tmpData; //here data is either null or it has the same value of tmpData
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