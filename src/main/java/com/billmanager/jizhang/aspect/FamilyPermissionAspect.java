package com.billmanager.jizhang.aspect;

import com.billmanager.jizhang.annotation.FamilyPermission;
import com.billmanager.jizhang.dto.UserContext;
import com.billmanager.jizhang.exception.BusinessException;
import com.billmanager.jizhang.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 家庭组权限检查切面
 * 
 * 在执行带有 @FamilyPermission 注解的方法前进行权限验证
 * 确保用户具有指定的操作权限
 * 
 * 工作流程：
 * 1. 拦截带 @FamilyPermission 注解的方法
 * 2. 从SecurityContext获取当前用户ID
 * 3. 调用PermissionService检查用户权限
 * 4. 权限不足时抛出异常或返回false
 */
@Aspect
@Component
@Slf4j
public class FamilyPermissionAspect {
    
    private final PermissionService permissionService;
    private final UserContext userContext;
    
    public FamilyPermissionAspect(PermissionService permissionService, UserContext userContext) {
        this.permissionService = permissionService;
        this.userContext = userContext;
    }
    
    /**
     * 在执行 @FamilyPermission 注解的方法前进行权限检查
     * 
     * @param joinPoint 连接点信息
     * @param familyPermission 权限注解
     * @throws BusinessException 权限不足时抛出
     */
    @Before("@annotation(familyPermission)")
    public void checkPermission(JoinPoint joinPoint, FamilyPermission familyPermission) {
        try {
            // 获取当前用户信息
            Long userId = getCurrentUserId();
            if (userId == null) {
                log.warn("【权限检查】无法获取当前用户信息");
                if (familyPermission.throwException()) {
                    throw new BusinessException("用户未认证");
                }
                return;
            }
            
            // 获取注解中的权限
            String requiredPermission = familyPermission.value();
            log.debug("【权限检查】用户ID: {}, 检查权限: {}", userId, requiredPermission);
            
            // 检查用户是否具有权限
            boolean hasPermission = permissionService.hasPermission(userId, requiredPermission);
            
            if (!hasPermission) {
                String methodName = joinPoint.getSignature().getName();
                String className = joinPoint.getTarget().getClass().getSimpleName();
                log.warn("【权限检查】用户ID: {} 无权执行方法 {}.{}, 需要权限: {}", 
                    userId, className, methodName, requiredPermission);
                
                if (familyPermission.throwException()) {
                    throw new BusinessException("权限不足：无法执行操作 [" + requiredPermission + "]");
                }
            } else {
                String methodName = joinPoint.getSignature().getName();
                String className = joinPoint.getTarget().getClass().getSimpleName();
                log.debug("【权限检查】用户ID: {} 权限验证通过，可以执行 {}.{}", 
                    userId, className, methodName);
            }
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("【权限检查】发生错误", e);
            if (familyPermission.throwException()) {
                throw new BusinessException("权限检查异常：" + e.getMessage());
            }
        }
    }
    
    /**
     * 从SecurityContext中获取当前用户ID
     * 
     * @return 用户ID，如果未认证则返回null
     */
    private Long getCurrentUserId() {
        try {
            // 方式1：从SecurityContext获取
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                    // principal 是 UserDetails 对象，需要从其他地方获取ID
                    // 这里假设已通过其他机制设置了UserContext
                    return userContext.getCurrentUserId();
                } else if (principal instanceof String) {
                    // principal 是用户名字符串，需要查询数据库获取ID
                    return userContext.getCurrentUserId();
                }
            }
            
            // 方式2：从UserContext直接获取（推荐）
            return userContext.getCurrentUserId();
            
        } catch (Exception e) {
            log.debug("【权限检查】无法从SecurityContext获取用户ID", e);
            return null;
        }
    }
}
