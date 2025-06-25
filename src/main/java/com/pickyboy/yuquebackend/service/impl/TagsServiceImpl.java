package com.pickyboy.yuquebackend.service.impl;

import com.pickyboy.yuquebackend.domain.entity.Tags;
import com.pickyboy.yuquebackend.mapper.TagsMapper;
import com.pickyboy.yuquebackend.service.ITagsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 标签表 服务实现类
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
@Service
public class TagsServiceImpl extends ServiceImpl<TagsMapper, Tags> implements ITagsService {

}
