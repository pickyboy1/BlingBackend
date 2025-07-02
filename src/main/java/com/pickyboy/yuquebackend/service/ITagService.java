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
 * @author pickyboy
 */
public interface ITagService extends IService<Tags> {

    /**
     * 获取当前用户的所有标签
     *
     * @return 标签列表
     */
    PageResult<TagVO> getUserTags(QueryTagsRequest queryTagsRequest);

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
     * @param updateRequest 更新请求
     * @return 更新后的标签信息
     */
    TagVO updateTag(UpdateTagRequest updateRequest);

    /**
     * 批量删除标签
     *
     * @param deleteRequest
     */
    Boolean deleteTags(DeleteTagsRequest deleteRequest);
}