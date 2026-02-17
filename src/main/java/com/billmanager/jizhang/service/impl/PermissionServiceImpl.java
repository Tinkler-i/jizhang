package com.billmanager.jizhang.service.impl;

import com.billmanager.jizhang.constant.PermissionConstants;
import com.billmanager.jizhang.entity.FamilyMember;
import com.billmanager.jizhang.mapper.FamilyMemberMapper;
import com.billmanager.jizhang.service.PermissionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 权限管理服务实现
 * 
 * 简化的8个权限模型：
 * - income_view / income_edit
 * - expense_view / expense_edit
 * - budget_view / budget_edit
 * - target_view / target_edit  ← 【新增】用于目标管理功能
 * 
 * 规则：
 * 1. 管理员（ADMIN）拥有所有权限
 * 2. 有edit权限则自动包含view权限
 * 3. 不在家庭组中的用户对自己的数据有全部权限
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    
    private final FamilyMemberMapper familyMemberMapper;
    private final ObjectMapper objectMapper;
    
    @Override
    public boolean hasPermission(Long userId, String permission) {
        try {
            FamilyMember member = familyMemberMapper.selectByUserId(userId);
            if (member == null) {
                // 用户未在任何家庭组中，对自己的数据有全部权限
                log.debug("【权限】用户ID: {} 未在家庭组中，自动授予个人数据权限: {}", userId, permission);
                return true;
            }
            
            // 管理员有所有权限
            if (PermissionConstants.ROLE_ADMIN.equals(member.getRole())) {
                log.debug("【权限】用户ID: {} 是管理员，自动授予权限: {}", userId, permission);
                return true;
            }
            
            // 从权限JSON检查
            return checkPermissionFromJson(member.getPermissions(), permission);
        } catch (Exception e) {
            log.error("【权限】检查用户ID: {} 的权限 {} 时出错", userId, permission, e);
            return false;
        }
    }
    
    @Override
    public boolean isAdmin(Long userId) {
        try {
            FamilyMember member = familyMemberMapper.selectByUserId(userId);
            boolean isAdmin = member != null && member.getStatus() == 1 
                    && PermissionConstants.ROLE_ADMIN.equals(member.getRole());
            log.debug("【权限】用户ID: {} 管理员检查 = {}", userId, isAdmin);
            return isAdmin;
        } catch (Exception e) {
            log.error("【权限】检查用户ID: {} 是否是管理员时出错", userId, e);
            return false;
        }
    }
    
    @Override
    public FamilyMember getFamilyMember(Long userId) {
        try {
            return familyMemberMapper.selectByUserId(userId);
        } catch (Exception e) {
            log.error("【权限】获取用户 {} 的家庭成员信息时出错", userId, e);
            return null;
        }
    }
    
    @Override
    public boolean canView(Long userId, String module) {
        String viewPermission = module + "_view";
        String editPermission = module + "_edit";
        // 有edit权限自动包含view权限
        return hasPermission(userId, viewPermission) || hasPermission(userId, editPermission);
    }
    
    @Override
    public boolean canEdit(Long userId, String module) {
        String editPermission = module + "_edit";
        return hasPermission(userId, editPermission);
    }
    
    /**
     * 从JSON权限字符串中检查权限
     * JSON格式: {"income_view":true,"income_edit":false,...}
     */
    private boolean checkPermissionFromJson(String permissionsJson, String permission) {
        try {
            if (permissionsJson == null || permissionsJson.isEmpty()) {
                return false;
            }
            
            JsonNode root = objectMapper.readTree(permissionsJson);
            JsonNode node = root.get(permission);
            
            if (node != null) {
                return node.asBoolean();
            }
            
            // 有edit权限自动有view权限
            if (permission.endsWith("_view")) {
                String editPerm = permission.replace("_view", "_edit");
                JsonNode editNode = root.get(editPerm);
                if (editNode != null && editNode.asBoolean()) {
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            log.error("【权限】解析权限JSON出错: {}", e.getMessage());
            return false;
        }
    }
}
