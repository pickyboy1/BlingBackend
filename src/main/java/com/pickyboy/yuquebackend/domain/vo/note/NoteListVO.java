package com.pickyboy.yuquebackend.domain.vo.note;

import com.pickyboy.yuquebackend.domain.vo.tag.TagSimpleVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 小记列表VO (使用TagSimpleVO)
 *
 * @author shiqi
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteListVO {
    private String id;
    private String title;     // 从content前200字符生成
    private String content;   // 截取前200字符作为摘要
    private String createdAt;
    private String updatedAt;
    private List<TagSimpleVO> tags; // 使用TagSimpleVO
}