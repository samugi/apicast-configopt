package com.configopt;

public class Path implements Comparable{
    private String path;

    public Path(String path){
        this.path = path;
    }

    public int length(){
        return path.endsWith("$") ? path.length() -1 : path.length();
    }

    public boolean startsWith(String pattern){
        return this.path.startsWith(pattern);
    }

    public boolean startsWith(Path path){
        return this.path.startsWith(path.toString());
    }

    public boolean endsWith(String pattern){
        return this.path.endsWith(pattern);
    }

    public boolean endsWith(Path path){
        return this.path.endsWith(path.toString());
    }

    public String substring(int start, int end){
        return this.path.substring(start, end);
    }

    public char charAt(int index){
        return this.path.charAt(index);
    }

    @Override
    public String toString(){
        return this.path;
    }

    @Override
    public int compareTo(Object o) {
        return this.path.compareTo(((Path) o).toString());
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof Path))
            return false;
        return ((Path)o).toString().equals(this.toString());
    }

}