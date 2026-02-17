package com.billmanager.jizhang.service.impl;

import com.billmanager.jizhang.dto.DashboardData;
import com.billmanager.jizhang.dto.CategoryAnalysis;
import com.billmanager.jizhang.dto.BudgetVsActual;
import com.billmanager.jizhang.entity.Expense;
import com.billmanager.jizhang.entity.ExpenseCategory;
import com.billmanager.jizhang.entity.Income;
import com.billmanager.jizhang.entity.IncomeCategory;
import com.billmanager.jizhang.mapper.*;
import com.billmanager.jizhang.service.AnalysisService;
import com.billmanager.jizhang.service.IncomeService;
import com.billmanager.jizhang.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalysisServiceImpl implements AnalysisService {
    
    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final ExpenseMapper expenseMapper;
    private final IncomeMapper incomeMapper;
    private final BudgetMapper budgetMapper;
    private final ExpenseCategoryMapper expenseCategoryMapper;
    private final IncomeCategoryMapper incomeCategoryMapper;
    
    @Override
    public DashboardData getDashboardData(Long userId) {
        DashboardData dashboard = new DashboardData();
        
        // 获取本月数据
        YearMonth now = YearMonth.now();
        LocalDate monthStart = now.atDay(1);
        LocalDate monthEnd = now.atEndOfMonth();
        
        List<Income> monthIncomes = incomeService.findByUserIdAndDateRange(userId, monthStart, monthEnd);
        List<Expense> monthExpenses = expenseService.findByUserIdAndDateRange(userId, monthStart, monthEnd);
        
        BigDecimal thisMonthIncome = monthIncomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal thisMonthExpense = monthExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        dashboard.setThisMonthIncome(thisMonthIncome);
        dashboard.setThisMonthExpense(thisMonthExpense);
        dashboard.setThisMonthBalance(thisMonthIncome.subtract(thisMonthExpense));
        
        // 计算本月预算使用率
        BigDecimal budgetUtilization = calculateBudgetUtilization(userId, now.toString());
        dashboard.setThisMonthBudgetUtilization(budgetUtilization);
        
        // 获取最近30天数据
        LocalDate last30DaysStart = LocalDate.now().minusDays(30);
        LocalDate last30DaysEnd = LocalDate.now();
        
        List<Income> last30DaysIncomes = incomeService.findByUserIdAndDateRange(userId, last30DaysStart, last30DaysEnd);
        List<Expense> last30DaysExpenses = expenseService.findByUserIdAndDateRange(userId, last30DaysStart, last30DaysEnd);
        
        BigDecimal last30DaysIncome = last30DaysIncomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal last30DaysExpense = last30DaysExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        dashboard.setLast30DaysIncome(last30DaysIncome);
        dashboard.setLast30DaysExpense(last30DaysExpense);
        dashboard.setLast30DaysBalance(last30DaysIncome.subtract(last30DaysExpense));
        
        // 获取当年累计数据
        LocalDate yearStart = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        LocalDate yearEnd = LocalDate.now();
        
        List<Income> yearIncomes = incomeService.findByUserIdAndDateRange(userId, yearStart, yearEnd);
        List<Expense> yearExpenses = expenseService.findByUserIdAndDateRange(userId, yearStart, yearEnd);
        
        BigDecimal yearToDateIncome = yearIncomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal yearToDateExpense = yearExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        dashboard.setYearToDateIncome(yearToDateIncome);
        dashboard.setYearToDateExpense(yearToDateExpense);
        dashboard.setYearToDateBalance(yearToDateIncome.subtract(yearToDateExpense));
        
        // 设置目标（默认值，实际应该从数据库读取）
        dashboard.setTargetIncome(new BigDecimal("50000"));
        dashboard.setTargetExpense(new BigDecimal("30000"));
        
        // 计算实现率
        BigDecimal incomeRate = dashboard.getTargetIncome().compareTo(BigDecimal.ZERO) > 0
                ? thisMonthIncome.divide(dashboard.getTargetIncome(), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;
        dashboard.setIncomeAchievementRate(incomeRate);
        
        BigDecimal expenseRate = dashboard.getTargetExpense().compareTo(BigDecimal.ZERO) > 0
                ? dashboard.getTargetExpense().divide(thisMonthExpense.compareTo(BigDecimal.ZERO) > 0 
                    ? thisMonthExpense : BigDecimal.ONE, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;
        dashboard.setExpenseControlRate(expenseRate);
        
        // 获取图表数据
        DashboardData.ChartData chartData = getChartData(userId);
        dashboard.setChartData(chartData);
        
        // 计算财务健康分数
        Integer healthScore = calculateFinancialHealthScore(userId);
        dashboard.setFinancialHealthScore(healthScore);
        
        // 获取建议
        String recommendation = getFinancialRecommendation(userId);
        dashboard.setRecommendation(recommendation);
        
        return dashboard;
    }
    
    @Override
    public Integer calculateFinancialHealthScore(Long userId) {
        int score = 60;  // 基础分
        
        // 获取本月数据
        YearMonth now = YearMonth.now();
        LocalDate monthStart = now.atDay(1);
        LocalDate monthEnd = now.atEndOfMonth();
        
        List<Income> monthIncomes = incomeService.findByUserIdAndDateRange(userId, monthStart, monthEnd);
        List<Expense> monthExpenses = expenseService.findByUserIdAndDateRange(userId, monthStart, monthEnd);
        
        BigDecimal monthIncome = monthIncomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal monthExpense = monthExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 收入大于支出加分
        if (monthIncome.compareTo(monthExpense) > 0) {
            score += 20;
        }
        
        // 支出在合理范围内加分（支出< 收入的60%）
        if (monthIncome.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal ratio = monthExpense.divide(monthIncome, 2, RoundingMode.HALF_UP);
            if (ratio.compareTo(new BigDecimal("0.6")) < 0) {
                score += 15;
            } else if (ratio.compareTo(new BigDecimal("0.8")) < 0) {
                score += 10;
            }
        }
        
        // 检查预算执行情况
        BigDecimal budgetUtilization = calculateBudgetUtilization(userId, now.toString());
        if (budgetUtilization.compareTo(new BigDecimal("100")) <= 0) {
            score += 5;
        }
        
        // 检查最近30天是否有收入
        LocalDate last30DaysStart = LocalDate.now().minusDays(30);
        List<Income> last30DaysIncomes = incomeService.findByUserIdAndDateRange(userId, last30DaysStart, LocalDate.now());
        if (!last30DaysIncomes.isEmpty()) {
            score += 5;
        }
        
        return Math.min(score, 100);
    }
    
    @Override
    public String getFinancialRecommendation(Long userId) {
        StringBuilder recommendation = new StringBuilder();
        
        // 获取本月数据
        YearMonth now = YearMonth.now();
        LocalDate monthStart = now.atDay(1);
        LocalDate monthEnd = now.atEndOfMonth();
        
        List<Income> monthIncomes = incomeService.findByUserIdAndDateRange(userId, monthStart, monthEnd);
        List<Expense> monthExpenses = expenseService.findByUserIdAndDateRange(userId, monthStart, monthEnd);
        
        BigDecimal monthIncome = monthIncomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal monthExpense = monthExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 分析支出比例
        if (monthIncome.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal expenseRatio = monthExpense.divide(monthIncome, 4, RoundingMode.HALF_UP);
            
            if (expenseRatio.compareTo(new BigDecimal("0.5")) < 0) {
                recommendation.append("✓ 支出控制良好，已达到理想的储蓄率。建议考虑增加投资或储蓄配置。\n");
            } else if (expenseRatio.compareTo(new BigDecimal("0.7")) < 0) {
                recommendation.append("✓ 支出处于合理水平，保持当前的消费习惯。\n");
            } else if (expenseRatio.compareTo(new BigDecimal("0.9")) < 0) {
                recommendation.append("⚠ 支出比例较高（").append(expenseRatio.multiply(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP)).append("%），建议适当控制支出，增加储蓄。\n");
            } else {
                recommendation.append("⚠ 支出过高（").append(expenseRatio.multiply(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP)).append("%），强烈建议立即制定节支计划。\n");
            }
        }
        
        // 分析预算执行情况
        BigDecimal budgetUtilization = calculateBudgetUtilization(userId, now.toString());
        if (budgetUtilization.compareTo(new BigDecimal("100")) > 0) {
            recommendation.append("⚠ 本月预算已超支，请检查大额支出项目。\n");
        } else if (budgetUtilization.compareTo(new BigDecimal("80")) > 0) {
            recommendation.append("✓ 预算执行良好，剩余预算不足20%，请谨慎安排后续支出。\n");
        }
        
        // 分析支出分类
        if (!monthExpenses.isEmpty()) {
            Map<Long, BigDecimal> categoryExpense = monthExpenses.stream()
                    .collect(Collectors.groupingBy(
                            Expense::getCategoryId,
                            Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                    ));
            
            // 找出支出最多的分类
            Optional<Map.Entry<Long, BigDecimal>> maxCategory = categoryExpense.entrySet().stream()
                    .max(Comparator.comparing(Map.Entry::getValue));
            
            if (maxCategory.isPresent()) {
                ExpenseCategory category = expenseCategoryMapper.findById(maxCategory.get().getKey());
                if (category != null) {
                    BigDecimal percentage = maxCategory.get().getValue().divide(monthExpense, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
                    recommendation.append("• 本月支出最多的类别是\"").append(category.getName())
                            .append("\"，占支出的").append(percentage.setScale(1, RoundingMode.HALF_UP)).append("%。\n");
                }
            }
        }
        
        // 分析收入情况
        if (monthIncome.compareTo(BigDecimal.ZERO) == 0) {
            recommendation.append("✗ 本月未记录收入，请检查数据是否遗漏。\n");
        }
        
        if (recommendation.length() == 0) {
            recommendation.append("财务状况良好，继续保持当前的管理习惯。");
        }
        
        return recommendation.toString();
    }
    
    /**
     * 获取图表数据
     */
    private DashboardData.ChartData getChartData(Long userId) {
        DashboardData.ChartData chartData = new DashboardData.ChartData();
        List<String> dates = new ArrayList<>();
        List<BigDecimal> incomeData = new ArrayList<>();
        List<BigDecimal> expenseData = new ArrayList<>();
        
        // 获取最近30天的数据
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(29);
        
        List<Income> allIncomes = incomeService.findByUserIdAndDateRange(userId, startDate, endDate);
        List<Expense> allExpenses = expenseService.findByUserIdAndDateRange(userId, startDate, endDate);
        
        // 按日期聚合
        Map<LocalDate, BigDecimal> dailyIncome = new TreeMap<>();
        Map<LocalDate, BigDecimal> dailyExpense = new TreeMap<>();
        
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            dailyIncome.put(d, BigDecimal.ZERO);
            dailyExpense.put(d, BigDecimal.ZERO);
        }
        
        for (Income income : allIncomes) {
            dailyIncome.merge(income.getTransactionDate(), income.getAmount(), BigDecimal::add);
        }
        
        for (Expense expense : allExpenses) {
            dailyExpense.merge(expense.getTransactionDate(), expense.getAmount(), BigDecimal::add);
        }
        
        // 构建图表数据
        for (Map.Entry<LocalDate, BigDecimal> entry : dailyIncome.entrySet()) {
            dates.add(entry.getKey().toString());
            incomeData.add(entry.getValue());
            expenseData.add(dailyExpense.getOrDefault(entry.getKey(), BigDecimal.ZERO));
        }
        
        chartData.setDates(dates);
        chartData.setIncomeData(incomeData);
        chartData.setExpenseData(expenseData);
        
        // 收入分类饼图
        List<DashboardData.CategoryData> incomeCategoryData = new ArrayList<>();
        YearMonth now = YearMonth.now();
        LocalDate monthStart = now.atDay(1);
        LocalDate monthEnd = now.atEndOfMonth();
        
        List<Income> monthIncomes = incomeService.findByUserIdAndDateRange(userId, monthStart, monthEnd);
        BigDecimal totalMonthIncome = monthIncomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Map<Long, BigDecimal> incomeByCategory = monthIncomes.stream()
                .collect(Collectors.groupingBy(
                        Income::getCategoryId,
                        Collectors.reducing(BigDecimal.ZERO, Income::getAmount, BigDecimal::add)
                ));
        
        for (Map.Entry<Long, BigDecimal> entry : incomeByCategory.entrySet()) {
            DashboardData.CategoryData catData = new DashboardData.CategoryData();
            IncomeCategory category = incomeCategoryMapper.findById(entry.getKey());
            catData.setName(category != null ? category.getName() : "未知");
            catData.setValue(entry.getValue());
            catData.setPercentage(totalMonthIncome.compareTo(BigDecimal.ZERO) > 0
                    ? entry.getValue().divide(totalMonthIncome, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                    : BigDecimal.ZERO);
            incomeCategoryData.add(catData);
        }
        chartData.setIncomeCategory(incomeCategoryData);
        
        // 支出分类饼图
        List<DashboardData.CategoryData> expenseCategoryData = new ArrayList<>();
        List<Expense> monthExpenses = expenseService.findByUserIdAndDateRange(userId, monthStart, monthEnd);
        BigDecimal totalMonthExpense = monthExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Map<Long, BigDecimal> expenseByCategory = monthExpenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategoryId,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));
        
        for (Map.Entry<Long, BigDecimal> entry : expenseByCategory.entrySet()) {
            DashboardData.CategoryData catData = new DashboardData.CategoryData();
            ExpenseCategory category = expenseCategoryMapper.findById(entry.getKey());
            catData.setName(category != null ? category.getName() : "未知");
            catData.setValue(entry.getValue());
            catData.setPercentage(totalMonthExpense.compareTo(BigDecimal.ZERO) > 0
                    ? entry.getValue().divide(totalMonthExpense, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                    : BigDecimal.ZERO);
            expenseCategoryData.add(catData);
        }
        chartData.setExpenseCategory(expenseCategoryData);
        
        return chartData;
    }
    
    /**
     * 计算预算使用率
     */
    private BigDecimal calculateBudgetUtilization(Long userId, String yearMonth) {
        List<com.billmanager.jizhang.entity.Budget> budgets = budgetMapper.findByUserIdAndYearMonth(userId, yearMonth);
        
        if (budgets.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalBudget = budgets.stream()
                .map(com.billmanager.jizhang.entity.Budget::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalSpent = budgets.stream()
                .map(com.billmanager.jizhang.entity.Budget::getSpent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return totalBudget.compareTo(BigDecimal.ZERO) > 0
                ? totalSpent.divide(totalBudget, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;
    }
    
    @Override
    public List<CategoryAnalysis> getCategoryAnalysis(Long userId, String yearMonth) {
        List<CategoryAnalysis> result = new ArrayList<>();
        
        // 解析年月
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd = ym.atEndOfMonth();
        
        // 获取该月的所有支出
        List<Expense> monthExpenses = expenseService.findByUserIdAndDateRange(userId, monthStart, monthEnd);
        
        if (monthExpenses.isEmpty()) {
            return result;
        }
        
        // 按分类汇总
        BigDecimal totalExpense = monthExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Map<Long, List<Expense>> expenseByCategory = monthExpenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategoryId));
        
        // 构建分析数据
        for (Map.Entry<Long, List<Expense>> entry : expenseByCategory.entrySet()) {
            Long categoryId = entry.getKey();
            List<Expense> expenses = entry.getValue();
            
            ExpenseCategory category = expenseCategoryMapper.findById(categoryId);
            if (category == null) {
                continue;
            }
            
            BigDecimal categoryAmount = expenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal percentage = totalExpense.compareTo(BigDecimal.ZERO) > 0
                    ? categoryAmount.divide(totalExpense, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                    : BigDecimal.ZERO;
            
            CategoryAnalysis analysis = new CategoryAnalysis();
            analysis.setCategoryId(categoryId);
            analysis.setCategoryName(category.getName());
            analysis.setAmount(categoryAmount);
            analysis.setPercentage(percentage);
            analysis.setCount(expenses.size());
            
            result.add(analysis);
        }
        
        // 按金额降序排列
        result.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));
        
        return result;
    }
    
    @Override
    public List<BudgetVsActual> getBudgetVsActual(Long userId, String yearMonth) {
        List<BudgetVsActual> result = new ArrayList<>();
        
        // 支持两种格式：年份（2025）和年月（2026-01）
        List<String> monthsToProcess = new ArrayList<>();
        LocalDate yearStart, yearEnd;
        
        if (yearMonth.length() == 4) {
            // 年度查询：格式为 "2025"
            int year = Integer.parseInt(yearMonth);
            yearStart = LocalDate.of(year, 1, 1);
            yearEnd = LocalDate.of(year, 12, 31);
            
            // 生成全年的月份列表
            for (int month = 1; month <= 12; month++) {
                String monthStr = String.format("%04d-%02d", year, month);
                monthsToProcess.add(monthStr);
            }
        } else {
            // 月度查询：格式为 "2026-01"
            YearMonth ym = YearMonth.parse(yearMonth);
            yearStart = ym.atDay(1);
            yearEnd = ym.atEndOfMonth();
            monthsToProcess.add(yearMonth);
        }
        
        // 获取年度范围内的所有预算和实际支出
        List<com.billmanager.jizhang.entity.Budget> allBudgets = budgetMapper.findByUserIdAndYearMonth(userId, monthsToProcess.get(0));
        
        // 获取年度范围内的实际支出
        List<Expense> yearExpenses = expenseService.findByUserIdAndDateRange(userId, yearStart, yearEnd);
        Map<Long, BigDecimal> actualByCategory = yearExpenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategoryId,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));
        
        // 如果是年度查询，需要汇总所有月份的预算
        if (yearMonth.length() == 4) {
            Map<Long, BigDecimal> budgetByCategory = new HashMap<>();
            for (String month : monthsToProcess) {
                List<com.billmanager.jizhang.entity.Budget> monthBudgets = budgetMapper.findByUserIdAndYearMonth(userId, month);
                for (com.billmanager.jizhang.entity.Budget budget : monthBudgets) {
                    budgetByCategory.put(
                            budget.getCategoryId(),
                            budgetByCategory.getOrDefault(budget.getCategoryId(), BigDecimal.ZERO)
                                    .add(budget.getAmount())
                    );
                }
            }
            
            // 构建年度预算与实际对比数据
            for (Map.Entry<Long, BigDecimal> entry : budgetByCategory.entrySet()) {
                ExpenseCategory category = expenseCategoryMapper.findById(entry.getKey());
                if (category == null) {
                    continue;
                }
                
                BigDecimal budgetAmount = entry.getValue();
                BigDecimal actualAmount = actualByCategory.getOrDefault(entry.getKey(), BigDecimal.ZERO);
                
                BigDecimal percentage = budgetAmount.compareTo(BigDecimal.ZERO) > 0
                        ? actualAmount.divide(budgetAmount, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                        : BigDecimal.ZERO;
                
                BigDecimal remaining = budgetAmount.subtract(actualAmount);
                
                BudgetVsActual bva = new BudgetVsActual();
                bva.setCategoryId(entry.getKey());
                bva.setCategoryName(category.getName());
                bva.setBudgetAmount(budgetAmount);
                bva.setActualAmount(actualAmount);
                bva.setPercentage(percentage);
                bva.setRemaining(remaining);
                
                result.add(bva);
            }
        } else {
            // 月度查询逻辑（原有逻辑）
            if (allBudgets.isEmpty()) {
                return result;
            }
            
            // 构建预算与实际对比数据
            for (com.billmanager.jizhang.entity.Budget budget : allBudgets) {
                ExpenseCategory category = expenseCategoryMapper.findById(budget.getCategoryId());
                if (category == null) {
                    continue;
                }
                
                BigDecimal actualAmount = actualByCategory.getOrDefault(budget.getCategoryId(), BigDecimal.ZERO);
                BigDecimal budgetAmount = budget.getAmount();
                
                BigDecimal percentage = budgetAmount.compareTo(BigDecimal.ZERO) > 0
                        ? actualAmount.divide(budgetAmount, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                        : BigDecimal.ZERO;
                
                BigDecimal remaining = budgetAmount.subtract(actualAmount);
                
                BudgetVsActual bva = new BudgetVsActual();
                bva.setCategoryId(budget.getCategoryId());
                bva.setCategoryName(category.getName());
                bva.setBudgetAmount(budgetAmount);
                bva.setActualAmount(actualAmount);
                bva.setPercentage(percentage);
                bva.setRemaining(remaining);
                
                result.add(bva);
            }
        }
        
        // 按预算金额降序排列
        result.sort((a, b) -> b.getBudgetAmount().compareTo(a.getBudgetAmount()));
        
        return result;
    }
}
