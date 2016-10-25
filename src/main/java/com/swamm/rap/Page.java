package com.swamm.rap;

import java.util.List;

/**
 * Created by chengpanwang on 2016/10/24.
 */
public class Page {

    private Integer      id;
    private Integer      moduleId;
    private String       introduction;
    private String       name;
    private List<Action> actionList;

    @Override
    public String toString() {
        return "Page{" + "id=" + id + ", introduction='" + introduction + '\'' + ", name='" + name + '\'' + ", actionList=" + actionList + '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getModuleId() {
        return moduleId;
    }

    public void setModuleId(Integer moduleId) {
        this.moduleId = moduleId;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Action> getActionList() {
        return actionList;
    }

    public void setActionList(List<Action> actionList) {
        this.actionList = actionList;
    }
}
