package io.swammdoc.plat.rap;

import java.util.List;

/**
 * Created by chengpanwang on 2016/10/24.
 */
public class Module {

    private Integer    id;
    private String     name;
    private String     introduction;
    private List<Page> pageList;

    @Override
    public String toString() {
        return "Module{" + "id=" + id + ", name='" + name + '\'' + ", introduction='" + introduction + '\'' + ", pageList=" + pageList + '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public List<Page> getPageList() {
        return pageList;
    }

    public void setPageList(List<Page> pageList) {
        this.pageList = pageList;
    }
}
