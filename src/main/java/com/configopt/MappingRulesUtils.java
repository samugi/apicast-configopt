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

    public static boolean validateInsertion(APIcast apicast, final PathNode node, final MappingRule mappingRule,
            final int index) {
        List<MappingRule> mrEndingInThisNode = node.getMappingRulesEndingHere();
        if (mrEndingInThisNode.size() > 0) {
            boolean insert = true;
            for (MappingRule mr : mrEndingInThisNode) {
                if (!mr.getMethod().equals(mappingRule.getMethod())) // ignore if different methods
                    return true;

                if (Utils.mode == Mode.SCAN) {
                    int severity = Utils.calculateSeverity(apicast, mr, mappingRule);
                    if (mr.equals(mappingRule))
                        issues.add(new CollisionIssue(new ArrayList<MappingRule>(Arrays.asList(mappingRule, mr)),
                                "identical rules", severity));
                    else
                        issues.add(new CollisionIssue(new ArrayList<MappingRule>(Arrays.asList(mappingRule, mr)),
                                "one rule matches the other", severity));
                } else if (Utils.mode == Mode.FIXINTERACTIVE) {
                    logger.log(Level.INFO, "This rule: [" + mappingRule + "] collides with: "
                            + Arrays.toString(mrEndingInThisNode.toArray()));
                    insert = UserInputManager.requestMappingKeep(mappingRule);
                    if (insert)
                        mappingRule.setForceInsertion(true); // ask the user only once for each rule
                    if (insert && !UserInputManager.requestMappingKeep(mr)) //avoid asking to remove the ones already inserted if the user already removed the current
                        node.removeMappingRuleFromTree(mr);
                }
            }
            return insert;
        }
        return true;
    }

}