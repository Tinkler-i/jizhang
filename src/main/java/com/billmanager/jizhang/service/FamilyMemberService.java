package com.billmanager.jizhang.service;

import com.billmanager.jizhang.entity.FamilyMember;
import com.billmanager.jizhang.entity.PermissionTemplate;
import java.util.List;

/**
 * 家庭成员管理服务接口
 */
public interface FamilyMemberService {
    
    /**
     * 根据用户ID加入家庭组（通过编号）
     * @param userId 用户ID
     * @param code 家庭组编号
     * @param bringExistingData 是否将现有数据带入家庭组（true: 同步到家庭组，false: 保留为个人数据）
     * @return 创建的家庭成员
     */
    FamilyMember joinFamilyGroup(Long userId, String code, Boolean bringExistingData);
    
    /**
     * 创建家庭成员（注册时自动调用，赋予管理员权限）
     * @param familyGroupId 家庭组ID
     * @param userId 用户ID
     * @param role 角色（ADMIN/MEMBER）
     * @param permissionTemplate 权限模板对象
     * @return 创建的家庭成员
     */
    FamilyMember createFamilyMember(Long familyGroupId, Long userId, String role, PermissionTemplate permissionTemplate);
    
    /**
     * 根据ID获取家庭成员
     */
    FamilyMember getFamilyMemberById(Long id);
    
    /**
     * 根据用户ID获取家庭成员
     */
    FamilyMember getFamilyMemberByUserId(Long userId);
    
    /**
     * 根据家庭组ID获取所有成员
     */
    List<FamilyMember> getFamilyMembersByGroupId(Long familyGroupId);
    
    /**
     * 获取家庭组成员数量
     */
    int countFamilyMembers(Long familyGroupId);
    
    /**
     * 更新成员权限
     * @param memberId 成员ID
     * @param permissionJson 权限JSON字符串
     */
    void updateMemberPermissions(Long memberId, String permissionJson);
    
    /**
     * 使用权限模板更新成员权限
     * @param memberId 成员ID
     * @param template 权限模板
     */
    void updateMemberPermissionsByTemplate(Long memberId, PermissionTemplate template);
    
    /**
     * 更新成员角色
     */
    void updateMemberRole(Long memberId, String role);
    
    /**
     * 移除成员
     * @param memberId 成员ID
     * @param deleteData 是否删除在家庭组中的数据（true: 删除，false: 转换为个人数据）
     */
    void removeMember(Long memberId, Boolean deleteData);
    
    /**
     * 检查用户是否已在某个家庭组中
     */
    boolean isUserInAnyFamilyGroup(Long userId);
    
    /**
     * 批量更新成员权限
     * @param memberIds 成员ID列表
     * @param permissionJson 权限JSON字符串
     */
    void batchUpdateMemberPermissions(java.util.List<Long> memberIds, String permissionJson);
    
    /**
     * 批量更新成员角色
     * @param memberIds 成员ID列表
     * @param role 新角色
     */
    void batchUpdateMemberRole(java.util.List<Long> memberIds, String role);
    
    /**
     * 获取成员权限的详细信息（解析JSON）
     * @param memberId 成员ID
     * @return 权限详情Map
     */
    java.util.Map<String, Object> getMemberPermissionDetails(Long memberId);
    
    /**
     * 直接保存 FamilyMember 对象（用于创建管理员等特殊场景）
     * @param member 要保存的 FamilyMember 对象
     * @return 保存的 FamilyMember
     */
    FamilyMember saveFamilyMember(FamilyMember member);
    
    /**
     * 检查权限冲突
     * @param permissionsJson 权限JSON字符串
     * @return 冲突信息列表，如果没有冲突则返回空列表
     */
    java.util.List<String> checkPermissionConflicts(String permissionsJson);
}
