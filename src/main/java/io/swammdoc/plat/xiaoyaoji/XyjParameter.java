package io.swammdoc.plat.xiaoyaoji;

import java.util.List;

/**
 * Created by panwang.chengpw on 2017/10/25.
 */
public class XyjParameter {

    private String             require = "false";
    private List<XyjParameter> children;
    private String             type;
    private String             name;
    private String             defaultValue;
    private String             description;

    public String getRequire() {
        return require;
    }

    public void setRequire(String require) {
        this.require = require;
    }

    public List<XyjParameter> getChildren() {
        return children;
    }

    public void setChildren(List<XyjParameter> children) {
        this.children = children;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
