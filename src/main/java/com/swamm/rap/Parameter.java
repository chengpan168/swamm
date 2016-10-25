package com.swamm.rap;

import java.util.List;

/**
 * Created by chengpanwang on 2016/10/24.
 */
public class Parameter {

    private Integer         id;
    private String          identifier;
    private String          name;
    private String          remark;
    private String          validator;
    private String          dataType;
    private List<Parameter> parameterList;

    @Override
    public String toString() {
        return "Parameter{" + "id=" + id + ", identifier='" + identifier + '\'' + ", name='" + name + '\'' + ", remark='" + remark + '\''
               + ", validator='" + validator + '\'' + ", dataType='" + dataType + '\'' + ", parameterList=" + parameterList + '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getValidator() {
        return validator;
    }

    public void setValidator(String validator) {
        this.validator = validator;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public List<Parameter> getParameterList() {
        return parameterList;
    }

    public void setParameterList(List<Parameter> parameterList) {
        this.parameterList = parameterList;
    }
}
