package com.rivannikov.bamboo.appcenter.util;

import com.atlassian.bamboo.build.logger.BuildLogger;

public class LogUtils {
    private boolean mDebug = false;

    public BuildLogger getLogger() {
        return logger;
    }

    private final BuildLogger logger;

    public LogUtils(BuildLogger logger) {
        this.logger = logger;
    }

    public boolean isDebug() {
        return mDebug;
    }

    public void setDebug(boolean debug) {
        mDebug = debug;
    }

    public final void info(String text) {
        logger.addBuildLogEntry(text);
    }

    public final void error(String text) {
        logger.addBuildLogEntry(text);
    }

    public final void debug(String text) {
        if (isDebug()) logger.addBuildLogEntry(text);
    }
}
