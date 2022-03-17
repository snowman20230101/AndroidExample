package com.windy.breakpadexample;

/**
 * 一、值传递、引用传递定义
 * 1.这个题目出的不严谨，但是很好(因为涉及了 Java 内存模型)
 * 2.就 Java 语言本身来说，只有值传递，没有引用传递。
 * 3.根据 值传递，引用传递的定义来说：
 *      Java 中的基本类型，属于值传递。
 *      Java 中的引用类型，属于引用传递。
 *      Java 中的 String 及包装类，属于特殊群体，作为形参时，由于每次赋值都相当于重新创建了对象，因此看起来像值传递，但是其特性已经破坏了，值传递、引用传递的定义。因此他们属于引用传递的定义，却表现为值传递。
 * <p>
 * <p>
 * 二、基本数据类型、引用类型
 * 1.基本数据类型、引用类型定义
 *      基本数据类：Java 中有八种基本数据类型“byte、short、int、long、float、double、char、boolean”
 *      引用类型：new 创建的实体类、对象、及数组
 * 2.基本数据类型、引用类型在内存中的存储方式
 *      基本数据类型：存放在栈内存中。用完就消失。
 *      引用类型：在栈内存中存放引用堆内存的地址，在堆内存中存储类、对象、数组等。当没用引用指向堆内存中的类、对象、数组时，由 GC回收机制不定期自动清理。
 */
public class ReferenceAndValue {
    String str = "test1"; // 实惨
    String a = "a";
    String b = "a";
    String c = "c";
    String ac = "ac";

    // s 值传递
    public void testStr(String s) { // s 属于形参
        s = "abc";
        System.out.println(s);
        System.out.println(a == b);
        System.out.println(ac == (a + c));
    }

    public class User {
        String name;
        int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return "User{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }
}
