package com.configopt;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import com.configopt.Utils.Mode;

import org.junit.Test;


public class ConfigOptTest 
{
    @Test
    public void alwaysReturnTrueInScanMode()
    {
        APIcast apicast = new APIcast();
        apicast.setPathRoutingEnabled(true);
        Utils.mode = Mode.SCAN;
        MappingRuleSM mappingRule = new MappingRuleSM("GET", "/", 0l, "proxy", 1l);
        MappingRuleSM mappingRule2 = new MappingRuleSM("GET", "/foo", 0l, "proxy", 2l);
        MappingRuleSM mappingRule3 = new MappingRuleSM("GET", "/bar", 0l, "proxy", 3l);
        List<MappingRuleSM> allRules = new ArrayList<>();
        allRules.add(mappingRule2); allRules.add(mappingRule3);
        MappingRulesUtils.validateMappingRule(apicast, mappingRule, allRules, 1);
    }
}
