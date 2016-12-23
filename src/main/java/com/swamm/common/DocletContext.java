package com.swamm.common;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.RootDoc;

/**
 * Created by chengpanwang on 2016/10/25.
 */
public class DocletContext {

    private static RootDoc             ROOT_DOC             = null;
    public static ClassDoc             COLLECTION_CLASS_DOC = null;
    public static int                  FIELD_DEEP           = 4;
    private static boolean             init                 = false;
    public static LogLevel             LOG_LEVEL            = LogLevel.DEBUG;
    private static Map<String, String> optionMap            = new HashMap<>();

    public static void init(RootDoc rootDoc) {
        ROOT_DOC = rootDoc;
        COLLECTION_CLASS_DOC = rootDoc.classNamed("java.util.Collection");

        init = true;
    }

    public static void init(RootDoc root, String tagName) {
        init(root);

        if (StringUtils.isNotBlank(tagName)) {
            for (String pair : StringUtils.split(tagName, Tags.SEPARATOR)) {
                String[] kv = StringUtils.split(pair, Tags.KV_SEPARATOR);
                if (kv != null && kv.length == 2) {
                    optionMap.put(kv[0], kv[1]);
                }
            }
        }

        LOG_LEVEL = LogLevel.fromCode(optionMap.get("logLevel"));

    }

    public static RootDoc getRootDoc() {
        checkInit();
        return ROOT_DOC;
    }

    private static void checkInit() {
        if (!init) {
            throw new RuntimeException("没有初始化 DocletContext");
        }
    }

    public static String getOption(String name) {
        return optionMap.get(name);
    }

}
