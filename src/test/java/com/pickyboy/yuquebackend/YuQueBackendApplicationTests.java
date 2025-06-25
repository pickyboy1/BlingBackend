package com.pickyboy.yuquebackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.TreeSet;

@SpringBootTest
class YuQueBackendApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void test() {
        TreeSet<Integer> treeSet = new TreeSet<>();
        treeSet.higher(1);
    	System.out.println("test");
    }

}
