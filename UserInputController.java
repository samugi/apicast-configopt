import java.util.Scanner;

public class UserInputController {
	public static boolean requestMappingKeep(final MappingRule mappingRule) {
        System.out.println("Would you like to remove " + mappingRule.toString() + "?  Y/N");
        final Scanner in = new Scanner(System.in);
        while (true) {
            final String response = in.nextLine();
                if(response.equalsIgnoreCase("Y")){
                    mappingRule.markForDeletion();
                    return false;
                }else if(response.equalsIgnoreCase("N")){
                    return true;
                }
                System.out.println("Invalid response, would you like to remove "+ mappingRule.toString() + "? Y/N");
            }
            
	}
}