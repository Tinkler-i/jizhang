package com.billmanager.jizhang.controller;

import com.billmanager.jizhang.dto.ApiResponse;
import com.billmanager.jizhang.dto.ExpenseRequest;
import com.billmanager.jizhang.dto.ExpenseStatistics;
import com.billmanager.jizhang.entity.Expense;
import com.billmanager.jizhang.entity.User;
import com.billmanager.jizhang.mapper.UserMapper;
import com.billmanager.jizhang.service.ExpenseService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 支出管理控制器
 * 
 * 权限说明：
 * - 所有API都需要用户登录
 * - 具体的数据访问权限由Service层控制
 * - 权限不足时会抛出 FamilyPermissionException
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ExpenseController {
    
    private final ExpenseService expenseService;
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
    
    @GetMapping("/expense")
    public String expensePage(Model model, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        
        List<Expense> expenses = expenseService.findByUserId(user.getId());
        model.addAttribute("expenses", expenses);
        model.addAttribute("user", user);
        
        return "expense";
    }
    
    /**
     * 创建支出记录
     * 需要 expense_edit 权限
     */
    @PostMapping("/api/expense")
    @ResponseBody
    public ApiResponse<Expense> add(@Valid @RequestBody ExpenseRequest request, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        log.info("【API】用户 {} 创建支出记录", user.getId());
        Expense expense = expenseService.add(request, user.getId());
        return ApiResponse.success("添加成功", expense);
    }
    
    /**
     * 更新支出记录
     * 需要 expense_edit 权限，普通成员只能编辑自己的数据
     */
    @PutMapping("/api/expense/{id}")
    @ResponseBody
    public ApiResponse<Expense> update(@PathVariable Long id, 
                                        @Valid @RequestBody ExpenseRequest request, 
                                        HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        log.info("【API】用户 {} 更新支出记录 {}", user.getId(), id);
        Expense expense = expenseService.update(id, request, user.getId());
        return ApiResponse.success("更新成功", expense);
    }
    
    /**
     * 删除支出记录
     * 需要 expense_edit 权限，普通成员只能删除自己的数据
     */
    @DeleteMapping("/api/expense/{id}")
    @ResponseBody
    public ApiResponse<Void> delete(@PathVariable Long id, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        log.info("【API】用户 {} 删除支出记录 {}", user.getId(), id);
        expenseService.delete(id, user.getId());
        return ApiResponse.success("删除成功", null);
    }
    
    /**
     * 查询单条支出记录
     * 需要 expense_view 权限或数据属于自己
     */
    @GetMapping("/api/expense/{id}")
    @ResponseBody
    public ApiResponse<Expense> get(@PathVariable Long id, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        log.debug("【API】用户 {} 查询支出记录 {}", user.getId(), id);
        Expense expense = expenseService.findById(id, user.getId());
        return ApiResponse.success("查询成功", expense);
    }
    
    /**
     * 查询支出列表
     * 根据用户权限返回可访问的数据
     */
    @GetMapping("/api/expense")
    @ResponseBody
    public ApiResponse<List<Expense>> list(HttpSession session,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                            @RequestParam(required = false) Long categoryId,
                                            @RequestParam(required = false) String keyword) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        log.debug("【API】用户 {} 查询支出列表", user.getId());
        
        List<Expense> expenses;
        if (categoryId != null) {
            expenses = expenseService.findByUserIdAndCategoryId(user.getId(), categoryId);
        } else if (startDate != null && endDate != null) {
            expenses = expenseService.findByUserIdAndDateRange(user.getId(), startDate, endDate);
        } else {
            expenses = expenseService.findByUserId(user.getId());
        }
        
        // 按关键字过滤描述
        if (keyword != null && !keyword.isEmpty()) {
            final String key = keyword.toLowerCase();
            expenses = expenses.stream()
                    .filter(e -> e.getDescription() != null && e.getDescription().toLowerCase().contains(key))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        return ApiResponse.success("查询成功", expenses);
    }
    
    /**
     * 查询支出统计
     */
    @GetMapping("/api/expense/statistics")
    @ResponseBody
    public ApiResponse<ExpenseStatistics> statistics(HttpSession session,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        log.debug("【API】用户 {} 查询支出统计", user.getId());
        ExpenseStatistics statistics = expenseService.getStatistics(user.getId(), startDate, endDate);
        return ApiResponse.success("查询成功", statistics);
    }
}
