package com.pickyboy.blingBackend.controller;


import com.pickyboy.blingBackend.common.response.Result;
import com.pickyboy.blingBackend.domain.dto.note.CreateNoteRequest;
import com.pickyboy.blingBackend.domain.dto.note.DeleteNotesRequest;
import com.pickyboy.blingBackend.domain.dto.note.SetNoteTagsRequest;
import com.pickyboy.blingBackend.domain.dto.note.UpdateNoteRequest;
import com.pickyboy.blingBackend.service.INoteService;
import com.pickyboy.blingBackend.domain.vo.note.NoteDetailVO;
import com.pickyboy.blingBackend.domain.vo.note.NoteListVO;
import com.pickyboy.blingBackend.domain.vo.tag.TagSimpleVO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 标签控制器
 * 处理标签相关的API请求
 *
 * @author shiqi
 */
@Slf4j
@RestController
@RequestMapping()
@RequiredArgsConstructor
public class NoteController {

    @Autowired
    private INoteService noteService;

    /**
     * 获取小记列表
     * GET /notes
     *
     * @param tagId 标签ID（可选）
     * @param keyword 搜索关键词（可选）
     * @param page 页码
     * @param limit 每页数量
     * @param sortBy 排序字段
     * @param order 排序方式
     * @return 小记列表
     */
    @GetMapping("/notes")
    public Result<List<NoteListVO>> getNoteList(
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order) {

        List<NoteListVO> result = noteService.getNoteList(tagId, keyword, page, limit, sortBy, order);
        return Result.success(result);
    }

    /**
     * 创建新的小记
     * POST /notes
     *
     * @param createNoteRequest 创建请求
     * @return 小记信息
     */
    @PostMapping("/notes")
    public Result<NoteDetailVO> createNote(@Valid @RequestBody CreateNoteRequest createNoteRequest) {
        NoteDetailVO result = noteService.createNote(createNoteRequest);
        return Result.success(result);
    }

    /**
     * 批量删除小记
     * DELETE /notes
     *
     * @param deleteNotesRequest 删除请求
     * @return 操作结果
     */
    @DeleteMapping("/notes")
    public Result<Void> deleteNotes(@Valid @RequestBody DeleteNotesRequest deleteNotesRequest) {
        noteService.deleteNotes(deleteNotesRequest);
        return Result.success(null);
    }

    /**
     * 获取单篇小记详情
     * GET /notes/{noteId}
     *
     * @param noteId 小记ID
     * @return 小记详情
     */
    @GetMapping("/notes/{noteId}")
    public Result<NoteDetailVO> getNoteDetail(@PathVariable Long noteId) {
        NoteDetailVO result = noteService.getNoteDetail(noteId);
        return Result.success(result);
    }

    /**
     * 编辑小记
     * PATCH /notes/{noteId}
     *
     * @param noteId 小记ID
     * @param updateNoteRequest 更新请求
     * @return 操作结果
     */
    @PatchMapping("/notes/{noteId}")
    public Result<Void> updateNote(
            @PathVariable Long noteId,
            @Valid @RequestBody UpdateNoteRequest updateNoteRequest) {
        noteService.updateNote(noteId, updateNoteRequest);
        return Result.success(null);
    }

    /**
     * 获取小记的标签列表
     * GET /notes/{noteId}/tags
     *
     * @param noteId 小记ID
     * @return 操作结果
     */
    @GetMapping("/notes/{noteId}/tags")
    public Result<List<TagSimpleVO>> getNoteTags(@PathVariable Long noteId) {
        List<TagSimpleVO> result = noteService.getNoteTags(noteId);
        return Result.success(result);
    }

    /**
     * 设置小记的标签
     * PUT /notes/{noteId}/tags
     *
     * @param noteId 小记ID
     * @param setNoteTagsRequest 设置小记标签请求
     * @return 操作结果
     */
    @PutMapping("/notes/{noteId}/tags")
    public Result<Void> setNoteTags(
            @PathVariable Long noteId,
            @Valid @RequestBody SetNoteTagsRequest setNoteTagsRequest) {
        noteService.setNoteTags(noteId, setNoteTagsRequest);
        return Result.success(null);
    }

}
