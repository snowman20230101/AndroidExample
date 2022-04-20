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

    @Test
    public void testInger() {
        Integer i = 9;
        System.out.println(Integer.toString(1, 2));
        System.out.println(Integer.toString(2, 2));
        System.out.println(Integer.toString(3, 2));
        System.out.println(Integer.toString(4, 2));
        System.out.println(Integer.toString(5, 2));
        System.out.println(Integer.toString(6, 2));
        System.out.println(Integer.toString(7, 2));
        System.out.println(Integer.toString(8, 2));
        System.out.println(Integer.toString(9, 2));

        System.out.println("sb=" + test(8));
    }


    public String test(int x) {
        StringBuilder sb = new StringBuilder();
        while (x > 0) {
            int m = x % 2;
            sb.append(m);
            x = x / 2;
        }

        return sb.toString();
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
