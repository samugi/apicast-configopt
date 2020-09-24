package com.configopt;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    protected static Mode mode = Mode.SCAN;
    protected static final String LOG_TAG = "CONFIGOPT_LOGS";

    public enum Mode {
        SCAN, FIXINTERACTIVE
    };

    protected static JSONObject extractConfigJSONFromFile(String filePath) {
        JSONParser jsonParser = new JSONParser();
        JSONObject obj = null;
        System.out.println("going to open: " + filePath);
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
        JSONArray JSONServices = (JSONArray) jsonConfig.get("proxy_configs");
        JSONServices.forEach(JSONService -> createServiceFromJSONService(services, (JSONObject) JSONService));
        return services;
    }

    private static void createServiceFromJSONService(List<Service> services, JSONObject JSONService) {
        JSONObject proxy = extractProxyFromJSONService(JSONService);
        Service service = new Service((Long) proxy.get("id"), (String) proxy.get("endpoint"));
        JSONArray rules = (JSONArray) proxy.get("proxy_rules");
        rules.forEach(rule -> addMappingRuleFromJSONRuleToService(service, (JSONObject) rule));
        services.add(service);
    }

    private static JSONObject extractProxyFromJSONService(JSONObject JSONService) {
        return (JSONObject) ((JSONObject) ((JSONObject) JSONService.get("proxy_config")).get("content")).get("proxy");
    }

    private static void addMappingRuleFromJSONRuleToService(Service service, JSONObject JSONRule) {
        service.addProductMappingRule(new MappingRule((String) JSONRule.get("http_method"),
                (String) JSONRule.get("pattern"), service.getId()));
    }

    /** Calculate severity of the mapping rules partial or full match assuming:
     *  The mapping rules methods are the same
     *  The services' hosts are colliding depending on the path routing rules (this is done in APIcast#createServiceGroups())
     *  The mapping rules partially match each others
     */
    public static int calculateSeverity(APIcast apicast, MappingRule mr, MappingRule mappingRule) {
        int severity = 5;
        if ((apicast.getPathRoutingEnabled() || apicast.getPathRoutingOnlyEnabled()) && mr.getServiceId() != mappingRule.getServiceId())
            severity = 1;
        return severity;
    }

}