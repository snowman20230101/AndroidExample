package com.windy.breakpadexample.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

/**
 * 哈夫曼树
 *
 * @param <V>
 */
public class HuffmanTree<V> {
    Node root;

    /**
     * 创建 哈夫曼树
     *
     * @param data
     * @return
     */
    public Node<V> create(List<Node<V>> data) {
        while (data.size() > 1) {
            Collections.sort(data);

            // 从有序节点中，取出两个最小节点，创建父节点
            Node left = data.get(data.size() - 1);
            Node right = data.get(data.size() - 2);
            Node parent = new Node<>(null, left.weight + right.weight);

            // 链接节点
            parent.left = left;
            left.parent = parent;
            parent.right = right;
            right.parent = parent;

            // 删除子节点
            data.remove(left);
            data.remove(right);

            // 添加父节点
            data.add(parent);
        }

        root = data.get(0);

        return root;
    }

    /**
     * 显示树
     *
     * @param node
     * @return
     */
    public List<Node<V>> showHuffman(Node<V> node) {
        List<Node<V>> list = new ArrayList<>();
        Queue<Node<V>> queue = new LinkedList<>();
        queue.offer(node);

        while (!queue.isEmpty()) {
            Node<V> poll = queue.poll();
            if (poll.data != null) {
                System.out.println("data:" + poll.data + ", weight:" + poll.weight);
                list.add(poll);
            }

            if (poll.right != null) {
                queue.offer(poll.right);
            }

            if (poll.left != null) {
                queue.offer(poll.left);
            }
        }

        return list;
    }

    /**
     * 编码
     *
     * @param node
     */
    public void getCode(Node<V> node) {
        Node<V> root = node;
        Stack<String> stack = new Stack<>();
        while (root != null && root.parent != null) {
            // left 0,right 1
            if (root.parent.left == root) {
                stack.push("0");
            } else if (root.parent.right == root) {
                stack.push("1");
            }

            root = root.parent;
        }

        while (!stack.isEmpty()) {
            System.out.print(stack.pop());
        }

        System.out.println();
    }


    /**
     * 请设计一个算法，给一个字符串进行二进制编码，使得编码后字符串的长度最短。（哈夫曼树）
     * 输入描述:
     * 每组数据一行，为待编码的字符串。保证字符串长度小于等于1000。
     * 输出描述:
     * 一行输出最短的编码后长度。
     * 输入例子:
     * MT-TECH-TEAM
     * 输出例子:
     * 33
     *
     * @param s
     */

    /**
     * 节点
     *
     * @param <V>
     */
    public static class Node<V> implements Comparable<Node<V>> {
        Node left;
        Node right;
        int weight;
        V data;
        Node parent;

        public Node(V data, int weight) {
            this.data = data;
            this.weight = weight;
        }

        @Override
        public int compareTo(Node<V> o) {
            if (this.weight > o.weight) {
                return -1;
            } else if (this.weight < o.weight) {
                return 1;
            }
            return 0;
        }
    }
}
