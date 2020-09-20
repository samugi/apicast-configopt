package com.configopt;

import java.util.logging.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.configopt.Utils.Mode;

public class MappingRulesUtils {

    protected static List<CollisionIssue> issues = new ArrayList<>();
    static Logger logger = Logger.getLogger(MappingRulesUtils.class.getName());

    public static boolean validateInsertion(final PathNode node, final MappingRule mappingRule, final int index){
        List<MappingRule> mrEndingInThisNode = node.getMappingRulesEndingHere();
        if(mrEndingInThisNode.size() > 0 && sameMethod(mappingRule, mrEndingInThisNode)){
            if(Utils.mode == Mode.SCAN){
                for(MappingRule mr : mrEndingInThisNode){
                    if(mr.equals(mappingRule))
                        issues.add(new CollisionIssue(new ArrayList<MappingRule>(Arrays.asList(mappingRule, mr)), "identical rules"));
                    else
                        issues.add(new CollisionIssue(new ArrayList<MappingRule>(Arrays.asList(mappingRule, mr)), "one rule matches the other"));
                }
                return true;
            }

            logger.log(Level.INFO, "This rule: `" + mappingRule + "` collides with: " + Arrays.toString(mrEndingInThisNode.toArray()));
            boolean insert = UserInputManager.requestMappingKeep(mappingRule);
            if(insert)
                mappingRule.setForceInsertion(true); //ask the user only once for each rule
        
            for(MappingRule mr : mrEndingInThisNode){
                if(!UserInputManager.requestMappingKeep(mr))
                    node.removeMappingRuleFromTree(mr);
            }
            return insert;
        }
        return true;
    }

    private static boolean sameMethod(MappingRule mappingRule, List<MappingRule> mrEndingInThisNode){
        boolean same = false;
        for(MappingRule mr : mrEndingInThisNode){
            same = same || mr.getMethod().equals(mappingRule.getMethod());
        }
        return same;
    }
}