package com.billmanager.jizhang.controller;

import com.billmanager.jizhang.dto.ApiResponse;
import com.billmanager.jizhang.dto.BudgetReport;
import com.billmanager.jizhang.dto.CashFlowAnalysis;
import com.billmanager.jizhang.dto.FinancialReport;
import com.billmanager.jizhang.dto.TrendAnalysis;
import com.billmanager.jizhang.entity.User;
import com.billmanager.jizhang.entity.Expense;
import com.billmanager.jizhang.entity.Income;
import com.billmanager.jizhang.entity.Budget;
import com.billmanager.jizhang.entity.UserTarget;
import com.billmanager.jizhang.mapper.UserMapper;
import com.billmanager.jizhang.mapper.UserTargetMapper;
import com.billmanager.jizhang.mapper.IncomeMapper;
import com.billmanager.jizhang.mapper.ExpenseMapper;
import com.billmanager.jizhang.mapper.BudgetMapper;
import com.billmanager.jizhang.mapper.IncomeCategoryMapper;
import com.billmanager.jizhang.mapper.ExpenseCategoryMapper;
import com.billmanager.jizhang.entity.IncomeCategory;
import com.billmanager.jizhang.entity.ExpenseCategory;
import com.billmanager.jizhang.service.ReportService;
import com.billmanager.jizhang.service.IncomeService;
import com.billmanager.jizhang.service.ExpenseService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.math.BigDecimal;
import java.util.List;

/**
 * 报表控制器
 * 处理财务报表、预算报表、现金流分析等请求
 */
@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    
    private final ReportService reportService;
    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final UserMapper userMapper;
    private final UserTargetMapper userTargetMapper;
    private final IncomeMapper incomeMapper;
    private final ExpenseMapper expenseMapper;
    private final BudgetMapper budgetMapper;
    private final IncomeCategoryMapper incomeCategoryMapper;
    private final ExpenseCategoryMapper expenseCategoryMapper;
    
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
     * 获取仪表盘摘要数据 (支持月度和年度查询)
     */
    @GetMapping("/summary")
    public ApiResponse<?> getSummary(
            @RequestParam String month,
            HttpSession session) {
        
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        System.out.println("【ReportController】获取摘要数据，时间参数: " + month);
        
        try {
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            
            if (month.length() == 4) {
                // 年度查询: 格式为 "2026"
                int year = Integer.parseInt(month);
                LocalDate yearStart = LocalDate.of(year, 1, 1);
                LocalDate yearEnd = LocalDate.of(year, 12, 31);
                
                System.out.println("【ReportController】年度查询，年份: " + year);
                
                // 获取全年收入
                List<Income> yearIncomes = incomeService.findByUserIdAndDateRange(user.getId(), yearStart, yearEnd);
                BigDecimal totalIncome = yearIncomes.stream()
                        .map(Income::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                // 获取全年支出
                List<Expense> yearExpenses = expenseService.findByUserIdAndDateRange(user.getId(), yearStart, yearEnd);
                BigDecimal totalExpense = yearExpenses.stream()
                        .map(Expense::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                // 获取全年预算总额 (需要聚合12个月的预算)
                BigDecimal budgetExpense = BigDecimal.ZERO;
                for (int m = 1; m <= 12; m++) {
                    String monthStr = String.format("%d-%02d", year, m);
                    List<Budget> budgets = budgetMapper.findByUserIdAndYearMonth(user.getId(), monthStr);
                    BigDecimal monthBudget = budgets.stream()
                            .map(Budget::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    budgetExpense = budgetExpense.add(monthBudget);
                }
                
                // 获取年度目标 (聚合全年12个月的目标)
                BigDecimal targetIncome = BigDecimal.ZERO;
                for (int m = 1; m <= 12; m++) {
                    String monthStr = String.format("%d-%02d", year, m);
                    UserTarget target = userTargetMapper.findByUserIdAndMonth(user.getId(), monthStr);
                    if (target != null) {
                        targetIncome = targetIncome.add(target.getIncomeTarget());
                    }
                }
                
                // 计算预算使用率
                BigDecimal budgetUsage = budgetExpense.compareTo(BigDecimal.ZERO) > 0
                        ? totalExpense.divide(budgetExpense, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                        : new BigDecimal("0");
                
                // 计算利润率
                BigDecimal profitRate = totalIncome.compareTo(BigDecimal.ZERO) > 0
                        ? totalIncome.subtract(totalExpense)
                            .divide(totalIncome, 4, java.math.RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"))
                        : new BigDecimal("0");
                
                data.put("totalIncome", totalIncome);
                data.put("totalExpense", totalExpense);
                data.put("targetIncome", targetIncome);
                data.put("budgetExpense", budgetExpense);
                data.put("budgetUsage", budgetUsage.setScale(2, java.math.RoundingMode.HALF_UP) + "%");
                data.put("profitRate", profitRate.setScale(2, java.math.RoundingMode.HALF_UP) + "%");
                
            } else {
                // 月度查询: 格式为 "2026-02"
                YearMonth ym = YearMonth.parse(month);
                LocalDate monthStart = ym.atDay(1);
                LocalDate monthEnd = ym.atEndOfMonth();
                
                System.out.println("【ReportController】月度查询，月份: " + month);
                
                // 获取本月收入
                List<Income> monthIncomes = incomeService.findByUserIdAndDateRange(user.getId(), monthStart, monthEnd);
                BigDecimal totalIncome = monthIncomes.stream()
                        .map(Income::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                // 获取本月支出
                List<Expense> monthExpenses = expenseService.findByUserIdAndDateRange(user.getId(), monthStart, monthEnd);
                BigDecimal totalExpense = monthExpenses.stream()
                        .map(Expense::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                // 获取本月收入目标
                UserTarget target = userTargetMapper.findByUserIdAndMonth(user.getId(), month);
                BigDecimal targetIncome = target != null ? target.getIncomeTarget() : BigDecimal.ZERO;
                
                // 获取本月预算总额
                List<Budget> budgets = budgetMapper.findByUserIdAndYearMonth(user.getId(), month);
                BigDecimal budgetExpense = budgets.stream()
                        .map(Budget::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                // 计算预算使用率
                BigDecimal budgetUsage = budgetExpense.compareTo(BigDecimal.ZERO) > 0
                        ? totalExpense.divide(budgetExpense, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                        : new BigDecimal("0");
                
                // 计算利润率
                BigDecimal profitRate = totalIncome.compareTo(BigDecimal.ZERO) > 0
                        ? totalIncome.subtract(totalExpense)
                            .divide(totalIncome, 4, java.math.RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"))
                        : new BigDecimal("0");
                
                data.put("totalIncome", totalIncome);
                data.put("totalExpense", totalExpense);
                data.put("targetIncome", targetIncome);
                data.put("budgetExpense", budgetExpense);
                data.put("budgetUsage", budgetUsage.setScale(2, java.math.RoundingMode.HALF_UP) + "%");
                data.put("profitRate", profitRate.setScale(2, java.math.RoundingMode.HALF_UP) + "%");
            }
            
            System.out.println("【ReportController】返回摘要数据: " + data);
            return ApiResponse.success("获取摘要成功", data);
        } catch (Exception e) {
            System.err.println("【ReportController】获取摘要失败: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error("获取摘要失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取收入趋势图表数据 (支持月度和年度)
     */
    @GetMapping("/income-chart/{month}")
    public ApiResponse<?> getIncomeChart(
            @PathVariable String month,
            HttpSession session) {
        
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        System.out.println("【ReportController】获取收入图表，时间参数: " + month);
        
        try {
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            
            if (month.length() == 4) {
                // 年度查询: 格式为 "2026"，返回每月的汇总数据
                int year = Integer.parseInt(month);
                
                java.util.List<String> labels = new java.util.ArrayList<>();
                java.util.List<BigDecimal> incomeList = new java.util.ArrayList<>();
                java.util.List<BigDecimal> expenseList = new java.util.ArrayList<>();
                java.util.Map<Integer, BigDecimal> monthlyIncome = new java.util.HashMap<>();
                java.util.Map<Integer, BigDecimal> monthlyExpense = new java.util.HashMap<>();
                
                // 初始化12个月的标签和数据
                for (int m = 1; m <= 12; m++) {
                    labels.add(m + "月");
                    monthlyIncome.put(m, BigDecimal.ZERO);
                    monthlyExpense.put(m, BigDecimal.ZERO);
                }
                
                LocalDate yearStart = LocalDate.of(year, 1, 1);
                LocalDate yearEnd = LocalDate.of(year, 12, 31);
                
                // 获取全年收入并按月汇总
                List<Income> yearIncomes = incomeService.findByUserIdAndDateRange(user.getId(), yearStart, yearEnd);
                for (Income income : yearIncomes) {
                    int month_num = income.getTransactionDate().getMonthValue();
                    monthlyIncome.put(month_num, monthlyIncome.get(month_num).add(income.getAmount()));
                }
                
                // 获取全年支出并按月汇总
                List<Expense> yearExpenses = expenseService.findByUserIdAndDateRange(user.getId(), yearStart, yearEnd);
                for (Expense expense : yearExpenses) {
                    int month_num = expense.getTransactionDate().getMonthValue();
                    monthlyExpense.put(month_num, monthlyExpense.get(month_num).add(expense.getAmount()));
                }
                
                // 收集所有月份的数据
                for (int m = 1; m <= 12; m++) {
                    incomeList.add(monthlyIncome.get(m));
                    expenseList.add(monthlyExpense.get(m));
                }
                
                data.put("labels", labels);
                data.put("income", incomeList);
                data.put("expense", expenseList);
                
            } else {
                // 月度查询: 格式为 "2026-02"，返回每日的数据
                YearMonth ym = YearMonth.parse(month);
                LocalDate monthStart = ym.atDay(1);
                LocalDate monthEnd = ym.atEndOfMonth();
                int daysInMonth = monthEnd.getDayOfMonth();
                
                // 初始化日期标签和数据数组
                java.util.List<String> labels = new java.util.ArrayList<>();
                java.util.Map<Integer, BigDecimal> dailyIncome = new java.util.HashMap<>();
                java.util.Map<Integer, BigDecimal> dailyExpense = new java.util.HashMap<>();
                
                for (int i = 1; i <= daysInMonth; i++) {
                    labels.add(i + "日");
                    dailyIncome.put(i, BigDecimal.ZERO);
                    dailyExpense.put(i, BigDecimal.ZERO);
                }
                
                // 获取本月收入并按日期汇总
                List<Income> monthIncomes = incomeService.findByUserIdAndDateRange(user.getId(), monthStart, monthEnd);
                for (Income income : monthIncomes) {
                    int day = income.getTransactionDate().getDayOfMonth();
                    dailyIncome.put(day, dailyIncome.get(day).add(income.getAmount()));
                }
                
                // 获取本月支出并按日期汇总
                List<Expense> monthExpenses = expenseService.findByUserIdAndDateRange(user.getId(), monthStart, monthEnd);
                for (Expense expense : monthExpenses) {
                    int day = expense.getTransactionDate().getDayOfMonth();
                    dailyExpense.put(day, dailyExpense.get(day).add(expense.getAmount()));
                }
                
                // 构建响应数据
                java.util.List<BigDecimal> incomeList = new java.util.ArrayList<>();
                java.util.List<BigDecimal> expenseList = new java.util.ArrayList<>();
                for (int i = 1; i <= daysInMonth; i++) {
                    incomeList.add(dailyIncome.get(i));
                    expenseList.add(dailyExpense.get(i));
                }
                data.put("labels", labels);
                data.put("income", incomeList);
                data.put("expense", expenseList);
            }
            
            System.out.println("【ReportController】返回收入图表数据成功");
            return ApiResponse.success("获取收入图表成功", data);
        } catch (Exception e) {
            System.err.println("【ReportController】获取收入图表失败: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error("获取收入图表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取收入分类分布图表数据 (支持月度和年度)
     */
    @GetMapping("/income-category-chart/{month}")
    public ApiResponse<?> getIncomeCategoryChart(
            @PathVariable String month,
            HttpSession session) {
        
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        System.out.println("【ReportController】获取收入分类图表，时间参数: " + month);
        
        try {
            List<Income> incomes;
            
            if (month.length() == 4) {
                // 年度查询
                int year = Integer.parseInt(month);
                LocalDate yearStart = LocalDate.of(year, 1, 1);
                LocalDate yearEnd = LocalDate.of(year, 12, 31);
                incomes = incomeService.findByUserIdAndDateRange(user.getId(), yearStart, yearEnd);
            } else {
                // 月度查询
                YearMonth ym = YearMonth.parse(month);
                LocalDate monthStart = ym.atDay(1);
                LocalDate monthEnd = ym.atEndOfMonth();
                incomes = incomeService.findByUserIdAndDateRange(user.getId(), monthStart, monthEnd);
            }
            
            // 按分类汇总
            java.util.Map<Long, BigDecimal> incomeByCategory = new java.util.HashMap<>();
            for (Income income : incomes) {
                Long categoryId = income.getCategoryId();
                incomeByCategory.put(categoryId, 
                    incomeByCategory.getOrDefault(categoryId, BigDecimal.ZERO).add(income.getAmount()));
            }
            
            // 计算总金额
            BigDecimal totalIncome = incomeByCategory.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // 构建响应数据
            java.util.List<String> labels = new java.util.ArrayList<>();
            java.util.List<BigDecimal> values = new java.util.ArrayList<>();
            java.util.List<java.util.Map<String, Object>> details = new java.util.ArrayList<>();
            
            for (java.util.Map.Entry<Long, BigDecimal> entry : incomeByCategory.entrySet()) {
                IncomeCategory category = incomeCategoryMapper.findById(entry.getKey());
                String categoryName = category != null ? category.getName() : "未知分类";
                BigDecimal amount = entry.getValue();
                
                BigDecimal percentage = totalIncome.compareTo(BigDecimal.ZERO) > 0
                        ? amount.divide(totalIncome, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                        : BigDecimal.ZERO;
                
                labels.add(categoryName);
                values.add(amount);
                
                java.util.Map<String, Object> detail = new java.util.HashMap<>();
                detail.put("categoryName", categoryName);
                detail.put("categoryId", entry.getKey());
                detail.put("amount", amount);
                detail.put("percentage", percentage.setScale(2, java.math.RoundingMode.HALF_UP));
                details.add(detail);
            }
            
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("labels", labels);
            data.put("values", values);
            data.put("details", details);
            data.put("total", totalIncome);
            
            System.out.println("【ReportController】返回收入分类图表数据成功");
            return ApiResponse.success("获取收入分类图表成功", data);
        } catch (Exception e) {
            System.err.println("【ReportController】获取收入分类图表失败: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error("获取收入分类图表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取支出分类分布图表数据 (支持月度和年度)
     */
    @GetMapping("/expense-chart/{month}")
    public ApiResponse<?> getExpenseChart(
            @PathVariable String month,
            HttpSession session) {
        
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("请先登录");
        }
        
        System.out.println("【ReportController】获取支出图表，时间参数: " + month);
        
        try {
            List<Expense> expenses;
            
            if (month.length() == 4) {
                // 年度查询
                int year = Integer.parseInt(month);
                LocalDate yearStart = LocalDate.of(year, 1, 1);
                LocalDate yearEnd = LocalDate.of(year, 12, 31);
                expenses = expenseService.findByUserIdAndDateRange(user.getId(), yearStart, yearEnd);
            } else {
                // 月度查询
                YearMonth ym = YearMonth.parse(month);
                LocalDate monthStart = ym.atDay(1);
                LocalDate monthEnd = ym.atEndOfMonth();
                expenses = expenseService.findByUserIdAndDateRange(user.getId(), monthStart, monthEnd);
            }
            
            // 按分类汇总
            java.util.Map<Long, BigDecimal> expenseByCategory = new java.util.HashMap<>();
            for (Expense expense : expenses) {
                Long categoryId = expense.getCategoryId();
                expenseByCategory.put(categoryId, 
                    expenseByCategory.getOrDefault(categoryId, BigDecimal.ZERO).add(expense.getAmount()));
            }
            
            // 计算总金额
            BigDecimal totalExpense = expenseByCategory.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // 构建响应数据
            java.util.List<String> labels = new java.util.ArrayList<>();
            java.util.List<BigDecimal> values = new java.util.ArrayList<>();
            java.util.List<java.util.Map<String, Object>> details = new java.util.ArrayList<>();
            
            for (java.util.Map.Entry<Long, BigDecimal> entry : expenseByCategory.entrySet()) {
                ExpenseCategory category = expenseCategoryMapper.findById(entry.getKey());
                String categoryName = category != null ? category.getName() : "未知分类";
                BigDecimal amount = entry.getValue();
                
                BigDecimal percentage = totalExpense.compareTo(BigDecimal.ZERO) > 0
                        ? amount.divide(totalExpense, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                        : BigDecimal.ZERO;
                
                labels.add(categoryName);
                values.add(amount);
                
                java.util.Map<String, Object> detail = new java.util.HashMap<>();
                detail.put("categoryName", categoryName);
                detail.put("categoryId", entry.getKey());
                detail.put("amount", amount);
                detail.put("percentage", percentage.setScale(2, java.math.RoundingMode.HALF_UP));
                details.add(detail);
            }
            
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("labels", labels);
            data.put("values", values);
            data.put("details", details);
            data.put("total", totalExpense);
            
            System.out.println("【ReportController】返回支出图表数据成功");
            return ApiResponse.success("获取支出图表成功", data);
        } catch (Exception e) {
            System.err.println("【ReportController】获取支出图表失败: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error("获取支出图表失败: " + e.getMessage());
        }
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
