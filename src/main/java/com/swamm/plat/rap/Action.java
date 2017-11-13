package com.swamm.plat.rap;

import java.util.List;

/**
 * Created by chengpanwang on 2016/10/24.
 */
public class Action {

    private Integer         id;
    private Integer         pageId;
    private String          name;
    private String          description;
    private Integer         requestType;
    private String          requestUrl;

    private String          responseTemplate;
    private List<Parameter> requestParameterList;
    private List<Parameter> responseParameterList;

    @Override
    public String toString() {
        return "Action{" + "id=" + id + ", pageId=" + pageId + ", description='" + description + '\'' + ", name='" + name + '\'' + ", requestType="
               + requestType + ", requestUrl='" + requestUrl + '\'' + ", responseTemplate='" + responseTemplate + '\'' + ", requestParameterList="
               + requestParameterList + ", responseParameterList=" + responseParameterList + '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPageId() {
        return pageId;
    }

    public void setPageId(Integer pageId) {
        this.pageId = pageId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRequestType() {
        return requestType;
    }

    public void setRequestType(Integer requestType) {
        this.requestType = requestType;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getResponseTemplate() {
        return responseTemplate;
    }

    public void setResponseTemplate(String responseTemplate) {
        this.responseTemplate = responseTemplate;
    }

    public List<Parameter> getRequestParameterList() {
        return requestParameterList;
    }

    public void setRequestParameterList(List<Parameter> requestParameterList) {
        this.requestParameterList = requestParameterList;
    }

    public List<Parameter> getResponseParameterList() {
        return responseParameterList;
    }

    public void setResponseParameterList(List<Parameter> responseParameterList) {
        this.responseParameterList = responseParameterList;
    }
}
