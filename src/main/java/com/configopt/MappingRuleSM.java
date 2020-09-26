package com.configopt;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class MappingRuleSM {
    private String path;
    private Map<String, String> queryPairs;
    private String method;
    private String serviceHost;
    private Long serviceId;
    private boolean markedForDeletion = false;
    private String owner;
    private boolean isExactMatch = false;

    public MappingRuleSM(String method, String rule, Long serviceId, String owner)  {
        this.method = method; this.serviceId = serviceId; this.owner = owner;
        this.queryPairs = new LinkedHashMap<String, String>();
        String query = getQuery(rule);
        path = query != null ? rule.replace("?"+query, "") : rule;
        if(path.endsWith("$")){
            path = path.substring(0, path.length()-1);
            this.isExactMatch = true;
        }
        if(query == null)
            return;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            try {
                queryPairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
                        URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    private String getQuery(String path){
        int lastQuery = path.lastIndexOf("?");
        int lastSlash = path.lastIndexOf("/");
        if(lastQuery > lastSlash){
            return path.substring(lastQuery+1);
        }
        return null;
    }

    public boolean canBeOptimized(MappingRuleSM mr){
        return this.matches(mr) && this.optimizationMatch(mr);
    }

    public int getPathSectionsLength(){
        String[] mr1 = this.getPath().split("/");
        if(mr1.length == 0)
            return 0;
        String lastSection = mr1[mr1.length-1];
        return lastSection.length() + mr1.length - 1;
    }

    public boolean brutalMatch(MappingRuleSM mr){
        return this.matches(mr) && !optimizationMatch(mr);
    }

    private boolean optimizationMatch(MappingRuleSM mr){
        boolean shorterExactMatch = MappingRulesUtils.getShorter(this, mr).isExactMatch();
        boolean samePathSectionsLengths = this.getPathSectionsLength() == mr.getPathSectionsLength();
        return !samePathSectionsLengths && shorterExactMatch;
    }

    private boolean matches(MappingRuleSM mr){
        boolean matchingMethods = this.getMethod().equalsIgnoreCase(mr.getMethod());
        boolean matchingQP = this.matchQP(mr);
        boolean matchPath = this.matchPath(mr);
        return matchingMethods && matchingQP && matchPath;
    }

    private boolean matchQP(MappingRuleSM mr){
        if(this.getQP().isEmpty() && mr.getQP().isEmpty())
            return true;
        if(this.getQP().isEmpty() && !mr.getQP().isEmpty() || mr.getQP().isEmpty() && !this.getQP().isEmpty())
            return false;
        return new ArrayList<>( this.getQP().values() ).equals(new ArrayList<>( mr.getQP().values() )) && this.getQP().keySet().equals( mr.getQP().keySet());
    }

    private boolean matchPath(MappingRuleSM mr){
        String path1 = this.getPath();
        String path2 = mr.getPath();
        if(path1.startsWith(path2) || path2.startsWith(path1))
            return true;
        if(this.matchWithParams(mr))
            return true;
        
        return false;
    }

    private boolean matchLastPartial(String last1, String last2){
        return last1.startsWith(last2) || last2.startsWith(last1);
    }

    private boolean matchWithParams(MappingRuleSM mr){
        String[] mr1 = this.getPath().split("/");
        String[] mr2 = mr.getPath().split("/");
        if(mr1.length != mr2.length)
            return false;
        for(int i = 0; i < mr1.length-1; i++){
            if(!mr1[i].equals(mr2[i]) && (!isParam(mr1[i]) && !isParam(mr2[i])))
                return false;
        }
        String last1 = mr1[mr1.length-1];
        String last2 = mr2[mr1.length-1];
        return matchLastPartial(last1, last2);
    }

    private static boolean isParam(String p){
        return p.startsWith("{") && p.endsWith("}");
    }

    public String getMethod(){
        return this.method;
    }

    public Long getServiceId(){
        return this.serviceId;
    }

    public boolean getMarkedForDeletion(){
        return this.markedForDeletion;
    }

    public void setMarkedForDeletion(boolean m){
        this.markedForDeletion = m;
    }

    public String getPath(){
        return this.path;
    }

    public String getRealPath(){
        return this.isExactMatch() ? this.path+"$" : this.path;
    }

    public String getOwner(){
        return this.owner;
    }

    public String getHost(){
        return this.serviceHost;
    }

    public void setHost(String serviceHost){
        this.serviceHost = serviceHost;
    }

    public boolean isExactMatch(){
        return this.isExactMatch;
    }

    public void setExactMatch(boolean b){
        this.isExactMatch = b;
    }

    public Map<String, String> getQP(){
        return this.queryPairs;
    }

    @Override
    public String toString(){
        return this.method + " " + this.getRealPath() + " - Service ID: " + this.serviceId + " Host: " + serviceHost;
    }
}