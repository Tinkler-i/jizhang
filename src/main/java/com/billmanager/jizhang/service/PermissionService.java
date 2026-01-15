package com.billmanager.jizhang.service;

import com.billmanager.jizhang.entity.FamilyMember;
import com.billmanager.jizhang.entity.PermissionTemplate;
import java.util.List;

/**
 * 权限管理服务接口
 */
public interface PermissionService {
    
    /**
     * 获取所有权限模板
     */
    List<PermissionTemplate> getAllTemplates();
    
    /**
     * 根据名称获取权限模板
     */
    PermissionTemplate getTemplateByName(String name);
    
    /**
     * 根据ID获取权限模板
     */
    PermissionTemplate getTemplateById(Long id);
    
    /**
     * 检查用户是否有某个权限
     * @param userId 用户ID
     * @param permission 权限标识，如：expense_create、expense_delete等
     * @return 是否有权限
     */
    boolean hasPermission(Long userId, String permission);
    
    /**
     * 检查用户是否有某个权限（使用FamilyMember对象）
     * @param familyMember 家庭成员对象
     * @param permission 权限标识
     * @return 是否有权限
     */
    boolean hasPermission(FamilyMember familyMember, String permission);
    
    /**
     * 检查用户是否是家庭组管理员
     */
    boolean isAdmin(Long userId);
    
    /**
     * 从权限JSON字符串中检查权限
     * @param permissionsJson 权限JSON字符串
     * @param permission 权限标识，格式如：income_view、expense_create等
     * @return 是否有权限
     */
    boolean checkPermissionFromJson(String permissionsJson, String permission);
}
