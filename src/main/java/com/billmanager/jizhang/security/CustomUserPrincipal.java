package com.billmanager.jizhang.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.io.Serializable;

/**
 * 自定义用户Principal对象
 * 用于在Spring Security的Authentication中存储用户信息
 */
@Getter
@AllArgsConstructor
public class CustomUserPrincipal implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long userId;
    private String username;
    
    @Override
    public String toString() {
        return username;
    }
}
