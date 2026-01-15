package com.billmanager.jizhang.annotation;

import java.lang.annotation.*;

/**
 * 家庭组权限检查注解
 * 在方法执行前验证用户是否具有指定的家庭组权限
 * 
 * 使用示例：
 * @FamilyPermission("income_view")
 * public List<Income> getIncomeList() { ... }
 * 
 * 权限格式：module_action
 * 如：income_view, income_create, income_edit, income_delete
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FamilyPermission {
    
    /**
     * 需要验证的权限字符串
     * 格式：module_action (如 income_view, expense_create, budget_edit 等)
     */
    String value();
    
    /**
     * 权限检查失败时是否抛出异常
     * 默认为true，即权限不足时抛出FamilyPermissionException
     */
    boolean throwException() default true;
}
