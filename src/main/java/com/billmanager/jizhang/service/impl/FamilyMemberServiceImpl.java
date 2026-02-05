package com.billmanager.jizhang.service.impl;

import com.billmanager.jizhang.entity.FamilyGroup;
import com.billmanager.jizhang.entity.FamilyMember;
import com.billmanager.jizhang.mapper.BudgetMapper;
import com.billmanager.jizhang.mapper.ExpenseCategoryMapper;
import com.billmanager.jizhang.mapper.ExpenseMapper;
import com.billmanager.jizhang.mapper.FamilyGroupMapper;
import com.billmanager.jizhang.mapper.FamilyMemberMapper;
import com.billmanager.jizhang.mapper.IncomeCategoryMapper;
import com.billmanager.jizhang.mapper.IncomeMapper;
import com.billmanager.jizhang.service.FamilyMemberService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 家庭成员管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FamilyMemberServiceImpl implements FamilyMemberService {
    
    private final FamilyMemberMapper familyMemberMapper;
    private final FamilyGroupMapper familyGroupMapper;
    private final IncomeMapper incomeMapper;
    private final ExpenseMapper expenseMapper;
    private final IncomeCategoryMapper incomeCategoryMapper;
    private final ExpenseCategoryMapper expenseCategoryMapper;
    private final BudgetMapper budgetMapper;
    private final ObjectMapper objectMapper;
    
    @Override
    public FamilyMember joinFamilyGroup(Long userId, String code, Boolean bringExistingData) {
        try {
            log.info("【家庭成员】用户ID: {} 尝试使用编号 {} 加入家庭组, 是否带入现有数据: {}", userId, code, bringExistingData);
            
            // 检查用户是否已属于某个家庭组（激活的）
            FamilyMember existing = familyMemberMapper.selectByUserId(userId);
            if (existing != null) {
                log.warn("【家庭成员】用户ID: {} 已属于家庭组ID: {}", userId, existing.getFamilyGroupId());
                throw new RuntimeException("您已属于一个家庭组，无法再加入其他家庭组");
            }
            
            // 查询家庭组
            FamilyGroup familyGroup = familyGroupMapper.selectByCode(code);
            if (familyGroup == null) {
                log.warn("【家庭成员】找不到编号为 {} 的家庭组", code);
                throw new RuntimeException("家庭组编号无效");
            }
            
            if (familyGroup.getStatus() != 1) {
                log.warn("【家庭成员】家庭组ID: {} 已禁用", familyGroup.getId());
                throw new RuntimeException("该家庭组已禁用");
            }
            
            // 使用默认成员权限（简化的6个权限，只有查看权限）
            String defaultPermissions = "{\"income_view\":true,\"income_edit\":false," +
                    "\"expense_view\":true,\"expense_edit\":false," +
                    "\"budget_view\":true,\"budget_edit\":false}";
            
            // 检查是否有历史记录（被删除的成员记录）
            FamilyMember historicalMember = familyMemberMapper.selectByFamilyGroupAndUserIdIncludeDeleted(familyGroup.getId(), userId);
            
            FamilyMember member;
            if (historicalMember != null && historicalMember.getStatus() == 0) {
                // 恢复历史记录
                log.info("【家庭成员】恢复用户ID: {} 在家庭组ID: {} 中的历史记录", userId, familyGroup.getId());
                historicalMember.setStatus(1);
                historicalMember.setPermissions(defaultPermissions);
                historicalMember.setRole("MEMBER");
                familyMemberMapper.updateStatus(historicalMember.getId(), 1);
                familyMemberMapper.updatePermissions(historicalMember.getId(), defaultPermissions);
                member = historicalMember;
            } else {
                // 创建新家庭成员
                member = new FamilyMember();
                member.setFamilyGroupId(familyGroup.getId());
                member.setUserId(userId);
                member.setRole("MEMBER");
                member.setPermissions(defaultPermissions);
                member.setStatus(1);
                
                familyMemberMapper.insert(member);
            }
            
            // 根据用户选择，决定是否带入现有数据
            if (bringExistingData != null && bringExistingData) {
                // 更新用户的所有收入和支出记录的家庭组ID
                int incomeUpdated = incomeMapper.updateFamilyGroupId(userId, familyGroup.getId());
                int expenseUpdated = expenseMapper.updateFamilyGroupId(userId, familyGroup.getId());
                
                // 更新用户的所有收入和支出分类的家庭组ID（不包括系统内置分类）
                // 系统内置分类应该始终保持 family_group_id = 0（个人数据）
                int incomeCategoryUpdated = incomeCategoryMapper.updateFamilyGroupIdExcludeBuiltIn(userId, familyGroup.getId());
                int expenseCategoryUpdated = expenseCategoryMapper.updateFamilyGroupIdExcludeBuiltIn(userId, familyGroup.getId());
                
                log.info("【家庭成员】用户ID: {} 加入家庭组ID: {} 后，更新了 {} 条收入记录、{} 条支出记录、{} 个收入分类和 {} 个支出分类", 
                        userId, familyGroup.getId(), incomeUpdated, expenseUpdated, incomeCategoryUpdated, expenseCategoryUpdated);
            } else {
                log.info("【家庭成员】用户ID: {} 加入家庭组ID: {} 后，未带入现有数据", userId, familyGroup.getId());
            }
            
            log.info("【家庭成员】用户ID: {} 成功加入家庭组ID: {}", userId, familyGroup.getId());
            
            return member;
        } catch (Exception e) {
            log.error("【家庭成员】用户ID: {} 加入家庭组失败", userId, e);
            throw new RuntimeException("加入家庭组失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public FamilyMember createFamilyMember(Long familyGroupId, Long userId, String role) {
        try {
            log.info("【家庭成员】为用户ID: {} 创建家庭成员，家庭组ID: {}, 角色: {}", userId, familyGroupId, role);
            
            // 根据角色选择权限
            String permissions;
            if ("ADMIN".equals(role)) {
                // 管理员拥有所有权限
                permissions = "{\"income_view\":true,\"income_edit\":true," +
                        "\"expense_view\":true,\"expense_edit\":true," +
                        "\"budget_view\":true,\"budget_edit\":true}";
            } else {
                // 普通成员只有查看权限
                permissions = "{\"income_view\":true,\"income_edit\":false," +
                        "\"expense_view\":true,\"expense_edit\":false," +
                        "\"budget_view\":true,\"budget_edit\":false}";
            }
            
            FamilyMember member = new FamilyMember();
            member.setFamilyGroupId(familyGroupId);
            member.setUserId(userId);
            member.setRole(role);
            member.setPermissions(permissions);
            member.setStatus(1);
            
            familyMemberMapper.insert(member);
            log.info("【家庭成员】成功创建家庭成员，ID: {}, 用户ID: {}", member.getId(), userId);
            
            return member;
        } catch (Exception e) {
            log.error("【家庭成员】创建家庭成员失败，用户ID: {}", userId, e);
            throw new RuntimeException("创建家庭成员失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public FamilyMember getFamilyMemberById(Long id) {
        return familyMemberMapper.selectById(id);
    }
    
    @Override
    public FamilyMember getFamilyMemberByUserId(Long userId) {
        return familyMemberMapper.selectByUserId(userId);
    }
    
    @Override
    public List<FamilyMember> getFamilyMembersByGroupId(Long familyGroupId) {
        return familyMemberMapper.selectByFamilyGroupId(familyGroupId);
    }
    
    @Override
    public int countFamilyMembers(Long familyGroupId) {
        return familyMemberMapper.countByFamilyGroupId(familyGroupId);
    }
    
    @Override
    public void updateMemberPermissions(Long memberId, String permissionJson) {
        try {
            familyMemberMapper.updatePermissions(memberId, permissionJson);
            log.info("【家庭成员】成功更新成员ID: {} 的权限", memberId);
        } catch (Exception e) {
            log.error("【家庭成员】更新成员ID: {} 的权限失败", memberId, e);
            throw new RuntimeException("更新权限失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void updateMemberRole(Long memberId, String role) {
        try {
            familyMemberMapper.updateRole(memberId, role);
            log.info("【家庭成员】成功更新成员ID: {} 的角色为: {}", memberId, role);
        } catch (Exception e) {
            log.error("【家庭成员】更新成员ID: {} 的角色失败", memberId, e);
            throw new RuntimeException("更新角色失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void removeMember(Long memberId, Boolean deleteData) {
        try {
            FamilyMember member = familyMemberMapper.selectById(memberId);
            if (member == null) {
                log.warn("【家庭成员】要移除的成员ID: {} 不存在", memberId);
                throw new RuntimeException("成员不存在");
            }
            
            FamilyGroup familyGroup = familyGroupMapper.selectById(member.getFamilyGroupId());
            
            // 如果该成员是家庭组的创建者，直接删除整个家庭组和所有成员
            if (familyGroup != null && familyGroup.getCreatorId().equals(member.getUserId())) {
                // 先删除所有家庭成员
                familyMemberMapper.deleteByFamilyGroupId(member.getFamilyGroupId());
                // 再删除家庭组
                familyGroupMapper.delete(familyGroup.getId());
                // 删除该用户的所有收入和支出记录（在家族组中的数据）
                incomeMapper.deleteByUserIdAndFamilyGroupId(member.getUserId(), familyGroup.getId());
                expenseMapper.deleteByUserIdAndFamilyGroupId(member.getUserId(), familyGroup.getId());
                // 删除该用户的所有收入和支出分类（在家族组中的数据）
                incomeCategoryMapper.deleteByUserIdAndFamilyGroupId(member.getUserId(), familyGroup.getId());
                expenseCategoryMapper.deleteByUserIdAndFamilyGroupId(member.getUserId(), familyGroup.getId());
                // 删除该用户的所有预算（在家族组中的数据）
                budgetMapper.deleteByUserIdAndFamilyGroupId(member.getUserId(), familyGroup.getId());
                log.info("【家庭成员】家庭组ID: {} 的创建者 (用户ID: {}) 已离开，该家庭组及其所有成员已被删除，相关的收入支出记录、分类和预算也已删除", 
                        familyGroup.getId(), member.getUserId());
            } else {
                // 普通成员：根据用户选择，决定是删除还是转换为个人数据
                familyMemberMapper.deleteById(memberId);
                
                if (deleteData != null && deleteData) {
                    // 删除该用户在此家族组中的所有收入和支出记录
                    incomeMapper.deleteByUserIdAndFamilyGroupId(member.getUserId(), member.getFamilyGroupId());
                    expenseMapper.deleteByUserIdAndFamilyGroupId(member.getUserId(), member.getFamilyGroupId());
                    // 删除该用户在此家族组中的所有收入和支出分类
                    incomeCategoryMapper.deleteByUserIdAndFamilyGroupId(member.getUserId(), member.getFamilyGroupId());
                    expenseCategoryMapper.deleteByUserIdAndFamilyGroupId(member.getUserId(), member.getFamilyGroupId());
                    // 删除该用户在此家族组中的所有预算
                    budgetMapper.deleteByUserIdAndFamilyGroupId(member.getUserId(), member.getFamilyGroupId());
                    log.info("【家庭成员】成功移除成员ID: {} (用户ID: {})，其在家族组中的收入、支出、分类和预算已删除", memberId, member.getUserId());
                } else {
                    // 将数据转换为个人数据（family_group_id 改为 0）
                    incomeMapper.updateFamilyGroupIdToPersonal(member.getUserId(), member.getFamilyGroupId());
                    expenseMapper.updateFamilyGroupIdToPersonal(member.getUserId(), member.getFamilyGroupId());
                    // 分类也要转换，但不转换系统内置分类（因为系统内置分类不应该在family_group_id > 0的情况下存在）
                    incomeCategoryMapper.updateFamilyGroupIdToPersonal(member.getUserId(), member.getFamilyGroupId());
                    expenseCategoryMapper.updateFamilyGroupIdToPersonal(member.getUserId(), member.getFamilyGroupId());
                    // 预算也要转换
                    budgetMapper.updateFamilyGroupIdToPersonal(member.getUserId(), member.getFamilyGroupId());
                    log.info("【家庭成员】成功移除成员ID: {} (用户ID: {})，其收入、支出、分类和预算已转换为个人数据", memberId, member.getUserId());
                }
            }
        } catch (Exception e) {
            log.error("【家庭成员】移除成员ID: {} 失败", memberId, e);
            throw new RuntimeException("移除成员失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isUserInAnyFamilyGroup(Long userId) {
        try {
            FamilyMember member = familyMemberMapper.selectByUserId(userId);
            return member != null && member.getStatus() == 1;
        } catch (Exception e) {
            log.error("【家庭成员】检查用户ID: {} 是否在家庭组中失败", userId, e);
            return false;
        }
    }
    
    @Override
    public void batchUpdateMemberPermissions(List<Long> memberIds, String permissionJson) {
        try {
            if (memberIds == null || memberIds.isEmpty()) {
                log.warn("【家庭成员】批量更新权限：成员ID列表为空");
                return;
            }
            
            for (Long memberId : memberIds) {
                familyMemberMapper.updatePermissions(memberId, permissionJson);
            }
            
            log.info("【家庭成员】成功批量更新 {} 个成员的权限", memberIds.size());
        } catch (Exception e) {
            log.error("【家庭成员】批量更新权限失败", e);
            throw new RuntimeException("批量更新权限失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void batchUpdateMemberRole(List<Long> memberIds, String role) {
        try {
            if (memberIds == null || memberIds.isEmpty()) {
                log.warn("【家庭成员】批量更新角色：成员ID列表为空");
                return;
            }
            
            for (Long memberId : memberIds) {
                familyMemberMapper.updateRole(memberId, role);
            }
            
            log.info("【家庭成员】成功批量更新 {} 个成员的角色为: {}", memberIds.size(), role);
        } catch (Exception e) {
            log.error("【家庭成员】批量更新角色失败", e);
            throw new RuntimeException("批量更新角色失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Map<String, Object> getMemberPermissionDetails(Long memberId) {
        try {
            FamilyMember member = familyMemberMapper.selectById(memberId);
            if (member == null) {
                return new HashMap<>();
            }
            
            Map<String, Object> details = new HashMap<>();
            details.put("memberId", member.getId());
            details.put("userId", member.getUserId());
            details.put("role", member.getRole());
            details.put("status", member.getStatus());
            details.put("joinTime", member.getJoinTime());
            
            // 解析权限JSON
            if (member.getPermissions() != null && !member.getPermissions().isEmpty()) {
                try {
                    JsonNode permNode = objectMapper.readTree(member.getPermissions());
                    details.put("permissions", objectMapper.convertValue(permNode, Map.class));
                } catch (Exception e) {
                    log.warn("【家庭成员】解析成员ID: {} 的权限JSON失败", memberId, e);
                    details.put("permissions", member.getPermissions());
                }
            }
            
            return details;
        } catch (Exception e) {
            log.error("【家庭成员】获取成员ID: {} 的权限详情失败", memberId, e);
            return new HashMap<>();
        }
    }
    
    @Override
    public List<String> checkPermissionConflicts(String permissionsJson) {
        List<String> conflicts = new ArrayList<>();
        
        try {
            if (permissionsJson == null || permissionsJson.isEmpty()) {
                return conflicts;
            }
            
            JsonNode root = objectMapper.readTree(permissionsJson);
            
            java.util.Iterator<String> fieldNames = root.fieldNames();
            while (fieldNames.hasNext()) {
                String module = fieldNames.next();
                JsonNode moduleNode = root.get(module);
                
                boolean canEdit = moduleNode.has("edit") && moduleNode.get("edit").asBoolean();
                boolean canDelete = moduleNode.has("delete") && moduleNode.get("delete").asBoolean();
                boolean canCreate = moduleNode.has("create") && moduleNode.get("create").asBoolean();
                
                // 检查冲突1：不能在没有编辑权的情况下拥有删除权
                if (canDelete && !canEdit) {
                    conflicts.add(module + "模块：不能在没有编辑权的情况下拥有删除权");
                }
                
                // 检查冲突2：不能在没有查看权的情况下拥有创建权
                boolean canView = moduleNode.has("view") && moduleNode.get("view").asBoolean();
                if ((canEdit || canDelete || canCreate) && !canView) {
                    conflicts.add(module + "模块：不能在没有查看权的情况下拥有编辑/删除/创建权");
                }
            }
            
            if (!conflicts.isEmpty()) {
                log.warn("【家庭成员】检测到权限冲突：{}", conflicts);
            }
            
        } catch (Exception e) {
            log.error("【家庭成员】检查权限冲突时出错", e);
        }
        
        return conflicts;
    }
    
    @Override
    public FamilyMember saveFamilyMember(FamilyMember member) {
        try {
            log.info("【家庭成员】保存 FamilyMember 对象，用户ID: {}, 角色: {}", member.getUserId(), member.getRole());
            familyMemberMapper.insert(member);
            return member;
        } catch (Exception e) {
            log.error("【家庭成员】保存 FamilyMember 失败", e);
            throw new RuntimeException("保存成员失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 确保用户拥有"待分类"分类（系统内置）
     * 如果不存在，则自动创建
     */
    private void ensureUnclassifiedCategories(Long userId, Long familyGroupId) {
        try {
            // 检查收入分类中是否有"待分类"
            com.billmanager.jizhang.entity.IncomeCategory incomeUnclassified = 
                incomeCategoryMapper.findByFamilyGroupIdAndName(familyGroupId, "待分类");
            if (incomeUnclassified == null) {
                incomeUnclassified = new com.billmanager.jizhang.entity.IncomeCategory();
                incomeUnclassified.setUserId(userId);
                incomeUnclassified.setFamilyGroupId(familyGroupId);
                incomeUnclassified.setName("待分类");
                incomeUnclassified.setDescription("自动导入账单时未匹配分类的默认分类");
                incomeUnclassified.setIsBuiltIn(1);
                incomeCategoryMapper.insert(incomeUnclassified);
                log.info("【家庭成员】为用户ID: {}, 家庭组ID: {} 创建了收入'待分类'分类", userId, familyGroupId);
            }
            
            // 检查支出分类中是否有"待分类"
            com.billmanager.jizhang.entity.ExpenseCategory expenseUnclassified = 
                expenseCategoryMapper.findByFamilyGroupIdAndName(familyGroupId, "待分类");
            if (expenseUnclassified == null) {
                expenseUnclassified = new com.billmanager.jizhang.entity.ExpenseCategory();
                expenseUnclassified.setUserId(userId);
                expenseUnclassified.setFamilyGroupId(familyGroupId);
                expenseUnclassified.setName("待分类");
                expenseUnclassified.setDescription("自动导入账单时未匹配分类的默认分类");
                expenseUnclassified.setIsBuiltIn(1);
                expenseCategoryMapper.insert(expenseUnclassified);
                log.info("【家庭成员】为用户ID: {}, 家庭组ID: {} 创建了支出'待分类'分类", userId, familyGroupId);
            }
        } catch (Exception e) {
            log.warn("【家庭成员】为用户ID: {}, 家庭组ID: {} 创建'待分类'分类时出错", userId, familyGroupId, e);
            // 不抛异常，这是一个辅助操作
        }
    }
}
