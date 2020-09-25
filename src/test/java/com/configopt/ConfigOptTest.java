package com.configopt;

import static org.junit.Assert.assertTrue;

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
        PathNode node = new PathNode();
        MappingRule mappingRule = new MappingRule("GET", "/", 0l);
        node.insert(apicast, mappingRule, 0);
        assertTrue(MappingRulesUtils.validateInsertion(apicast, node, mappingRule));
    }
}
