package com.configopt;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class OutputUtils {
    static String outputFile = null;

    public static void printIssues(List<CollisionIssue> issues) {
        if (outputFile == null) {
            for (CollisionIssue issue : MappingRulesUtils.issues) {
                System.out.println(issue);
            }
            return;
        }
        try {
            FileWriter writer = new FileWriter(outputFile, true);
            BufferedWriter buffer = new BufferedWriter(writer);
            for (CollisionIssue issue : MappingRulesUtils.issues) {
                buffer.newLine();
                buffer.write(issue.toString());
                buffer.newLine();
            }
            buffer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}