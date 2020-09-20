package com.configopt;

import static org.junit.Assert.assertTrue;

import com.configopt.Utils.Mode;

import org.junit.Test;


public class ConfigOptTest 
{
    @Test
    public void alwaysReturnTrueInScanMode()
    {
        Utils.mode = Mode.SCAN;
        PathNode node = new PathNode();
        MappingRule mappingRule = new MappingRule("GET", "/");
        node.insert(mappingRule, 0);
        assertTrue(MappingRulesUtils.validateInsertion(node, mappingRule, 0));
    }
}
