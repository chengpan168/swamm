package com.swamm.doc;

import java.util.List;

import com.sun.javadoc.FieldDoc;
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

    private List<FieldModel> innerField;

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

    public List<FieldModel> getInnerField() {
        return innerField;
    }

    public void setInnerField(List<FieldModel> innerField) {
        this.innerField = innerField;
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
}
