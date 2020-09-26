package com.configopt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.configopt.Utils.Mode;

public class MappingRulesUtils {

    protected static List<CollisionIssue> issues = new ArrayList<>();
    static Logger logger = Logger.getLogger(Utils.LOG_TAG);

    protected static MappingRuleSM getShorter(MappingRuleSM m1, MappingRuleSM m2) {
        return m1.getPath().length() < m2.getPath().length() ? m1 : m2;
    }

    public static void validateMappingRule(APIcast apicast, MappingRuleSM mappingRule,
            List<MappingRuleSM> allRulesToVerify, int i) {
        for (int index = i; index < allRulesToVerify.size(); index++) {
            MappingRuleSM currentRule = allRulesToVerify.get(index);
            int severity = Utils.calculateSeverity(apicast, mappingRule, currentRule);
            if (Utils.mode == Mode.SCAN) {
                if (mappingRule.brutalMatch(currentRule))
                    issues.add(new CollisionIssue(new ArrayList<MappingRuleSM>(Arrays.asList(mappingRule, currentRule)),
                            "one rule matches the other", severity));
                else if (mappingRule.canBeOptimized(currentRule))
                    issues.add(new CollisionIssue(new ArrayList<MappingRuleSM>(Arrays.asList(mappingRule, currentRule)),
                            "rules could be optimized", severity));
            } else if (Utils.mode == Mode.FIXINTERACTIVE) {
                if (mappingRule.brutalMatch(currentRule)) {
                    boolean keep = UserInputManager.requestMappingKeep(mappingRule, currentRule, true);
                    if(!keep)
                        mappingRule.setMarkedForDeletion(true);
                    else{
                        boolean keep2 = UserInputManager.requestMappingKeep(currentRule, mappingRule, false);
                        if(!keep2)
                            currentRule.setMarkedForDeletion(true);
                    }
                } else if (mappingRule.canBeOptimized(currentRule)) {
                    boolean optimize = UserInputManager.requestOptimization(currentRule, mappingRule);
                    MappingRuleSM shorter = getShorter(currentRule, mappingRule);
                    MappingRuleSM longer = shorter.equals(currentRule) ? mappingRule : currentRule;
                    if (optimize)
                        if (shorter.isExactMatch())
                            shorter.setExactMatch(false);
                    longer.setMarkedForDeletion(true);
                }
            }

        }
    }
}