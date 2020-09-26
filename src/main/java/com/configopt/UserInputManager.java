package com.configopt;

import java.util.Scanner;

public class UserInputManager {
    public static boolean requestMappingKeep(final MappingRuleSM mappingRule, MappingRuleSM otherRule,
            boolean ask) {
        if (ask)
            System.out.println(
                    "This rule: " + mappingRule + " collides with: " + otherRule);
        System.out.println("Would you like to keep " + mappingRule.toString() + "?  Y/N");
        final Scanner in = new Scanner(System.in);
        while (true) {
            final String response = in.nextLine();
            if (response.equalsIgnoreCase("Y")) {
                return true;
            } else if (response.equalsIgnoreCase("N")) {
                return false;
            }
            System.out.println("Invalid response, would you like to keep " + mappingRule.toString() + "? Y/N");
        }
    }

    // public static boolean requestMappingOptimize(MappingRule mappingRule) {
    //     System.out.println("Would you like to proceed?  Y/N");
    //     final Scanner in = new Scanner(System.in);
    //     while (true) {
    //         final String response = in.nextLine();
    //         if (response.equalsIgnoreCase("Y")) {
    //             return false;
    //         } else if (response.equalsIgnoreCase("N")) {
    //             mappingRule.markForDeletion();
    //             return true;
    //         }
    //         System.out.println("Invalid response, would you like to proceed? Y/N");
    //     }
    // }

    public static boolean requestOptimization(MappingRuleSM m1, MappingRuleSM m2) {

        MappingRuleSM shorter = MappingRulesUtils.getShorter(m1, m2);
        MappingRuleSM longer = shorter.matches(m1) ? m2 : m1;

        if (!shorter.getPath().endsWith("$"))
            throw new IllegalArgumentException("optimizable not ending with $");
        System.out.println("These rules " + shorter.toString() + ", " + longer.toString()
                + " could be optimized by removing the dollar from " + shorter + " and deleting " + longer
                + ". Would you like to proceed?  Y/N");
        final Scanner in = new Scanner(System.in);
        boolean optimize = false;
        while (true) {
            final String response = in.nextLine();
            if (response.equalsIgnoreCase("Y")) {
                optimize = true;
                break;
            } else if (response.equalsIgnoreCase("N")) {
                break;
            }
            System.out.println("Invalid response, would you like to proceed? Y/N");
        }
        return optimize;
    }
}