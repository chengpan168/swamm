package com.swamm.doc;

import java.util.List;

/**
 * Created by chengpanwang on 2016/10/20.
 */
public class ClassModel {

    private String type;
    private String name;
    private String desc;

    private List<MethodModel> methodModels;

    public List<MethodModel> getMethodModels() {
        return methodModels;
    }

    public void setMethodModels(List<MethodModel> methodModels) {
        this.methodModels = methodModels;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "ClassModel{" + "type='" + type + '\'' + ", name='" + name + '\'' + ", desc='" + desc + '\''
               + ", methodModels=" + methodModels + '}';
    }
}
