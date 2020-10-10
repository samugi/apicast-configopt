package com.configopt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APIcast {
    private boolean pathRoutingEnabled = false;
    private boolean pathRoutingOnlyEnabled = false;
    static APIcast apicast = null;
    List<Service> services = new ArrayList<>();
    private List<CollisionIssue> issues = new ArrayList<>();


    public static APIcast getAPIcast(){
        if(apicast == null)
            apicast = new APIcast();
        return apicast;
    }

    public void addService(Service service) {
        this.services.add(service);
    }

    public List<Service> getServices() {
        return this.services;
    }

    @Deprecated
    public void setPathRoutingEnabled(boolean pathRoutingEnabled) {
        this.pathRoutingEnabled = pathRoutingEnabled;
    }

    @Deprecated
    public boolean getPathRoutingEnabled() {
        return this.pathRoutingEnabled;
    }

    public void setPathRoutingOnlyEnabled(boolean pathRoutingOnlyEnabled) {
        this.pathRoutingOnlyEnabled = pathRoutingOnlyEnabled;
    }

    public boolean getPathRoutingOnlyEnabled() {
        return this.pathRoutingOnlyEnabled;
    }

    /**
     * creates service groups based on how their rules need to be validated for
     * example normally they would be all distinct with path routing they would be
     * grouped by same host with path routing only they would be grouped all
     * together
     * 
     * @return the list of groups
     */
    private List<List<Service>> createServiceGroups() {
        /** helper map to group by host for path based routing */
        Map<String, List<Service>> serviceGroupsMap = new HashMap<>();
        /** the variable we will return */
        List<List<Service>> serviceGroups = new ArrayList<>();

        if (!pathRoutingOnlyEnabled) {
            for (Service service : services) {
                List<Service> value = serviceGroupsMap.get(service.getHost());
                if (value != null)
                    value.add(service);
                else {
                    // This is the first service with this host, create a new group
                    List<Service> tmpServices = new ArrayList<>();
                    tmpServices.add(service);
                    serviceGroupsMap.put(service.getHost(), tmpServices);
                }
            }
            for (List<Service> serviceGroup : serviceGroupsMap.values()) {
                serviceGroups.add(serviceGroup);
            }
            return serviceGroups;
        } else {
            List<Service> tmpServices = new ArrayList<>();
            for (Service service : services) {
                tmpServices.add(service);
            }
            serviceGroups.add(tmpServices);
            return serviceGroups;
        }
    }

    public void validateAllServices() {
        List<List<Service>> serviceGroups = this.createServiceGroups();
        for (List<Service> serviceGroup : serviceGroups) {
            List<MappingRuleSM> allRulesToVerify = new ArrayList<>();
            for (Service service : serviceGroup){
                allRulesToVerify.addAll(service.getProductMappingRules());
            }
            ConfigOptProgressBar pb = new ConfigOptProgressBar(allRulesToVerify.size());
            for (int i = 0; i < allRulesToVerify.size() -1; i++){
                MappingRulesUtils.validateMappingRule(this, allRulesToVerify.get(i), allRulesToVerify, i+1);
                pb.postProgress(i);
            }
        }
        System.out.println("");
        switch (Utils.mode) {
            case FIXINTERACTIVE:
                //generate output config here 
                break;
            case SCAN:
                OutputUtils.printIssues(issues);
                break;
        }
    }

	public void addIssue(CollisionIssue collisionIssue) {
        this.issues.add(collisionIssue);
	}
}