package com.configopt;

import java.util.logging.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.configopt.GlobalController.Mode;

public class MappingRulesController {

    protected static List<CollisionIssue> issues = new ArrayList<>();
    static Logger logger = Logger.getLogger(MappingRulesController.class.getName());

    public static boolean validateInsertion(final PathNode node, final MappingRule mappingRule, final int index){
        List<MappingRule> mrEndingInThisNode = node.getMappingRulesEndingHere();
        if(mrEndingInThisNode.size() > 0){
            if(GlobalController.mode == Mode.STDOUTPUT){
                for(MappingRule mr : mrEndingInThisNode){
                    if(mr.equals(mappingRule))
                        issues.add(new CollisionIssue(new ArrayList<MappingRule>(Arrays.asList(mappingRule, mr)), "identical rules"));
                    else
                        issues.add(new CollisionIssue(new ArrayList<MappingRule>(Arrays.asList(mappingRule, mr)), "one rule matches the other"));
                }
                return true;
            }

            logger.log(Level.INFO, "This rule: `" + mappingRule + "` collides with: " + Arrays.toString(mrEndingInThisNode.toArray()));
            boolean insert = UserInputController.requestMappingKeep(mappingRule);
            if(insert)
                mappingRule.setForceInsertion(true); //ask the user only once for each rule
        
            for(MappingRule mr : mrEndingInThisNode){
                if(!UserInputController.requestMappingKeep(mr))
                    node.removeMappingRuleFromTree(mr);
            }
            return insert;
        }
        return true;
    }
}