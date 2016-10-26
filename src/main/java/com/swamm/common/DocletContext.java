package com.swamm.common;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.RootDoc;

/**
 * Created by chengpanwang on 2016/10/25.
 */
public class DocletContext {

    private static RootDoc  ROOT_DOC             = null;
    public  static ClassDoc COLLECTION_CLASS_DOC = null;
    private static boolean  init                 = false;

    public static void init(RootDoc rootDoc) {
        ROOT_DOC = rootDoc;
        COLLECTION_CLASS_DOC = rootDoc.classNamed("java.util.Collection");
        init = true;
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
}
