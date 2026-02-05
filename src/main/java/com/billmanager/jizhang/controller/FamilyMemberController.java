package com.billmanager.jizhang.controller;

import com.billmanager.jizhang.dto.UserContext;
import com.billmanager.jizhang.dto.FamilyMemberDTO;
import com.billmanager.jizhang.dto.JoinFamilyGroupRequest;
import com.billmanager.jizhang.dto.ExitFamilyGroupRequest;
import com.billmanager.jizhang.entity.FamilyMember;
import com.billmanager.jizhang.entity.User;
import com.billmanager.jizhang.exception.BusinessException;
import com.billmanager.jizhang.mapper.UserMapper;
import com.billmanager.jizhang.service.FamilyMemberService;
import com.billmanager.jizhang.service.PermissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 家庭组成员管理API控制器
 * 
 * 提供成员加入、权限管理、成员移除等操作
 * 所有接口都需要用户已认证
 */
@RestController
@RequestMapping("/api/family-members")
@RequiredArgsConstructor
@Slf4j
public class FamilyMemberController {
    
    private final FamilyMemberService familyMemberService;
    private final PermissionService permissionService;
    private final UserContext userContext;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    
    /**
     * 用户加入家庭组
     * 
     * 请求体格式:
     * {
     *   "familyGroupCode": "ABC123"
     * }
     * 
     * @param request 包含家庭组编号的请求
     * @return 加入成功的成员信息
     */
    @PostMapping("/join")
    public ResponseEntity<?> joinFamilyGroup(@RequestBody JoinFamilyGroupRequest request) {
        try {
            Long userId = userContext.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("code", 401, "message", "用户未认证"));
            }
            
            String code = request.getCode();
            if (code == null || code.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("code", 400, "message", "缺少家庭组编号"));
            }
            
            Boolean bringExistingData = request.getBringExistingData();
            if (bringExistingData == null) {
                bringExistingData = false; // 默认不带入数据
            }
            
            log.info("【成员加入】用户{} 申请加入家庭组 {}, 是否带入现有数据: {}", userId, code, bringExistingData);
            
            FamilyMember member = familyMemberService.joinFamilyGroup(userId, code, bringExistingData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", member);
            response.put("message", "成功加入家庭组");
            
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            log.warn("【成员加入】业务异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("code", 400, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("【成员加入】加入家庭组失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", 500, "message", "加入失败：" + e.getMessage()));
        }
    }
    
    /**
     * @deprecated 使用 /api/family-members/join 替代
     */
    @Deprecated
    @PostMapping("/join-old")
    public ResponseEntity<?> joinFamilyGroup(@RequestBody Map<String, String> request) {
        try {
            Long userId = userContext.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("code", 401, "message", "用户未认证"));
            }
            
            String code = request.get("familyGroupCode");
            if (code == null || code.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("code", 400, "message", "缺少家庭组编号"));
            }
            
            log.info("【成员加入】用户{} 申请加入家庭组 {}", userId, code);
            
            FamilyMember member = familyMemberService.joinFamilyGroup(userId, code, false);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", member);
            response.put("message", "成功加入家庭组");
            
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            log.warn("【成员加入】业务异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("code", 400, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("【成员加入】加入家庭组失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", 500, "message", "加入失败：" + e.getMessage()));
        }
    }
    
    /**
     * 获取当前用户在家庭组中的成员信息
     * 
     * @return 当前用户的成员信息（包含权限）
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentMember() {
        try {
            Long userId = userContext.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("code", 401, "message", "用户未认证"));
            }
            
            log.info("【成员查询】用户{} 查询自己的成员信息", userId);
            
            FamilyMember member = familyMemberService.getFamilyMemberByUserId(userId);
            if (member == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("code", 404, "message", "用户未加入任何家庭组"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", member);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("【成员查询】查询成员信息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", 500, "message", "查询失败：" + e.getMessage()));
        }
    }
    
    /**
     * 更新成员权限
     * 需要是家庭组管理员
     * 
     * 请求体格式:
     * {
     *   "memberId": 2,
     *   "permissions": {"income_view":true,"income_edit":false,...}
     * }
     * 
     * @param request 包含成员ID和权限JSON
     * @return 更新后的成员信息
     */
    @PutMapping("/permissions")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> updateMemberPermissions(@RequestBody Map<String, Object> request) {
        try {
            Long userId = userContext.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("code", 401, "message", "用户未认证"));
            }
            
            Long memberId = ((Number) request.get("memberId")).longValue();
            Object permissionsObj = request.get("permissions");
            
            log.info("【权限更新】用户{} 更新成员{}的权限，原始对象类型: {}", userId, memberId, 
                    permissionsObj != null ? permissionsObj.getClass().getName() : "null");
            
            // 验证当前用户是否为管理员
            if (!permissionService.isAdmin(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("code", 403, "message", "只有管理员可以修改权限"));
            }
            
            FamilyMember targetMember = familyMemberService.getFamilyMemberById(memberId);
            if (targetMember == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("code", 404, "message", "成员不存在"));
            }
            
            // 将权限对象转换为 JSON 字符串
            String permissionsJson;
            if (permissionsObj instanceof String) {
                permissionsJson = (String) permissionsObj;
                log.debug("【权限更新】权限已是字符串格式");
            } else if (permissionsObj instanceof Map) {
                permissionsJson = objectMapper.writeValueAsString(permissionsObj);
                log.debug("【权限更新】权限 Map 已转换为 JSON");
            } else {
                permissionsJson = objectMapper.writeValueAsString(permissionsObj);
                log.debug("【权限更新】权限对象已转换为 JSON");
            }
            
            log.debug("【权限更新】最终权限 JSON: {}", permissionsJson);
            
            // 更新权限
            familyMemberService.updateMemberPermissions(memberId, permissionsJson);
            
            FamilyMember updatedMember = familyMemberService.getFamilyMemberById(memberId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", updatedMember);
            response.put("message", "权限已更新");
            
            log.info("【权限更新】成员{}的权限已成功更新", memberId);
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            log.warn("【权限更新】业务异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("code", 400, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("【权限更新】更新权限失败，异常堆栈:", e);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", 500, "message", "更新失败：" + e.getClass().getSimpleName() + " - " + e.getMessage()));
        }
    }
    
    /**
     * 更新成员角色
     * 需要是家庭组管理员
     * 
     * 请求体格式:
     * {
     *   "memberId": 2,
     *   "role": "MEMBER"
     * }
     * 
     * @param request 包含成员ID和新角色
     * @return 更新后的成员信息
     */
    @PutMapping("/role")
    public ResponseEntity<?> updateMemberRole(@RequestBody Map<String, Object> request) {
        try {
            Long userId = userContext.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("code", 401, "message", "用户未认证"));
            }
            
            Long memberId = ((Number) request.get("memberId")).longValue();
            String newRole = (String) request.get("role");
            
            log.info("【角色更新】用户{} 更新成员{}的角色为: {}", userId, memberId, newRole);
            
            // 验证当前用户是否为管理员
            if (!permissionService.isAdmin(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("code", 403, "message", "只有管理员可以修改角色"));
            }
            
            FamilyMember targetMember = familyMemberService.getFamilyMemberById(memberId);
            if (targetMember == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("code", 404, "message", "成员不存在"));
            }
            
            // 验证新角色的有效性
            if (!newRole.equals("ADMIN") && !newRole.equals("MEMBER")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("code", 400, "message", "无效的角色值"));
            }
            
            familyMemberService.updateMemberRole(memberId, newRole);
            
            FamilyMember updatedMember = familyMemberService.getFamilyMemberById(memberId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", updatedMember);
            response.put("message", "角色已更新");
            
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            log.warn("【角色更新】业务异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("code", 400, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("【角色更新】更新角色失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", 500, "message", "更新失败：" + e.getMessage()));
        }
    }
    
    /**
     * 移除家庭组成员
     * 需要是家庭组管理员
     * 
     * @param memberId 要移除的成员ID
     * @return 移除结果
     */
    @PostMapping("/{memberId}/exit")
    public ResponseEntity<?> exitFamilyGroup(@PathVariable Long memberId, @RequestBody ExitFamilyGroupRequest request) {
        try {
            Long userId = userContext.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("code", 401, "message", "用户未认证"));
            }
            
            FamilyMember member = familyMemberService.getFamilyMemberById(memberId);
            if (member == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("code", 404, "message", "成员不存在"));
            }
            
            // 权限检查：允许用户删除自己，或者由管理员删除他人
            boolean isDeletingSelf = member.getUserId().equals(userId);
            boolean isAdmin = permissionService.isAdmin(userId);
            
            if (!isDeletingSelf && !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("code", 403, "message", "只有管理员可以移除其他成员，您只能退出自己"));
            }
            
            Boolean deleteData = request.getDeleteData();
            if (deleteData == null) {
                deleteData = true; // 默认删除数据
            }
            
            log.info("【成员退出】用户{} 退出家庭组, 成员ID: {}, 删除数据: {}", userId, memberId, deleteData);
            
            familyMemberService.removeMember(memberId, deleteData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            if (isDeletingSelf) {
                response.put("message", deleteData ? "您已退出家庭组，相关数据已删除" : "您已退出家庭组，相关数据已转换为个人数据");
            } else {
                response.put("message", deleteData ? "成员已移除，其相关数据已删除" : "成员已移除，其相关数据已转换为个人数据");
            }
            
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            log.warn("【成员退出】业务异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("code", 400, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("【成员退出】退出家庭组失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", 500, "message", "退出失败：" + e.getMessage()));
        }
    }
    
    /**
     * @deprecated 使用 /api/family-members/{memberId}/exit 替代
     */
    @Deprecated
    @DeleteMapping("/{memberId}")
    public ResponseEntity<?> removeMember(@PathVariable Long memberId) {
        try {
            Long userId = userContext.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("code", 401, "message", "用户未认证"));
            }
            
            log.info("【成员移除】用户{} 移除成员{}", userId, memberId);
            
            FamilyMember member = familyMemberService.getFamilyMemberById(memberId);
            if (member == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("code", 404, "message", "成员不存在"));
            }
            
            // 权限检查：允许用户删除自己，或者由管理员删除他人
            boolean isDelingSelf = member.getUserId().equals(userId);
            boolean isAdmin = permissionService.isAdmin(userId);
            
            if (!isDelingSelf && !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("code", 403, "message", "只有管理员可以移除其他成员，您只能退出自己"));
            }
            
            familyMemberService.removeMember(memberId, true);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", isDelingSelf ? "您已退出家庭组" : "成员已移除");
            
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            log.warn("【成员移除】业务异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("code", 400, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("【成员移除】移除成员失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", 500, "message", "移除失败：" + e.getMessage()));
        }
    }
    
    /**
     * 获取家庭组的所有成员（包含用户昵称和其他信息）
     * 
     * 返回格式:
     * {
     *   "code": 200,
     *   "data": [
     *     {
     *       "id": 1,
     *       "userId": 1,
     *       "familyGroupId": 1,
     *       "username": "user1",
     *       "nickname": "小明",
     *       "role": "ADMIN",
     *       "status": 1,
     *       ...
     *     }
     *   ],
     *   "count": 3
     * }
     * 
     * @return 成员列表（包含用户信息）
     */
    @GetMapping("/list")
    public ResponseEntity<?> getFamilyMembers() {
        try {
            Long userId = userContext.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("code", 401, "message", "用户未认证"));
            }
            
            log.info("【成员列表】用户{} 查询家庭组成员列表", userId);
            
            // 获取当前用户的家庭组
            FamilyMember currentMember = familyMemberService.getFamilyMemberByUserId(userId);
            if (currentMember == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("code", 404, "message", "用户未加入任何家庭组"));
            }
            
            // 获取该家庭组的所有成员
            java.util.List<FamilyMember> members = familyMemberService.getFamilyMembersByGroupId(currentMember.getFamilyGroupId());
            
            // 构建返回的 DTO，包含用户昵称和其他信息
            java.util.List<FamilyMemberDTO> memberDTOs = new java.util.ArrayList<>();
            for (FamilyMember member : members) {
                User user = userMapper.findById(member.getUserId());
                if (user != null) {
                    FamilyMemberDTO dto = new FamilyMemberDTO();
                    dto.setId(member.getId());
                    dto.setFamilyGroupId(member.getFamilyGroupId());
                    dto.setUserId(member.getUserId());
                    dto.setRole(member.getRole());
                    dto.setPermissions(member.getPermissions());
                    dto.setJoinTime(member.getJoinTime());
                    dto.setStatus(member.getStatus());
                    dto.setCreateTime(member.getCreateTime());
                    dto.setUpdateTime(member.getUpdateTime());
                    
                    // 添加用户信息
                    dto.setUsername(user.getUsername());
                    dto.setNickname(user.getNickname());
                    dto.setEmail(user.getEmail());
                    dto.setPhone(user.getPhone());
                    
                    memberDTOs.add(dto);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", memberDTOs);
            response.put("count", memberDTOs.size());
            response.put("currentUserId", userId);  // 明确返回当前用户 ID
            
            log.debug("【成员列表】成功获取成员列表，共 {} 个成员，当前用户ID: {}", memberDTOs.size(), userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("【成员列表】查询成员列表失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", 500, "message", "查询失败：" + e.getMessage()));
        }
    }
    
    /**
     * 获取成员权限详情
     * 返回权限的详细解析信息（包括每个模块的具体权限）
     * 
     * @param memberId 成员ID
     * @return 权限详情
     */
    @GetMapping("/{memberId}/permission-details")
    public ResponseEntity<?> getMemberPermissionDetails(@PathVariable Long memberId) {
        try {
            Long userId = userContext.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("code", 401, "message", "用户未认证"));
            }
            
            log.info("【权限详情】用户{} 查询成员{}的权限详情", userId, memberId);
            
            FamilyMember member = familyMemberService.getFamilyMemberById(memberId);
            if (member == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("code", 404, "message", "成员不存在"));
            }
            
            Map<String, Object> details = familyMemberService.getMemberPermissionDetails(memberId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", details);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("【权限详情】获取权限详情失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", 500, "message", "获取失败：" + e.getMessage()));
        }
    }
    
    /**
     * 检查权限冲突
     * 
     * 请求体格式：
     * {
     *   "permissions": "{\"income\": {\"view\": true, \"create\": true, \"edit\": true, \"delete\": false}, ...}"
     * }
     * 
     * @param request 包含权限JSON的请求
     * @return 冲突列表
     */
    @PostMapping("/check-permission-conflicts")
    public ResponseEntity<?> checkPermissionConflicts(@RequestBody Map<String, String> request) {
        try {
            Long userId = userContext.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("code", 401, "message", "用户未认证"));
            }
            
            String permissionsJson = request.get("permissions");
            if (permissionsJson == null || permissionsJson.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("code", 400, "message", "缺少权限JSON"));
            }
            
            log.info("【权限冲突检查】用户{} 检查权限冲突", userId);
            
            java.util.List<String> conflicts = familyMemberService.checkPermissionConflicts(permissionsJson);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("hasConflicts", !conflicts.isEmpty());
            response.put("conflicts", conflicts);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("【权限冲突检查】检查权限冲突失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", 500, "message", "检查失败：" + e.getMessage()));
        }
    }
    
    /**
     * 批量更新成员权限
     * 
     * 请求体格式：
     * {
     *   "memberIds": [1, 2, 3],
     *   "permissions": "{\"income_view\":true,\"income_edit\":false,...}"
     * }
     * 
     * @param request 包含成员ID列表和权限JSON
     * @return 更新结果
     */
    @PostMapping("/batch/permissions")
    public ResponseEntity<?> batchUpdatePermissions(@RequestBody Map<String, Object> request) {
        try {
            Long userId = userContext.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("code", 401, "message", "用户未认证"));
            }
            
            // 验证管理员权限
            if (!permissionService.isAdmin(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("code", 403, "message", "只有管理员可以批量修改权限"));
            }
            
            @SuppressWarnings("unchecked")
            java.util.List<Long> memberIds = (java.util.List<Long>) request.get("memberIds");
            Object permissionsObj = request.get("permissions");
            
            if (memberIds == null || memberIds.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("code", 400, "message", "成员ID列表为空"));
            }
            
            if (permissionsObj == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("code", 400, "message", "缺少权限参数"));
            }
            
            // 将权限对象转换为 JSON 字符串
            String permissionsJson;
            if (permissionsObj instanceof String) {
                permissionsJson = (String) permissionsObj;
            } else if (permissionsObj instanceof Map) {
                permissionsJson = objectMapper.writeValueAsString(permissionsObj);
            } else {
                permissionsJson = objectMapper.writeValueAsString(permissionsObj);
            }
            
            log.info("【批量权限更新】用户{} 批量更新 {} 个成员的权限", userId, memberIds.size());
            
            // 批量更新
            familyMemberService.batchUpdateMemberPermissions(memberIds, permissionsJson);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "已批量更新 " + memberIds.size() + " 个成员的权限");
            response.put("updatedCount", memberIds.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("【批量权限更新】批量更新失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", 500, "message", "批量更新失败：" + e.getMessage()));
        }
    }
}
