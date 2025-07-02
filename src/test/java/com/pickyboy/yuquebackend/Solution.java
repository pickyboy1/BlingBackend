package com.pickyboy.yuquebackend;

import java.util.*;

public class Solution {
    public int longestConsecutive(int[] nums) {
        if(nums.length==0){
            return 0;
        }
        if(nums.length==1){
            return 1;
        }
            Set<Integer> set = new HashSet<>();
            for(int i :nums){
                set.add(i);
            }
            int ans = 1;

            while(!set.isEmpty()){
                int max = 1;
                int toUp = set.iterator().next();
                set.remove(toUp);
                int toDown = toUp;
                // 向上查找
                while(set.contains(++toUp)){
                    max++;
                    set.remove(toUp);
                }
                //向下查找
                while(set.contains(--toDown)){
                    max++;
                    set.remove(toDown);
                }
                if(max>ans){
                    ans = max;
                }
            }
            return ans;
    }
}
