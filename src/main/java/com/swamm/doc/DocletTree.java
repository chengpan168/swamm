package com.swamm.doc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengpanwang on 2016/10/26.
 */
public class DocletTree {

    private Node root;
    public DocletTree() {
        root = new Node("root");
    }

    public Node add(String name) {
        Node node = new Node(name);
        root.addChildren(node);
        return node;
    }

    public class Node {

        public Node(String name) {
            if (name == null || name.isEmpty()) {
                throw new RuntimeException("名称不能为空");
            }
            this.name = name;
        }

        private String     name;
        private Node       parent;
        private List<Node> children;

        public Node addChildren(Node node) {
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
        public Node addChildren(String name) {
            Node node = new Node(name);
            return addChildren(node);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Node getParent() {
            return parent;
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }

        public List<Node> getChildren() {
            return children;
        }

        public void setChildren(List<Node> children) {
            this.children = children;
        }

        public int deep() {
            if (this.parent == null) {
                return 0;
            }

            int deep = 1;
            Node parent = this.getParent();
            while (parent != null) {
                deep ++;
                parent = parent.getParent();
            }
            return deep;
        }
    }
}
