package com.windy.breakpadexample.singleton;

import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Singleton implements Serializable {

    private volatile static Singleton instance;

    private Singleton() {
    }

    /**
     * 懒汉式
     *
     * @return
     */
    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }

        return instance;
    }


    /**
     * 静态内部类
     */
    private static class SingletonHolder {
        private static Singleton INSTANCE = new Singleton();
    }

    /**
     * 静态内部类 单利
     * 1、通过静态内部类的方式实现单例模式是线程安全的，
     * 2、同时静态内部类不会在Singleton类加载时就加载，而是在调用getInstance2()方法时才进行加载，达到了懒加载的效果。
     * <p>
     * TODO 似乎静态内部类看起来已经是最完美的方法了，其实不是，可能还存在反射攻击或者反序列化攻击。且看如下代码：
     *
     * @return
     */
    public static Singleton getInstance2() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 测试静态内部类单利，可以被攻击
     * 利用反射
     */
    public boolean hook() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Singleton instance2 = Singleton.getInstance2();
        Constructor<Singleton> constructor = Singleton.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Singleton newInstance = constructor.newInstance();

        return instance2 == newInstance;
    }

    /**
     * 枚举单利
     */
    enum SingletonEX {
        INSTANCE;
    }
}
