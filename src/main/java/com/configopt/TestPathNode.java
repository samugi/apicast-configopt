package com.configopt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.configopt.Utils.Mode;
import org.apache.commons.cli.*;
import org.json.simple.JSONObject;

public class TestPathNode {
    public static void main(String[] args) {

        Options options = new Options();

        Option configurationOption = Option.builder("c").longOpt("configuration").required(true)
                .desc("JSON configuration file path").hasArg().build();
        Option outputFileOption = Option.builder("o").longOpt("output").required(false).desc("output file").hasArg()
                .build();
        Option debugLogLevelOption = Option.builder("v").longOpt("verbose").required(false).desc("verbose logs")
                .build();

        // configurationOption.setRequired(true);
        options.addOption(configurationOption);
        options.addOption(outputFileOption);
        options.addOption(debugLogLevelOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        System.out.println("args: " + Arrays.toString(args));
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("ConfigOpt", options);
            System.exit(1);
        }

        String inputFilePath = cmd.getOptionValue("configuration");
        OutputUtils.outputFile = cmd.getOptionValue("output");

        JSONObject JSONConfig = Utils.extractConfigJSONFromFile(inputFilePath);
        List<Service> services = Utils.createServicesFromJSONConfig(JSONConfig);

        if(cmd.hasOption(debugLogLevelOption.getOpt()))
            Logger.getLogger(Utils.LOG_TAG).setLevel(Level.ALL);
        else
            Logger.getLogger(Utils.LOG_TAG).setLevel(Level.SEVERE);
        // Service service1 = new Service(12l, "example.org");
        // service1.addProductMappingRule(new MappingRule("GET", "/foo/bar", 12l));
        // service1.addProductMappingRule(new MappingRule("GET", "/foo/bar/test", 12l));
        // service1.addProductMappingRule(new MappingRule("GET", "/whatever", 12l));
        // service1.addProductMappingRule(new MappingRule("GET", "/fo", 12l));

        // Service service2 = new Service(12l, "example.org");
        // service1.addProductMappingRule(new MappingRule("GET", "/foo/bar", 12l));
        // service1.addProductMappingRule(new MappingRule("GET", "/foo/bar/test", 12l));
        // service1.addProductMappingRule(new MappingRule("GET", "/whatever", 12l));
        // service1.addProductMappingRule(new MappingRule("GET", "/fo", 12l));
        // List<Service> services = new ArrayList<>();
        // services.add(service1);
        // services.add(service2);

        Utils.mode = Mode.SCAN;
        APIcast apicast = new APIcast();
        apicast.setPathRoutingEnabled(true);
        for (Service service : services) {
            apicast.addService(service);
        }
        apicast.validateAllServices();

    }
}