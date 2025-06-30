package com.pickyboy.yuquebackend.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pickyboy.yuquebackend.domain.dto.resource.CopyResourceRequest;
import com.pickyboy.yuquebackend.domain.dto.resource.CreateResourceRequest;
import com.pickyboy.yuquebackend.domain.dto.resource.MoveResourceRequest;
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
     * 在知识库中新建资源
     *
     * @param kbId 知识库ID
     * @param createRequest 创建请求
     * @return 资源信息
     */
    Resources createResource(Long kbId, CreateResourceRequest createRequest);

    /**
     * 查看单个资源的完整信息
     *
     * @param resId 资源ID
     * @return 资源内容
     */
    Resources getResourceById(Long resId);

    /**
     * 更新资源内容或标题
     *
     * @param resId 资源ID
     * @param updateRequest 更新请求
     */
    void updateResource(Long resId, UpdateResourceContentRequest updateRequest);

    /**
     * 删除资源 (逻辑删除)
     *
     * @param resId 资源ID
     */
    void deleteResource(Long resId);

    /**
     * 重命名资源
     *
     * @param resId 资源ID
     * @param infoRequest 信息更新请求
     */
    void renameResource(Long resId, UpdateResourceInfoRequest infoRequest);

    /**
     * 更新资源可见性
     *
     * @param resId 资源ID
     * @param visibilityRequest 可见性更新请求
     */
    void updateResourceVisibility(Long resId, Object visibilityRequest);

    /**
     * 更新资源上架/下架状态
     *
     * @param resId 资源ID
     * @param statusRequest 状态更新请求
     */
    void updateResourceStatus(Long resId, Object statusRequest);

    /**
     * 恢复资源
     *
     * @param resId 资源ID
     */
    void restoreResource(Long resId);

    /**
     * 彻底删除资源
     *
     * @param resId 资源ID
     */
    void permanentlyDeleteResource(Long resId);

    /**
     * 移动资源或目录
     *
     * @param resId 资源ID
     * @param moveRequest 移动请求
     */
    void moveResource(Long resId, MoveResourceRequest moveRequest);

    /**
     * 复制资源
     *
     * @param resId 资源ID
     * @param copyRequest 复制请求
     */
    void copyResource(Long resId, CopyResourceRequest copyRequest);

    /**
     * 复制目录(及目录下所有子资源)
     *
     * @param resId 资源ID
     * @param copyRequest 复制请求
     */
    void copyResourceTree(Long resId, CopyResourceRequest copyRequest);

    /**
     * 生成资源分享链接
     *
     * @param resId 资源ID
     * @return 分享链接信息
     */
    ShareUrlVO generateResourceShareLink(Long resId);

    /**
     * 访问分享链接查看资源
     *
     * @param kbShareId 知识库分享ID
     * @param resShareId 资源分享ID
     * @return 公开资源内容
     */
    PublicResourceVO accessSharedResource(String kbShareId, String resShareId);

    /**
     * 点赞文章
     *
     * @param articleId 文章ID
     */
    void likeArticle(Long articleId);

    /**
     * 取消点赞文章
     *
     * @param articleId 文章ID
     */
    void unlikeArticle(Long articleId);

    /**
     * 获取文章的评论列表
     *
     * @param articleId 文章ID
     * @return 评论列表
     */
    List<?> listArticleComments(Long articleId);

    /**
     * 发表评论
     *
     * @param articleId 文章ID
     * @param commentRequest 评论请求
     * @return 评论信息
     */
    Object createComment(Long articleId, Object commentRequest);

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     */
    void deleteComment(Long commentId);

    /**
     * 提交投稿（申请推荐）
     *
     * @param submissionRequest 投稿请求
     */
    void createSubmission(Object submissionRequest);

    /**
     * 获取推荐文章列表
     *
     * @return 推荐文章列表
     */
    List<PublicResourceVO> listExploreArticles();

    /**
     * 获取知识库下的资源目录树
     *
     * @param kbId 知识库ID
     * @return 资源目录树
     */
    List<?> getResourceTree(Long kbId);
}