package com.swamm.common;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by chengpanwang on 2016/12/22.
 */
public enum LogLevel {
    DEBUG("debug"), INFO("info"), ERROR("error");

    private String code;

    LogLevel(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public static LogLevel fromCode(String level) {
        if (StringUtils.isBlank(level)) {
            return INFO;
        }
        for (LogLevel logLevel : values()) {
            if (StringUtils.equalsIgnoreCase(level, logLevel.getCode())) {
                return logLevel;
            }
        }

        return INFO;
    }

    public boolean isDebug() {
        if (this == DEBUG) {
            return true;
        }

        return false;
    }
}
