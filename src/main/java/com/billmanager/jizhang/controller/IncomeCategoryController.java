package com.billmanager.jizhang.controller;

import com.billmanager.jizhang.dto.ApiResponse;
import com.billmanager.jizhang.dto.IncomeCategoryRequest;
import com.billmanager.jizhang.entity.IncomeCategory;
import com.billmanager.jizhang.entity.User;
import com.billmanager.jizhang.mapper.UserMapper;
import com.billmanager.jizhang.service.IncomeCategoryService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class IncomeCategoryController {
    
    private final IncomeCategoryService incomeCategoryService;
    private final UserMapper userMapper;
    
    private User getCurrentUser(HttpSession session) {
        // 先从Session中获取
        User user = (User) session.getAttribute("user");
        if (user != null) {
            return user;
        }
        
        // 如果Session中没有，尝试从SecurityContext获取用户名
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String username = auth.getName();
            // 从数据库查询用户信息
            user = userMapper.findByUsername(username);
            if (user != null) {
                // 缓存到Session中供后续使用
                session.setAttribute("user", user);
                return user;
            }
        }
        
        return null;
    }
    
    @GetMapping("/income-category")
    public String categoryPage(Model model, HttpSession session) {
        try {
            User user = getCurrentUser(session);
            if (user == null) {
                return "redirect:/login";
            }
            
            List<IncomeCategory> categories = incomeCategoryService.findByUserId(user.getId());
            model.addAttribute("categories", categories);
            model.addAttribute("user", user);
            
            return "income-category";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/login";
        }
    }
    
    @PostMapping("/api/income-category")
    @ResponseBody
    public ApiResponse<IncomeCategory> add(@Valid @RequestBody IncomeCategoryRequest request, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        IncomeCategory category = incomeCategoryService.add(request, user.getId());
        return ApiResponse.success("添加成功", category);
    }
    
    @PutMapping("/api/income-category/{id}")
    @ResponseBody
    public ApiResponse<IncomeCategory> update(@PathVariable Long id, 
                                                  @Valid @RequestBody IncomeCategoryRequest request, 
                                                  HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        IncomeCategory category = incomeCategoryService.update(id, request, user.getId());
        return ApiResponse.success("更新成功", category);
    }
    
    @DeleteMapping("/api/income-category/{id}")
    @ResponseBody
    public ApiResponse<Void> delete(@PathVariable Long id, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        incomeCategoryService.delete(id, user.getId());
        return ApiResponse.success("删除成功", null);
    }
    
    @GetMapping("/api/income-category")
    @ResponseBody
    public ApiResponse<List<IncomeCategory>> list(HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        List<IncomeCategory> categories = incomeCategoryService.findByUserId(user.getId());
        return ApiResponse.success("查询成功", categories);
    }
}
