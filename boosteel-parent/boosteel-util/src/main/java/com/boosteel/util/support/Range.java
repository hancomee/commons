package com.boosteel.util.support;

public class Range {


    public static final int[] range(int... nums) {
        if (nums.length == 0) return new int[]{};
        if (nums.length == 1) return new int[]{nums[0]};
        if (nums.length > 2) return nums;

        int start = nums[0], end = nums[1];

        if(start == end) return new int[]{start};

        // 1, 5
        if (start < end) {
            end++;
            int[] range = new int[end - start];
            for (int i = 0; start < end; start++)
                range[i++] = start;
            return range;
        }

        // 5, 1
        else {
            end--;
            int[] range = new int[start - end];
            for (int i = 0; start > end; start--)
                range[i++] = start;
            return range;
        }

    }

}
