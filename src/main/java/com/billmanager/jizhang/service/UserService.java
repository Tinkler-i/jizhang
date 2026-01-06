package com.billmanager.jizhang.service;

import com.billmanager.jizhang.dto.LoginRequest;
import com.billmanager.jizhang.entity.User;

public interface UserService {
    
    User login(LoginRequest request);
    
    User findByUsername(String username);
    
    User findByPhone(String phone);
    
    User findById(Long id);
}