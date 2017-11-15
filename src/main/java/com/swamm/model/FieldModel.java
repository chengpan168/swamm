package com.swamm.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.alibaba.fastjson.annotation.JSONField;
import com.sun.javadoc.Type;

/**
 * Created by chengpanwang on 2016/10/21.
 */
public class FieldModel {

    private String           desc;
    private String           name;
    @JSONField(serialize = false, deserialize = false)
    private Type             type;

    private String           typeName;

    private List<FieldModel> innerFields;

    public FieldModel() {
    }

    public FieldModel(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
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

    public List<FieldModel> getInnerFields() {
        return innerFields;
    }

    public void setInnerFields(List<FieldModel> innerFields) {
        this.innerFields = innerFields;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getTypeName() {
        if (type != null) {
            this.typeName = type.qualifiedTypeName();
        }
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    /**
     * 有重复的直接返回
     * @param innerFieldModel
     */
    public void addInnerField(FieldModel innerFieldModel) {
        if (innerFieldModel == null) {
            return;
        }

        if (innerFields == null) {
            innerFields = new ArrayList<>();
        }

        for (FieldModel fieldModel : innerFields) {
            if (fieldModel.getName().equals(innerFieldModel.getName())) {
                return;
            }
        }

        innerFields.add(innerFieldModel);

    }

    public FieldModel getInnerField(String name, boolean create) {
        if (StringUtils.isBlank(name)) {
            return null;
        }

        if (innerFields == null) {
            innerFields = new ArrayList<>();
        }

        for (FieldModel fieldModel : innerFields) {
            if (fieldModel.getName().equals(name)) {
                return fieldModel;
            }
        }

        FieldModel innerField = null;
        if (create) {
            innerField = new FieldModel();
            innerField.setName(name);

            innerFields.add(innerField);

        }

        return innerField;

    }
}
