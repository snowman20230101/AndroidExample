package com.windy.breakpadexample;

import org.junit.Test;

/**
 * 链表，数组测试
 */
public class LinkedNodeUnitTest {
    private static class Node {
        public Node() {
        }

        public Node(int value) {
            this.value = value;
        }

        int value;
        Node next;
    }

    /**
     * 测试合并有序链表
     */
    @Test
    public void testMergeTwoNode() {
        Node node = new Node(1);
        Node node3 = new Node(3);
        Node node5 = new Node(5);
        node.next = node3;
        node3.next = node5;

        Node node2 = new Node(2);
        Node node4 = new Node(4);
        Node node6 = new Node(6);
        node2.next = node4;
        node4.next = node6;

        System.out.println("合并链表：");
        Node root = mergeNode(node2, node);
        printNode(root);
    }

    private void printNode(Node root) {
        while (root != null) {
            System.out.print(root.value + " ");
            root = root.next;
        }

        System.out.println();
    }

    /**
     * 测试反转链表
     */
    @Test
    public void testReverseNode() {
        Node node = new Node(1);
        Node node3 = new Node(3);
        Node node5 = new Node(5);
        node.next = node3;
        node3.next = node5;

        Node root = reverseNode(node);
        System.out.println("反转链表：");
        printNode(root);
    }

    /**
     * 合并两个有序链表 LeeCode 21题
     * 递归
     *
     * @param first
     * @param second
     * @return
     */
    public Node mergeNode(Node first, Node second) {
        if (first == null)
            return second;
        else if (second == null)
            return first;
        else if (first.value < second.value) {
            first.next = mergeNode(first.next, second);
            return first;
        } else {
            second.next = mergeNode(first, second.next);
            return second;
        }
    }

    /**
     * 合并两个有序链表
     * 遍历法
     *
     * @param first
     * @param second
     * @return
     */
    public Node mergeNode2(Node first, Node second) {
        if (first == null)
            return second;
        else if (second == null)
            return first;

        // 创建新的节点，来做返回值
        Node head = new Node(-1);
        Node prev = head;
        while (first != null && second != null) {
            if (first.value < second.value) {
                prev.next = first;
                first = first.next;
            } else {
                prev.next = second;
                second = second.next;
            }

            prev = prev.next;
        }

        // 合并后 first 和 second 最多只有一个还未被合并完，我们直接将链表末尾指向未合并完的链表即可
        prev.next = first == null ? second : first;

        return head.next;
    }

    /**
     * LeeCode 206. 反转链表
     *
     * @param root
     * @return
     */
    public Node reverseNode(Node root) {
        // 1->2->3
        Node curr = root; // 当前节点
        Node prev = null; // 当前的上一节点
        while (curr != null) {
            Node next = curr.next; // 临时存储当前的下一个节点
            curr.next = prev; // 当前的节点的下一个从新链接 上一个节点，也就是具体的反转
            prev = curr; // 当前的编程头节点
            curr = next; // 重新替换当前节点
        }

        return prev;
    }

    /**
     * 递归法不懂
     *
     * @param root
     * @return
     */
    public Node reserveNode2(Node root) {
        if (root == null || root.next == null) {
            return root;
        }

        Node newNode = reserveNode2(root.next);
        newNode.next.next = root;
        root.next = null;
        return newNode;
    }
}
