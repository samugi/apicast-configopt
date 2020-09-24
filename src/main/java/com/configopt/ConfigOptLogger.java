package com.configopt;

import java.util.logging.Logger;

public class ConfigOptLogger {
    private static Logger logger;
    
    public Logger getLogger(){
        return Logger.getLogger(ConfigOptLogger.class.getName());
    }
}