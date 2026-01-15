package com.billmanager.jizhang.controller;

import com.billmanager.jizhang.annotation.FamilyPermission;
import com.billmanager.jizhang.dto.ApiResponse;
import com.billmanager.jizhang.dto.IncomeRequest;
import com.billmanager.jizhang.dto.IncomeStatistics;
import com.billmanager.jizhang.entity.Income;
import com.billmanager.jizhang.entity.User;
import com.billmanager.jizhang.mapper.UserMapper;
import com.billmanager.jizhang.service.IncomeService;
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

@Slf4j
@Controller
@RequiredArgsConstructor
public class IncomeController {
    
    private final IncomeService incomeService;
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
    
    @GetMapping("/income")
    public String incomePage(Model model, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        
        List<Income> incomes = incomeService.findByUserId(user.getId());
        model.addAttribute("incomes", incomes);
        model.addAttribute("user", user);
        
        return "income";
    }
    
    @PostMapping("/api/income")
    @ResponseBody
    @FamilyPermission("income_create")
    public ApiResponse<Income> add(@Valid @RequestBody IncomeRequest request, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        Income income = incomeService.add(request, user.getId());
        return ApiResponse.success("添加成功", income);
    }
    
    @PutMapping("/api/income/{id}")
    @ResponseBody
    @FamilyPermission("income_edit")
    public ApiResponse<Income> update(@PathVariable Long id, 
                                       @Valid @RequestBody IncomeRequest request, 
                                       HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        Income income = incomeService.update(id, request, user.getId());
        return ApiResponse.success("更新成功", income);
    }
    
    @DeleteMapping("/api/income/{id}")
    @ResponseBody
    @FamilyPermission("income_delete")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        incomeService.delete(id, user.getId());
        return ApiResponse.success("删除成功", null);
    }
    
    @GetMapping("/api/income/{id}")
    @ResponseBody
    @FamilyPermission("income_view")
    public ApiResponse<Income> get(@PathVariable Long id, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        try {
            Income income = incomeService.findById(id, user.getId());
            return ApiResponse.success("查询成功", income);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/api/income")
    @ResponseBody
    @FamilyPermission("income_view")
    public ApiResponse<List<Income>> list(HttpSession session,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                           @RequestParam(required = false) Long categoryId,
                                           @RequestParam(required = false) String keyword) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        List<Income> incomes;
        if (categoryId != null) {
            incomes = incomeService.findByUserIdAndCategoryId(user.getId(), categoryId);
        } else if (startDate != null && endDate != null) {
            incomes = incomeService.findByUserIdAndDateRange(user.getId(), startDate, endDate);
        } else {
            incomes = incomeService.findByUserId(user.getId());
        }
        
        // 按关键字过滤描述
        if (keyword != null && !keyword.isEmpty()) {
            final String key = keyword.toLowerCase();
            incomes = incomes.stream()
                    .filter(i -> i.getDescription() != null && i.getDescription().toLowerCase().contains(key))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        return ApiResponse.success("查询成功", incomes);
    }
    
    @GetMapping("/api/income/statistics")
    @ResponseBody
    public ApiResponse<IncomeStatistics> statistics(HttpSession session,
                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        IncomeStatistics statistics = incomeService.getStatistics(user.getId(), startDate, endDate);
        return ApiResponse.success("查询成功", statistics);
    }
}
