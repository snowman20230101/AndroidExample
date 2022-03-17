package com.windy.breakpadexample.tree;

import org.junit.Test;

import static com.windy.breakpadexample.tree.HuffmanTree.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class HuffmanTreeUnitTest {

    public static final String jsonStr = "{\"success\":\"1\",\"result\":[{\"weaid\":\"1\",\"days\":\"2021-11-30\",\"week\":\"星期二\",\"cityno\":\"beijing\",\"citynm\":\"北京\",\"cityid\":\"101010100\",\"temperature\":\"6℃/-5℃\",\"humidity\":\"0%/0%\",\"weather\":\"晴\",\"weather_icon\":\"http://api.k780.com/upload/weather/d/0.gif\",\"weather_icon1\":\"http://api.k780.com/upload/weather/n/0.gif\",\"wind\":\"西北风转西风\",\"winp\":\"4-5级转4-5级\",\"temp_high\":\"6\",\"temp_low\":\"-5\",\"humi_high\":\"0\",\"humi_low\":\"0\",\"weatid\":\"1\",\"weatid1\":\"1\",\"windid\":\"7\",\"winpid\":\"2\",\"weather_iconid\":\"0\",\"weather_iconid1\":\"0\"},{\"weaid\":\"1\",\"days\":\"2021-12-01\",\"week\":\"星期三\",\"cityno\":\"beijing\",\"citynm\":\"北京\",\"cityid\":\"101010100\",\"temperature\":\"7℃/-4℃\",\"humidity\":\"0%/0%\",\"weather\":\"晴\",\"weather_icon\":\"http://api.k780.com/upload/weather/d/0.gif\",\"weather_icon1\":\"http://api.k780.com/upload/weather/n/0.gif\",\"wind\":\"西南风\",\"winp\":\"小于3级\",\"temp_high\":\"7\",\"temp_low\":\"-4\",\"humi_high\":\"0\",\"humi_low\":\"0\",\"weatid\":\"1\",\"weatid1\":\"1\",\"windid\":\"5\",\"winpid\":\"0\",\"weather_iconid\":\"0\",\"weather_iconid1\":\"0\"},{\"weaid\":\"1\",\"days\":\"2021-12-02\",\"week\":\"星期四\",\"cityno\":\"beijing\",\"citynm\":\"北京\",\"cityid\":\"101010100\",\"temperature\":\"10℃/-2℃\",\"humidity\":\"0%/0%\",\"weather\":\"多云\",\"weather_icon\":\"http://api.k780.com/upload/weather/d/1.gif\",\"weather_icon1\":\"http://api.k780.com/upload/weather/n/1.gif\",\"wind\":\"西南风转北风\",\"winp\":\"小于3级\",\"temp_high\":\"10\",\"temp_low\":\"-2\",\"humi_high\":\"0\",\"humi_low\":\"0\",\"weatid\":\"2\",\"weatid1\":\"2\",\"windid\":\"5\",\"winpid\":\"0\",\"weather_iconid\":\"1\",\"weather_iconid1\":\"1\"},{\"weaid\":\"1\",\"days\":\"2021-12-03\",\"week\":\"星期五\",\"cityno\":\"beijing\",\"citynm\":\"北京\",\"cityid\":\"101010100\",\"temperature\":\"10℃/-1℃\",\"humidity\":\"0%/0%\",\"weather\":\"晴\",\"weather_icon\":\"http://api.k780.com/upload/weather/d/0.gif\",\"weather_icon1\":\"http://api.k780.com/upload/weather/n/0.gif\",\"wind\":\"西南风转北风\",\"winp\":\"小于3级\",\"temp_high\":\"10\",\"temp_low\":\"-1\",\"humi_high\":\"0\",\"humi_low\":\"0\",\"weatid\":\"1\",\"weatid1\":\"1\",\"windid\":\"5\",\"winpid\":\"0\",\"weather_iconid\":\"0\",\"weather_iconid1\":\"0\"},{\"weaid\":\"1\",\"days\":\"2021-12-04\",\"week\":\"星期六\",\"cityno\":\"beijing\",\"citynm\":\"北京\",\"cityid\":\"101010100\",\"temperature\":\"10℃/-3℃\",\"humidity\":\"0%/0%\",\"weather\":\"晴\",\"weather_icon\":\"http://api.k780.com/upload/weather/d/0.gif\",\"weather_icon1\":\"http://api.k780.com/upload/weather/n/0.gif\",\"wind\":\"北风转东风\",\"winp\":\"小于3级\",\"temp_high\":\"10\",\"temp_low\":\"-3\",\"humi_high\":\"0\",\"humi_low\":\"0\",\"weatid\":\"1\",\"weatid1\":\"1\",\"windid\":\"8\",\"winpid\":\"0\",\"weather_iconid\":\"0\",\"weather_iconid1\":\"0\"},{\"weaid\":\"1\",\"days\":\"2021-12-05\",\"week\":\"星期日\",\"cityno\":\"beijing\",\"citynm\":\"北京\",\"cityid\":\"101010100\",\"temperature\":\"7℃/-2℃\",\"humidity\":\"0%/0%\",\"weather\":\"多云\",\"weather_icon\":\"http://api.k780.com/upload/weather/d/1.gif\",\"weather_icon1\":\"http://api.k780.com/upload/weather/n/1.gif\",\"wind\":\"南风转北风\",\"winp\":\"小于3级\",\"temp_high\":\"7\",\"temp_low\":\"-2\",\"humi_high\":\"0\",\"humi_low\":\"0\",\"weatid\":\"2\",\"weatid1\":\"2\",\"windid\":\"4\",\"winpid\":\"0\",\"weather_iconid\":\"1\",\"weather_iconid1\":\"1\"},{\"weaid\":\"1\",\"days\":\"2021-12-06\",\"week\":\"星期一\",\"cityno\":\"beijing\",\"citynm\":\"北京\",\"cityid\":\"101010100\",\"temperature\":\"14℃/1℃\",\"humidity\":\"0%/0%\",\"weather\":\"晴\",\"weather_icon\":\"http://api.k780.com/upload/weather/d/0.gif\",\"weather_icon1\":\"http://api.k780.com/upload/weather/n/0.gif\",\"wind\":\"无持续风向\",\"winp\":\"小于3级\",\"temp_high\":\"14\",\"temp_low\":\"1\",\"humi_high\":\"0\",\"humi_low\":\"0\",\"weatid\":\"1\",\"weatid1\":\"1\",\"windid\":\"0\",\"winpid\":\"0\",\"weather_iconid\":\"0\",\"weather_iconid1\":\"0\"}]}";


    @Test
    public void testHuffmanTree() {
        ArrayList<Node<String>> nodes = new ArrayList<>();
        Node<String> node = new Node<>("hello", 10);
        Node<String> node1 = new Node<>("good", 23);
        Node<String> node2 = new Node<>("morning", 55);
        Node<String> node3 = new Node<>("afternoon", 1);
        Node<String> node4 = new Node<>("evening", 6);
        Node<String> node5 = new Node<>("haha", 6666);
        Node<String> node6 = new Node<>("oh", 8);
        Node<String> node7 = new Node<>("yeah", 45);

        nodes.add(node);
        nodes.add(node1);
        nodes.add(node2);
        nodes.add(node3);
        nodes.add(node4);
        nodes.add(node5);
        nodes.add(node6);
        nodes.add(node7);

        HuffmanTree<String> huffmanTree = new HuffmanTree<>();
        huffmanTree.create(nodes);

        System.out.println("显示树：");
        huffmanTree.showHuffman(huffmanTree.root);
        System.out.println("编码：");
        huffmanTree.getCode(node5);
    }

    @Test
    public void testMailTree() {
        String s = "MT-TECH-TEAM";
        ArrayList<Node<Character>> arrayList = createHuffmanNode(s);
        HuffmanTree<Character> huffmanTree = new HuffmanTree<>();
        Collections.sort(arrayList);

        Node<Character> root = huffmanTree.create(arrayList);
        System.out.println("一行输出最短的编码后长度：" + getLength(root, 0));

        // 显示树元素
        List<Node<Character>> nodes = huffmanTree.showHuffman(root);

        for (Node<Character> characterNode : nodes) {
            huffmanTree.getCode(characterNode);
        }
    }

    @Test
    public void testTree() {
        String s = "MT-TECH-TEAM";
        System.out.println(createHuf(s));
    }

    /**
     * 前序遍历
     *
     * @param node
     * @param depth
     * @return
     */
    public static int getLength(Node<Character> node, int depth) {
        if (node == null) {
            return 0;
        } else {
            return (node.data == null ? 0 : node.weight) * depth + getLength(node.left, depth + 1) + getLength(node.right, depth + 1);
        }
    }

    public static int createHuf(String s) {
        char[] chars = s.toCharArray();
        HashMap<Character, Integer> map = new HashMap<>();
        for (int i = 0; i < chars.length; i++) {

            if (map.containsKey(chars[i])) {
                map.put(chars[i], map.get(chars[i]) + 1);
            } else {
                map.put(chars[i], 1);
            }
        }

        PriorityQueue<Node<Character>> queue = new PriorityQueue<>(map.size(), Comparator.comparingInt(o -> o.weight));
        for (Map.Entry<Character, Integer> entry : map.entrySet()) {
            queue.offer(new Node<>(entry.getKey(), entry.getValue()));
        }

        while (queue.size() > 1) {
            Node<Character> lnode = queue.poll();
            Node<Character> rnode = queue.poll();
            Node<Character> fatherNode = new Node<>(null, lnode.weight + rnode.weight);
            fatherNode.left = lnode;
            fatherNode.right = rnode;
            queue.offer(fatherNode);
        }

        Node<Character> root = queue.poll();
        return getLength(root, 0);
    }


    public static ArrayList<Node<Character>> createHuffmanNode(String s) {
        char[] chars = s.toCharArray();
        HashMap<Character, Integer> map = new HashMap<>();

        for (int i = 0; i < chars.length; i++) {
            if (map.containsKey(chars[i])) {
                map.put(chars[i], map.get(chars[i]) + 1);
            } else {
                map.put(chars[i], 1);
            }
        }

        Set<Map.Entry<Character, Integer>> entries = map.entrySet();
        Iterator<Map.Entry<Character, Integer>> iterator = entries.iterator();

        ArrayList<Node<Character>> arrayList = new ArrayList<>();

        while (iterator.hasNext()) {
            Map.Entry<Character, Integer> next = iterator.next();
            Character key = next.getKey();
            Integer value = next.getValue();
//            System.out.println(key + ":" + value);
            Node<Character> characterNode = new Node<>(key, value);
            arrayList.add(characterNode);
        }

        return arrayList;
    }
}

















