package com.configopt;

import java.util.Scanner;

public class UserInputManager {
	public static boolean requestMappingKeep(final MappingRule mappingRule) {
        System.out.println("Would you like to keep " + mappingRule.toString() + "?  Y/N");
        final Scanner in = new Scanner(System.in);
        while (true) {
            final String response = in.nextLine();
                if(response.equalsIgnoreCase("Y")){
                    return true;
                }else if(response.equalsIgnoreCase("N")){
                    mappingRule.markForDeletion();
                    return false;
                }
                System.out.println("Invalid response, would you like to keep "+ mappingRule.toString() + "? Y/N");
            }
            
	}
}