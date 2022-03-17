package com.windy.breakpadexample.tree;

public class BinaryTree {
    TreeNode root;

    public TreeNode put(int value) {
        if (root == null) {
            TreeNode node = new TreeNode(value);
            root = node;
            return node;
        }
        TreeNode parent = null;
        TreeNode node = root;
        // 找到要放入的位置
        while (node != null) {
            parent = node;
            if (value < node.value) {
                node = node.left;
            } else if (value > node.value) {
                node = node.right;
            } else {//是重复值 就不理会了
                return node;
            }
        }
        // 生成一个节点放入
        TreeNode newNode = new TreeNode(value);
        if (value < parent.value) {
            parent.left = newNode;
        } else {
            parent.right = newNode;
        }
        newNode.parent = parent;

        return newNode;
    }

    /**
     * 树节点
     */
    public static class TreeNode {
        int value;
        TreeNode parent;
        TreeNode left;
        TreeNode right;

        public TreeNode(int value) {
            this.value = value;
            this.left = null;
            this.right = null;
            this.parent = null;
        }
    }

    /**
     * LeetCode 144 二叉树的前序遍历
     *
     * LeetCode 589 N叉树的前序遍历
     *
     * LeetCode 226 翻转二叉树
     *
     * LeetCode 剑指Offer32. 从上到下打印二叉树Ⅱ
     *
     * LeetCode 107 二叉树的层序遍历Ⅱ
     *
     * LeetCode 103 二叉树的锯齿形层序遍历
     */


    // 二叉树进阶刷题部分
    /**
     * LeetCode 110 平衡二叉树
     *
     * LeetCode 112 路径总和
     *
     * LeetCode 105 从前序与中序遍历序列构造二叉树
     *
     * LeetCode 222 完全二叉树节点个数
     *
     * LeetCode 剑指Offer 54 二叉搜索树的第k大节点
     *
     * LeetCode 剑指Offer 26 树的子结构
     *
     *  LeetCode 622 二叉树的最大宽度
     */

}
