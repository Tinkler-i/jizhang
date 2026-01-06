package com.billmanager.jizhang.mapper;

import com.billmanager.jizhang.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    
    User findByUsername(@Param("username") String username);
    
    User findByPhone(@Param("phone") String phone);
    
    User findByEmail(@Param("email") String email);
    
    int insert(User user);
    
    int update(User user);
    
    User findById(@Param("id") Long id);
}