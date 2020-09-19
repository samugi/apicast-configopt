package com.configopt;

import java.util.ArrayList;
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
        for(int i = 0; i < rules.size(); i++){
            sb.append(rules.get(i));
            if(i < rules.size() -1 )
                sb.append(", ");
        }
        return sb.toString();
    }
}