package com.billmanager.jizhang.service.impl;

import com.billmanager.jizhang.dto.*;
import com.billmanager.jizhang.entity.*;
import com.billmanager.jizhang.mapper.*;
import com.billmanager.jizhang.service.ReportService;
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
public class ReportServiceImpl implements ReportService {
    
    private final ExpenseMapper expenseMapper;
    private final IncomeMapper incomeMapper;
    private final BudgetMapper budgetMapper;
    private final ExpenseCategoryMapper expenseCategoryMapper;
    private final IncomeCategoryMapper incomeCategoryMapper;
    
    @Override
    public FinancialReport generateFinancialReport(Long userId, LocalDate startDate, LocalDate endDate) {
        FinancialReport report = new FinancialReport();
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setPeriodType("CUSTOM");
        
        // 获取时间范围内的收支数据
        List<Income> incomes = incomeMapper.findByUserIdAndDateRange(userId, startDate, endDate);
        List<Expense> expenses = expenseMapper.findByUserIdAndDateRange(userId, startDate, endDate);
        
        // 计算基本统计
        BigDecimal totalIncome = incomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalExpense = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal netProfit = totalIncome.subtract(totalExpense);
        BigDecimal savingRate = totalIncome.compareTo(BigDecimal.ZERO) > 0
                ? netProfit.divide(totalIncome, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;
        
        report.setTotalIncome(totalIncome);
        report.setTotalExpense(totalExpense);
        report.setNetProfit(netProfit);
        report.setSavingRate(savingRate);
        report.setIncomeCount((long) incomes.size());
        report.setExpenseCount((long) expenses.size());
        
        // 计算平均值
        report.setAvgIncome(incomes.isEmpty() ? BigDecimal.ZERO 
                : totalIncome.divide(new BigDecimal(incomes.size()), 2, RoundingMode.HALF_UP));
        report.setAvgExpense(expenses.isEmpty() ? BigDecimal.ZERO
                : totalExpense.divide(new BigDecimal(expenses.size()), 2, RoundingMode.HALF_UP));
        
        // 按分类统计收入
        List<FinancialReport.CategoryAmount> incomeByCategory = groupByCategory(
                incomes, true, userId);
        report.setIncomeByCategory(incomeByCategory);
        
        // 按分类统计支出
        List<FinancialReport.CategoryAmount> expenseByCategory = groupByCategory(
                expenses, false, userId);
        report.setExpenseByCategory(expenseByCategory);
        
        return report;
    }
    
    @Override
    public BudgetReport generateBudgetReport(Long userId, String budgetMonth) {
        BudgetReport report = new BudgetReport();
        report.setBudgetMonth(budgetMonth);
        
        // 获取该月的所有预算
        List<Budget> budgets = budgetMapper.findByUserIdAndYearMonth(userId, budgetMonth);
        
        // 获取该月的实际支出
        LocalDate startOfMonth = LocalDate.parse(budgetMonth + "-01");
        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);
        List<Expense> monthlyExpenses = expenseMapper.findByUserIdAndDateRange(userId, startOfMonth, endOfMonth);
        
        // 按分类统计支出
        Map<Long, BigDecimal> expenseByCategory = monthlyExpenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategoryId,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));
        
        List<BudgetReport.BudgetItem> budgetItems = new ArrayList<>();
        BigDecimal totalBudget = BigDecimal.ZERO;
        BigDecimal totalSpent = BigDecimal.ZERO;
        BigDecimal totalRemaining = BigDecimal.ZERO;
        BigDecimal overBudgetAmount = BigDecimal.ZERO;
        
        for (Budget budget : budgets) {
            BudgetReport.BudgetItem item = new BudgetReport.BudgetItem();
            item.setCategoryId(budget.getCategoryId());
            
            // 获取分类名称
            ExpenseCategory category = expenseCategoryMapper.findById(budget.getCategoryId());
            item.setCategoryName(category != null ? category.getName() : "未知分类");
            
            item.setBudgetAmount(budget.getAmount());
            
            // 获取实际支出
            BigDecimal spent = expenseByCategory.getOrDefault(budget.getCategoryId(), BigDecimal.ZERO);
            item.setSpentAmount(spent);
            
            // 计算剩余
            BigDecimal remaining = budget.getAmount().subtract(spent);
            item.setRemainingAmount(remaining);
            
            // 计算使用率
            BigDecimal utilizationRate = budget.getAmount().compareTo(BigDecimal.ZERO) > 0
                    ? spent.divide(budget.getAmount(), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                    : BigDecimal.ZERO;
            item.setUtilizationRate(utilizationRate);
            
            // 判断是否超预算
            item.setIsOverBudget(spent.compareTo(budget.getAmount()) > 0);
            
            if (item.getIsOverBudget()) {
                overBudgetAmount = overBudgetAmount.add(spent.subtract(budget.getAmount()));
            }
            
            budgetItems.add(item);
            totalBudget = totalBudget.add(budget.getAmount());
            totalSpent = totalSpent.add(spent);
            totalRemaining = totalRemaining.add(remaining);
        }
        
        report.setBudgetItems(budgetItems);
        report.setTotalBudget(totalBudget);
        report.setTotalSpent(totalSpent);
        report.setTotalRemaining(totalRemaining);
        report.setOverBudgetAmount(overBudgetAmount);
        
        // 计算总体预算使用率
        BigDecimal budgetUtilizationRate = totalBudget.compareTo(BigDecimal.ZERO) > 0
                ? totalSpent.divide(totalBudget, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;
        report.setBudgetUtilizationRate(budgetUtilizationRate);
        
        return report;
    }
    
    @Override
    public CashFlowAnalysis generateCashFlowAnalysis(Long userId, LocalDate startDate, LocalDate endDate) {
        CashFlowAnalysis analysis = new CashFlowAnalysis();
        analysis.setStartDate(startDate);
        analysis.setEndDate(endDate);
        
        // 获取时间范围内的收支数据
        List<Income> incomes = incomeMapper.findByUserIdAndDateRange(userId, startDate, endDate);
        List<Expense> expenses = expenseMapper.findByUserIdAndDateRange(userId, startDate, endDate);
        
        // 计算汇总数据
        BigDecimal totalIncome = incomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalExpense = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal netCashFlow = totalIncome.subtract(totalExpense);
        
        analysis.setTotalIncome(totalIncome);
        analysis.setTotalExpense(totalExpense);
        analysis.setNetCashFlow(netCashFlow);
        
        // 按日期分组计算每日现金流
        long dayCount = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        List<CashFlowAnalysis.DailyCashFlow> dailyFlows = new ArrayList<>();
        
        for (long i = 0; i < dayCount; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            
            BigDecimal dayIncome = incomes.stream()
                    .filter(inc -> inc.getTransactionDate().equals(currentDate))
                    .map(Income::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal dayExpense = expenses.stream()
                    .filter(exp -> exp.getTransactionDate().equals(currentDate))
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal dayNetFlow = dayIncome.subtract(dayExpense);
            
            CashFlowAnalysis.DailyCashFlow flow = new CashFlowAnalysis.DailyCashFlow();
            flow.setDate(currentDate);
            flow.setIncome(dayIncome);
            flow.setExpense(dayExpense);
            flow.setNetFlow(dayNetFlow);
            
            dailyFlows.add(flow);
        }
        
        // 计算累计余额
        BigDecimal cumulativeBalance = BigDecimal.ZERO;
        for (CashFlowAnalysis.DailyCashFlow flow : dailyFlows) {
            cumulativeBalance = cumulativeBalance.add(flow.getNetFlow());
            flow.setCumulativeBalance(cumulativeBalance);
        }
        
        analysis.setDailyCashFlows(dailyFlows);
        
        // 计算平均值和波动度
        BigDecimal incomeAvg = incomes.isEmpty() ? BigDecimal.ZERO
                : totalIncome.divide(new BigDecimal(dayCount), 2, RoundingMode.HALF_UP);
        BigDecimal expenseAvg = expenses.isEmpty() ? BigDecimal.ZERO
                : totalExpense.divide(new BigDecimal(dayCount), 2, RoundingMode.HALF_UP);
        
        analysis.setIncomeAverage(incomeAvg);
        analysis.setExpenseAverage(expenseAvg);
        
        // 计算最大最小值
        if (!incomes.isEmpty()) {
            analysis.setMaxDailyIncome(incomes.stream().map(Income::getAmount).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
            analysis.setMinDailyIncome(incomes.stream().map(Income::getAmount).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
        }
        
        if (!expenses.isEmpty()) {
            analysis.setMaxDailyExpense(expenses.stream().map(Expense::getAmount).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
            analysis.setMinDailyExpense(expenses.stream().map(Expense::getAmount).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
        }
        
        // 计算波动度（标准差）
        BigDecimal volatility = calculateVolatility(dailyFlows);
        analysis.setVolatility(volatility);
        
        // 计算现金流健康度评分
        Integer healthScore = calculateCashFlowHealthScore(analysis);
        analysis.setHealthScore(healthScore);
        
        if (healthScore >= 80) {
            analysis.setHealthStatus("EXCELLENT");
        } else if (healthScore >= 60) {
            analysis.setHealthStatus("GOOD");
        } else if (healthScore >= 40) {
            analysis.setHealthStatus("FAIR");
        } else {
            analysis.setHealthStatus("POOR");
        }
        
        return analysis;
    }
    
    @Override
    public TrendAnalysis generateTrendAnalysis(Long userId, Integer monthCount) {
        TrendAnalysis analysis = new TrendAnalysis();
        analysis.setAnalysisDate(LocalDate.now());
        
        // 获取历史数据（过去N个月）
        List<TrendAnalysis.MonthlySummary> historicalData = new ArrayList<>();
        BigDecimal totalHistoricalIncome = BigDecimal.ZERO;
        BigDecimal totalHistoricalExpense = BigDecimal.ZERO;
        
        for (int i = monthCount - 1; i >= 0; i--) {
            YearMonth yearMonth = YearMonth.now().minusMonths(i);
            LocalDate startOfMonth = yearMonth.atDay(1);
            LocalDate endOfMonth = yearMonth.atEndOfMonth();
            
            List<Income> monthIncomes = incomeMapper.findByUserIdAndDateRange(userId, startOfMonth, endOfMonth);
            List<Expense> monthExpenses = expenseMapper.findByUserIdAndDateRange(userId, startOfMonth, endOfMonth);
            
            BigDecimal monthIncome = monthIncomes.stream()
                    .map(Income::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal monthExpense = monthExpenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal monthNetProfit = monthIncome.subtract(monthExpense);
            
            TrendAnalysis.MonthlySummary summary = new TrendAnalysis.MonthlySummary();
            summary.setMonth(yearMonth.toString());
            summary.setIncome(monthIncome);
            summary.setExpense(monthExpense);
            summary.setNetProfit(monthNetProfit);
            summary.setTransactionCount(monthIncomes.size() + monthExpenses.size());
            
            historicalData.add(summary);
            totalHistoricalIncome = totalHistoricalIncome.add(monthIncome);
            totalHistoricalExpense = totalHistoricalExpense.add(monthExpense);
        }
        
        analysis.setHistoricalData(historicalData);
        
        // 计算增长率
        if (historicalData.size() >= 2) {
            TrendAnalysis.MonthlySummary lastMonth = historicalData.get(historicalData.size() - 1);
            TrendAnalysis.MonthlySummary prevMonth = historicalData.get(historicalData.size() - 2);
            
            if (prevMonth.getIncome().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal incomeGrowth = lastMonth.getIncome()
                        .subtract(prevMonth.getIncome())
                        .divide(prevMonth.getIncome(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                analysis.setIncomeGrowthRate(incomeGrowth);
            }
            
            if (prevMonth.getExpense().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal expenseGrowth = lastMonth.getExpense()
                        .subtract(prevMonth.getExpense())
                        .divide(prevMonth.getExpense(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                analysis.setExpenseGrowthRate(expenseGrowth);
            }
            
            // 判断趋势
            if (analysis.getIncomeGrowthRate() != null && analysis.getIncomeGrowthRate().compareTo(BigDecimal.ZERO) > 0) {
                analysis.setIncomeTrend("UP");
            } else if (analysis.getIncomeGrowthRate() != null && analysis.getIncomeGrowthRate().compareTo(BigDecimal.ZERO) < 0) {
                analysis.setIncomeTrend("DOWN");
            } else {
                analysis.setIncomeTrend("STABLE");
            }
            
            if (analysis.getExpenseGrowthRate() != null && analysis.getExpenseGrowthRate().compareTo(BigDecimal.ZERO) > 0) {
                analysis.setExpenseTrend("UP");
            } else if (analysis.getExpenseGrowthRate() != null && analysis.getExpenseGrowthRate().compareTo(BigDecimal.ZERO) < 0) {
                analysis.setExpenseTrend("DOWN");
            } else {
                analysis.setExpenseTrend("STABLE");
            }
        }
        
        // 生成未来3个月的预测
        List<TrendAnalysis.Forecast> forecasts = new ArrayList<>();
        BigDecimal avgIncome = totalHistoricalIncome.divide(new BigDecimal(monthCount), 2, RoundingMode.HALF_UP);
        BigDecimal avgExpense = totalHistoricalExpense.divide(new BigDecimal(monthCount), 2, RoundingMode.HALF_UP);
        
        for (int i = 1; i <= 3; i++) {
            YearMonth futureMonth = YearMonth.now().plusMonths(i);
            
            // 简单的线性预测：基于增长率推测
            BigDecimal predictedIncome = avgIncome;
            BigDecimal predictedExpense = avgExpense;
            
            if (analysis.getIncomeGrowthRate() != null) {
                BigDecimal growthFactor = BigDecimal.ONE.add(analysis.getIncomeGrowthRate().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                predictedIncome = avgIncome.multiply(growthFactor);
            }
            
            if (analysis.getExpenseGrowthRate() != null) {
                BigDecimal growthFactor = BigDecimal.ONE.add(analysis.getExpenseGrowthRate().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                predictedExpense = avgExpense.multiply(growthFactor);
            }
            
            BigDecimal predictedNetProfit = predictedIncome.subtract(predictedExpense);
            
            TrendAnalysis.Forecast forecast = new TrendAnalysis.Forecast();
            forecast.setMonth(futureMonth.toString());
            forecast.setPredictedIncome(predictedIncome);
            forecast.setPredictedExpense(predictedExpense);
            forecast.setPredictedNetProfit(predictedNetProfit);
            forecast.setConfidence(new BigDecimal("0.85"));  // 简化处理
            
            if (predictedNetProfit.compareTo(BigDecimal.ZERO) > 0) {
                forecast.setTrend("UP");
            } else if (predictedNetProfit.compareTo(BigDecimal.ZERO) < 0) {
                forecast.setTrend("DOWN");
            } else {
                forecast.setTrend("STABLE");
            }
            
            forecasts.add(forecast);
        }
        
        analysis.setForecastData(forecasts);
        analysis.setConfidenceLevel(85);
        analysis.setConfidenceNote("基于最近6个月数据的线性预测，准确度受历史数据波动影响");
        
        return analysis;
    }
    
    /**
     * 按分类分组统计
     */
    @SuppressWarnings("unchecked")
    private List<FinancialReport.CategoryAmount> groupByCategory(List<?> transactions, 
                                                                  boolean isIncome, Long userId) {
        Map<Long, BigDecimal> categoryAmounts = new LinkedHashMap<>();
        Map<Long, Long> categoryCounts = new LinkedHashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        
        if (isIncome) {
            List<Income> incomes = (List<Income>) transactions;
            for (Income income : incomes) {
                categoryAmounts.merge(income.getCategoryId(), income.getAmount(), BigDecimal::add);
                categoryCounts.merge(income.getCategoryId(), 1L, Long::sum);
            }
        } else {
            List<Expense> expenses = (List<Expense>) transactions;
            for (Expense expense : expenses) {
                categoryAmounts.merge(expense.getCategoryId(), expense.getAmount(), BigDecimal::add);
                categoryCounts.merge(expense.getCategoryId(), 1L, Long::sum);
            }
        }
        
        total = categoryAmounts.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        
        List<FinancialReport.CategoryAmount> result = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : categoryAmounts.entrySet()) {
            FinancialReport.CategoryAmount ca = new FinancialReport.CategoryAmount();
            ca.setCategoryId(entry.getKey());
            
            // 获取分类名称
            if (isIncome) {
                IncomeCategory category = incomeCategoryMapper.findById(entry.getKey());
                ca.setCategoryName(category != null ? category.getName() : "未知分类");
            } else {
                ExpenseCategory category = expenseCategoryMapper.findById(entry.getKey());
                ca.setCategoryName(category != null ? category.getName() : "未知分类");
            }
            
            ca.setAmount(entry.getValue());
            ca.setCount(categoryCounts.get(entry.getKey()));
            
            BigDecimal percentage = total.compareTo(BigDecimal.ZERO) > 0
                    ? entry.getValue().divide(total, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                    : BigDecimal.ZERO;
            ca.setPercentage(percentage);
            
            result.add(ca);
        }
        
        return result;
    }
    
    /**
     * 计算现金流波动度（标准差）
     */
    private BigDecimal calculateVolatility(List<CashFlowAnalysis.DailyCashFlow> dailyFlows) {
        if (dailyFlows.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // 计算平均值
        BigDecimal avg = dailyFlows.stream()
                .map(CashFlowAnalysis.DailyCashFlow::getNetFlow)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(dailyFlows.size()), 4, RoundingMode.HALF_UP);
        
        // 计算方差
        BigDecimal variance = dailyFlows.stream()
                .map(flow -> flow.getNetFlow().subtract(avg).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(dailyFlows.size()), 4, RoundingMode.HALF_UP);
        
        // 开平方得到标准差
        return new BigDecimal(Math.sqrt(variance.doubleValue()));
    }
    
    /**
     * 计算现金流健康度评分
     */
    private Integer calculateCashFlowHealthScore(CashFlowAnalysis analysis) {
        int score = 50;  // 基础分
        
        // 净现金流为正加分
        if (analysis.getNetCashFlow().compareTo(BigDecimal.ZERO) > 0) {
            score += 20;
        }
        
        // 波动度低加分
        if (analysis.getVolatility().compareTo(analysis.getExpenseAverage()) < 0) {
            score += 15;
        }
        
        // 收入大于支出加分
        if (analysis.getTotalIncome().compareTo(analysis.getTotalExpense()) > 0) {
            score += 15;
        }
        
        return Math.min(score, 100);
    }
}
