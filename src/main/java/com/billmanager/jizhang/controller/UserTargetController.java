package com.billmanager.jizhang.controller;

import com.billmanager.jizhang.dto.ApiResponse;
import com.billmanager.jizhang.entity.User;
import com.billmanager.jizhang.entity.UserTarget;
import com.billmanager.jizhang.exception.BusinessException;
import com.billmanager.jizhang.mapper.UserMapper;
import com.billmanager.jizhang.service.UserTargetService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户目标控制器
 * 处理用户收入目标相关的请求
 */
@Slf4j
@RestController
@RequestMapping("/api/user-target")
@RequiredArgsConstructor
public class UserTargetController {
    
    private final UserTargetService userTargetService;
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
            UserTarget target = userTargetService.findByUserIdAndMonth(user.getId(), month);
            
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
            log.error("获取目标失败", e);
            return ApiResponse.error("获取失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户的所有目标
     */
    @GetMapping
    public ApiResponse<?> getAllTargets(HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        try {
            List<UserTarget> targets = userTargetService.findByUserId(user.getId());
            return ApiResponse.success("获取成功", targets);
        } catch (Exception e) {
            log.error("获取目标列表失败", e);
            return ApiResponse.error("获取失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户在某个月份范围内的目标
     * @param startMonth 起始年月（格式：YYYY-MM）
     * @param endMonth 结束年月（格式：YYYY-MM）
     */
    @GetMapping("/range")
    public ApiResponse<?> getTargetsByRange(
            @RequestParam String startMonth,
            @RequestParam String endMonth,
            HttpSession session) {
        
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        try {
            List<UserTarget> targets = userTargetService.findByUserIdAndMonthRange(
                    user.getId(), startMonth, endMonth);
            return ApiResponse.success("获取成功", targets);
        } catch (BusinessException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("获取目标范围失败", e);
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
            UserTarget target = userTargetService.createOrUpdate(user.getId(), month, incomeTarget);
            return ApiResponse.success("保存成功", target);
        } catch (BusinessException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("保存目标失败", e);
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
            
            UserTarget target = new UserTarget();
            target.setUserId(user.getId());
            target.setTargetMonth(targetMonth);
            target.setIncomeTarget(incomeTarget);
            target = userTargetService.create(target);
            
            return ApiResponse.success("新增成功", target);
        } catch (BusinessException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("新增目标失败", e);
            return ApiResponse.error("新增失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除目标
     * @param id 目标ID
     */
    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteTarget(
            @PathVariable Long id,
            HttpSession session) {
        
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        try {
            userTargetService.delete(id, user.getId());
            return ApiResponse.success("删除成功");
        } catch (BusinessException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("删除目标失败", e);
            return ApiResponse.error("删除失败: " + e.getMessage());
        }
    }
}
