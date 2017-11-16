package io.swammdoc.plat.rap;

/**
 * Created by chengpanwang on 2016/10/24.
 */
public class User {
    private Integer id;
    private String name;

    @Override
    public String toString() {
        return "User{" + "id=" + id + ", name='" + name + '\'' + '}';
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
}
