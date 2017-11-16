package io.swammdoc.doc;

import java.util.List;

/**
 * Created by chengpanwang on 2016/10/26.
 */
public class GenericType {

    private String declareType;
    private String type;

    private List<GenericType> innerType;

    public String getDeclareType() {
        return declareType;
    }

    public void setDeclareType(String declareType) {
        this.declareType = declareType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<GenericType> getInnerType() {
        return innerType;
    }

    public void setInnerType(List<GenericType> innerType) {
        this.innerType = innerType;
    }
}
