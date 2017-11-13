package com.swamm.common;

import com.swamm.doc.DocletContext;

/**
 * Created by chengpanwang on 2016/10/21.
 */
public class Logger {

    public static void info(Object obj) {
        System.out.println("INFO    " + obj);

    }

    public static void debug(Object obj) {
        if (DocletContext.LOG_LEVEL.isDebug()) {
            System.out.println("DEBUG   " + obj);
        }
    }
}
