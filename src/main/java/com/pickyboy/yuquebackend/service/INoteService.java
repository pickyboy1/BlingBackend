package com.pickyboy.yuquebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pickyboy.yuquebackend.common.response.PageResult;
import com.pickyboy.yuquebackend.domain.dto.note.*;
import com.pickyboy.yuquebackend.domain.entity.Notes;
import com.pickyboy.yuquebackend.domain.vo.note.NoteDetailVO;
import com.pickyboy.yuquebackend.domain.vo.note.NoteListVO;
import com.pickyboy.yuquebackend.domain.vo.tag.TagSimpleVO;
import com.pickyboy.yuquebackend.domain.vo.tag.TagVO;

import java.util.List;

/**
 * 小记服务接口
 *
 * @author pickyboy
 */
public interface INoteService extends IService<Notes> {

    /**
     * 获取小记列表
     *
     * @return 小记列表
     */
    PageResult<NoteListVO> getNoteList(QueryNotesRequest queryNotesRequest);

    /**
     * 创建小记
     *
     * @param createNoteRequest 创建请求
     * @return 小记信息
     */
    NoteDetailVO createNote(CreateNoteRequest createNoteRequest);

    /**
     * 搜索小记 (ES实现)
     *
     * @param keyword 搜索关键词
     * @return 搜索结果
     */
    List<?> searchNotes(String keyword);

    /**
     * 获取指定小记的详情
     *
     * @param noteId 小记ID
     * @return 小记详情
     */
    NoteDetailVO getNoteDetail(Long noteId);

    /**
     * 更新小记
     *
     * @param updateNoteRequest 更新请求
     * @return 更新后的小记
     */
    NoteDetailVO updateNote(UpdateNoteRequest updateNoteRequest);

    /**
     * 批量删除小记 (逻辑删除)
     *
     * @param deleteNotesRequest 删除请求
     */
    Boolean deleteNotes(DeleteNotesRequest deleteNotesRequest);

    /**
     * 获取小记的标签列表
     *
     * @param noteId 小记ID
     */
    List<TagSimpleVO> getNoteTags(Long noteId);

    /**
     * 设置小记的标签
     *
     * @param setNoteTagsRequest 设置小记标签请求
     */
    Boolean setNoteTags(SetNoteTagsRequest setNoteTagsRequest);

    /**
     * 移除小记的某个标签
     *
     * @param noteId 小记ID
     * @param tagId 标签ID
     */
    void removeNoteTag(Long noteId, Long tagId);
}