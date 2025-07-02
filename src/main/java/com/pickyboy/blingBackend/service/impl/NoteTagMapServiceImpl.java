package com.pickyboy.blingBackend.service.impl;

import com.pickyboy.blingBackend.domain.entity.NoteTagMap;
import com.pickyboy.blingBackend.mapper.NoteTagMapMapper;
import com.pickyboy.blingBackend.service.INoteTagMapService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 小记与标签的关联表 服务实现类
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
@Service
public class NoteTagMapServiceImpl extends ServiceImpl<NoteTagMapMapper, NoteTagMap> implements INoteTagMapService {

}
