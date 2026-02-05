package com.billmanager.jizhang.service;

import com.billmanager.jizhang.entity.FamilyGroup;
import java.util.List;

/**
 * 家庭组管理服务接口
 */
public interface FamilyGroupService {
    
    /**
     * 创建家庭组（用户注册时调用）
     * @param userId 用户ID
     * @param familyName 家庭组名称，默认"我的家庭"
     * @return 创建的家庭组
     */
    FamilyGroup createFamilyGroup(Long userId, String familyName);
    
    /**
     * 根据ID获取家庭组
     */
    FamilyGroup getFamilyGroupById(Long id);
    
    /**
     * 根据编码获取家庭组
     */
    FamilyGroup getFamilyGroupByCode(String code);
    
    /**
     * 根据创建者ID获取家庭组
     */
    FamilyGroup getFamilyGroupByCreatorId(Long creatorId);
    
    /**
     * 根据用户ID获取该用户所属的家庭组
     */
    FamilyGroup getFamilyGroupByUserId(Long userId);
    
    /**
     * 更新家庭组信息
     */
    void updateFamilyGroup(FamilyGroup familyGroup);
    
    /**
     * 生成唯一的家庭组编号（6位）
     */
    String generateFamilyGroupCode();
}
