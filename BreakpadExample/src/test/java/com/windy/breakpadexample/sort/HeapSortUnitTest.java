package com.windy.breakpadexample.sort;

//import com.windy.breakpadexample.util.Utils;

import org.junit.Test;

public class HeapSortUnitTest {

    /**
     * 堆排序 调整堆
     *
     * @param array
     * @param begin
     * @param end
     */
    private static void maxHeapModify(int[] array, int begin, int end) {
        int dad = begin;
        int son = dad * 2 + 1;

        while (son <= end) {
            if (son + 1 <= end && array[son] < array[son + 1]) {
                son++;
            }

            if (array[son] < array[dad]) {
                return;
            } else {
                int tmp = array[dad];
                array[dad] = array[son];
                array[son] = tmp;

                // 指向，下个 父节点
                dad = son;
                son = dad * 2 + 1;
            }
        }
    }

    /**
     * 堆排序
     *
     * @param array
     * @param size
     */
    public static void heapSort(int[] array, int size) {
        // 建堆 从最小非叶子跟节点
        for (int i = size / 2 - 1; i >= 0; i--) {
            maxHeapModify(array, i, size - 1);
        }

        // 排序，交换跟节点和末尾节点
        for (int i = size - 1; i > 0; i--) {
            int root = array[0];
            array[0] = array[i];
            array[i] = root;
            maxHeapModify(array, 0, i - 1);
        }
    }

    @Test
    public void testHeapSort() {
        int[] array = {7, 3, 2, 1, 6, 9, 4, 8, 5};
        heapSort(array, array.length);
//        Utils.printArray(array);
    }
}
