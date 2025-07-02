package com.pickyboy.yuquebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pickyboy.yuquebackend.common.response.PageResult;
import com.pickyboy.yuquebackend.domain.dto.tag.CreateTagRequest;
import com.pickyboy.yuquebackend.domain.dto.tag.DeleteTagsRequest;
import com.pickyboy.yuquebackend.domain.dto.tag.QueryTagsRequest;
import com.pickyboy.yuquebackend.domain.dto.tag.UpdateTagRequest;
import com.pickyboy.yuquebackend.domain.entity.Tags;
import com.pickyboy.yuquebackend.domain.vo.tag.TagVO;

import java.util.List;

/**
 * 标签服务接口
 *
 * @author shiqi
 */
public interface ITagService extends IService<Tags> {

    /**
     * 获取当前用户的所有标签
     *
     * @param page 页码
     * @param limit 每页数量
     * @param sortBy 排序字段
     * @param order 排序方式
     * @return 标签列表
     */
    List<TagVO> getUserTags(Integer page, Integer limit, String sortBy, String order);

    /**
     * 创建新标签
     *
     * @param createRequest 创建请求
     * @return 标签信息
     */
    TagVO createTag(CreateTagRequest createRequest);

    /**
     * 修改标签名
     *
     * @param tagId 标签ID
     * @param updateRequest 更新请求
     */
    void updateTag(Long tagId, UpdateTagRequest updateRequest);

    /**
     * 批量删除标签
     *
     * @param deleteRequest 删除请求
     */
    void deleteTags(DeleteTagsRequest deleteRequest);
}