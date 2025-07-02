package com.pickyboy.blingBackend.domain.vo.knowledgebase;

import java.util.List;

import com.pickyboy.blingBackend.domain.entity.Resources;

import lombok.Data;

@Data
public class KbsWithRecentResourceVo {

    private Long id;
    private String name;
    private String iconIndex;
    private Integer visibility;

    private List<Resources> recentResources;
}
