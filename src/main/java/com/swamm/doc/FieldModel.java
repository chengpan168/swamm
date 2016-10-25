package com.swamm.doc;

import java.util.List;

/**
 * Created by chengpanwang on 2016/10/21.
 */
public class FieldModel {

    private String           desc;
    private String           name;
    private String           type;
    private List<FieldModel> innerField;

    @Override
    public String toString() {
        return "FieldModel{" + "desc='" + desc + '\'' + ", name='" + name + '\'' + ", type='" + type + '\'' + ", innerField=" + innerField + '}';
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<FieldModel> getInnerField() {
        return innerField;
    }

    public void setInnerField(List<FieldModel> innerField) {
        this.innerField = innerField;
    }
}
