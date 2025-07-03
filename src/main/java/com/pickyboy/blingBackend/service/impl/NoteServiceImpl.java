package com.pickyboy.blingBackend.service.impl;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.pickyboy.blingBackend.common.exception.BusinessException;
import com.pickyboy.blingBackend.common.exception.ErrorCode;
import com.pickyboy.blingBackend.common.utils.CurrentHolder;
import com.pickyboy.blingBackend.domain.dto.note.CreateNoteRequest;
import com.pickyboy.blingBackend.domain.dto.note.DeleteNotesRequest;
import com.pickyboy.blingBackend.domain.dto.note.SetNoteTagsRequest;
import com.pickyboy.blingBackend.domain.dto.note.UpdateNoteRequest;
import com.pickyboy.blingBackend.domain.entity.NoteTagMap;
import com.pickyboy.blingBackend.domain.entity.Tags;
import com.pickyboy.blingBackend.service.INoteTagMapService;
import com.pickyboy.blingBackend.service.ITagService;

import com.pickyboy.blingBackend.domain.vo.note.NoteDetailVO;
import com.pickyboy.blingBackend.domain.vo.note.NoteListVO;
import com.pickyboy.blingBackend.domain.vo.tag.TagSimpleVO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pickyboy.blingBackend.domain.entity.Notes;
import com.pickyboy.blingBackend.mapper.NotesMapper;
import com.pickyboy.blingBackend.service.INoteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

/**
 * 小记服务实现类
 *
 * @author shiqi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl extends ServiceImpl<NotesMapper, Notes> implements INoteService {

    @Autowired
    private ITagService tagService;

    @Autowired
    private INoteTagMapService noteTagMapService;

    @Override
    public List<NoteListVO> getNoteList(Long tagId, Integer page, Integer limit, String sortBy, String order) {
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 构建分页对象
        Page<Notes> pageObj = new Page<>(page, limit);

        // 构建查询条件
        LambdaQueryWrapper<Notes> wrapper = new LambdaQueryWrapper<Notes>()
                .eq(Notes::getUserId, userId);

        // 如果有标签筛选
        if (tagId != null) {
            // 先查询出该标签关联的所有小记ID
            List<Long> noteIds = noteTagMapService.list(
                            new LambdaQueryWrapper<NoteTagMap>()
                                    .eq(NoteTagMap::getTagId, tagId)
                    ).stream()
                    .map(NoteTagMap::getNoteId)
                    .collect(Collectors.toList());

            if (noteIds.isEmpty()) {
                return new ArrayList<>();
            }

            wrapper.in(Notes::getId, noteIds);
        }

        // 设置排序
        if ("createdAt".equals(sortBy)) {
            if ("asc".equals(order)) {
                wrapper.orderByAsc(Notes::getCreatedAt);
            } else {
                wrapper.orderByDesc(Notes::getCreatedAt);
            }
        } else {
            // 默认按 updatedAt 排序
            if ("asc".equals(order)) {
                wrapper.orderByAsc(Notes::getUpdatedAt);
            } else {
                wrapper.orderByDesc(Notes::getUpdatedAt);
            }
        }

        Page<Notes> notePage = page(pageObj, wrapper);

        // 转换为VO
        List<NoteListVO> voList = notePage.getRecords().stream()
                .map(this::convertToNoteListVO)
                .collect(Collectors.toList());

        return voList;
    }

    @Override
    @Transactional
    public NoteDetailVO createNote(CreateNoteRequest createNoteRequest) {
        // TODO: 测试创建小记逻辑（还未修改完成）
        log.info("创建新的小记: title={}");

        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 创建小记
        Notes note = new Notes();
        note.setUserId(userId);
        note.setContent(createNoteRequest.getContent());
        // 从内容生成title
        note.setTitle(generateTitleFromContent(createNoteRequest.getContent()));

        boolean saved = save(note);
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "小记创建失败");
        }

        // 处理标签关联
        if (createNoteRequest.getTagIds() != null && !createNoteRequest.getTagIds().isEmpty()) {
            setNoteTagsInternal(note.getId(), createNoteRequest.getTagIds());
        }

        // 重新查询保存后的完整数据（包含自动生成的时间戳）
        Notes savedNote = getById(note.getId());
        return convertToNoteDetailVO(savedNote);
    }

    @Override
    public NoteDetailVO getNoteDetail(Long noteId) {
        // TODO: 测试获取小记详情逻辑
        log.info("获取小记详情: noteId={}", noteId);

        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        Notes note = getOne(
                new LambdaQueryWrapper<Notes>()
                        .eq(Notes::getId, noteId)
                        .eq(Notes::getUserId, userId)
        );

        if (note == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "小记不存在");
        }

        return convertToNoteDetailVO(note);
    }

    @Override
    public void updateNote(Long noteId, UpdateNoteRequest updateNoteRequest) {
        // TODO: 测试更新小记逻辑（还未完成修改）
        log.info("更新小记: noteId={}", noteId);

        Long userId = CurrentHolder.getCurrentUserId();

        // 验证小记是否属于当前用户
        Notes note = getOne(
                new LambdaQueryWrapper<Notes>()
                        .eq(Notes::getId, noteId)
                        .eq(Notes::getUserId, userId)
        );

        if (note == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "小记不存在");
        }

        // 更新小记内容和标题
        note.setContent(updateNoteRequest.getContent());
        // 从新内容生成新的title
        note.setTitle(generateTitleFromContent(updateNoteRequest.getContent()));

        boolean updated = updateById(note);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "小记更新失败");
        }
    }

    @Override
    public void deleteNotes(DeleteNotesRequest deleteNotesRequest) {
        // TODO: 测试删除小记逻辑
        List<String> noteIds = deleteNotesRequest.getNoteIds();

        log.info("批量删除小记: noteIds={}", noteIds);

        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 转换为Long类型
        List<Long> longNoteIds = noteIds.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());

        // 验证小记是否属于当前用户
        long count = count(
                new LambdaQueryWrapper<Notes>()
                        .eq(Notes::getUserId, userId)
                        .in(Notes::getId, longNoteIds)
        );

        if (count != longNoteIds.size()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "存在无效的小记ID");
        }

        // 获取要删除的小记关联的标签，用于更新标签引用计数
        List<NoteTagMap> relationsToDelete = noteTagMapService.list(
                new LambdaQueryWrapper<NoteTagMap>()
                        .in(NoteTagMap::getNoteId, longNoteIds)
        );

        // 统计每个标签的引用次数减少量
        Map<Long, Long> tagDecrementMap = relationsToDelete.stream()
                .collect(Collectors.groupingBy(
                        NoteTagMap::getTagId,
                        Collectors.counting()
                ));

        // 删除小记
        boolean notesDeleted = removeByIds(longNoteIds);

        // 删除小记-标签关联关系
        if (!relationsToDelete.isEmpty()) {
            List<Long> relationIds = relationsToDelete.stream()
                    .map(NoteTagMap::getId)
                    .collect(Collectors.toList());

            boolean relationsDeleted = noteTagMapService.removeByIds(relationIds);

            if (!relationsDeleted) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除小记标签关联失败");
            }

            // 更新标签引用计数
            for (Map.Entry<Long, Long> entry : tagDecrementMap.entrySet()) {
                Long tagId = entry.getKey();
                Long decrementCount = entry.getValue();

                tagService.update(
                        new LambdaUpdateWrapper<Tags>()
                                .eq(Tags::getId, tagId)
                                .setSql("refered_count = GREATEST(refered_count - " + decrementCount + ", 0)")
                );
            }
        }

        if (!notesDeleted) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "小记删除失败");
        }

        log.info("批量删除小记成功，数量: {}", longNoteIds.size());
    }

    @Override
    @Transactional
    public void setNoteTags(Long noteId, SetNoteTagsRequest setNoteTagsRequest) {
        // TODO:测试设置小记的标签逻辑
        List<String> tagIds = setNoteTagsRequest.getTagIds();

        log.info("设置小记标签: noteId={}, tagIds={}", noteId, tagIds);

        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 验证小记是否属于当前用户
        Notes note = getOne(
                new LambdaQueryWrapper<Notes>()
                        .eq(Notes::getId, noteId)
                        .eq(Notes::getUserId, userId)
        );

        if (note == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "小记不存在");
        }

        setNoteTagsInternal(noteId, tagIds);
    }

    @Override
    public List<TagSimpleVO> getNoteTags(Long noteId){
        // TODO: 测试获取小记标签列表逻辑
        log.info("获取小记标签: noteId={}", noteId);

        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 验证小记是否属于当前用户
        Notes note = getOne(
                new LambdaQueryWrapper<Notes>()
                        .eq(Notes::getId, noteId)
                        .eq(Notes::getUserId, userId)
        );

        if (note == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "小记不存在");
        }

        // 获取小记关联的标签ID
        List<Long> tagIds = noteTagMapService.list(
                        new LambdaQueryWrapper<NoteTagMap>()
                                .eq(NoteTagMap::getNoteId, noteId)
                ).stream()
                .map(NoteTagMap::getTagId)
                .collect(Collectors.toList());

        if (tagIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取标签详情
        List<Tags> tags = tagService.list(
                new LambdaQueryWrapper<Tags>()
                        .in(Tags::getId, tagIds)
                        .orderByAsc(Tags::getName)
        );

        return tags.stream()
                .map(this::convertToTagSimpleVO)
                .collect(Collectors.toList());
    }

    @Override
    public void removeNoteTag(Long noteId, Long tagId) {
        // TODO: 实现移除小记标签逻辑
        log.info("移除小记标签: noteId={}, tagId={}", noteId, tagId);
        throw new UnsupportedOperationException("待实现");
    }

    @Override
    public List<?> searchNotes(String keyword) {
        // TODO: 实现搜索小记逻辑
        log.info("搜索小记: keyword={}", keyword);
        throw new UnsupportedOperationException("待实现");
    }

    /**
     * 转换为NoteDetailVO
     */
    private NoteDetailVO convertToNoteDetailVO(Notes note) {
        NoteDetailVO vo = new NoteDetailVO();
        vo.setId(String.valueOf(note.getId()));
        vo.setTitle(note.getTitle());
        vo.setContent(note.getContent());

        // 设置时间字段
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (note.getCreatedAt() != null) {
            vo.setCreatedAt(note.getCreatedAt().format(formatter));
        }
        if (note.getUpdatedAt() != null) {
            vo.setUpdatedAt(note.getUpdatedAt().format(formatter));
        }

        // 获取关联的标签
        List<TagSimpleVO> tags = getNoteTags(note.getId());
        vo.setTags(tags);

        return vo;
    }


    /**
     * 转换为NoteListVO
     */
    private NoteListVO convertToNoteListVO(Notes note) {
        NoteListVO vo = new NoteListVO();
        vo.setId(String.valueOf(note.getId()));
        vo.setTitle(note.getTitle());

        // 设置时间字段 - 注意：列表页面通常只需要updatedAt
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (note.getUpdatedAt() != null) {
            vo.setUpdatedAt(note.getUpdatedAt().format(formatter));
        }

        // 根据你的需求，如果列表页面也需要content，取消下面的注释
        // 添加内容摘要（前200字符） - 根据你的反馈，这个可能不需要
        // vo.setContent(generateContentSummary(note.getContent()));

        // 获取关联的标签 - 根据你的反馈，这个可能不需要
        // List<TagSimpleVO> tags = getNoteTags(note.getId());
        // vo.setTags(tags);

        return vo;
    }

    /**
     * 从content生成title（前200字符）
     */
    private String generateTitleFromContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "无标题";
        }

        String trimmedContent = content.trim();

        // 移除多余的空白字符和换行符，用空格替代
        String cleanedContent = trimmedContent.replaceAll("\\s+", " ");

        if (cleanedContent.length() <= 200) {
            return cleanedContent;
        }

        // 截取前200个字符，并尝试在最后一个完整的词处截断
        String truncated = cleanedContent.substring(0, 200);

        // 查找最后一个空格、句号、感叹号或问号的位置
        int lastPunctuation = Math.max(
                Math.max(truncated.lastIndexOf(' '), truncated.lastIndexOf('。')),
                Math.max(truncated.lastIndexOf('！'), truncated.lastIndexOf('？'))
        );

        // 如果找到了合适的截断点且位置合理（不要太靠前）
        if (lastPunctuation > 150) {
            return truncated.substring(0, lastPunctuation) + "...";
        }

        return truncated + "...";
    }

    // 新增：生成内容摘要的方法
    private String generateContentSummary(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "";
        }

        String trimmedContent = content.trim();
        String cleanedContent = trimmedContent.replaceAll("\\s+", " ");

        if (cleanedContent.length() <= 200) {
            return cleanedContent;
        }

        String truncated = cleanedContent.substring(0, 200);

        // 查找最后一个合适的截断点
        int lastPunctuation = Math.max(
                Math.max(truncated.lastIndexOf(' '), truncated.lastIndexOf('。')),
                Math.max(truncated.lastIndexOf('！'), truncated.lastIndexOf('？'))
        );

        if (lastPunctuation > 150) {
            return truncated.substring(0, lastPunctuation) + "...";
        }

        return truncated + "...";
    }

    /**
     * 转换为TagSimpleVO
     */
    private TagSimpleVO convertToTagSimpleVO(Tags tag) {
        TagSimpleVO vo = new TagSimpleVO();
        vo.setId(String.valueOf(tag.getId()));
        vo.setName(tag.getName());

        // 不设置时间字段，因为：
        // 1. TagSimpleVO是简化版本，主要用于显示标签基本信息
        // 2. 小记的时间信息已经在NoteDetailVO的根级别提供
        // 3. 如果需要在标签级别显示小记时间，考虑使用专门的VO类

        return vo;
    }

    /**
     * 内部方法：设置小记标签关联
     */
    private void setNoteTagsInternal(Long noteId, List<String> tagIds) {
        // 如果提供了标签ID，验证标签是否属于当前用户
        if (tagIds != null && !tagIds.isEmpty()) {
            List<Long> longTagIds = tagIds.stream()
                    .filter(Objects::nonNull)
                    .map(Long::valueOf)
                    .collect(Collectors.toList());

            if (!longTagIds.isEmpty()) {
                Long userId = CurrentHolder.getCurrentUserId();
                long tagCount = tagService.count(
                        new LambdaQueryWrapper<Tags>()
                                .eq(Tags::getUserId, userId)
                                .in(Tags::getId, longTagIds)
                );

                if (tagCount != longTagIds.size()) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "存在无效的标签ID");
                }
            }
        }

        // 获取当前标签关联
        List<NoteTagMap> currentRelations = noteTagMapService.list(
                new LambdaQueryWrapper<NoteTagMap>()
                        .eq(NoteTagMap::getNoteId, noteId)
        );

        Set<Long> currentTagIds = currentRelations.stream()
                .map(NoteTagMap::getTagId)
                .collect(Collectors.toSet());

        Set<Long> newTagIdSet = tagIds != null ?
                tagIds.stream()
                        .filter(Objects::nonNull)
                        .map(Long::valueOf)
                        .collect(Collectors.toSet()) :
                new HashSet<>();

        // 计算需要添加和删除的标签
        Set<Long> toAdd = new HashSet<>(newTagIdSet);
        toAdd.removeAll(currentTagIds);

        Set<Long> toRemove = new HashSet<>(currentTagIds);
        toRemove.removeAll(newTagIdSet);

        // 删除不需要的关联
        if (!toRemove.isEmpty()) {
            noteTagMapService.remove(
                    new LambdaQueryWrapper<NoteTagMap>()
                            .eq(NoteTagMap::getNoteId, noteId)
                            .in(NoteTagMap::getTagId, toRemove)
            );

            // 更新标签引用计数
            for (Long tagId : toRemove) {
                tagService.update(
                        new LambdaUpdateWrapper<Tags>()
                                .eq(Tags::getId, tagId)
                                .setSql("refered_count = GREATEST(refered_count - 1, 0)")
                );
            }
        }

        // 添加新的关联
        if (!toAdd.isEmpty()) {
            List<NoteTagMap> newRelations = toAdd.stream()
                    .map(tagId -> {
                        NoteTagMap relation = new NoteTagMap();
                        relation.setNoteId(noteId);
                        relation.setTagId(tagId);
                        return relation;
                    })
                    .collect(Collectors.toList());

            boolean relationsAdded = noteTagMapService.saveBatch(newRelations);
            if (!relationsAdded) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加标签关联失败");
            }

            // 更新标签引用计数
            for (Long tagId : toAdd) {
                tagService.update(
                        new LambdaUpdateWrapper<Tags>()
                                .eq(Tags::getId, tagId)
                                .setSql("refered_count = refered_count + 1")
                );
            }
        }

        log.info("设置小记标签成功，noteId: {}, 添加: {}, 删除: {}",
                noteId, toAdd, toRemove);
    }
}