package com.pickyboy.blingBackend.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pickyboy.blingBackend.domain.entity.Notes;
import com.pickyboy.blingBackend.mapper.NotesMapper;
import com.pickyboy.blingBackend.service.INoteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 小记服务实现类
 *
 * @author pickyboy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl extends ServiceImpl<NotesMapper, Notes> implements INoteService {

    @Override
    public List<?> getNotes() {
        // TODO: 实现获取小记列表逻辑
        log.info("获取小记列表");
        throw new UnsupportedOperationException("待实现");
    }

    @Override
    public Object createNote(Object createRequest) {
        // TODO: 实现创建小记逻辑
        log.info("创建小记");
        throw new UnsupportedOperationException("待实现");
    }

    @Override
    public List<?> searchNotes(String keyword) {
        // TODO: 实现搜索小记逻辑
        log.info("搜索小记: keyword={}", keyword);
        throw new UnsupportedOperationException("待实现");
    }

    @Override
    public Object getNoteDetail(Long noteId) {
        // TODO: 实现获取小记详情逻辑
        log.info("获取小记详情: noteId={}", noteId);
        throw new UnsupportedOperationException("待实现");
    }

    @Override
    public Object updateNote(Long noteId, Object updateRequest) {
        // TODO: 实现更新小记逻辑
        log.info("更新小记: noteId={}", noteId);
        throw new UnsupportedOperationException("待实现");
    }

    @Override
    public void deleteNote(Long noteId) {
        // TODO: 实现删除小记逻辑
        log.info("删除小记: noteId={}", noteId);
        throw new UnsupportedOperationException("待实现");
    }

    @Override
    public void addNoteTag(Long noteId, Object tagRequest) {
        // TODO: 实现为小记添加标签逻辑
        log.info("为小记添加标签: noteId={}", noteId);
        throw new UnsupportedOperationException("待实现");
    }

    @Override
    public void removeNoteTag(Long noteId, Long tagId) {
        // TODO: 实现移除小记标签逻辑
        log.info("移除小记标签: noteId={}, tagId={}", noteId, tagId);
        throw new UnsupportedOperationException("待实现");
    }
}