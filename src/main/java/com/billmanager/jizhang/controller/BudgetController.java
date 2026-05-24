package com.billmanager.jizhang.controller;

import com.billmanager.jizhang.dto.ApiResponse;
import com.billmanager.jizhang.dto.BudgetRequest;
import com.billmanager.jizhang.dto.BudgetStatistics;
import com.billmanager.jizhang.entity.Budget;
import com.billmanager.jizhang.entity.User;
import com.billmanager.jizhang.mapper.UserMapper;
import com.billmanager.jizhang.service.BudgetService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class BudgetController {
    
    private final BudgetService budgetService;
    private final UserMapper userMapper;
    
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
    
    @GetMapping("/budget")
    public String budgetPage(Model model, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        
        String currentMonth = java.time.YearMonth.now().toString();
        List<BudgetStatistics> statistics = budgetService.getStatistics(user.getId(), currentMonth);
        
        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("budgets", statistics);
        model.addAttribute("user", user);
        
        return "budget";
    }
    
    @GetMapping("/api/budget/month/{budgetMonth}")
    @ResponseBody
    public ApiResponse<List<BudgetStatistics>> getBudgetsByMonth(
            @PathVariable String budgetMonth,
            HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("未登录");
        }
        
        List<BudgetStatistics> statistics = budgetService.getStatistics(user.getId(), budgetMonth);
        return ApiResponse.success("获取成功", statistics);
    }
    
    @PostMapping("/api/budget")
    @ResponseBody
    public ApiResponse<Budget> addBudget(
            @Valid @RequestBody BudgetRequest request,
            HttpSession session) {
        log.info("【预算创建】收到请求: categoryId={}, amount={}, budgetMonth={}", 
                request.getCategoryId(), request.getAmount(), request.getBudgetMonth());
        
        User user = getCurrentUser(session);
        if (user == null) {
            log.warn("【预算创建】用户未登录");
            return ApiResponse.error("未登录");
        }
        log.info("【预算创建】当前用户: id={}, username={}", user.getId(), user.getUsername());
        
        try {
            Budget budget = budgetService.add(request, user.getId());
            log.info("【预算创建】成功: id={}", budget.getId());
            return ApiResponse.success("添加成功", budget);
        } catch (Exception e) {
            log.error("【预算创建】失败: {}", e.getMessage(), e);
            return ApiResponse.error("添加失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/api/budget/{id}")
    @ResponseBody
    public ApiResponse<Budget> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request,
            HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("未登录");
        }
        
        try {
            Budget budget = budgetService.update(id, request, user.getId());
            return ApiResponse.success("修改成功", budget);
        } catch (Exception e) {
            return ApiResponse.error("修改失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/api/budget/{id}")
    @ResponseBody
    public ApiResponse<Void> deleteBudget(
            @PathVariable Long id,
            HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("未登录");
        }
        
        try {
            budgetService.delete(id, user.getId());
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error("删除失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/api/budget/{id}")
    @ResponseBody
    public ApiResponse<BudgetStatistics> getBudgetDetails(
            @PathVariable Long id,
            HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("未登录");
        }
        
        try {
            BudgetStatistics statistics = budgetService.getStatisticsById(id, user.getId());
            if (statistics == null) {
                return ApiResponse.error("预算不存在");
            }
            return ApiResponse.success("获取成功", statistics);
        } catch (Exception e) {
            return ApiResponse.error("获取失败: " + e.getMessage());
        }
    }
}
