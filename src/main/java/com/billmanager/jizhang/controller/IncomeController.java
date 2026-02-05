package com.billmanager.jizhang.controller;

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

/**
 * 收入管理控制器
 * 
 * 权限说明：
 * - 所有API都需要用户登录
 * - 具体的数据访问权限由Service层控制
 * - 权限不足时会抛出 FamilyPermissionException
 */
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
    
    /**
     * 创建收入记录
     * 需要 income_edit 权限
     */
    @PostMapping("/api/income")
    @ResponseBody
    public ApiResponse<Income> add(@Valid @RequestBody IncomeRequest request, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        log.info("【API】用户 {} 创建收入记录", user.getId());
        Income income = incomeService.add(request, user.getId());
        return ApiResponse.success("添加成功", income);
    }
    
    /**
     * 更新收入记录
     * 需要 income_edit 权限，普通成员只能编辑自己的数据
     */
    @PutMapping("/api/income/{id}")
    @ResponseBody
    public ApiResponse<Income> update(@PathVariable Long id, 
                                       @Valid @RequestBody IncomeRequest request, 
                                       HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        log.info("【API】用户 {} 更新收入记录 {}", user.getId(), id);
        Income income = incomeService.update(id, request, user.getId());
        return ApiResponse.success("更新成功", income);
    }
    
    /**
     * 删除收入记录
     * 需要 income_edit 权限，普通成员只能删除自己的数据
     */
    @DeleteMapping("/api/income/{id}")
    @ResponseBody
    public ApiResponse<Void> delete(@PathVariable Long id, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        log.info("【API】用户 {} 删除收入记录 {}", user.getId(), id);
        incomeService.delete(id, user.getId());
        return ApiResponse.success("删除成功", null);
    }
    
    /**
     * 查询单条收入记录
     * 需要 income_view 权限或数据属于自己
     */
    @GetMapping("/api/income/{id}")
    @ResponseBody
    public ApiResponse<Income> get(@PathVariable Long id, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        log.debug("【API】用户 {} 查询收入记录 {}", user.getId(), id);
        Income income = incomeService.findById(id, user.getId());
        return ApiResponse.success("查询成功", income);
    }
    
    /**
     * 查询收入列表
     * 根据用户权限返回可访问的数据
     */
    @GetMapping("/api/income")
    @ResponseBody
    public ApiResponse<List<Income>> list(HttpSession session,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                           @RequestParam(required = false) Long categoryId,
                                           @RequestParam(required = false) String keyword) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        log.debug("【API】用户 {} 查询收入列表", user.getId());
        
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
    
    /**
     * 查询收入统计
     */
    @GetMapping("/api/income/statistics")
    @ResponseBody
    public ApiResponse<IncomeStatistics> statistics(HttpSession session,
                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        log.debug("【API】用户 {} 查询收入统计", user.getId());
        IncomeStatistics statistics = incomeService.getStatistics(user.getId(), startDate, endDate);
        return ApiResponse.success("查询成功", statistics);
    }
}
