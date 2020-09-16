import java.util.Arrays;
import java.util.Scanner;

public class MappingRulesController {
    public static void validateInsertion(final PathNode node, final MappingRule mappingRule, final int index){
        if(node.getMappingRulesEndingHere().size() > 0){
            System.out.println("This rule: `" + mappingRule + "` collides with: " + Arrays.toString(node.getMappingRulesEndingHere().toArray()));
        }

    
            /*System.out.println("Would you like to remove " + mappingRule.toString() + "?  Y/N");
            Scanner in = new Scanner(System.in);
            while(true){
                String response = in.nextLine();
                if(response.equalsIgnoreCase("Y")){
                    mappingRule.markForDeletion();
                    break;
                }else if(response.equalsIgnoreCase("N")){
                    break;
                }
                System.out.println("Invalid response, would you like to remove "+ mappingRule.toString() + "? Y/N");
            }
            in.close();*/
    }
}