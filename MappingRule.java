public class MappingRule{
    private String method;
    private String path;
    private String serviceHost;
    private boolean markedForDeletion = false;

    public MappingRule(String method, String path){
        this.method = method;
        this.path = path;
    }

    public void setHost(String host){
        this.serviceHost = host;
    }

    public String getHost(){
        return this.serviceHost;
    }

    public String getPath(){
        return this.path;
    }

    public void markForDeletion(){
        this.markedForDeletion = true;
    }

    public boolean isMarkedForDeletion(){
        return this.markedForDeletion;
    }

    public String getMethod(){
        return this.method;
    }

    @Override
    public String toString(){
        return this.method + " " + this.path + " host: " + serviceHost;
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof MappingRule))
            return false;
        return ((MappingRule)o).getMethod().equals(this.getMethod()) && ((MappingRule)o).getPath().equals(this.getPath());
    }
}