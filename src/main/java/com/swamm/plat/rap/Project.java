package com.swamm.plat.rap;

import java.util.List;

/**
 * Created by chengpanwang on 2016/10/24.
 */
public class Project {
    private Integer id;
    private String  version;
    private String  introduction;
    private String  name;
    private List<Module>    moduleList;

    @Override
    public String toString() {
        return "Project{" + "id=" + id + ", version='" + version + '\'' + ", introduction='" + introduction + '\'' + ", name='" + name + '\''
               + ", moduleList=" + moduleList + '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public List<Module> getModuleList() {
        return moduleList;
    }

    public void setModuleList(List<Module> moduleList) {
        this.moduleList = moduleList;
    }
}
