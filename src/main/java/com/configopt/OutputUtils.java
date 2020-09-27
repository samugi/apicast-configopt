package com.configopt;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.json.simple.JSONObject;

public class OutputUtils {
    static String outputFile = null;

    protected static void rewriteConfig(APIcast apicast, JSONObject oldConfig) {
        if (outputFile == null)
            return;
        List<Service> services = apicast.getServices();
        for (Service service : services) {
            List<MappingRuleSM> mappingRules = service.getProductMappingRules();
            for (MappingRuleSM mappingRule : mappingRules) {
                Utils.findAndReplaceMappingRule(oldConfig, mappingRule);
            }
        }
        FileWriter writer = null;
        try {
            writer = new FileWriter(outputFile, false);            
            writer.write(oldConfig.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void printIssues(List<CollisionIssue> issues) {
        Collections.sort(issues);
        if (outputFile == null) {
            for (CollisionIssue issue : issues) {
                System.out.println(issue);
            }
            return;
        }
        try {
            FileWriter writer = new FileWriter(outputFile, false);
            BufferedWriter buffer = new BufferedWriter(writer);
            for (CollisionIssue issue : issues) {
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