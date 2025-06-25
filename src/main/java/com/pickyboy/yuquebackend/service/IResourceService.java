package com.pickyboy.yuquebackend.service;

import java.util.List;

/**
 * 文档/资源服务接口
 *
 * @author pickyboy
 */
public interface IResourceService {

    /**
     * 创建新文档
     *
     * @param createRequest 创建请求
     * @return 文档信息
     */
    Object createDocument(Object createRequest);

    /**
     * 获取文档内容
     *
     * @param documentId 文档ID
     * @return 文档内容
     */
    Object getDocument(Long documentId);

    /**
     * 更新文档内容
     *
     * @param documentId 文档ID
     * @param updateRequest 更新请求
     * @return 更新后的文档
     */
    Object updateDocument(Long documentId, Object updateRequest);

    /**
     * 删除文档 (逻辑删除)
     *
     * @param documentId 文档ID
     */
    void deleteDocument(Long documentId);

    /**
     * 更新文档信息 (如重命名, 修改可见性)
     *
     * @param documentId 文档ID
     * @param infoRequest 信息更新请求
     * @return 更新后的文档信息
     */
    Object updateDocumentInfo(Long documentId, Object infoRequest);

    /**
     * 从回收站恢复文档
     *
     * @param documentId 文档ID
     */
    void restoreDocument(Long documentId);

    /**
     * 移动文档或目录
     *
     * @param documentId 文档ID
     * @param moveRequest 移动请求
     */
    void moveDocument(Long documentId, Object moveRequest);

    /**
     * 复制文档或目录 (递归)
     *
     * @param documentId 文档ID
     * @param copyRequest 复制请求
     * @return 复制后的文档
     */
    Object copyDocument(Long documentId, Object copyRequest);

    /**
     * 生成并获取文档分享链接
     *
     * @param documentId 文档ID
     * @return 分享链接信息
     */
    Object shareDocument(Long documentId);

    /**
     * 查看分享的文档
     *
     * @param shareId 分享ID
     * @return 公开文档内容
     */
    Object getSharedDocument(String shareId);

    /**
     * 点赞文章
     *
     * @param resourceId 文章ID
     */
    void likeResource(Long resourceId);

    /**
     * 取消点赞文章
     *
     * @param resourceId 文章ID
     */
    void unlikeResource(Long resourceId);

    /**
     * 获取文章的评论列表
     *
     * @param resourceId 文章ID
     * @return 评论列表
     */
    List<?> getResourceComments(Long resourceId);

    /**
     * 发表顶级评论
     *
     * @param resourceId 文章ID
     * @param commentRequest 评论请求
     * @return 评论信息
     */
    Object createComment(Long resourceId, Object commentRequest);
}