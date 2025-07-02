package com.pickyboy.blingBackend.service.impl;

import com.pickyboy.blingBackend.domain.entity.Likes;
import com.pickyboy.blingBackend.mapper.LikesMapper;
import com.pickyboy.blingBackend.service.ILikesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 点赞表 服务实现类
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
@Service
public class LikesServiceImpl extends ServiceImpl<LikesMapper, Likes> implements ILikesService {

}
