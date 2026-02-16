package com.billmanager.jizhang.controller;

import com.billmanager.jizhang.entity.User;
import com.billmanager.jizhang.entity.UserTarget;
import com.billmanager.jizhang.mapper.UserMapper;
import com.billmanager.jizhang.service.UserTargetService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.YearMonth;
import java.util.List;

/**
 * 目标管理控制器
 * 处理用户目标管理页面的路由
 */
@Slf4j
@Controller
@RequestMapping
@RequiredArgsConstructor
public class TargetController {
    
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
     * 目标管理页面
     */
    @GetMapping("/target")
    public String targetPage(Model model, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        
        String currentMonth = YearMonth.now().toString();
        try {
            // 获取当前月份的目标
            UserTarget currentTarget = userTargetService.findByUserIdAndMonth(user.getId(), currentMonth);
            
            // 获取用户的所有目标
            List<UserTarget> allTargets = userTargetService.findByUserId(user.getId());
            
            model.addAttribute("currentMonth", currentMonth);
            model.addAttribute("currentTarget", currentTarget);
            model.addAttribute("allTargets", allTargets);
            model.addAttribute("user", user);
        } catch (Exception e) {
            log.error("获取目标数据失败", e);
            model.addAttribute("error", "获取目标数据失败: " + e.getMessage());
        }
        
        return "target";
    }
}
