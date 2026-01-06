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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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
    
    @PostMapping("/api/expense")
    @ResponseBody
    public ApiResponse<Expense> add(@Valid @RequestBody ExpenseRequest request, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        Expense expense = expenseService.add(request, user.getId());
        return ApiResponse.success("添加成功", expense);
    }
    
    @PutMapping("/api/expense/{id}")
    @ResponseBody
    public ApiResponse<Expense> update(@PathVariable Long id, 
                                        @Valid @RequestBody ExpenseRequest request, 
                                        HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        Expense expense = expenseService.update(id, request, user.getId());
        return ApiResponse.success("更新成功", expense);
    }
    
    @DeleteMapping("/api/expense/{id}")
    @ResponseBody
    public ApiResponse<Void> delete(@PathVariable Long id, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        expenseService.delete(id, user.getId());
        return ApiResponse.success("删除成功", null);
    }
    
    @GetMapping("/api/expense/{id}")
    @ResponseBody
    public ApiResponse<Expense> get(@PathVariable Long id, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        try {
            Expense expense = expenseService.findById(id, user.getId());
            return ApiResponse.success("查询成功", expense);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/api/expense")
    @ResponseBody
    public ApiResponse<List<Expense>> list(HttpSession session,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                            @RequestParam(required = false) Long categoryId) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        List<Expense> expenses;
        if (categoryId != null) {
            expenses = expenseService.findByUserIdAndCategoryId(user.getId(), categoryId);
        } else if (startDate != null && endDate != null) {
            expenses = expenseService.findByUserIdAndDateRange(user.getId(), startDate, endDate);
        } else {
            expenses = expenseService.findByUserId(user.getId());
        }
        
        return ApiResponse.success("查询成功", expenses);
    }
    
    @GetMapping("/api/expense/statistics")
    @ResponseBody
    public ApiResponse<ExpenseStatistics> statistics(HttpSession session,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        ExpenseStatistics statistics = expenseService.getStatistics(user.getId(), startDate, endDate);
        return ApiResponse.success("查询成功", statistics);
    }
}
