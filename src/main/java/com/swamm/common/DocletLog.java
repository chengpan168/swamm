package com.swamm.common;

/**
 * Created by chengpanwang on 2016/10/21.
 */
public class DocletLog {

    public static void log(Object obj) {
        System.out.println("INFO    " + obj);

    }

    public static void debug(Object obj) {
        if (DocletContext.LOG_LEVEL.isDebug()) {
            System.out.println("DEBUG   " + obj);
        }
    }

    public static void info(Object obj) {
        System.out.println("INFO    " + obj);
    }
}
