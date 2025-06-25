package com.pickyboy.yuquebackend.service.impl;

import com.pickyboy.yuquebackend.domain.entity.Notes;
import com.pickyboy.yuquebackend.mapper.NotesMapper;
import com.pickyboy.yuquebackend.service.INotesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 小记表 服务实现类
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
@Service
public class NotesServiceImpl extends ServiceImpl<NotesMapper, Notes> implements INotesService {

}
