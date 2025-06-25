package com.pickyboy.yuquebackend.service;

import java.util.List;

/**
 * 小记服务接口
 *
 * @author pickyboy
 */
public interface INoteService {

    /**
     * 获取小记列表
     *
     * @return 小记列表
     */
    List<?> getNotes();

    /**
     * 创建小记
     *
     * @param createRequest 创建请求
     * @return 小记信息
     */
    Object createNote(Object createRequest);

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
    Object getNoteDetail(Long noteId);

    /**
     * 更新小记
     *
     * @param noteId 小记ID
     * @param updateRequest 更新请求
     * @return 更新后的小记
     */
    Object updateNote(Long noteId, Object updateRequest);

    /**
     * 删除小记 (逻辑删除)
     *
     * @param noteId 小记ID
     */
    void deleteNote(Long noteId);

    /**
     * 为小记打上标签
     *
     * @param noteId 小记ID
     * @param tagRequest 标签请求
     */
    void addNoteTag(Long noteId, Object tagRequest);

    /**
     * 移除小记的某个标签
     *
     * @param noteId 小记ID
     * @param tagId 标签ID
     */
    void removeNoteTag(Long noteId, Long tagId);
}