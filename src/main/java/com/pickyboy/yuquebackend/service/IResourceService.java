package com.pickyboy.yuquebackend.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pickyboy.yuquebackend.domain.dto.resource.CopyResourceRequest;
import com.pickyboy.yuquebackend.domain.dto.resource.CreateResourceRequest;
import com.pickyboy.yuquebackend.domain.dto.resource.MoveResourceRequest;
import com.pickyboy.yuquebackend.domain.dto.resource.RestoreResourceRequest;
import com.pickyboy.yuquebackend.domain.dto.resource.UpdateResourceContentRequest;
import com.pickyboy.yuquebackend.domain.dto.resource.UpdateResourceInfoRequest;
import com.pickyboy.yuquebackend.domain.entity.Resources;
import com.pickyboy.yuquebackend.domain.vo.resource.PublicResourceVO;
import com.pickyboy.yuquebackend.domain.vo.resource.ShareUrlVO;

/**
 * 文档/资源服务接口
 *
 * @author pickyboy
 */
public interface IResourceService extends IService<Resources> {

    /**
     * 创建新文档
     *
     * @param createRequest 创建请求
     * @return 文档信息
     */
    Resources createDocument(CreateResourceRequest createRequest);

    /**
     * 获取文档内容
     *
     * @param documentId 文档ID
     * @return 文档内容
     */
    Resources getDocument(Long documentId);

    /**
     * 更新文档内容
     *
     * @param documentId 文档ID
     * @param updateRequest 更新请求
     * @return 更新后的文档
     */
    Resources updateDocument(Long documentId, UpdateResourceContentRequest updateRequest);

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
     */
    void updateDocumentInfo(Long documentId, UpdateResourceInfoRequest infoRequest);

    /**
     * 从回收站恢复文档
     *
     * @param documentId 文档ID
     * @param restoreRequest 恢复请求
     */
    void restoreDocument(Long documentId, RestoreResourceRequest restoreRequest);

    /**
     * 移动文档或目录
     *
     * @param documentId 文档ID
     * @param moveRequest 移动请求
     */
    void moveDocument(Long documentId, MoveResourceRequest moveRequest);

    /**
     * 复制文档或目录 (递归)
     *
     * @param documentId 文档ID
     * @param copyRequest 复制请求
     */
    void copyDocument(Long documentId, CopyResourceRequest copyRequest);

    /**
     * 生成并获取文档分享链接
     *
     * @param documentId 文档ID
     * @return 分享链接信息
     */
    ShareUrlVO shareDocument(Long documentId);

    /**
     * 查看分享的文档
     *
     * @param shareId 分享ID
     * @return 公开文档内容
     */
    PublicResourceVO getSharedDocument(String shareId);

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