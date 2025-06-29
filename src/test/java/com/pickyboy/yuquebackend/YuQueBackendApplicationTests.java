package com.pickyboy.yuquebackend;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.pickyboy.yuquebackend.domain.entity.Resources;
import com.pickyboy.yuquebackend.domain.vo.resource.ResourceTreeVo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    @Test
    void createTreeTest() {
        List<Resources> resources = List.of(
                // 根节点: 公司文档
                new Resources(10L, "公司文档", "folder", null),

                // Level 1: 人力资源
                new Resources(11L, "人力资源", "folder", 10L),
                new Resources(12L, "员工手册.pdf", "document", 11L),

                // Level 2: 绩效考核
                new Resources(13L, "绩效考核", "folder", 11L),
                new Resources(14L, "2024年度考核表.xlsx", "document", 13L),
                new Resources(15L, "考核说明.md", "document", 13L),

                // Level 1: 研发中心
                new Resources(20L, "研发中心", "folder", 10L),

                // Level 2: 后端项目
                new Resources(21L, "后端项目", "folder", 20L),
                new Resources(22L, "API文档.md", "document", 21L),
                new Resources(23L, "数据库设计.sql", "document", 21L),

                // Level 2: 前端项目
                new Resources(24L, "前端项目", "folder", 20L),
                new Resources(25L, "UI设计稿.png", "document", 24L),

                new Resources(26L, "技术分享会.pptx", "document", 20L),

                // Level 1: 市场部
                new Resources(30L, "市场部", "folder", 10L),
                new Resources(31L, "市场推广方案.docx", "document", 30L),

                // 另一个根节点: 个人笔记
                new Resources(100L, "个人笔记", "folder", null),

                // Level 1: 学习资料
                new Resources(101L, "学习资料", "folder", 100L),
                new Resources(102L, "Java编程思想.md", "document", 101L),

                // 独立的根节点文档
                new Resources(200L, "未分类文档.txt", "document", null)
        );
        Map<Long, ResourceTreeVo> map = resources.stream()
                .map(resource -> {
                    ResourceTreeVo vo = new ResourceTreeVo();
                    vo.setId(resource.getId());
                    vo.setTitle(resource.getTitle());
                    vo.setType(resource.getType());
                    vo.setPreId(resource.getPreId());
                    vo.setChildren(new ArrayList<>()); // 初始化 children 列表
                    return vo;
                })
                .collect(Collectors.toMap(ResourceTreeVo::getId, vo -> vo));

        // 2. 再次遍历，将每个节点放入其父节点的 children 列表中
        List<ResourceTreeVo> rootNodes = new ArrayList<>();
        map.values().forEach(node -> {
            Long preId = node.getPreId();
            if (preId == null) {
                // 如果 preId 是 null，说明是根节点
                rootNodes.add(node);
            } else {
                // 如果不是根节点，就从 map 中找到它的父节点
                ResourceTreeVo parent = map.get(preId);
                if (parent != null) {
                    // 将当前节点加入父节点的 children 列表
                    parent.getChildren().add(node);
                }
            }
        });
        log.info("{}", rootNodes);
    }


}
