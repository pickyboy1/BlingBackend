package com.pickyboy.blingBackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pickyboy.blingBackend.domain.dto.note.CreateNoteRequest;
import com.pickyboy.blingBackend.domain.dto.note.DeleteNotesRequest;
import com.pickyboy.blingBackend.domain.dto.note.SetNoteTagsRequest;
import com.pickyboy.blingBackend.domain.dto.note.UpdateNoteRequest;
import com.pickyboy.blingBackend.common.response.PageResult;
import com.pickyboy.blingBackend.domain.entity.Notes;
import com.pickyboy.blingBackend.domain.vo.note.NoteDetailVO;
import com.pickyboy.blingBackend.domain.vo.note.NoteListVO;
import com.pickyboy.blingBackend.domain.vo.tag.TagSimpleVO;

import java.util.List;

/**
 * 小记服务接口
 *
 * @author shiqi
 */
public interface INoteService extends IService<Notes> {

    /**
     * 获取小记列表
     *
     * @param tagId 标签ID（可选）
     * @param keyword 搜索关键词（可选）
     * @param page 页码
     * @param limit 每页数量
     * @param sortBy 排序字段
     * @param order 排序方式
     * @return 小记列表
     */
    List<NoteListVO> getNoteList(Long tagId, String keyword, Integer page, Integer limit, String sortBy, String order);

    /**
     * 创建小记
     *
     * @param createNoteRequest 创建请求
     * @return 小记信息
     */
    NoteDetailVO createNote(CreateNoteRequest createNoteRequest);

    /**
     * 搜索小记 (只搜索title,直接用数据库模糊搜索)
     *
     * @param keyword 搜索关键词
     * @return 搜索结果
     */
    List<NoteListVO> searchNotes(String keyword);

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
     * @param noteId 小记ID
     * @param updateNoteRequest 更新请求
     */
    void updateNote(Long noteId, UpdateNoteRequest updateNoteRequest);

    /**
     * 批量删除小记 (逻辑删除)
     *
     * @param deleteNotesRequest 删除请求
     */
    void deleteNotes(DeleteNotesRequest deleteNotesRequest);

    /**
     * 获取小记的标签列表
     *
     * @param noteId 小记ID
     * @return 标签列表
     */
    List<TagSimpleVO> getNoteTags(Long noteId);

    /**
     * 设置小记的标签
     *
     * @param noteId 小记ID
     * @param setNoteTagsRequest 设置小记标签请求
     */
    void setNoteTags(Long noteId, SetNoteTagsRequest setNoteTagsRequest);


}