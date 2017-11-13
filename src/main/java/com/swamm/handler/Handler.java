package com.swamm.handler;

import com.sun.javadoc.RootDoc;
import com.swamm.model.ClassModel;

import java.util.List;

/**
 * Created by chengpanwang on 2016/10/24.
 */
public interface Handler {

    void execute(RootDoc rootDoc, List<ClassModel> classModels);
}
