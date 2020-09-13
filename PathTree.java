public class PathTree{

    public PathNode root = null;

    public void insertMappingRule(MappingRule mappingRule){
        String path = mappingRule.getPath();
        if(root == null)
            root = new PathNode();
        root.insert(path, 0);
    }
}