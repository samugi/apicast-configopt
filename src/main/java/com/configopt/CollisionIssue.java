package com.configopt;

import java.util.ArrayList;
import java.util.List;

public class CollisionIssue implements Comparable {
    List<MappingRuleSM> rules = new ArrayList<>();
    String problemDescription;
    int severity;

    public CollisionIssue(List<MappingRuleSM> rules, String problem, int severity) {
        this.rules = rules;
        this.problemDescription = problem;
        this.severity = severity;
    }

    private String getSeverityText() {
        String severe =   "[ SEVERE   ]";
        String minor =    "[ MINOR    ]";
        String optional = "[ OPTIONAL ]";
        switch (severity) {
            case 1:
                return severe;
            case 2:
                return minor;
            case 5:
                return optional;
            default:
                return minor;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Issue found " + getSeverityText() + " - " + problemDescription + " - for Mapping rules : \n");
        for (int i = 0; i < rules.size(); i++) {
            sb.append(rules.get(i));
            if (i < rules.size() - 1)
                sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public int compareTo(Object o) {
        return this.severity - ((CollisionIssue) o).severity;
    }
}