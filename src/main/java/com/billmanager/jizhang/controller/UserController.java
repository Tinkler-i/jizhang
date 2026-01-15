package com.billmanager.jizhang.controller;

import com.billmanager.jizhang.dto.UserContext;
import com.billmanager.jizhang.entity.User;
import com.billmanager.jizhang.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理 API 控制器
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserContext userContext;
    private final UserMapper userMapper;
    
    /**
     * 获取当前登录用户信息
     * 
     * @return 当前用户信息
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Long userId = userContext.getCurrentUserId();
            log.debug("【当前用户】获取当前用户信息，用户ID: {}", userId);
            
            if (userId == null) {
                log.warn("【当前用户】用户未认证");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("code", 401, "message", "用户未认证"));
            }
            
            User user = userMapper.findById(userId);
            if (user == null) {
                log.warn("【当前用户】用户ID: {} 不存在", userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("code", 404, "message", "用户不存在"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("data", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "nickname", user.getNickname(),
                    "createTime", user.getCreateTime()
            ));
            
            log.debug("【当前用户】成功获取用户信息: {}", user.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("【当前用户】获取当前用户信息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", 500, "message", "获取失败：" + e.getMessage()));
        }
    }
}
