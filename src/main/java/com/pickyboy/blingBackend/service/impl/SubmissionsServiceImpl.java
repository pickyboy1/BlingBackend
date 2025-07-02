package com.pickyboy.blingBackend.service.impl;

import com.pickyboy.blingBackend.domain.entity.Submissions;
import com.pickyboy.blingBackend.mapper.SubmissionsMapper;
import com.pickyboy.blingBackend.service.ISubmissionsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 投稿审核表 服务实现类
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
@Service
public class SubmissionsServiceImpl extends ServiceImpl<SubmissionsMapper, Submissions> implements ISubmissionsService {

}
