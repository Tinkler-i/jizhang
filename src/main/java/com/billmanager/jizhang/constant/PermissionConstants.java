package com.billmanager.jizhang.constant;

/**
 * 权限常量类
 * 
     * 8个权限：
     * - income_view: 查看收入
     * - income_edit: 修改收入（包含查看权限）
     * - expense_view: 查看支出
     * - expense_edit: 修改支出（包含查看权限）
     * - budget_view: 查看预算
     * - budget_edit: 修改预算（包含查看权限）
     * - target_view: 查看目标管理
     * - target_edit: 修改目标管理（包含查看权限）
     */
public class PermissionConstants {
    
    // 权限名称
    public static final String INCOME_VIEW = "income_view";
    public static final String INCOME_EDIT = "income_edit";
    public static final String EXPENSE_VIEW = "expense_view";
    public static final String EXPENSE_EDIT = "expense_edit";
    public static final String BUDGET_VIEW = "budget_view";
    public static final String BUDGET_EDIT = "budget_edit";
    public static final String TARGET_VIEW = "target_view";
    public static final String TARGET_EDIT = "target_edit";
    
    // 角色
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MEMBER = "MEMBER";
    
    /**
     * 默认成员权限JSON（只有查看权限）
     */
    public static final String DEFAULT_MEMBER_PERMISSIONS = 
        "{\"income_view\":true,\"income_edit\":false," +
        "\"expense_view\":true,\"expense_edit\":false," +
        "\"budget_view\":true,\"budget_edit\":false," +
        "\"target_view\":true,\"target_edit\":false}";
    
    /**
     * 管理员权限JSON（所有权限）
     */
    public static final String ADMIN_PERMISSIONS = 
        "{\"income_view\":true,\"income_edit\":true," +
        "\"expense_view\":true,\"expense_edit\":true," +
        "\"budget_view\":true,\"budget_edit\":true," +
        "\"target_view\":true,\"target_edit\":true}";
}
