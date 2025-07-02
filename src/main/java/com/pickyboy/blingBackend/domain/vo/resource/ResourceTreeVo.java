package com.pickyboy.blingBackend.domain.vo.resource;

import java.util.List;

import lombok.Data;

@Data
public class ResourceTreeVo {
    private Long id;
    private String title;
    private String type;
    private Long preId;
    private List<ResourceTreeVo> children;
}
