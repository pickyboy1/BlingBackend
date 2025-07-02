package com.pickyboy.yuquebackend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pickyboy.yuquebackend.domain.entity.Resources;

/**
 * <p>
 * 文档/资源表 Mapper 接口
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
public interface ResourcesMapper extends BaseMapper<Resources> {

    /**
     * 自定义SQL查询，使用JOIN获取回收站中的文档
     * @param userId 用户ID
     * @return 文档列表
     */
    List<Resources> selectDeletedResourcesInActiveKbs(@Param("userId") Long userId);

    /**
     * 查询资源并验证其知识库未被删除
     * 用于核心资源操作的安全查询
     * @param resourceId 资源ID
     * @return 资源对象，如果资源不存在或知识库被删除则返回null
     */
    Resources selectResourceInActiveKb(@Param("resourceId") Long resourceId);

    /**
     * 查询资源并验证其知识库未被删除（带用户权限验证）
     * 用于需要权限控制的资源操作
     * @param resourceId 资源ID
     * @param userId 用户ID
     * @return 资源对象，如果资源不存在或知识库被删除则返回null
     */
    Resources selectResourceInActiveKbWithUser(@Param("resourceId") Long resourceId, @Param("userId") Long userId);

    /**
     * 查询已删除的资源并验证其知识库未被删除
     * 用于恢复资源时的验证
     * @param resourceId 资源ID
     * @param userId 用户ID
     * @return 已删除的资源对象，如果资源不存在或知识库被删除则返回null
     */
    Resources selectDeletedResourceInActiveKb(@Param("resourceId") Long resourceId, @Param("userId") Long userId);



    /**
     * 通过评论ID查询对应的资源，并验证资源及其知识库状态
     * ⚠️ **注意**: 此方法不检查知识库可见性，仅验证存在性
     * 如需权限验证，建议先获取评论的resourceId，再使用selectResourceInActiveKbWithUser()
     * @param commentId 评论ID
     * @return 资源对象，如果资源不存在或知识库被删除则返回null
     */
    Resources selectResourceByCommentInActiveKb(@Param("commentId") Long commentId);

    /**
     * 【核心优化】递归查询指定父节点下的所有子孙节点（无论删除状态）
     * 使用WITH RECURSIVE一次性查询，按层级排序，确保完整性
     * @param parentId 父节点ID
     * @return 所有子孙节点列表（按层级排序，包含已删除和未删除节点）
     */
    List<Resources> selectAllDescendants(@Param("parentId") Long parentId);

    /**
     * 批量更新资源的知识库ID
     * 用于移动操作的性能优化
     * @param resourceIds 资源ID列表
     * @param newKbId 新知识库ID
     * @return 更新的记录数
     */
    int batchUpdateKnowledgeBaseId(@Param("resourceIds") List<Long> resourceIds, @Param("newKbId") Long newKbId);

    /**
     * 批量逻辑删除资源
     * 用于删除操作的性能优化
     * @param resourceIds 资源ID列表
     * @return 删除的记录数
     */
    int batchLogicalDelete(@Param("resourceIds") List<Long> resourceIds);

    // ====== 【原子操作】计数器更新方法 ======

    /**
     * 原子增加资源浏览量
     * @param resourceId 资源ID
     * @return 影响行数
     */
    int incrementViewCount(@Param("resourceId") Long resourceId);

    /**
     * 原子增加资源点赞数
     * @param resourceId 资源ID
     * @return 影响行数
     */
    int incrementLikeCount(@Param("resourceId") Long resourceId);

    /**
     * 原子减少资源点赞数
     * @param resourceId 资源ID
     * @return 影响行数
     */
    int decrementLikeCount(@Param("resourceId") Long resourceId);

    /**
     * 原子增加资源评论数
     * @param resourceId 资源ID
     * @return 影响行数
     */
    int incrementCommentCount(@Param("resourceId") Long resourceId);

    /**
     * 原子减少资源评论数
     * @param resourceId 资源ID
     * @return 影响行数
     */
    int decrementCommentCount(@Param("resourceId") Long resourceId);
}
