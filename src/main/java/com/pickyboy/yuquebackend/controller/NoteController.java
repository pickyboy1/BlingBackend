package com.pickyboy.yuquebackend.controller;

import com.pickyboy.yuquebackend.common.response.PageResult;
import com.pickyboy.yuquebackend.common.response.Result;
import com.pickyboy.yuquebackend.domain.dto.note.*;
import com.pickyboy.yuquebackend.domain.vo.note.NoteDetailVO;
import com.pickyboy.yuquebackend.domain.vo.note.NoteListVO;
import com.pickyboy.yuquebackend.domain.vo.tag.TagSimpleVO;
import com.pickyboy.yuquebackend.domain.vo.tag.TagVO;
import com.pickyboy.yuquebackend.service.INoteService;
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
     * @param queryNotesRequest 查询请求
     * @return 小记列表
     */
    @GetMapping("/notes")
    public Result<PageResult<NoteListVO>> getNoteList(@Valid @RequestBody QueryNotesRequest queryNotesRequest) {
        PageResult<NoteListVO> result = noteService.getNoteList(queryNotesRequest);
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
     * @param deleteNotesRequest 创建请求
     * @return 操作结果
     */
    @DeleteMapping("/notes")
    public Result<Boolean> deleteNotes(@Valid @RequestBody DeleteNotesRequest deleteNotesRequest) {
        Boolean result = noteService.deleteNotes(deleteNotesRequest);
        return Result.success(result);
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
     * PATCH /notes
     *
     * @param updateNoteRequest 更新请求
     * @return 更新后的小记
     */
    @PatchMapping("/notes")
    public Result<NoteDetailVO> updateNote(@Valid @RequestBody UpdateNoteRequest updateNoteRequest) {
        NoteDetailVO result = noteService.updateNote(updateNoteRequest);
        return Result.success(result);
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
     * PUT /notes/tags
     *
     * @param setNoteTagsRequest 设置小记标签请求
     * @return 操作结果
     */
    @PutMapping("/notes/tags")
    public Result<Boolean> setNoteTags(@Valid @RequestBody SetNoteTagsRequest setNoteTagsRequest) {
        Boolean result = noteService.setNoteTags(setNoteTagsRequest);
        return Result.success(result);
    }

}
