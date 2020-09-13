public class TestPathNode{
    public static void main(String[]args){
        String[] mappingRulesPaths = new String[]{"/foo/bar","/foo/bar/test","/whatever","/fo"};
        PathTree tree = new PathTree();
        for(String mappingRulePath : mappingRulesPaths){
            MappingRule mappingRule = new MappingRule("GET", mappingRulePath);
            tree.insertMappingRule(mappingRule);
        }
    }
}