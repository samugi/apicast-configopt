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

    public static boolean validateInsertion(APIcast apicast, final PathNode node, final MappingRule mappingRule) {
        boolean insert = true;
        List<MappingRule> mrEndingInThisNode = node.getMappingRulesEndingHere();

        if (mrEndingInThisNode.size() > 0) {
            for (MappingRule mr : mrEndingInThisNode) {
                if (!mr.getMethod().equals(mappingRule.getMethod()))
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
                        boolean optimize = UserInputManager.requestOptimization(mr, mappingRule);
                        if (optimize) {
                            if (mr.getPath().endsWith("$"))
                                mr.setPath(new Path(mr.getPath().substring(0, mr.getPath().length() - 1))); // remove $
                            insert = false; // stop inserting mappingRule (longer)
                        }
                        insert = true;
                    } else {
                        insert = UserInputManager.requestMappingKeep(mappingRule, mrEndingInThisNode, true); //collision
                    }
                    if (insert && !checkOptimization(mr, mappingRule) && !UserInputManager.requestMappingKeep(mr, mrEndingInThisNode, false)) //remove existing?
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
        return !m1.matches(m2) && (longer.getPath().startsWith(shorter.getPath()) && (shorter.getPath().endsWith("$")
                || (shorter.getPath().length() == longer.getPath().length() && longer.getPath().endsWith("$"))));
    }

    protected static MappingRule getShorter(MappingRule m1, MappingRule m2) {
        return m1.getPath().length() < m2.getPath().length() ? m1 : m2;
    }

    public static PathPiece getNextPiece(MappingRule mappingRule, int index) {
        String path = mappingRule.getPath().toString();
        if (index == path.length())
            return new PathPiece("");
        String nextPiece = path.substring(index, index + 1);
        if (path.charAt(index) == '{') {
            int nextDelimiter = MappingRulesUtils.nextCurlySlash(path, index);
            if (nextDelimiter >= 0)
                nextPiece = path.substring(index, nextDelimiter);
        } else if (index > 0 && path.charAt(index - 1) == '/') {
            int nextSlashQP$ = MappingRulesUtils.nextSlashQP$(path, index);
            if (nextSlashQP$ >= 0)
                nextPiece = path.substring(index, nextSlashQP$);
        }
        if (nextPiece.endsWith("$"))
            nextPiece = nextPiece.substring(0, nextPiece.length() - 1);
        return new PathPiece(nextPiece);
    }

    public static int nextCurlySlash(String path, int startIndex) {
        int nextCurly = path.indexOf("}", startIndex);
        int nextSlash = path.indexOf("/", startIndex);
        List<Integer> ni = new ArrayList<>();
        if (nextCurly > 0)
            ni.add(nextCurly);
        if (nextSlash > 0)
            ni.add(nextSlash);
        if (ni.isEmpty())
            return -1;
        Collections.sort(ni);
        if (ni.get(0) == nextCurly)
            return ni.get(0) + 1;
        return ni.get(0);
    }

    public static int nextSlashQP$(String path, int startIndex) {
        int nextQP = path.indexOf("?", startIndex);
        int nextSlash = path.indexOf("/", startIndex);
        int nextDollar = path.indexOf("$", startIndex);
        List<Integer> ni = new ArrayList<>();
        if (nextQP > 0)
            ni.add(nextQP);
        if (nextSlash > 0)
            ni.add(nextSlash);
        if (nextDollar > 0)
            ni.add(nextDollar);
        if (ni.isEmpty())
            return -1;
        Collections.sort(ni);
        return ni.get(0);
    }
}