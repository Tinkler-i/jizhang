package com.billmanager.jizhang.service.impl;

import com.billmanager.jizhang.dto.LoginRequest;
import com.billmanager.jizhang.entity.User;
import com.billmanager.jizhang.exception.BusinessException;
import com.billmanager.jizhang.mapper.UserMapper;
import com.billmanager.jizhang.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public User login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        
        return user;
    }
    
    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }
    
    @Override
    public User findByPhone(String phone) {
        return userMapper.findByPhone(phone);
    }
    
    @Override
    public User findById(Long id) {
        return userMapper.findById(id);
    }
}