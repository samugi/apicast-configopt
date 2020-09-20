package com.configopt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APIcast {
    private boolean pathRoutingEnabled = false;
    private boolean pathRoutingOnlyEnabled = false;
    List<Service> services = new ArrayList<>();

    public void addService(Service service) {
        this.services.add(service);
    }

    public List<Service> getServices() {
        return this.services;
    }

    public void setPathRoutingEnabled(boolean pathRoutingEnabled) {
        this.pathRoutingEnabled = pathRoutingEnabled;
    }

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

        if (pathRoutingEnabled && !pathRoutingOnlyEnabled) {
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
        } else if (pathRoutingOnlyEnabled) {
            List<Service> tmpServices = new ArrayList<>();
            for (Service service : services) {
                tmpServices.add(service);
            }
            serviceGroups.add(tmpServices);
            return serviceGroups;
        }
        for (Service service : services) {
            List<Service> singleElementList = new ArrayList<>();
            singleElementList.add(service);
            serviceGroups.add(singleElementList);
        }
        return serviceGroups;
    }

    public void validateAllServices() {
        List<List<Service>> serviceGroups = this.createServiceGroups();
        for (List<Service> serviceGroup : serviceGroups) {
            PathTree tree = new PathTree();
            for (Service service : serviceGroup)
                for (MappingRule mappingRule : service.getProductMappingRules())
                    tree.insertMappingRule(this, mappingRule);
        }

        switch (Utils.mode) {
            case FIXINTERACTIVE:
                //generate output config here 
                break;
            case SCAN:
                OutputUtils.printIssues(MappingRulesUtils.issues);
                break;
        }
    }
}