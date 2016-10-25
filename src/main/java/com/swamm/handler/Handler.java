package com.swamm.handler;

import com.swamm.doc.ClassModel;

import java.util.List;
import java.util.Map;

/**
 * Created by chengpanwang on 2016/10/24.
 */
public interface Handler {

    void execute(List<ClassModel> classModels, Map<String, String> options);
}
