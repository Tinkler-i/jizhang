package com.billmanager.jizhang.service.impl;

import com.billmanager.jizhang.entity.FamilyGroup;
import com.billmanager.jizhang.mapper.FamilyGroupMapper;
import com.billmanager.jizhang.mapper.FamilyMemberMapper;
import com.billmanager.jizhang.service.FamilyGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

/**
 * 家庭组管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FamilyGroupServiceImpl implements FamilyGroupService {
    
    private final FamilyGroupMapper familyGroupMapper;
    private final FamilyMemberMapper familyMemberMapper;
    
    @Override
    public FamilyGroup createFamilyGroup(Long userId, String familyName) {
        try {
            // 生成唯一的家庭组编号
            String code = generateFamilyGroupCode();
            log.info("【家庭组】为用户ID: {} 生成家庭组编号: {}", userId, code);
            
            // 创建家庭组
            FamilyGroup familyGroup = new FamilyGroup();
            familyGroup.setCode(code);
            familyGroup.setName(familyName != null ? familyName : "我的家庭");
            familyGroup.setCreatorId(userId);
            familyGroup.setStatus(1);
            
            familyGroupMapper.insert(familyGroup);
            log.info("【家庭组】成功创建家庭组，ID: {}, 编号: {}, 创建者: {}", familyGroup.getId(), code, userId);
            
            return familyGroup;
        } catch (Exception e) {
            log.error("【家庭组】创建家庭组失败，用户ID: {}", userId, e);
            throw new RuntimeException("创建家庭组失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public FamilyGroup getFamilyGroupById(Long id) {
        return familyGroupMapper.selectById(id);
    }
    
    @Override
    public FamilyGroup getFamilyGroupByCode(String code) {
        return familyGroupMapper.selectByCode(code);
    }
    
    @Override
    public FamilyGroup getFamilyGroupByCreatorId(Long creatorId) {
        return familyGroupMapper.selectByCreatorId(creatorId);
    }
    
    @Override
    public FamilyGroup getFamilyGroupByUserId(Long userId) {
        try {
            // 从family_member表查询用户所属的家庭组ID，再查询家庭组信息
            com.billmanager.jizhang.entity.FamilyMember member = familyMemberMapper.selectByUserId(userId);
            if (member == null) {
                log.warn("【家庭组】用户ID: {} 未在任何家庭组中", userId);
                return null;
            }
            return familyGroupMapper.selectById(member.getFamilyGroupId());
        } catch (Exception e) {
            log.error("【家庭组】查询用户ID: {} 的家庭组失败", userId, e);
            return null;
        }
    }
    
    @Override
    public void updateFamilyGroup(FamilyGroup familyGroup) {
        try {
            familyGroupMapper.update(familyGroup);
            log.info("【家庭组】成功更新家庭组，ID: {}", familyGroup.getId());
        } catch (Exception e) {
            log.error("【家庭组】更新家庭组失败，ID: {}", familyGroup.getId(), e);
            throw new RuntimeException("更新家庭组失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String generateFamilyGroupCode() {
        // 生成6位编号：使用UUID的前6位，然后转换为大写字母+数字
        // 避免使用容易混淆的字母：I, O, L 等
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"; // 排除I, O, L, 0, 1
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        String generatedCode = code.toString();
        
        // 检查编号是否已存在，如果存在则递归重新生成
        FamilyGroup existing = familyGroupMapper.selectByCode(generatedCode);
        if (existing != null) {
            log.warn("【家庭组】生成的编号 {} 已存在，重新生成", generatedCode);
            return generateFamilyGroupCode();
        }
        
        return generatedCode;
    }
}
