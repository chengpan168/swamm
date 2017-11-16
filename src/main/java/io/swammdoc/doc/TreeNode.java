package io.swammdoc.doc;

import io.swammdoc.model.FieldModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengpanwang on 2016/12/22.
 */
public class TreeNode {

    private FieldModel     fieldModel;
    private String         name;
    private TreeNode       parent;
    private List<TreeNode> children;


    public TreeNode(String name) {
        if (name == null || name.isEmpty()) {
            throw new RuntimeException("名称不能为空");
        }
        this.name = name;
    }

    public TreeNode addChildren(TreeNode node) {
        if (node == null) {
            throw new RuntimeException("tree node 不能为空");
        }
        if (children == null) {
            children = new ArrayList<>();
        }

        node.setParent(this);
        children.add(node);

        return node;
    }

    public TreeNode addChildren(String name) {
        TreeNode node = new TreeNode(name);
        return addChildren(node);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }

    public int deep() {
        if (this.parent == null) {
            return 0;
        }

        int deep = 1;
        TreeNode parent = this.getParent();
        while (parent != null) {
            deep++;
            parent = parent.getParent();
        }
        return deep;
    }

    public String path() {
        StringBuilder path = new StringBuilder(getName());
        TreeNode parent = getParent();

        while (parent != null) {
            path.insert(0, parent.getName() + "/");

            parent = parent.getParent();
        }

        return path.toString();
    }

    public FieldModel getCurrentFieldModel() {

        List<TreeNode> list = new ArrayList<>();
        list.add(this);
        TreeNode parent = getParent();
        while (parent != null) {
            list.add(parent);

            parent = parent.getParent();
        }

//        Logger.debug("===========" + list);

        FieldModel fieldModelTemp = null;
        for (int i = list.size() - 1; i >= 0; i--) {
            TreeNode  node = list.get(i);


//            Logger.debug("===========" + node.getName());
            if (i == list.size() - 1) {
                fieldModelTemp = node.getFieldModel();

                if (fieldModelTemp == null) {
                    return null;
                }
                continue;
            }

//            Logger.debug("===========" + fieldModelTemp.getName());


            if (fieldModelTemp.getInnerFields() == null || fieldModelTemp.getInnerFields().size() == 0) {
                return null;
            }
            for (FieldModel innerFieldModel : fieldModelTemp.getInnerFields()) {
                if (node.getName().equals(innerFieldModel.getName())) {
                    fieldModelTemp = innerFieldModel;
                }
            }

        }

        return fieldModelTemp;
    }

    public FieldModel getFieldModel() {
        return fieldModel;
    }

    public void setFieldModel(FieldModel fieldModel) {
        if (getParent() != null) {
            throw new RuntimeException("");
        }
        this.fieldModel = fieldModel;
    }

    public TreeNode getRoot() {
        TreeNode node = this;
        while (node != null) {
            node = node.getParent();
            if (node == null) {
                return this;
            }
        }

        return this;
    }

    @Override
    public String toString() {
        return "TreeNode{" + "fieldModel=" + fieldModel + ", name='" + name + '\'' + '}';
    }


}
