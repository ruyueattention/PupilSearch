package com.ruyue.util;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 实现树结构
 *
 * @author ijiangtao
 * @create 2019-04-18 15:13
 **/
public class TreeNode<T> implements Iterable<TreeNode<T>> {
    //判断当前节点是否被search过
    public Boolean isAccess = false;
    //判断当前节点是否需要继续追溯
    public Boolean exit = false;
    /**
     * 树节点
     */
    public T nodeData;

    /**
     * 父节点，根没有父节点
     */
    public TreeNode<T> parent;

    /**
     * 子节点，叶子节点没有子节点
     */
    public ConcurrentLinkedQueue<TreeNode<T>> children;

    /**
     * 保存了当前节点及其所有子节点，方便查询
     */
    public ConcurrentLinkedQueue<TreeNode<T>> elementsIndex;

    /**
     * 构造函数
     *
     * @param data
     */
    public TreeNode(T data) {
        this.nodeData = data;
        this.children = new ConcurrentLinkedQueue<TreeNode<T>>();
        this.elementsIndex = new ConcurrentLinkedQueue<TreeNode<T>>();
        this.elementsIndex.add(this);
    }

    /**
     * 判断是否为根：根没有父节点
     *
     * @return
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * 判断是否为叶子节点：子节点没有子节点
     *
     * @return
     */
    public boolean isLeaf() {
        return children.size() == 0;
    }

    /**
     * 添加一个子节点
     *
     * @param child
     * @return
     */
    public TreeNode<T> addChild(T child) {
        TreeNode<T> childNode = new TreeNode<T>(child);

        childNode.parent = this;

        this.children.add(childNode);

        this.registerChildForSearch(childNode);

        return childNode;
    }

    /**
     * 获取当前节点的层
     *
     * @return
     */
    public int getLevel() {
        if (this.isRoot()) {
            return 0;
        } else {
            return parent.getLevel() + 1;
        }
    }

    /**
     * 递归为当前节点以及当前节点的所有父节点增加新的节点
     *
     * @param node
     */
    private void registerChildForSearch(TreeNode<T> node) {
        elementsIndex.add(node);
        if (parent != null) {
            parent.registerChildForSearch(node);
        }
    }
    public TreeNode getRoot() {
        if (this.isRoot()) {
            return this;
        } else {
            return parent.getRoot();
        }
    }
    /**
     * 从当前节点及其所有子节点中搜索某节点
     *
     * @param cmp
     * @return
     */
    public TreeNode<T> findTreeNode(Comparable<T> cmp) {
        for (TreeNode<T> element : this.elementsIndex) {
            T elData = element.nodeData;
            if (cmp.compareTo(elData) == 0) {
                return element;
            }
        }

        return null;
    }

    /**
     * 获取当前节点的迭代器
     *
     * @return
     */
    @Override
    public Iterator<TreeNode<T>> iterator() {
        TreeNodeIterator<T> iterator = new TreeNodeIterator<T>(this);
        return iterator;
    }

    @Override
    public String toString() {
        return nodeData != null ? nodeData.toString() : "[tree data null]";
    }

}

