package com.billmanager.jizhang.dto;

import com.billmanager.jizhang.security.CustomUserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

/**
 * 用户上下文工具类
 * 
 * 从Spring Security的SecurityContext中获取当前认证用户的信息
 * 提供线程安全的用户信息访问
 * 
 * 注意：需要确保 JWT 或 Session 认证过程中已将用户ID设置到Principal中
 */
@Component
@Slf4j
public class UserContext {
    
    /**
     * 获取当前用户ID
     * 
     * 实现策略（按优先级）：
     * 1. 尝试从自定义的 CustomUserPrincipal 获取用户ID
     * 2. 尝试从Principal的name属性解析（如果name是用户ID）
     * 3. 尝试从Principal对象获取（如果是自定义UserPrincipal）
     * 4. 返回null（未认证用户）
     * 
     * @return 用户ID，如果未认证返回null
     */
    public Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                log.debug("【用户上下文】当前请求未认证");
                return null;
            }
            
            // 从Principal获取用户标识
            Object principal = authentication.getPrincipal();
            
            // 方式1：检查是否为自定义的 CustomUserPrincipal
            if (principal instanceof CustomUserPrincipal) {
                Long userId = ((CustomUserPrincipal) principal).getUserId();
                log.debug("【用户上下文】从 CustomUserPrincipal 获取用户ID: {}", userId);
                return userId;
            }
            
            // 方式2：检查是否为自定义的UserPrincipal接口
            if (principal instanceof UserPrincipal) {
                Long userId = ((UserPrincipal) principal).getUserId();
                log.debug("【用户上下文】从UserPrincipal获取用户ID: {}", userId);
                return userId;
            }
            
            // 方式3：Spring Security默认的UserDetails
            if (principal instanceof User) {
                String username = ((User) principal).getUsername();
                log.debug("【用户上下文】从UserDetails获取用户名: {}", username);
                // 这里需要通过用户名查询ID，但这会导致每次都查数据库
                // 更好的方案是在认证时就设置了ID
                return null;
            }
            
            // 方式4：直接使用principal.toString()
            String principalStr = principal.toString();
            log.debug("【用户上下文】Principal字符串: {}", principalStr);
            
            // 如果Principal是用户ID数字，尝试解析
            try {
                Long userId = Long.parseLong(principalStr);
                log.debug("【用户上下文】从Principal解析用户ID: {}", userId);
                return userId;
            } catch (NumberFormatException e) {
                log.debug("【用户上下文】Principal不是数字ID");
            }
            
            log.warn("【用户上下文】无法从认证信息中提取用户ID，principal类型: {}", principal.getClass().getName());
            return null;
            
        } catch (Exception e) {
            log.error("【用户上下文】获取用户ID异常", e);
            return null;
        }
    }
    
    /**
     * 获取当前用户名
     * 
     * @return 用户名，如果未认证返回null
     */
    public String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }
            
            return authentication.getName();
            
        } catch (Exception e) {
            log.error("【用户上下文】获取用户名异常", e);
            return null;
        }
    }
    
    /**
     * 检查当前用户是否已认证
     * 
     * @return true 如果用户已认证，否则false
     */
    public boolean isAuthenticated() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication != null && authentication.isAuthenticated();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取当前认证信息
     * 
     * @return Authentication对象，如果未认证返回null
     */
    public Authentication getAuthentication() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication;
            }
            return null;
        } catch (Exception e) {
            log.error("【用户上下文】获取认证信息异常", e);
            return null;
        }
    }
    
    /**
     * 自定义用户Principal接口
     * 如果项目使用了自定义的UserPrincipal类，需要实现此接口
     */
    public interface UserPrincipal {
        Long getUserId();
        String getUsername();
    }
}
