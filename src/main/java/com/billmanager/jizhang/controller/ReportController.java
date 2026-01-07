package com.billmanager.jizhang.controller;

import com.billmanager.jizhang.dto.ApiResponse;
import com.billmanager.jizhang.dto.BudgetReport;
import com.billmanager.jizhang.dto.CashFlowAnalysis;
import com.billmanager.jizhang.dto.FinancialReport;
import com.billmanager.jizhang.dto.TrendAnalysis;
import com.billmanager.jizhang.entity.User;
import com.billmanager.jizhang.mapper.UserMapper;
import com.billmanager.jizhang.service.ReportService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 报表控制器
 * 处理财务报表、预算报表、现金流分析等请求
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    
    private final ReportService reportService;
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
     * 生成财务报表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 财务报表
     */
    @GetMapping("/financial")
    public ApiResponse<FinancialReport> getFinancialReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpSession session) {
        
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        // 默认为本月
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        FinancialReport report = reportService.generateFinancialReport(user.getId(), startDate, endDate);
        return ApiResponse.success("获取财务报表成功", report);
    }
    
    /**
     * 生成预算执行情况报表
     * @param budgetMonth 预算月份 (YYYY-MM)
     * @return 预算报表
     */
    @GetMapping("/budget")
    public ApiResponse<BudgetReport> getBudgetReport(
            @RequestParam(required = false) String budgetMonth,
            HttpSession session) {
        
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        // 默认为当前月份
        if (budgetMonth == null) {
            budgetMonth = LocalDate.now().toString().substring(0, 7);
        }
        
        BudgetReport report = reportService.generateBudgetReport(user.getId(), budgetMonth);
        return ApiResponse.success("获取预算报表成功", report);
    }
    
    /**
     * 生成现金流分析报告
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 现金流分析
     */
    @GetMapping("/cashflow")
    public ApiResponse<CashFlowAnalysis> getCashFlowAnalysis(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpSession session) {
        
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        // 默认为最近30天
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate == null) {
            startDate = endDate.minusDays(30);
        }
        
        CashFlowAnalysis analysis = reportService.generateCashFlowAnalysis(user.getId(), startDate, endDate);
        return ApiResponse.success("获取现金流分析成功", analysis);
    }
    
    /**
     * 生成收支趋势预测
     * @param monthCount 用于分析的历史月数
     * @return 趋势分析
     */
    @GetMapping("/trend")
    public ApiResponse<TrendAnalysis> getTrendAnalysis(
            @RequestParam(defaultValue = "6") Integer monthCount,
            HttpSession session) {
        
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        if (monthCount < 2) {
            monthCount = 6;
        }
        
        TrendAnalysis analysis = reportService.generateTrendAnalysis(user.getId(), monthCount);
        return ApiResponse.success("获取趋势分析成功", analysis);
    }
}
