package com.windy.breakpadexample.tree;

import org.junit.Test;

import java.util.List;

public class TreeNodeUnitTest {

    @Test
    public void testTreeNodeTraversal() {
        int[] arr = {2, 4, 6, 7, 8, 1, 9, 5, 3};
        BinaryTree binaryTree = new BinaryTree();
        for (int i = 0; i < arr.length; i++) {
            binaryTree.put(arr[i]);
        }

        preOlderTraversal(binaryTree.root);
        System.out.println();
    }

    /**
     * 二叉树前序遍历 -> 跟节点，左子树，右子树
     */
    public void preOlderTraversal(BinaryTree.TreeNode root) {
        if (root == null)
            return;

        preOlderTraversal(root.left);
        System.out.print(root.value + " ");
        preOlderTraversal(root.right);
    }
}
