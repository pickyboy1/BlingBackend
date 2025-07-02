package com.pickyboy.yuquebackend.controller;

import com.pickyboy.yuquebackend.common.response.PageResult;
import com.pickyboy.yuquebackend.common.response.Result;
import com.pickyboy.yuquebackend.domain.dto.tag.CreateTagRequest;
import com.pickyboy.yuquebackend.domain.dto.tag.DeleteTagsRequest;
import com.pickyboy.yuquebackend.domain.dto.tag.QueryTagsRequest;
import com.pickyboy.yuquebackend.domain.dto.tag.UpdateTagRequest;
import com.pickyboy.yuquebackend.domain.vo.tag.TagVO;
import com.pickyboy.yuquebackend.service.ITagService;
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
     * @param queryTagsRequest 查询请求
     * @return 操作结果
     */
    @GetMapping("/tags")
    public Result<PageResult<TagVO>> getUserTags(@Valid @RequestBody QueryTagsRequest queryTagsRequest) {
        PageResult<TagVO> result = tagService.getUserTags(queryTagsRequest);
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
    public Result<Boolean> deleteTags(@Valid @RequestBody DeleteTagsRequest deleteRequest) {
        Boolean result = tagService.deleteTags(deleteRequest);
        return Result.success(result);
    }

    /**
     * 修改标签
     * PATCH /tags
     *
     * @param updateRequest 修改请求
     * @return 操作结果
     */
    @PatchMapping("/tags")
    public Result<TagVO> updateTag(@Valid @RequestBody UpdateTagRequest updateRequest) {
        TagVO result = tagService.updateTag(updateRequest);
        return Result.success(result);
    }
}
