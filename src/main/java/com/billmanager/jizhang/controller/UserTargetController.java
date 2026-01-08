package com.billmanager.jizhang.controller;

import com.billmanager.jizhang.dto.ApiResponse;
import com.billmanager.jizhang.entity.User;
import com.billmanager.jizhang.entity.UserTarget;
import com.billmanager.jizhang.mapper.UserMapper;
import com.billmanager.jizhang.mapper.UserTargetMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户目标控制器
 * 处理用户收入目标相关的请求
 */
@RestController
@RequestMapping("/api/user-target")
@RequiredArgsConstructor
public class UserTargetController {
    
    private final UserTargetMapper userTargetMapper;
    private final UserMapper userMapper;
    
    /**
     * 获取当前用户
     */
    private User getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            return user;
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String username = auth.getName();
            user = userMapper.findByUsername(username);
            if (user != null) {
                session.setAttribute("user", user);
                return user;
            }
        }
        
        return null;
    }
    
    /**
     * 获取指定月份的收入目标
     * @param month 年月（格式：YYYY-MM）
     */
    @GetMapping("/{month}")
    public ApiResponse<?> getByMonth(
            @PathVariable String month,
            HttpSession session) {
        
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        try {
            UserTarget target = userTargetMapper.findByUserIdAndMonth(user.getId(), month);
            
            if (target == null) {
                // 如果没有目标，返回默认值
                Map<String, Object> data = new HashMap<>();
                data.put("id", null);
                data.put("incomeTarget", BigDecimal.ZERO);
                data.put("targetMonth", month);
                return ApiResponse.success("获取成功", data);
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", target.getId());
            data.put("incomeTarget", target.getIncomeTarget());
            data.put("targetMonth", target.getTargetMonth());
            
            return ApiResponse.success("获取成功", data);
        } catch (Exception e) {
            return ApiResponse.error("获取失败: " + e.getMessage());
        }
    }
    
    /**
     * 新增或更新收入目标
     * @param month 年月（格式：YYYY-MM）
     * @param request 请求体包含 incomeTarget
     */
    @PutMapping("/{month}")
    public ApiResponse<?> updateTarget(
            @PathVariable String month,
            @RequestBody Map<String, Object> request,
            HttpSession session) {
        
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        try {
            BigDecimal incomeTarget = new BigDecimal(request.get("incomeTarget").toString());
            
            UserTarget existing = userTargetMapper.findByUserIdAndMonth(user.getId(), month);
            
            if (existing == null) {
                // 新增
                UserTarget newTarget = new UserTarget();
                newTarget.setUserId(user.getId());
                newTarget.setTargetMonth(month);
                newTarget.setIncomeTarget(incomeTarget);
                userTargetMapper.insert(newTarget);
                return ApiResponse.success("新增成功", newTarget);
            } else {
                // 更新
                existing.setIncomeTarget(incomeTarget);
                userTargetMapper.update(existing);
                return ApiResponse.success("更新成功", existing);
            }
        } catch (Exception e) {
            return ApiResponse.error("保存失败: " + e.getMessage());
        }
    }
    
    /**
     * 新增收入目标
     * @param request 请求体包含 targetMonth 和 incomeTarget
     */
    @PostMapping
    public ApiResponse<?> createTarget(
            @RequestBody Map<String, Object> request,
            HttpSession session) {
        
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        try {
            String targetMonth = request.get("targetMonth").toString();
            BigDecimal incomeTarget = new BigDecimal(request.get("incomeTarget").toString());
            
            // 检查是否已存在
            UserTarget existing = userTargetMapper.findByUserIdAndMonth(user.getId(), targetMonth);
            if (existing != null) {
                return ApiResponse.error("该月份目标已存在，请使用更新接口");
            }
            
            UserTarget target = new UserTarget();
            target.setUserId(user.getId());
            target.setTargetMonth(targetMonth);
            target.setIncomeTarget(incomeTarget);
            userTargetMapper.insert(target);
            
            return ApiResponse.success("新增成功", target);
        } catch (Exception e) {
            return ApiResponse.error("新增失败: " + e.getMessage());
        }
    }
}
