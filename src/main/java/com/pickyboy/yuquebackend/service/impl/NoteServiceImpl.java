package com.pickyboy.yuquebackend.service.impl;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pickyboy.yuquebackend.common.exception.BusinessException;
import com.pickyboy.yuquebackend.common.exception.ErrorCode;
import com.pickyboy.yuquebackend.common.response.PageResult;
import com.pickyboy.yuquebackend.common.utils.CurrentHolder;
import com.pickyboy.yuquebackend.domain.dto.note.*;
import com.pickyboy.yuquebackend.domain.entity.NoteTagMap;
import com.pickyboy.yuquebackend.domain.entity.Tags;
import com.pickyboy.yuquebackend.domain.vo.note.NoteDetailVO;
import com.pickyboy.yuquebackend.domain.vo.note.NoteListVO;
import com.pickyboy.yuquebackend.domain.vo.tag.TagSimpleVO;
import com.pickyboy.yuquebackend.domain.vo.tag.TagVO;
import com.pickyboy.yuquebackend.service.INoteTagMapService;
import com.pickyboy.yuquebackend.service.ITagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pickyboy.yuquebackend.domain.entity.Notes;
import com.pickyboy.yuquebackend.mapper.NotesMapper;
import com.pickyboy.yuquebackend.service.INoteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 小记服务实现类
 *
 * @author pickyboy
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
    public PageResult<NoteListVO> getNoteList(QueryNotesRequest queryNotesRequest) {
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        Page<Notes> page = new Page<>(queryNotesRequest.getPageNum(), queryNotesRequest.getPageSize());

        LambdaQueryWrapper<Notes> wrapper = new LambdaQueryWrapper<Notes>()
                .eq(Notes::getUserId, userId)
                .orderByDesc(Notes::getUpdatedAt);

        // 关键词搜索
        if (StringUtils.hasText(queryNotesRequest.getKeyword())) {
            wrapper.like(Notes::getContent, queryNotesRequest.getKeyword());
        }

        Page<Notes> notePage = page(page, wrapper);

        // 转换为VO并获取标签信息
        List<NoteListVO> voList = notePage.getRecords().stream()
                .map(this::convertToNoteListVO)
                .collect(Collectors.toList());

        // 如果有标签筛选，需要进一步过滤
        if (queryNotesRequest.getTagIds() != null && queryNotesRequest.getTagIds().length > 0) {
            voList = filterByTags(voList, queryNotesRequest.getTagIds());
        }

        return new PageResult<>(notePage.getTotal(), voList);
    }

    @Override
    @Transactional
    public NoteDetailVO createNote(CreateNoteRequest createNoteRequest) {
        // TODO: 测试创建小记逻辑
        //log.info("创建新的小记: title={}", createNoteRequest.getTitle());

        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // 创建小记
        Notes note = new Notes();
        note.setUserId(userId);
        note.setContent(createNoteRequest.getContent());

        boolean saved = save(note);
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "小记创建失败");
        }

        return convertToNoteDetailVO(note);
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
    public NoteDetailVO updateNote(UpdateNoteRequest updateNoteRequest) {
        // TODO: 测试更新小记逻辑
        Long userId = CurrentHolder.getCurrentUserId();
        Long noteId = updateNoteRequest.getNoteId();

        log.info("更新小记: noteId={}", noteId);

        // 验证小记是否属于当前用户
        Notes note = getOne(
                new LambdaQueryWrapper<Notes>()
                        .eq(Notes::getId, noteId)
                        .eq(Notes::getUserId, userId)
        );

        if (note == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "小记不存在");
        }

        // 更新小记
        note.setContent(updateNoteRequest.getContent());

        boolean updated = updateById(note);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "小记更新失败");
        }

        return convertToNoteDetailVO(note);

    }

    @Override
    public Boolean deleteNotes(DeleteNotesRequest deleteNotesRequest) {
        // TODO: 测试删除小记逻辑
        Long userId = CurrentHolder.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        Long[] noteIds = deleteNotesRequest.getNoteIds();

        // 验证小记是否属于当前用户
        long count = count(
                new LambdaQueryWrapper<Notes>()
                        .eq(Notes::getUserId, userId)
                        .in(Notes::getId, Arrays.asList(noteIds))
        );

        if (count != noteIds.length) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "存在无效的小记ID");
        }

        // 获取要删除的小记关联的标签，用于更新标签引用计数
        List<NoteTagMap> relationsToDelete = noteTagMapService.list(
                new LambdaQueryWrapper<NoteTagMap>()
                        .in(NoteTagMap::getNoteId, Arrays.asList(noteIds))
        );

        // 统计每个标签的引用次数减少量
        Map<Long, Long> tagDecrementMap = relationsToDelete.stream()
                .collect(Collectors.groupingBy(
                        NoteTagMap::getTagId,
                        Collectors.counting()
                ));

        // 删除小记（逻辑删除）
        boolean notesDeleted = removeByIds(Arrays.asList(noteIds));

        // 删除小记-标签关联关系（逻辑删除）
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

        log.info("批量删除小记成功，数量: {}, IDs: {}", noteIds.length, Arrays.toString(noteIds));

        return true;
    }

    @Override
    @Transactional
    public Boolean setNoteTags(SetNoteTagsRequest setNoteTagsRequest) {
        // TODO:测试获取小记的标签列表逻辑
        Long userId = CurrentHolder.getCurrentUserId();
        Long noteId = setNoteTagsRequest.getNoteId();
        Long[] newTagIds = setNoteTagsRequest.getTagIds();

        // 验证小记是否属于当前用户
        Notes note = getOne(
                new LambdaQueryWrapper<Notes>()
                        .eq(Notes::getId, noteId)
                        .eq(Notes::getUserId, userId)
        );

        if (note == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "小记不存在");
        }

        // 如果提供了标签ID，验证标签是否属于当前用户
        if (newTagIds != null && newTagIds.length > 0) {
            List<Long> validTagIds = Arrays.stream(newTagIds)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!validTagIds.isEmpty()) {
                long tagCount = tagService.count(
                        new LambdaQueryWrapper<Tags>()
                                .eq(Tags::getUserId, userId)
                                .in(Tags::getId, validTagIds)
                );

                if (tagCount != validTagIds.size()) {
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

        Set<Long> newTagIdSet = newTagIds != null ?
                Arrays.stream(newTagIds)
                        .filter(Objects::nonNull)
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

        return true;
    }

    @Override
    public List<TagSimpleVO> getNoteTags(Long noteId){
        // TODO: 测试为小记添加标签
        log.info("为小记添加标签: noteId={}", noteId);

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

    private NoteDetailVO convertToNoteDetailVO(Notes note) {
        NoteDetailVO vo = new NoteDetailVO();
        vo.setId(String.valueOf(note.getId()));
        vo.setTitle(generateTitleFromContent(note.getContent())); // 生成title
        vo.setContent(note.getContent());
        vo.setCreatedAt(note.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        vo.setUpdatedAt(note.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 获取关联的标签
        List<TagSimpleVO> tags = getNoteTags(note.getId());
        vo.setTags(tags.toArray(new TagSimpleVO[0]));

        return vo;
    }

    private NoteListVO convertToNoteListVO(Notes note) {
        NoteListVO vo = new NoteListVO();
        vo.setId(String.valueOf(note.getId()));
        vo.setTitle(generateTitleFromContent(note.getContent())); // 生成title

        // 截取前200字符作为摘要
        String content = note.getContent();
        if (content != null && content.length() > 200) {
            content = content.substring(0, 200) + "...";
        }
        vo.setContent(content);
        vo.setCreatedAt(note.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        vo.setUpdatedAt(note.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 获取关联的标签
        List<TagSimpleVO> tags = getNoteTags(note.getId());
        vo.setTags(tags.toArray(new TagSimpleVO[0]));

        return vo;
    }

    /**
     * 从content生成title（前200字符）
     */
    private String generateTitleFromContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "";
        }

        String trimmedContent = content.trim();
        if (trimmedContent.length() <= 200) {
            return trimmedContent;
        }

        return trimmedContent.substring(0, 200);
    }

    /**
     * 转换为TagSimpleVO
     */
    private TagSimpleVO convertToTagSimpleVO(Tags tag) {
        TagSimpleVO vo = new TagSimpleVO();
        vo.setId(String.valueOf(tag.getId()));
        vo.setName(tag.getName());
        vo.setCreatedAt(tag.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return vo;
    }

    /**
     * 根据标签筛选小记列表
     */
    private List<NoteListVO> filterByTags(List<NoteListVO> noteList, Long[] tagIds) {
        if (tagIds == null || tagIds.length == 0) {
            return noteList;
        }

        Set<Long> filterTagIds = Arrays.stream(tagIds)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (filterTagIds.isEmpty()) {
            return noteList;
        }

        return noteList.stream()
                .filter(note -> {
                    Set<Long> noteTagIds = Arrays.stream(note.getTags())
                            .map(tag -> Long.valueOf(tag.getId()))
                            .collect(Collectors.toSet());

                    // 检查是否包含任一筛选标签
                    return !Collections.disjoint(noteTagIds, filterTagIds);
                })
                .collect(Collectors.toList());
    }
}