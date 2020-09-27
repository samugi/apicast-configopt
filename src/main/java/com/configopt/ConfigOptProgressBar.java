package com.configopt;

public class ConfigOptProgressBar{
    private int total;
    private int current;
    private final static int TOTAL_CHARS = 60;

    public ConfigOptProgressBar(int total){
        this.total = total;
    }

    public void postProgress(int current){
        this.current = Math.min(current, total);
        this.printProgress();
    }

    private void printProgress(){
        StringBuilder sb = new StringBuilder();
        sb.append("|");
        for(int i = 0; i < getProgressChars(); i++)
            sb.append("=");
        for(int i = 0; i < TOTAL_CHARS - getProgressChars(); i++ )
            sb.append(" ");
        sb.append("|\r");
        System.out.print(sb.toString());
    }

    private int getProgressChars(){
        return (int) Math.min(TOTAL_CHARS, Math.ceil((TOTAL_CHARS * current) / total));
    }
}