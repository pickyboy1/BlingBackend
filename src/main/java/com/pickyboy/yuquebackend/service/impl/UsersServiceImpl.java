package com.pickyboy.yuquebackend.service.impl;

import com.pickyboy.yuquebackend.domain.entity.Users;
import com.pickyboy.yuquebackend.mapper.UsersMapper;
import com.pickyboy.yuquebackend.service.IUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author pickyboy
 * @since 2025-06-25
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements IUsersService {

}
