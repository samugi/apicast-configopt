package com.configopt;

public class GlobalController{
    protected static Mode mode = Mode.STDOUTPUT;
    public enum Mode {STDOUTPUT, INTERACTIVE};
}