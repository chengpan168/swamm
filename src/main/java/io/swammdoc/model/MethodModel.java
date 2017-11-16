package io.swammdoc.model;

import java.util.List;

/**
 * Created by chengpanwang on 2016/10/20.
 */
public class MethodModel {

    private String           title;
    private String           desc;
    private String           name;
    private String           url;
    private List<FieldModel> paramModels;
    private FieldModel       returnModel;

    @Override
    public String toString() {
        return "MethodModel{" + "title='" + title + '\'' + ", desc='" + desc + '\'' + ", name='" + name + '\'' + ", paramModels=" + paramModels
               + ", returnModel=" + returnModel + '}';
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public List<FieldModel> getParamModels() {
        return paramModels;
    }

    public void setParamModels(List<FieldModel> paramModels) {
        this.paramModels = paramModels;
    }

    public FieldModel getReturnModel() {
        return returnModel;
    }

    public void setReturnModel(FieldModel returnModel) {
        this.returnModel = returnModel;
    }
}
