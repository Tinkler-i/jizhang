package com.billmanager.jizhang.service.impl;

import com.billmanager.jizhang.entity.FamilyMember;
import com.billmanager.jizhang.entity.PermissionTemplate;
import com.billmanager.jizhang.mapper.FamilyMemberMapper;
import com.billmanager.jizhang.mapper.PermissionTemplateMapper;
import com.billmanager.jizhang.service.PermissionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 权限管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    
    private final FamilyMemberMapper familyMemberMapper;
    private final PermissionTemplateMapper permissionTemplateMapper;
    private final ObjectMapper objectMapper;
    
    @Override
    public List<PermissionTemplate> getAllTemplates() {
        return permissionTemplateMapper.selectAll();
    }
    
    @Override
    public PermissionTemplate getTemplateByName(String name) {
        return permissionTemplateMapper.selectByName(name);
    }
    
    @Override
    public PermissionTemplate getTemplateById(Long id) {
        return permissionTemplateMapper.selectById(id);
    }
    
    @Override
    public boolean hasPermission(Long userId, String permission) {
        try {
            FamilyMember member = familyMemberMapper.selectByUserId(userId);
            if (member == null) {
                // 用户未在任何家庭组中，允许访问个人数据权限
                log.debug("【权限】用户ID: {} 未在任何家庭组中，检查个人数据权限: {}", userId, permission);
                // 对于个人数据，允许以下权限：income_view, income_create, income_edit, income_delete
                // expense_view, expense_create, expense_edit, expense_delete
                // income_category_view, income_category_create, income_category_edit, income_category_delete
                // expense_category_view, expense_category_create, expense_category_edit, expense_category_delete
                return isPersonalDataPermission(permission);
            }
            return hasPermission(member, permission);
        } catch (Exception e) {
            log.error("【权限】检查用户ID: {} 的权限 {} 时出错", userId, permission, e);
            return false;
        }
    }
    
    /**
     * 检查是否是个人数据权限
     * 用户没有家族组时可以访问个人数据
     */
    private boolean isPersonalDataPermission(String permission) {
        // 个人数据权限列表
        return permission != null && (
            permission.startsWith("income_") || 
            permission.startsWith("expense_") || 
            permission.startsWith("budget_") ||
            permission.startsWith("income_category_") || 
            permission.startsWith("expense_category_")
        );
    }
    
    @Override
    public boolean hasPermission(FamilyMember familyMember, String permission) {
        if (familyMember == null || familyMember.getStatus() != 1) {
            return false;
        }
        
        // 管理员有所有权限
        if ("ADMIN".equals(familyMember.getRole())) {
            return true;
        }
        
        // 从权限JSON检查
        return checkPermissionFromJson(familyMember.getPermissions(), permission);
    }
    
    @Override
    public boolean isAdmin(Long userId) {
        try {
            FamilyMember member = familyMemberMapper.selectByUserId(userId);
            return member != null && member.getStatus() == 1 && "ADMIN".equals(member.getRole());
        } catch (Exception e) {
            log.error("【权限】检查用户ID: {} 是否是管理员时出错", userId, e);
            return false;
        }
    }
    
    @Override
    public boolean checkPermissionFromJson(String permissionsJson, String permission) {
        try {
            if (permissionsJson == null || permissionsJson.isEmpty()) {
                return false;
            }
            
            // 权限格式：income_view, expense_create, budget_delete 等
            // 对应JSON结构：{ "income": { "view": true, ... }, ... }
            String[] parts = permission.split("_");
            if (parts.length != 2) {
                log.warn("【权限】权限格式错误: {}", permission);
                return false;
            }
            
            String module = parts[0];      // income, expense, budget, category, member_management
            String action = parts[1];      // view, create, edit, delete 等
            
            JsonNode root = objectMapper.readTree(permissionsJson);
            JsonNode moduleNode = root.get(module);
            
            if (moduleNode == null) {
                log.warn("【权限】权限JSON中未找到模块: {}", module);
                return false;
            }
            
            JsonNode actionNode = moduleNode.get(action);
            if (actionNode == null) {
                log.warn("【权限】权限JSON中未找到操作: {}.{}", module, action);
                return false;
            }
            
            return actionNode.asBoolean();
        } catch (Exception e) {
            log.error("【权限】解析权限JSON时出错: {}", permissionsJson, e);
            return false;
        }
    }
}
