package com.configopt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CollisionIssue {
    List<MappingRule> rules = new ArrayList<>();
    String problemDescription;

    public CollisionIssue(List<MappingRule> rules, String problem){
        this.rules = rules;
        this.problemDescription = problem;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Issue found: " + problemDescription + " for rules : ");
        for(MappingRule mr : rules)
            sb.append(mr);
        return sb.toString();
    }
}