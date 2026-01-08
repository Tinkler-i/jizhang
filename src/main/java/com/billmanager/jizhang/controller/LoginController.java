package com.billmanager.jizhang.controller;

import com.billmanager.jizhang.dto.ApiResponse;
import com.billmanager.jizhang.dto.LoginRequest;
import com.billmanager.jizhang.entity.User;
import com.billmanager.jizhang.mapper.UserMapper;
import com.billmanager.jizhang.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@Controller
@RequiredArgsConstructor
public class LoginController {
    
    private final UserService userService;
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
    
    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }
    
    @PostMapping("/api/login")
    @ResponseBody
    public ApiResponse<User> login(@Valid @RequestBody LoginRequest request, HttpSession session, HttpServletRequest httpRequest) {
        User user = userService.login(request);
        session.setAttribute("user", user);
        
        // 创建Spring Security认证对象，包含权限
        UsernamePasswordAuthenticationToken token = 
            new UsernamePasswordAuthenticationToken(
                user.getUsername(), 
                null, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
        
        // 设置到SecurityContextHolder
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(token);
        
        // 显式保存到Session
        session.setAttribute(
            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
            context
        );
        
        return ApiResponse.success("登录成功", user);
    }
    
    @PostMapping("/api/auth/login")
    @ResponseBody
    public ApiResponse<User> authLogin(@Valid @RequestBody LoginRequest request, HttpSession session, HttpServletRequest httpRequest) {
        User user = userService.login(request);
        session.setAttribute("user", user);
        
        // 创建Spring Security认证对象，包含权限
        UsernamePasswordAuthenticationToken token = 
            new UsernamePasswordAuthenticationToken(
                user.getUsername(), 
                null, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
        
        // 设置到SecurityContextHolder
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(token);
        
        // 显式保存到Session
        session.setAttribute(
            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
            context
        );
        
        return ApiResponse.success("登录成功", user);
    }
    
    /**
     * 获取当前用户信息，用于验证登录状态
     */
    @GetMapping("/api/user/profile")
    @ResponseBody
    public ApiResponse<User> getProfile(HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("未登录或会话已过期");
        }
        System.out.println("【LoginController】获取用户信息成功: " + user.getUsername());
        return ApiResponse.success("获取成功", user);
    }
    
    /**
     * 获取当前用户信息，用于验证登录状态（auth 路径）
     */
    @GetMapping("/api/auth/profile")
    @ResponseBody
    public ApiResponse<User> getAuthProfile(HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("未登录或会话已过期");
        }
        System.out.println("【LoginController】获取用户信息成功: " + user.getUsername());
        return ApiResponse.success("获取成功", user);
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "dashboard";
    }
    
    @GetMapping("/jizhang/dashboard")
    public String dashboardWithPrefix(HttpSession session, Model model) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "dashboard";
    }
    
    @GetMapping("/report")
    public String report(HttpSession session, Model model) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "report";
    }
    
    @GetMapping("/jizhang/report")
    public String reportWithPrefix(HttpSession session, Model model) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "report";
    }
}