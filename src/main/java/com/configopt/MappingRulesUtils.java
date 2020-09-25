package com.configopt;

import java.util.logging.Level;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.configopt.Utils.Mode;

public class MappingRulesUtils {

    protected static List<CollisionIssue> issues = new ArrayList<>();
    static Logger logger = Logger.getLogger(Utils.LOG_TAG);

    public static boolean validateInsertion(APIcast apicast, final PathNode node, final MappingRule mappingRule) {

        boolean insert = true;

        List<MappingRule> mrEndingInThisNode = node.getMappingRulesEndingHere();
        if (mrEndingInThisNode.size() > 0) {
            for (MappingRule mr : mrEndingInThisNode) {
                if (!mr.getMethod().equals(mappingRule.getMethod())) // ignore if different methods
                    return true;

                if (Utils.mode == Mode.SCAN) {
                    int severity = Utils.calculateSeverity(apicast, mr, mappingRule);
                    if (mr.matches(mappingRule))
                        issues.add(new CollisionIssue(new ArrayList<MappingRule>(Arrays.asList(mappingRule, mr)),
                                "identical rules", severity));
                    else if (checkOptimization(mr, mappingRule))
                        issues.add(new CollisionIssue(new ArrayList<MappingRule>(Arrays.asList(mappingRule, mr)),
                                "rules could be optimized", severity));
                    else
                        issues.add(new CollisionIssue(new ArrayList<MappingRule>(Arrays.asList(mappingRule, mr)),
                                "one rule matches the other", severity));
                } else if (Utils.mode == Mode.FIXINTERACTIVE) {
                    if (checkOptimization(mr, mappingRule)) {
                        System.out.println("This rule: [" + mr + "] can be optimized by removing the '$'' and then deleting: "
                                + Arrays.toString(mrEndingInThisNode.toArray()));
                        boolean optimize = UserInputManager.requestOptimization(mr, mappingRule);
                        if (optimize) {
                            if (mr.getPath().endsWith("$"))
                                mr.setPath(new Path(mr.getPath().substring(0, mr.getPath().length() - 1))); // remove $
                            insert = false; //stop inserting mappingRule (longer)
                        }
                        insert = true;
                    } else {
                        System.out.println("This rule: [" + mappingRule + "] collides with: "
                                + Arrays.toString(mrEndingInThisNode.toArray()));
                        insert = UserInputManager.requestMappingKeep(mappingRule);
                    }
                 //   if (insert && !checkOptimization(mr, mappingRule))
                //        mappingRule.setForceInsertion(true); // ask the user only once for each rule
                    if (insert  && !checkOptimization(mr, mappingRule) && !UserInputManager.requestMappingKeep(mr)) // avoid asking to remove the ones already
                                                                            // inserted if the user already removed the
                                                                            // current
                        node.removeMappingRuleFromTree(mr);
                }
            }
            return insert;
        }
        return true;
    }

    protected static boolean checkOptimization(MappingRule m1, MappingRule m2) {
        MappingRule shorter = getShorter(m1, m2);
        MappingRule longer = shorter.matches(m1) ? m2 : m1;
        return !m1.matches(m2) && (longer.getPath().startsWith(shorter.getPath()) && shorter.getPath().endsWith("$")
                || (shorter.getPath().length() == longer.getPath().length() && longer.getPath().endsWith("$")));
    }

    protected static MappingRule getShorter(MappingRule m1, MappingRule m2) {
        return m1.getPath().length() < m2.getPath().length() ? m1 : m2;
    }

	public static PathPiece getNextPiece(MappingRule mappingRule, int index) {
        String path = mappingRule.getPath().toString();
        String nextPiece = path.substring(index, index+1);
        if(path.charAt(index) == '{'){
            for(int i = index; i < path.length(); i++){
                if(path.charAt(i) == '/')
                    break;
                if(path.charAt(i) == '}')
                    nextPiece = path.substring(index, i+1);
            }
        }
        else if(index > 0 && path.charAt(index-1) == '/'){
            String p = "";
            for(int i = index; i < path.length() && path.charAt(i) != '/'; i++){
                p += path.charAt(i);
            }
            nextPiece = p;
        }
        if(nextPiece.endsWith("$"))
            nextPiece = nextPiece.substring(0, nextPiece.length()-1);
        return new PathPiece(nextPiece);
	}
}