package com.billmanager.jizhang.service;

import com.billmanager.jizhang.entity.Expense;
import com.billmanager.jizhang.entity.ExpenseCategory;
import com.billmanager.jizhang.entity.Income;
import com.billmanager.jizhang.entity.IncomeCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI数据提供者 - 聚合用户财务数据并格式化为文本
 */
@Service
public class AIDataProvider {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private IncomeService incomeService;

    @Autowired
    private ExpenseCategoryService expenseCategoryService;

    @Autowired
    private IncomeCategoryService incomeCategoryService;

    /**
     * 获取用户本月的财务数据文本
     */
    public String getUserFinancialDataText(Long userId) {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        return getUserFinancialDataText(userId, year, month);
    }

    /**
     * 获取用户指定月份的财务数据文本
     */
    public String getUserFinancialDataText(Long userId, int year, int month) {
        // 构造该月的开始和结束日期
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        // 查询支出
        List<Expense> expenses = expenseService.findByUserIdAndDateRange(userId, startDate, endDate);
        
        // 查询收入
        List<Income> incomes = incomeService.findByUserIdAndDateRange(userId, startDate, endDate);
        
        return formatFinancialData(expenses, incomes, userId, year, month);
    }

    /**
     * 将财务数据格式化为人类可读的文本
     */
    private String formatFinancialData(List<Expense> expenses, List<Income> incomes, 
                                       Long userId, int year, int month) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("【").append(year).append("年").append(month).append("月账单数据】\n\n");
        
        // 支出部分
        sb.append("【支出详情】\n");
        if (expenses.isEmpty()) {
            sb.append("本月无支出\n");
        } else {
            // 按类别分组展示
            Map<String, Double> expenseByCategory = expenses.stream()
                    .collect(Collectors.groupingBy(
                            e -> getCategoryName(e.getCategoryId(), userId, true),
                            Collectors.summingDouble(e -> e.getAmount().doubleValue())
                    ));
            
            double totalExpense = 0;
            for (Map.Entry<String, Double> entry : expenseByCategory.entrySet()) {
                sb.append("- ").append(entry.getKey()).append(": ¥").append(String.format("%.2f", entry.getValue())).append("\n");
                totalExpense += entry.getValue();
            }
            sb.append("支出合计: ¥").append(String.format("%.2f", totalExpense)).append("\n");
        }
        
        sb.append("\n");
        
        // 收入部分
        sb.append("【收入详情】\n");
        if (incomes.isEmpty()) {
            sb.append("本月无收入\n");
        } else {
            // 按类别分组展示
            Map<String, Double> incomeByCategory = incomes.stream()
                    .collect(Collectors.groupingBy(
                            i -> getCategoryName(i.getCategoryId(), userId, false),
                            Collectors.summingDouble(i -> i.getAmount().doubleValue())
                    ));
            
            double totalIncome = 0;
            for (Map.Entry<String, Double> entry : incomeByCategory.entrySet()) {
                sb.append("- ").append(entry.getKey()).append(": ¥").append(String.format("%.2f", entry.getValue())).append("\n");
                totalIncome += entry.getValue();
            }
            sb.append("收入合计: ¥").append(String.format("%.2f", totalIncome)).append("\n");
        }
        
        return sb.toString();
    }

    /**
     * 根据分类ID获取分类名称
     * @param categoryId 分类ID
     * @param userId 用户ID
     * @param isExpense 是否为支出分类（true为支出，false为收入）
     */
    private String getCategoryName(Long categoryId, Long userId, boolean isExpense) {
        if (categoryId == null) {
            return "未分类";
        }
        
        try {
            if (isExpense) {
                ExpenseCategory category = expenseCategoryService.findById(categoryId, userId);
                return category != null && category.getName() != null ? category.getName() : "未知分类";
            } else {
                IncomeCategory category = incomeCategoryService.findById(categoryId, userId);
                return category != null && category.getName() != null ? category.getName() : "未知分类";
            }
        } catch (Exception e) {
            return "分类查询失败";
        }
    }
}
