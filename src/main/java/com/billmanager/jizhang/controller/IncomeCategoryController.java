package com.billmanager.jizhang.controller;

import com.billmanager.jizhang.dto.ApiResponse;
import com.billmanager.jizhang.dto.IncomeCategoryRequest;
import com.billmanager.jizhang.entity.IncomeCategory;
import com.billmanager.jizhang.entity.User;
import com.billmanager.jizhang.service.IncomeCategoryService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class IncomeCategoryController {
    
    private final IncomeCategoryService incomeCategoryService;
    
    @GetMapping("/income-category")
    public String categoryPage(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        List<IncomeCategory> categories = incomeCategoryService.findByUserId(user.getId());
        model.addAttribute("categories", categories);
        model.addAttribute("user", user);
        
        return "income-category";
    }
    
    @PostMapping("/api/income-category")
    @ResponseBody
    public ApiResponse<IncomeCategory> add(@Valid @RequestBody IncomeCategoryRequest request, HttpSession session) {
        User user = (User) session.getAttribute("user");
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
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        IncomeCategory category = incomeCategoryService.update(id, request, user.getId());
        return ApiResponse.success("更新成功", category);
    }
    
    @DeleteMapping("/api/income-category/{id}")
    @ResponseBody
    public ApiResponse<Void> delete(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        incomeCategoryService.delete(id, user.getId());
        return ApiResponse.success("删除成功", null);
    }
    
    @GetMapping("/api/income-category")
    @ResponseBody
    public ApiResponse<List<IncomeCategory>> list(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        List<IncomeCategory> categories = incomeCategoryService.findByUserId(user.getId());
        return ApiResponse.success("查询成功", categories);
    }
}
