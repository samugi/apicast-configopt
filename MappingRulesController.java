import java.util.Arrays;

public class MappingRulesController {
    public static void validateInsertion(final PathNode node, final MappingRule mappingRule, final int index){
        if ((node.isLeaf() || index == mappingRule.getPath().length()-1) && node.getData() != '\u0000') {
            System.out.println("Duplicate rules: " + Arrays.toString(node.getRouteMappings().toArray()));
        }
    }
}