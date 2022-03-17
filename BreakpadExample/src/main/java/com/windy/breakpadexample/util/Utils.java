package com.windy.breakpadexample.util;

public class Utils {
    private Utils() {
    }

    public static void printArray(int[] arr) {
        for (int t : arr) {
            System.out.print(t + " ");
        }

        System.out.println();
    }
}
