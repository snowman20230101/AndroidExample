package com.windy.breakpadexample.sort;

import com.windy.breakpadexample.util.Utils;

import org.junit.Test;

/**
 * 快速排序
 */
public class QuicklySortUnitTest {
    /**
     * 快速排序 二叉树遍历中：前序遍历
     *
     * @param array 具体数据
     * @param from  起始位置
     * @param to    结束位置
     */
    public void quickSort(int[] array, int from, int to) {
        if ((to - from) <= 0) return; // 递归 边界条件
        int low = from;
        int high = to;
        int value = array[low];
        boolean direction = true;

        L1:
        while (low < high) {
            if (direction) {
                // 从右边找比较
                for (int i = high; i > low; i--) {
                    if (array[i] < value) {
                        array[low++] = array[i];
                        direction = !direction;
                        high = i;
                        continue L1;
                    }
                }

                high = low;
            } else {
                // 从左边比较
                for (int i = low; i < high; i++) {
                    if (array[i] > value) {
                        array[high--] = array[i];
                        direction = !direction;
                        low = i;
                        continue L1;
                    }
                }

                low = high;
            }
        }

        array[low] = value;
        quickSort(array, from, low - 1);
        quickSort(array, low + 1, to);
    }

    @Test
    public void testQuicklySort() {
        int[] array = {7, 3, 2, 1, 6, 9, 4, 8, 5};
        quickSort(array, 0, array.length - 1);
        Utils.printArray(array);
    }
}
