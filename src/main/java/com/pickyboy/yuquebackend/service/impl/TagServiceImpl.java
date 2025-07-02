package com.pickyboy.yuquebackend.service.impl;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pickyboy.yuquebackend.common.exception.BusinessException;
import com.pickyboy.yuquebackend.common.exception.ErrorCode;
import com.pickyboy.yuquebackend.common.response.PageResult;
import com.pickyboy.yuquebackend.common.utils.CurrentHolder;
import com.pickyboy.yuquebackend.domain.dto.tag.CreateTagRequest;
import com.pickyboy.yuquebackend.domain.dto.tag.DeleteTagsRequest;
import com.pickyboy.yuquebackend.domain.dto.tag.QueryTagsRequest;
import com.pickyboy.yuquebackend.domain.dto.tag.UpdateTagRequest;
import com.pickyboy.yuquebackend.domain.entity.NoteTagMap;
import com.pickyboy.yuquebackend.domain.vo.tag.TagVO;
import org.assertj.core.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pickyboy.yuquebackend.domain.entity.Tags;
import com.pickyboy.yuquebackend.mapper.TagsMapper;
import com.pickyboy.yuquebackend.service.ITagService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 标签服务实现类
 *
 * @author pickyboy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl extends ServiceImpl<TagsMapper, Tags> implements ITagService {

    @Autowired
    private NoteTagMapServiceImpl noteTagMapService;

    @Override
    public PageResult<TagVO> getUserTags(QueryTagsRequest queryTagsRequest) {
        // TODO: 测试获取用户标签列表逻辑
        log.info("用户标签列表查询");
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        Page<Tags> page = new Page<>(queryTagsRequest.getPageNum(), queryTagsRequest.getPageSize());

        LambdaQueryWrapper<Tags> wrapper = new LambdaQueryWrapper<Tags>()
                .eq(Tags::getUserId, userId)
                .orderByDesc(Tags::getCreatedAt);

        List<Tags> tags = list(
                new LambdaQueryWrapper<Tags>()
                        .eq(Tags::getUserId, userId)
                        .orderByDesc(Tags::getCreatedAt)
        );

        // 关键词搜索
        if (StringUtils.hasText(queryTagsRequest.getKeyword())) {
            wrapper.like(Tags::getName, queryTagsRequest.getKeyword());
        }

        Page<Tags> tagPage = page(page, wrapper);

        List<TagVO> voList = tagPage.getRecords().stream()
                .map(this::convertToTagVO)
                .collect(Collectors.toList());

        return new PageResult<>(tagPage.getTotal(), voList);
    }

    @Override
    @Transactional
    public TagVO createTag(CreateTagRequest createRequest) {
        // TODO: 测试创建标签逻辑
        log.info("创建新的标签: tagName={}", createRequest.getName());

        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        String tagName = createRequest.getName().trim();

        // 检查标签名称是否重复（用户范围内）
        boolean exists = exists(
                new LambdaQueryWrapper<Tags>()
                        .eq(Tags::getUserId, userId)
                        .eq(Tags::getName, tagName)
        );

        if (exists) {
            throw new BusinessException(ErrorCode.TAG_NAME_DUPLICATE,
                    "标签名称已存在: " + tagName);
        }

        // 创建标签
        Tags tag = new Tags();
        tag.setUserId(userId);
        tag.setName(tagName);
        tag.setReferedCount(0); // 初始引用次数为0

        boolean saved = save(tag);
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "标签创建失败");
        }

        log.info("标签创建成功: id={}, name={}", tag.getId(), tag.getName());

        return convertToTagVO(tag);
    }

    @Override
    @Transactional
    public TagVO updateTag(UpdateTagRequest updateRequest) {
        // TODO: 测试更新标签逻辑

        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        Long tagId = updateRequest.getTagId();
        String newName = updateRequest.getName().trim();
        log.info("标签名称更新: tagId={}, newName={}", tagId, newName);

        // 验证标签是否属于当前用户
        Tags existingTag = getOne(
                new LambdaQueryWrapper<Tags>()
                        .eq(Tags::getId, tagId)
                        .eq(Tags::getUserId, userId)
        );

        if (existingTag == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签不存在");
        }

        // 检查新名称是否与其他标签重复
        boolean nameExists = exists(
                new LambdaQueryWrapper<Tags>()
                        .eq(Tags::getUserId, userId)
                        .eq(Tags::getName, newName)
                        .ne(Tags::getId, tagId)
        );

        if (nameExists) {
            throw new BusinessException(ErrorCode.TAG_NAME_DUPLICATE,
                    "标签名称已存在: " + newName);
        }

        // 更新标签
        existingTag.setName(newName);
        boolean updated = updateById(existingTag);

        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "标签更新失败");
        }

        return convertToTagVO(existingTag);
    }

    @Override
    @Transactional
    public Boolean deleteTags(DeleteTagsRequest deleteRequest) {
        // TODO: 测试删除标签逻辑
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        Long[] tagIds = deleteRequest.getTagIds();

        // 验证标签是否属于当前用户
        long count = count(
                new LambdaQueryWrapper<Tags>()
                        .eq(Tags::getUserId, userId)
                        .in(Tags::getId, Arrays.asList(tagIds))
        );

        if (count != tagIds.length) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "存在无效的标签ID");
        }

        // 删除标签（物理删除，因为标签表没有逻辑删除）
        boolean deleted = removeByIds(Arrays.asList(tagIds));

        // 删除相关的小记-标签关联关系
        noteTagMapService.remove(
                new LambdaQueryWrapper<NoteTagMap>()
                        .in(NoteTagMap::getTagId, Arrays.asList(tagIds))
        );

        return deleted;
    }

    private TagVO convertToTagVO(Tags tag) {
        TagVO vo = new TagVO();
        vo.setId(String.valueOf(tag.getId()));
        vo.setName(tag.getName());
        vo.setCount(tag.getReferedCount());
        vo.setCreatedAt(tag.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return vo;
    }
}