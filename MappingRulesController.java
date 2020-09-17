import java.util.Arrays;

public class MappingRulesController {
    public static boolean validateInsertion(final PathNode node, final MappingRule mappingRule, final int index){
        if(node.getMappingRulesEndingHere().size() > 0){
            System.out.println("This rule: `" + mappingRule + "` collides with: " + Arrays.toString(node.getMappingRulesEndingHere().toArray()));

            
            boolean insert = UserInputController.requestMappingKeep(mappingRule);
            if(insert)
                mappingRule.setForceInsertion(true); //ask the user only once for each rule
        
            for(MappingRule mr : node.getMappingRulesEndingHere()){
                if(!UserInputController.requestMappingKeep(mr))
                    node.removeMappingRuleFromTree(mr);
            }
            return insert;
        }
        return true;
    }
}