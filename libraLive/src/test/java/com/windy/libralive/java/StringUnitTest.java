package com.windy.libralive.java;

import org.junit.Test;

public class StringUnitTest {

    /**
     * 调用intern方法时，如果池已经包含equals(Object)方法确定的此String对象的字符串，则返回池中的字符串。 否则，将此String对象添加到池中，并返回对此String对象的引用。
     */
    @Test
    public void testStringIntern() {
        String a = new StringBuffer("abc").append("d").toString();
        System.out.println(a.intern() == a);

        String a1 = new StringBuffer("abc").append("d").toString();
        String b = new String("abcd");
        a1.intern();
        System.out.println(a1.intern() == b);

        String a2 = new StringBuffer("abc").append("d").toString();
        String c = "abcd";
        System.out.println(a2.intern() == c);


    }
}
