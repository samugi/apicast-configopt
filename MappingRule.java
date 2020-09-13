public class MappingRule{
    private String method;
    private String path;

    public MappingRule(String method, String path){
        this.method = method;
        this.path = path;
    }

    public String getPath(){
        return this.path;
    }
}