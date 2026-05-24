package com.billmanager.jizhang.util;

import com.billmanager.jizhang.entity.FamilyMember;
import com.billmanager.jizhang.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

/**
 * 权限验证工具类
 * 
 * 提供细粒度的权限验证，支持以下场景：
 * 1. 基于操作对象的权限验证（如只有创建者和管理员能编辑）
 * 2. 基于批量操作的权限验证
 * 3. 权限冲突检查
 */
@Slf4j
public class PermissionValidator {
    
    /**
     * 验证用户是否可以编辑某个资源
     * 规则：创建者、管理员、或具有编辑权限的用户
     * 
     * @param creatorId 资源创建者ID
     * @param userId 当前用户ID
     * @param familyMember 家庭成员信息（包含权限）
     * @param modulePermission 模块权限标识（如 "income", "expense"）
     * @throws BusinessException 如果没有编辑权限
     */
    public static void validateEditPermission(Long creatorId, Long userId, FamilyMember familyMember, String modulePermission) {
        if (familyMember == null || familyMember.getStatus() != 1) {
            throw new BusinessException("权限验证失败：成员不存在或已禁用");
        }
        
        // 管理员拥有所有权限
        if ("ADMIN".equals(familyMember.getRole())) {
            log.debug("【权限验证】用户ID: {} 是管理员，允许编辑操作", userId);
            return;
        }
        
        // 创建者可以编辑自己的资源
        if (userId.equals(creatorId)) {
            log.debug("【权限验证】用户ID: {} 是资源创建者，允许编辑操作", userId);
            return;
        }
        
        throw new BusinessException("权限不足：只有创建者和管理员可以编辑此资源");
    }
    
    /**
     * 验证用户是否可以删除某个资源
     * 规则：创建者、管理员、或具有删除权限的用户
     * 
     * @param creatorId 资源创建者ID
     * @param userId 当前用户ID
     * @param familyMember 家庭成员信息（包含权限）
     * @param modulePermission 模块权限标识（如 "income", "expense"）
     * @throws BusinessException 如果没有删除权限
     */
    public static void validateDeletePermission(Long creatorId, Long userId, FamilyMember familyMember, String modulePermission) {
        if (familyMember == null || familyMember.getStatus() != 1) {
            throw new BusinessException("权限验证失败：成员不存在或已禁用");
        }
        
        // 只有管理员可以删除
        if (!"ADMIN".equals(familyMember.getRole())) {
            throw new BusinessException("权限不足：只有管理员可以删除资源");
        }
        
        log.debug("【权限验证】用户ID: {} 是管理员，允许删除操作", userId);
    }
    
    /**
     * 验证用户是否可以管理权限
     * 规则：只有管理员可以管理权限
     * 
     * @param userId 当前用户ID
     * @param familyMember 家庭成员信息
     * @throws BusinessException 如果不是管理员
     */
    public static void validatePermissionManagement(Long userId, FamilyMember familyMember) {
        if (familyMember == null || familyMember.getStatus() != 1) {
            throw new BusinessException("权限验证失败：成员不存在或已禁用");
        }
        
        if (!"ADMIN".equals(familyMember.getRole())) {
            throw new BusinessException("权限不足：只有管理员可以管理权限");
        }
        
        log.debug("【权限验证】用户ID: {} 权限验证通过，可以管理权限", userId);
    }
    
    /**
     * 验证用户是否可以移除成员
     * 规则：只有管理员可以移除成员，且不能移除自己
     * 
     * @param userId 当前用户ID
     * @param targetUserId 目标成员用户ID
     * @param familyMember 家庭成员信息
     * @throws BusinessException 如果无法移除
     */
    public static void validateRemoveMember(Long userId, Long targetUserId, FamilyMember familyMember) {
        if (familyMember == null || familyMember.getStatus() != 1) {
            throw new BusinessException("权限验证失败：成员不存在或已禁用");
        }
        
        if (!"ADMIN".equals(familyMember.getRole())) {
            throw new BusinessException("权限不足：只有管理员可以移除成员");
        }
        
        if (userId.equals(targetUserId)) {
            throw new BusinessException("无法移除自己，请先转移管理员身份给其他成员");
        }
        
        log.debug("【权限验证】用户ID: {} 权限验证通过，可以移除成员 {}", userId, targetUserId);
    }
    
    /**
     * 验证用户是否可以批量操作（编辑、删除等）
     * 
     * @param userId 当前用户ID
     * @param resourceOwnerIds 资源所有者ID列表
     * @param familyMember 家庭成员信息
     * @param operationType 操作类型（"edit" 或 "delete"）
     * @throws BusinessException 如果权限不足
     */
    public static void validateBatchOperation(Long userId, java.util.List<Long> resourceOwnerIds, FamilyMember familyMember, String operationType) {
        if (familyMember == null || familyMember.getStatus() != 1) {
            throw new BusinessException("权限验证失败：成员不存在或已禁用");
        }
        
        // 管理员可以批量操作所有资源
        if ("ADMIN".equals(familyMember.getRole())) {
            log.debug("【权限验证】用户ID: {} 是管理员，允许批量 {} 操作", userId, operationType);
            return;
        }
        
        // 普通成员只能操作自己创建的资源
        for (Long ownerId : resourceOwnerIds) {
            if (!userId.equals(ownerId)) {
                throw new BusinessException("权限不足：您只能" + operationType + "自己创建的资源");
            }
        }
        
        log.debug("【权限验证】用户ID: {} 权限验证通过，可以批量 {} 操作", userId, operationType);
    }
    
    /**
     * 检查权限冲突
     * 如果权限模板中存在冲突，返回冲突说明
     * 
     * @param permissionsJson 权限JSON字符串
     * @return 冲突说明，如果没有冲突则返回空字符串
     */
    public static String checkPermissionConflicts(String permissionsJson) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(permissionsJson);
            
            // 检查编辑权而删除权为假的冲突
            java.util.Iterator<String> fieldNames = root.fieldNames();
            while (fieldNames.hasNext()) {
                String module = fieldNames.next();
                com.fasterxml.jackson.databind.JsonNode moduleNode = root.get(module);
                
                boolean canEdit = moduleNode.has("edit") && moduleNode.get("edit").asBoolean();
                boolean canDelete = moduleNode.has("delete") && moduleNode.get("delete").asBoolean();
                
                // 不应该有删除权但没有编辑权的情况
                if (canDelete && !canEdit) {
                    return module + "模块：不能在没有编辑权的情况下拥有删除权";
                }
            }
            
            return "";
        } catch (Exception e) {
            log.error("【权限冲突检查】解析权限JSON时出错", e);
            return "";
        }
    }
}
