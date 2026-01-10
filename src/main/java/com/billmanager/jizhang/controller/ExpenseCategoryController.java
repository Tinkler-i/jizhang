package com.billmanager.jizhang.controller;

import com.billmanager.jizhang.dto.ApiResponse;
import com.billmanager.jizhang.dto.ExpenseCategoryRequest;
import com.billmanager.jizhang.entity.ExpenseCategory;
import com.billmanager.jizhang.entity.User;
import com.billmanager.jizhang.mapper.UserMapper;
import com.billmanager.jizhang.service.ExpenseCategoryService;
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
public class ExpenseCategoryController {
    
    private final ExpenseCategoryService expenseCategoryService;
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
    
    @GetMapping("/expense-category")
    public String categoryPage(Model model, HttpSession session) {
        try {
            User user = getCurrentUser(session);
            if (user == null) {
                return "redirect:/login";
            }
            
            List<ExpenseCategory> categories = expenseCategoryService.findByUserId(user.getId());
            model.addAttribute("categories", categories);
            model.addAttribute("user", user);
            
            return "expense-category";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/login";
        }
    }
    
    @PostMapping("/api/expense-category")
    @ResponseBody
    public ApiResponse<ExpenseCategory> add(@Valid @RequestBody ExpenseCategoryRequest request, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        ExpenseCategory category = expenseCategoryService.add(request, user.getId());
        return ApiResponse.success("添加成功", category);
    }
    
    @PutMapping("/api/expense-category/{id}")
    @ResponseBody
    public ApiResponse<ExpenseCategory> update(@PathVariable Long id, 
                                                   @Valid @RequestBody ExpenseCategoryRequest request, 
                                                   HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        ExpenseCategory category = expenseCategoryService.update(id, request, user.getId());
        return ApiResponse.success("更新成功", category);
    }
    
    @DeleteMapping("/api/expense-category/{id}")
    @ResponseBody
    public ApiResponse<Void> delete(@PathVariable Long id, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        expenseCategoryService.delete(id, user.getId());
        return ApiResponse.success("删除成功", null);
    }
    
    @GetMapping("/api/expense-category")
    @ResponseBody
    public ApiResponse<List<ExpenseCategory>> list(HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            log.warn("获取支出分类失败: 用户未登录");
            return ApiResponse.error("请先登录");
        }
        
        List<ExpenseCategory> categories = expenseCategoryService.findByUserId(user.getId());
        log.info("查询用户 {} 的支出分类，共 {} 条", user.getId(), categories.size());
        return ApiResponse.success("查询成功", categories);
    }
}
