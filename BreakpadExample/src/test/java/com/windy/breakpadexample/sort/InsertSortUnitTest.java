package com.windy.breakpadexample.sort;

import com.windy.breakpadexample.util.Utils;

import org.junit.Test;

/**
 * 插入排序
 */
public class InsertSortUnitTest {
    /**
     * 插入排序
     *
     * @param array
     */
    public static void insertSort(int[] array) {
        // 从第第二个开始排序
        for (int i = 1; i < array.length; i++) {
            int j = i;
            int target = array[j];
            while (j > 0 && target < array[j - 1]) {
                array[j] = array[j - 1];
                j--;
            }

            array[j] = target;
        }
    }

    /**
     * 希尔排序
     *
     * @param array
     * @param step  步长
     */
    public static void shelSort(int[] array, int step) {
        for (int i = step; i < array.length; i = i + step) {
            int j = i;
            int target = array[j];
            while (j > 0 && target < array[j - 1]) {
                array[j] = array[j - 1];
                j--;
            }

            array[j] = target;
        }

    }

    @Test
    public void testInsertSort() {
        int[] array = {7, 3, 2, 1, 6, 9, 4, 8, 5};
        shelSort(array, 3);
        shelSort(array, 1);
        Utils.printArray(array);
    }
}