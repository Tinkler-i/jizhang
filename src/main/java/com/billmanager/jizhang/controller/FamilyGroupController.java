package com.billmanager.jizhang.controller;

import com.billmanager.jizhang.constant.PermissionConstants;
import com.billmanager.jizhang.dto.UserContext;
import com.billmanager.jizhang.entity.FamilyGroup;
import com.billmanager.jizhang.entity.FamilyMember;
import com.billmanager.jizhang.exception.BusinessException;
import com.billmanager.jizhang.mapper.IncomeMapper;
import com.billmanager.jizhang.mapper.ExpenseMapper;
import com.billmanager.jizhang.mapper.IncomeCategoryMapper;
import com.billmanager.jizhang.mapper.ExpenseCategoryMapper;
import com.billmanager.jizhang.service.FamilyGroupService;
import com.billmanager.jizhang.service.FamilyMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 家庭组管理API控制器
 * 
 * 提供家庭组的查看、创建、更新等操作
 * 所有接口都需要用户已认证
 */
@RestController
@RequestMapping("/api/family-groups")
@RequiredArgsConstructor
@Slf4j
public class FamilyGroupController {
    
    private final FamilyGroupService familyGroupService;
    private final FamilyMemberService familyMemberService;
    private final UserContext userContext;
    private final IncomeMapper incomeMapper;
    private final ExpenseMapper expenseMapper;
    private final IncomeCategoryMapper incomeCategoryMapper;
    private final ExpenseCategoryMapper expenseCategoryMapper;
    
    /**
     * 获取当前用户的家庭组信息
     * 
     * @return 用户所属的家庭组信息
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentFamilyGroup() {
        try {
            Long userId = userContext.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("code", 401, "message", "用户未认证"));
            }
            
            log.info("【家庭组】用户{} 查询当前家庭组", userId);
            FamilyGroup familyGroup = familyGroupService.getFamilyGroupByUserId(userId);
            
            if (familyGroup == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("code", 404, "message", "用户未加入任何家庭组"));
            }
            
            // 获取该家庭组的成员数
            long memberCount = familyMemberService.countFamilyMembers(familyGroup.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", familyGroup);
            response.put("memberCount", memberCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("【家庭组】查询家庭组失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", 500, "message", "查询失败：" + e.getMessage()));
        }
    }
    
    /**
     * 通过家庭组编号获取家庭组信息
     * 
     * @param code 家庭组6位编号
     * @return 家庭组详情
     */
    @GetMapping("/by-code/{code}")
    public ResponseEntity<?> getFamilyGroupByCode(@PathVariable String code) {
        try {
            Long userId = userContext.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("code", 401, "message", "用户未认证"));
            }
            
            log.info("【家庭组】用户{} 查询家庭组编号: {}", userId, code);
            FamilyGroup familyGroup = familyGroupService.getFamilyGroupByCode(code);
            
            if (familyGroup == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("code", 404, "message", "家庭组不存在"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", familyGroup);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("【家庭组】查询家庭组失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", 500, "message", "查询失败：" + e.getMessage()));
        }
    }
    
    /**
     * 获取家庭组的编号（用于分享）
     * 
     * @return 家庭组编号
     */
    @GetMapping("/code")
    public ResponseEntity<?> getFamilyGroupCode() {
        try {
            Long userId = userContext.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("code", 401, "message", "用户未认证"));
            }
            
            log.info("【家庭组】用户{} 获取家庭组编号", userId);
            FamilyGroup familyGroup = familyGroupService.getFamilyGroupByUserId(userId);
            
            if (familyGroup == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("code", 404, "message", "用户未加入任何家庭组"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", Map.of(
                    "familyGroupId", familyGroup.getId(),
                    "familyGroupCode", familyGroup.getCode(),
                    "familyGroupName", familyGroup.getName()
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("【家庭组】获取家庭组编号失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", 500, "message", "获取失败：" + e.getMessage()));
        }
    }
    
    /**
     * 更新家庭组基本信息
     * 需要是家庭组的创建者或管理员
     * 
     * @param familyGroup 更新的家庭组数据
     * @return 更新后的家庭组信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFamilyGroup(@PathVariable Long id, 
                                               @RequestBody FamilyGroup familyGroup) {
        try {
            Long userId = userContext.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("code", 401, "message", "用户未认证"));
            }
            
            log.info("【家庭组】用户{} 更新家庭组{}", userId, id);
            
            FamilyGroup existing = familyGroupService.getFamilyGroupById(id);
            if (existing == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("code", 404, "message", "家庭组不存在"));
            }
            
            // 只允许创建者修改
            if (!existing.getCreatorId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("code", 403, "message", "无权修改此家庭组"));
            }
            
            existing.setName(familyGroup.getName());
            existing.setDescription(familyGroup.getDescription());
            
            familyGroupService.updateFamilyGroup(existing);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", existing);
            response.put("message", "家庭组已更新");
            
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            log.error("【家庭组】业务异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("code", 400, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("【家庭组】更新家庭组失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", 500, "message", "更新失败：" + e.getMessage()));
        }
    }
    
    /**
     * 获取家庭组成员列表
     * 
     * @return 家庭组的所有成员
     */
    @GetMapping("/members")
    public ResponseEntity<?> getFamilyGroupMembers() {
        try {
            Long userId = userContext.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("code", 401, "message", "用户未认证"));
            }
            
            log.info("【家庭组】用户{} 查询家庭组成员列表", userId);
            
            FamilyGroup familyGroup = familyGroupService.getFamilyGroupByUserId(userId);
            if (familyGroup == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("code", 404, "message", "用户未加入任何家庭组"));
            }
            
            java.util.List<FamilyMember> members = familyMemberService.getFamilyMembersByGroupId(familyGroup.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", members);
            response.put("count", members.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("【家庭组】查询成员列表失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", 500, "message", "查询失败：" + e.getMessage()));
        }
    }
    
    /**
     * 创建新的家庭组
     * 当前用户将成为该家庭组的创建者和管理员
     * 
     * @param familyGroupData 包含家庭组名称等信息的数据
     * @return 创建的家庭组信息
     */
    @PostMapping("/create")
    public ResponseEntity<?> createFamilyGroup(@RequestBody Map<String, Object> familyGroupData) {
        try {
            Long userId = userContext.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("code", 401, "message", "用户未认证"));
            }
            
            String familyName = (String) familyGroupData.get("name");
            if (familyName == null || familyName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("code", 400, "message", "家庭组名称不能为空"));
            }
            
            Boolean bringExistingData = (Boolean) familyGroupData.get("bringExistingData");
            if (bringExistingData == null) {
                bringExistingData = false;
            }
            
            log.info("【家庭组】用户{} 创建家庭组: {}, 是否带入现有数据: {}", userId, familyName, bringExistingData);
            
            // 检查用户是否已经在一个家庭组中
            FamilyGroup existingGroup = familyGroupService.getFamilyGroupByUserId(userId);
            if (existingGroup != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("code", 400, "message", "您已经加入了一个家庭组，无法同时加入多个家庭组"));
            }
            
            // 创建新家庭组
            FamilyGroup newFamily = familyGroupService.createFamilyGroup(userId, familyName);
            
            // 为创建者创建管理员身份的 FamilyMember 记录
            FamilyMember creatorMember = new FamilyMember();
            creatorMember.setFamilyGroupId(newFamily.getId());
            creatorMember.setUserId(userId);
            creatorMember.setRole("ADMIN");
            // 管理员拥有所有权限
            creatorMember.setPermissions(PermissionConstants.ADMIN_PERMISSIONS);
            creatorMember.setStatus(1);
            
            familyMemberService.saveFamilyMember(creatorMember);
            
            // 根据用户选择，决定是否带入现有数据
            if (bringExistingData) {
                // 更新用户的所有收入和支出记录的家庭组ID
                incomeMapper.updateFamilyGroupId(userId, newFamily.getId());
                expenseMapper.updateFamilyGroupId(userId, newFamily.getId());
                
                // 更新用户的所有收入和支出分类的家庭组ID（包括系统内置的"待分类"分类）
                incomeCategoryMapper.updateAllCategoriesFamilyGroupId(userId, newFamily.getId());
                expenseCategoryMapper.updateAllCategoriesFamilyGroupId(userId, newFamily.getId());
                
                log.info("【家庭组】用户ID: {} 创建家庭组ID: {} 后，将现有数据(收入、支出、分类)转移到家庭组", 
                        userId, newFamily.getId());
            } else {
                log.info("【家庭组】用户ID: {} 创建家庭组ID: {} 后，未带入现有数据", userId, newFamily.getId());
            }
            
            log.info("【家庭组】为创建者用户ID: {} 创建了管理员身份的成员记录", userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", newFamily);
            response.put("message", "家庭组创建成功");
            
            log.info("【家庭组】家庭组创建成功，ID: {}, 名称: {}", newFamily.getId(), newFamily.getName());
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            log.warn("【家庭组】创建家庭组失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("code", 400, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("【家庭组】创建家庭组异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", 500, "message", "创建失败：" + e.getMessage()));
        }
    }
}
