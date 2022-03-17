package com.windy.breakpadexample;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

import static org.junit.Assert.*;


import com.windy.breakpadexample.singleton.Singleton;

import java.lang.reflect.InvocationTargetException;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testStr2() {
        Thread thread = new Thread(new joinDemo());
        thread.start();

        for (int i = 0; i < 20; i++) {
            System.out.println("主线程第" + i + "此执行！");
            if (i >= 2) {
                try {
                    // t1线程合并到主线程中，主线程停止执行过程，转而执行t1线程，直到t1执行完毕后继续；
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void testStr() {
//        new ReferenceAndValue().testStr("a");
        String a = "abc";
        String c = "abc";
        String b = new String("abc");

        System.out.println(a == b);
        System.out.println(a == c);
    }

    /**
     * 斐波那契数 动态规划可以解决效率
     * fibonacciSequence数列   8=7+6   7=6+5  6=5+4
     * 1  1  2  3  5  8  13  21  34  55  89  144  ......
     *
     * @param n 第几个数列
     * @return 第n数列的具体数值
     */
    public int fibonacciSequence(int n) {
        if (n < 1)
            return 0;

        if (n == 1 || n == 2) {
            return 1;
        } else {
            return fibonacciSequence(n - 1) + fibonacciSequence(n - 2);
        }
    }

    @Test
    public void testSingletonObj() {
        try {
            System.out.println("静态内部类单利安全吗:" + Singleton.getInstance2().hook());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSingletonSerialize() {
        Singleton instance2 = Singleton.getInstance2();
        byte[] serialize = SerializationUtils.serialize(instance2);
        Singleton deserialize = SerializationUtils.deserialize(serialize);
        System.out.println("序列化后单利" + (instance2 == deserialize));
    }

    class joinDemo implements Runnable {

        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                System.out.println("线程1第" + i + "次执行");
            }
        }
    }
}