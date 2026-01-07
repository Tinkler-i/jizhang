package com.billmanager.jizhang.controller;

import com.billmanager.jizhang.dto.ApiResponse;
import com.billmanager.jizhang.dto.DashboardData;
import com.billmanager.jizhang.entity.User;
import com.billmanager.jizhang.mapper.UserMapper;
import com.billmanager.jizhang.service.AnalysisService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分析控制器
 * 处理仪表盘和财务分析相关的请求
 */
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {
    
    private final AnalysisService analysisService;
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
     * 获取仪表盘数据
     * @return 仪表盘数据
     */
    @GetMapping("/dashboard")
    public ApiResponse<DashboardData> getDashboard(HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        DashboardData dashboardData = analysisService.getDashboardData(user.getId());
        return ApiResponse.success("获取仪表盘数据成功", dashboardData);
    }
    
    /**
     * 获取财务健康分数
     * @return 健康分数(0-100)
     */
    @GetMapping("/health-score")
    public ApiResponse<Integer> getHealthScore(HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        Integer score = analysisService.calculateFinancialHealthScore(user.getId());
        return ApiResponse.success("获取财务健康分数成功", score);
    }
    
    /**
     * 获取财务建议
     * @return 建议文本
     */
    @GetMapping("/recommendation")
    public ApiResponse<String> getRecommendation(HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        String recommendation = analysisService.getFinancialRecommendation(user.getId());
        return ApiResponse.success("获取财务建议成功", recommendation);
    }
}
