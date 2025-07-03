package com.pickyboy.blingBackend.controller;


import com.pickyboy.blingBackend.common.response.Result;
import com.pickyboy.blingBackend.domain.dto.tag.CreateTagRequest;
import com.pickyboy.blingBackend.domain.dto.tag.DeleteTagsRequest;
import com.pickyboy.blingBackend.domain.dto.tag.UpdateTagRequest;
import com.pickyboy.blingBackend.domain.vo.tag.TagVO;
import com.pickyboy.blingBackend.service.ITagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小记控制器
 * 处理小记相关的API请求
 *
 * @author shiqi
 */
@Slf4j
@RestController
@RequestMapping()
@RequiredArgsConstructor
public class TagController {

    @Autowired
    private ITagService tagService;

    /**
     * 获取用户标签列表
     * GET /tags
     *
     * @param page 页码
     * @param limit 每页数量
     * @param sortBy 排序字段
     * @param order 排序方式
     * @return 操作结果
     */
    @GetMapping("/tags")
    public Result<List<TagVO>> getUserTags(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "30") Integer limit,
            @RequestParam(defaultValue = "count") String sortBy,
            @RequestParam(defaultValue = "desc") String order) {
        List<TagVO> result = tagService.getUserTags(page, limit, sortBy, order);
        return Result.success(result);
    }

    /**
     * 创建新的标签
     * POST /tags
     *
     * @param createRequest 创建请求
     * @return 操作结果
     */
    @PostMapping("/tags")
    public Result<TagVO> createKnowledgeBase(@Valid @RequestBody CreateTagRequest createRequest) {
        log.info("创建新的标签: name={}", createRequest.getName());
        TagVO result = tagService.createTag(createRequest);
        return Result.success(result);
    }

    /**
     * 批量删除标签
     * DELETE /tags
     *
     * @param deleteRequest 删除请求
     * @return 操作结果
     */
    @DeleteMapping("/tags")
    public Result<Void> deleteTags(@Valid @RequestBody DeleteTagsRequest deleteRequest) {
        tagService.deleteTags(deleteRequest);
        return Result.success(null);
    }

    /**
     * 修改标签
     * PATCH /tags/{tagId}
     *
     * @param tagId 标签ID
     * @param updateRequest 修改请求
     * @return 操作结果
     */
    @PatchMapping("/tags/{tagId}")
    public Result<Void> updateTag(@PathVariable Long tagId,
            @Valid @RequestBody UpdateTagRequest updateRequest) {
        tagService.updateTag(tagId,updateRequest);
        return Result.success(null);
    }
}
