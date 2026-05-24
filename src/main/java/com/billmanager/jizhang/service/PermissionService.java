package com.billmanager.jizhang.service;

import com.billmanager.jizhang.entity.FamilyMember;

/**
 * 权限管理服务接口
 * 
 * 简化的6个权限：
 * - income_view / income_edit
 * - expense_view / expense_edit  
 * - budget_view / budget_edit
 * 
 * 规则：有edit权限则自动包含view权限
 */
public interface PermissionService {
    
    /**
     * 检查用户是否有某个权限
     * @param userId 用户ID
     * @param permission 权限名，如 income_view, expense_edit 等
     * @return 是否有权限
     */
    boolean hasPermission(Long userId, String permission);
    
    /**
     * 检查用户是否是家庭组管理员
     */
    boolean isAdmin(Long userId);
    
    /**
     * 获取用户的家庭成员信息
     */
    FamilyMember getFamilyMember(Long userId);
    
    /**
     * 检查用户是否能查看某模块的数据
     * @param userId 用户ID
     * @param module income/expense/budget
     */
    boolean canView(Long userId, String module);
    
    /**
     * 检查用户是否能编辑某模块的数据
     * @param userId 用户ID
     * @param module income/expense/budget
     */
    boolean canEdit(Long userId, String module);
}
