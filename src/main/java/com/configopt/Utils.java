package com.configopt;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Utils {
    protected static Mode mode = Mode.SCAN;
    protected static final String LOG_TAG = "CONFIGOPT_LOGS";
    private final static String PATTERN = "pattern";
    private final static String ID = "id";
    private final static String PROXY_CONFIGS = "proxy_configs";
    private final static String PROXY_CONFIG = "proxy_config";
    private final static String PROXY_RULES = "proxy_rules";

    public enum Mode {
        SCAN, FIXINTERACTIVE
    };

    protected static JSONObject extractConfigJSONFromFile(String filePath) {
        JSONParser jsonParser = new JSONParser();
        JSONObject obj = null;
        Logger.getLogger(Utils.LOG_TAG).log(Level.INFO, "Reading from: " + filePath);
        try (FileReader reader = new FileReader(filePath)) {
            obj = (JSONObject) jsonParser.parse(reader);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return obj;
    }

    protected static List<Service> createServicesFromJSONConfig(JSONObject jsonConfig) {
        List<Service> services = new ArrayList<>();
        JSONArray JSONServices = extractJSONServicesFromJSONConfig(jsonConfig);
        JSONServices.forEach(JSONService -> createServiceFromJSONService(services, (JSONObject) JSONService));
        return services;
    }

    protected static JSONArray extractJSONServicesFromJSONConfig(JSONObject jsonConfig) {
        return (JSONArray) jsonConfig.get(PROXY_CONFIGS);
    }

    private static void createServiceFromJSONService(List<Service> services, JSONObject JSONService) {
        JSONObject proxy = extractProxyFromJSONService(JSONService);
        Service service = new Service((Long) proxy.get(ID), (String) proxy.get("endpoint"));
        JSONArray rules = (JSONArray) proxy.get(PROXY_RULES);
        rules.forEach(rule -> addMappingRuleFromJSONRuleToService(service, (JSONObject) rule));
        services.add(service);
    }

    private static JSONObject extractProxyFromJSONService(JSONObject JSONService) {
        return (JSONObject) ((JSONObject) ((JSONObject) JSONService.get(PROXY_CONFIG)).get("content")).get("proxy");
    }

    private static void addMappingRuleFromJSONRuleToService(Service service, JSONObject JSONRule) {
        service.addProductMappingRule(
                new MappingRuleSM((String) JSONRule.get("http_method"), (String) JSONRule.get(PATTERN), service.getId(),
                        (String) JSONRule.get("owner_type"), (Long) JSONRule.get(ID)));
    }

    /**
     * Calculate severity of the mapping rules partial or full match assuming: The
     * mapping rules methods are the same The services' hosts are colliding
     * depending on the path routing rules (this is done in
     * APIcast#createServiceGroups()) The mapping rules partially match each others
     */
    public static int calculateSeverity(APIcast apicast, MappingRuleSM mr, MappingRuleSM mappingRule) {
        int severity = 2;
        if (mr.canBeOptimized(mappingRule))
            severity = 5;
        else if ((apicast.getPathRoutingEnabled() || apicast.getPathRoutingOnlyEnabled())
                && mr.getServiceId() != mappingRule.getServiceId())
            severity = 1;
        // else if(checkOptimization(mr, mappingRule))
        // severity = 5;
        return severity;
    }

    public static void findAndReplaceMappingRule(JSONObject oldConfig, MappingRuleSM mappingRule) {
        JSONArray JSONServices = extractJSONServicesFromJSONConfig(oldConfig);
        for (Object jsonObjectSvc : JSONServices) {
            JSONObject service = (JSONObject) jsonObjectSvc;
            JSONObject proxy = extractProxyFromJSONService(service);
            if ((Long) proxy.get(ID) == mappingRule.getServiceId()) {
                JSONArray rules = (JSONArray) proxy.get(PROXY_RULES);
                Iterator<Object> iterator = rules.iterator();
                while (iterator.hasNext()) {
                    Object jsonObjectRule = iterator.next();
                    JSONObject rule = (JSONObject) jsonObjectRule;
                    if (rule.get(ID) == mappingRule.getId()) {
                        if (mappingRule.isMarkedForDeletion())
                            iterator.remove();
                        else {
                            rule.put(PATTERN, mappingRule.getRealPath());
                        }
                    }
                }
            }
        }
    }
}