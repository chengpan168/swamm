package com.swamm.handler;

import com.sun.javadoc.RootDoc;
import com.swamm.doc.ClassModel;

import java.util.List;
import java.util.Map;

/**
 * Created by chengpanwang on 2016/10/24.
 */
public interface Handler {

    void execute(RootDoc rootDoc, List<ClassModel> classModels);
}
