package com.pickyboy.blingBackend.service.impl;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.pickyboy.blingBackend.common.exception.BusinessException;
import com.pickyboy.blingBackend.common.exception.ErrorCode;
import com.pickyboy.blingBackend.common.utils.CurrentHolder;
import com.pickyboy.blingBackend.domain.entity.NoteTagMap;
import com.pickyboy.blingBackend.domain.dto.tag.CreateTagRequest;
import com.pickyboy.blingBackend.domain.dto.tag.DeleteTagsRequest;
import com.pickyboy.blingBackend.domain.dto.tag.UpdateTagRequest;

import com.pickyboy.blingBackend.domain.vo.tag.TagVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pickyboy.blingBackend.domain.entity.Tags;
import com.pickyboy.blingBackend.mapper.TagsMapper;
import com.pickyboy.blingBackend.service.ITagService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

/**
 * 标签服务实现类
 *
 * @author shiqi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl extends ServiceImpl<TagsMapper, Tags> implements ITagService {

    @Autowired
    private NoteTagMapServiceImpl noteTagMapService;

    // 时间格式化器
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<TagVO> getUserTags(Integer page, Integer limit, String sortBy, String order) {
        // TODO: 测试获取用户标签列表逻辑
        log.info("用户标签列表查询");
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        Page<Tags> pageObj = new Page<>(page, limit);

        // 构建查询条件
        LambdaQueryWrapper<Tags> wrapper = new LambdaQueryWrapper<Tags>()
                .eq(Tags::getUserId, userId);

        // 设置排序
        if ("name".equals(sortBy)) {
            if ("asc".equals(order)) {
                wrapper.orderByAsc(Tags::getName);
            } else {
                wrapper.orderByDesc(Tags::getName);
            }
        } else {
            // 默认按 count 排序
            if ("asc".equals(order)) {
                wrapper.orderByAsc(Tags::getReferedCount);
            } else {
                wrapper.orderByDesc(Tags::getReferedCount);
            }
        }

        Page<Tags> tagPage = page(pageObj, wrapper);

        List<TagVO> voList = tagPage.getRecords().stream()
                .map(this::convertToTagVO)
                .collect(Collectors.toList());

        return voList;
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
    public void updateTag(Long tagId, UpdateTagRequest updateRequest) {
        // TODO: 测试更新标签逻辑
        log.info("标签名称更新: tagId={}, newName={}", tagId, updateRequest.getName());

        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        String newName = updateRequest.getName().trim();

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

        log.info("标签更新成功: id={}, name={}", existingTag.getId(), existingTag.getName());
    }

    @Override
    @Transactional
    public void deleteTags(DeleteTagsRequest deleteRequest) {
        // TODO: 测试删除标签逻辑

        List<String> tagIds = deleteRequest.getTagIds();

        log.info("批量删除标签: tagIds={}", tagIds);

        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 转换为Long类型
        List<Long> longTagIds = tagIds.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());

        // 验证标签是否属于当前用户
        long count = count(
                new LambdaQueryWrapper<Tags>()
                        .eq(Tags::getUserId, userId)
                        .in(Tags::getId, longTagIds)
        );

        if (count != longTagIds.size()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "存在无效的标签ID");
        }

        // 删除标签
        boolean deleted = removeByIds(longTagIds);

        // 删除相关的小记-标签关联关系
        noteTagMapService.remove(
                new LambdaQueryWrapper<NoteTagMap>()
                        .in(NoteTagMap::getTagId, longTagIds)
        );

        if (!deleted) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "标签删除失败");
        }

        log.info("批量删除标签成功，数量: {}", longTagIds.size());
    }

    /**
     * 转换为TagVO，包含完整的时间信息
     */
    private TagVO convertToTagVO(Tags tag) {
        TagVO vo = new TagVO();
        vo.setId(String.valueOf(tag.getId()));
        vo.setName(tag.getName());
        vo.setCount(tag.getReferedCount());

        return vo;
    }
}