package com.pickyboy.yuquebackend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pickyboy.yuquebackend.common.response.Result;
import com.pickyboy.yuquebackend.domain.dto.InsertKnowledgeBaseRequest;
import com.pickyboy.yuquebackend.domain.entity.KnowledgeBases;
import com.pickyboy.yuquebackend.domain.entity.Resources;
import com.pickyboy.yuquebackend.domain.vo.KbsWithRecentResourceVo;
import com.pickyboy.yuquebackend.domain.vo.ResourceTreeVo;
import com.pickyboy.yuquebackend.domain.vo.TrashVO;
import com.pickyboy.yuquebackend.service.IKnowledgeBaseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 知识库控制器
 * 处理知识库相关的API请求
 *
 * @author pickyboy
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final IKnowledgeBaseService knowledgeBaseService;

    /**
     * 获取当前用户的知识库列表
     * GET /knowledge-bases
     *
     * @return 知识库列表
     */
    @GetMapping("/knowledge-bases")
    public Result<List<KbsWithRecentResourceVo>> getUserKnowledgeBases(@PathVariable boolean withRecentResources) {
        log.info("获取当前用户的知识库列表");
        List<KbsWithRecentResourceVo> knowledgeBases = knowledgeBaseService.getUserKnowledgeBases(withRecentResources);
        return Result.success(knowledgeBases);
    }

    /**
     * 创建新的知识库
     * POST /knowledge-bases
     *
     * @param createRequest 创建请求
     * @return 创建的知识库信息
     */
    @PostMapping("/knowledge-bases")
    public Result<KnowledgeBases> createKnowledgeBase(@RequestBody InsertKnowledgeBaseRequest createRequest) {
        log.info("创建新的知识库: name={}", createRequest.getName());
        boolean success = knowledgeBaseService.createKnowledgeBase(createRequest);
        if(!success){
            return Result.error("创建知识库失败");
        }
        return Result.success();
    }

    /**
     * 获取指定知识库的详细信息
     * GET /knowledge-bases/{kbId}
     *
     * @param kbId 知识库ID
     * @return 知识库详细信息
     */
    @GetMapping("/knowledge-bases/{kbId}")
    public Result<KnowledgeBases> getKnowledgeBase(@PathVariable Long kbId) {
        log.info("获取知识库详细信息: kbId={}", kbId);
        KnowledgeBases knowledgeBase = knowledgeBaseService.getKnowledgeBase(kbId);
        return Result.success(knowledgeBase);
    }

    /**
     * 获取指定知识库下文档树
     * GET /knowledge-bases/{kbId}/documents
     *
     * @param kbId 知识库ID
     * @return 知识库下文档树
     */
    @GetMapping("/knowledge-bases/{kbId}/documents")
    public Result<List<ResourceTreeVo>> getKnowledgeBaseWithDocuments(@PathVariable Long kbId) {
        log.info("获取知识库详细信息及其文档树: kbId={}", kbId);
        List<ResourceTreeVo> knowledgeBaseWithDocuments = knowledgeBaseService.getKnowledgeBaseWithDocuments(kbId);
        return Result.success(knowledgeBaseWithDocuments);
    }

    /**
     * 更新知识库信息
     * PUT /knowledge-bases/{kbId}
     *
     * @param kbId 知识库ID
     * @param updateRequest 更新请求
     * @return 更新后的知识库信息
     */
    @PutMapping("/knowledge-bases/{kbId}")
    public Result<Void> updateKnowledgeBase(@PathVariable Long kbId,
                                                       @RequestBody InsertKnowledgeBaseRequest updateRequest) {
        log.info("更新知识库信息: kbId={}", kbId);
        boolean success = knowledgeBaseService.updateKnowledgeBase(kbId, updateRequest);
        if(!success){
            return Result.error("更新知识库失败");
        }
        return Result.success();
    }

    /**
     * 删除知识库 (逻辑删除)
     * DELETE /knowledge-bases/{kbId}
     *
     * @param kbId 知识库ID
     * @return 操作结果
     */
    @DeleteMapping("/knowledge-bases/{kbId}")
    public Result<Void> deleteKnowledgeBase(@PathVariable Long kbId) {
        log.info("删除知识库: kbId={}", kbId);
        boolean success = knowledgeBaseService.deleteKnowledgeBase(kbId);
        if(!success){
            return Result.error("删除知识库失败,知识库不存在");
        }
        return Result.success();
    }

    /**
     * 从回收站恢复知识库
     * POST /knowledge-bases/{kbId}/restore
     *
     * @param kbId 知识库ID
     * @return 操作结果
     */
    @PostMapping("/knowledge-bases/{kbId}/restore")
    public Result<Void> restoreKnowledgeBase(@PathVariable Long kbId) {
        log.info("从回收站恢复知识库: kbId={}", kbId);
        knowledgeBaseService.restoreKnowledgeBase(kbId);
        return Result.success();
    }

    /**
     * 获取知识库下最近编辑的文档
     * GET /knowledge-bases/{kbId}/recent-documents
     *
     * @param kbId 知识库ID
     * @return 最近编辑的文档列表
     */
    @GetMapping("/knowledge-bases/{kbId}/recent-documents")
    public Result<List<Resources>> getRecentDocuments(@PathVariable Long kbId) {
        log.info("获取知识库下最近编辑的文档: kbId={}", kbId);
        List<Resources> documents = knowledgeBaseService.getRecentDocuments(kbId);
        return Result.success(documents);
    }

    /**
     * 获取回收站内容列表
     * GET /trash
     *
     * @return 回收站内容
     */
    @GetMapping("/trash")
    public Result<TrashVO> getTrashContent() {
        log.info("获取回收站内容列表");
        TrashVO trash = knowledgeBaseService.getTrashContent();
        return Result.success(trash);
    }
}