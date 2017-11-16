package io.swammdoc.common;

import io.swammdoc.doc.DocletContext;

/**
 * Created by chengpanwang on 2016/10/21.
 */
public class Logger {

    public static void info(Object obj) {
        if (DocletContext.LOG_LEVEL.isInfo()) {
            System.out.println("INFO    " + obj);
        }
    }

    public static void debug(Object obj) {
        if (DocletContext.LOG_LEVEL.isDebug()) {
            System.out.println("DEBUG   " + obj);
        }
    }

    public static void error(Object obj) {
        System.err.println("ERROR   " + obj);
    }
}
