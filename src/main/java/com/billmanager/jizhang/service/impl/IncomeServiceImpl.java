package com.billmanager.jizhang.service.impl;

import com.billmanager.jizhang.annotation.FamilyPermission;
import com.billmanager.jizhang.dto.IncomeRequest;
import com.billmanager.jizhang.dto.IncomeStatistics;
import com.billmanager.jizhang.entity.Income;
import com.billmanager.jizhang.entity.FamilyGroup;
import com.billmanager.jizhang.exception.BusinessException;
import com.billmanager.jizhang.mapper.IncomeMapper;
import com.billmanager.jizhang.service.IncomeService;
import com.billmanager.jizhang.service.FamilyGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncomeServiceImpl implements IncomeService {
    
    private final IncomeMapper incomeMapper;
    private final FamilyGroupService familyGroupService;
    
    @Override
    public Income add(IncomeRequest request, Long userId) {
        Income income = new Income();
        income.setUserId(userId);
        income.setCategoryId(request.getCategoryId());
        income.setAmount(request.getAmount());
        income.setTransactionDate(request.getTransactionDate());
        income.setDescription(request.getDescription());
        
        // 获取用户的家族组ID，如果没有则设为0（个人数据）
        try {
            FamilyGroup familyGroup = familyGroupService.getFamilyGroupByUserId(userId);
            if (familyGroup != null) {
                income.setFamilyGroupId(familyGroup.getId());
            } else {
                income.setFamilyGroupId(0L); // 0 表示个人数据
            }
        } catch (Exception e) {
            log.debug("获取用户的家族组失败，使用个人数据范围", e);
            income.setFamilyGroupId(0L); // 默认使用个人数据
        }
        
        incomeMapper.insert(income);
        return income;
    }
    
    @Override
    public Income update(Long id, IncomeRequest request, Long userId) {
        Income income = incomeMapper.findById(id);
        if (income == null) {
            throw new BusinessException("收入记录不存在");
        }
        if (!income.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此记录");
        }
        
        income.setCategoryId(request.getCategoryId());
        income.setAmount(request.getAmount());
        income.setTransactionDate(request.getTransactionDate());
        income.setDescription(request.getDescription());
        
        incomeMapper.update(income);
        return income;
    }
    
    @Override
    public void delete(Long id, Long userId) {
        Income income = incomeMapper.findById(id);
        if (income == null) {
            throw new BusinessException("收入记录不存在");
        }
        if (!income.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此记录");
        }
        
        incomeMapper.deleteById(id);
    }
    
    @Override
    public Income findById(Long id, Long userId) {
        Income income = incomeMapper.findById(id);
        if (income == null) {
            throw new BusinessException("收入记录不存在");
        }
        if (!income.getUserId().equals(userId)) {
            throw new BusinessException("无权查看此记录");
        }
        return income;
    }
    
    @Override
    @FamilyPermission("income_view")
    public List<Income> findByUserId(Long userId) {
        FamilyGroup familyGroup = familyGroupService.getFamilyGroupByUserId(userId);
        if (familyGroup != null) {
            // 用户属于某个家庭组，按familyGroupId查询
            return incomeMapper.findByFamilyGroupIdOrderByDateDesc(familyGroup.getId());
        } else {
            // 用户不属于任何家庭组，按userId查询（完全访问权限）
            log.info("【收入】用户{}不属于任何家庭组，按个人身份查询收入", userId);
            return incomeMapper.findByUserIdOrderByDateDesc(userId);
        }
    }
    
    @Override
    @FamilyPermission("income_view")
    public List<Income> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        FamilyGroup familyGroup = familyGroupService.getFamilyGroupByUserId(userId);
        if (familyGroup != null) {
            return incomeMapper.findByFamilyGroupIdAndDateRange(familyGroup.getId(), startDate, endDate);
        } else {
            // 用户不属于任何家庭组，按userId查询
            return incomeMapper.findByUserIdAndDateRange(userId, startDate, endDate);
        }
    }
    
    @Override
    @FamilyPermission("income_view")
    public List<Income> findByUserIdAndCategoryId(Long userId, Long categoryId) {
        FamilyGroup familyGroup = familyGroupService.getFamilyGroupByUserId(userId);
        if (familyGroup != null) {
            return incomeMapper.findByFamilyGroupIdAndCategoryId(familyGroup.getId(), categoryId);
        } else {
            // 用户不属于任何家庭组，按userId查询
            return incomeMapper.findByUserIdAndCategoryId(userId, categoryId);
        }
    }
    
    @Override
    @FamilyPermission("income_view")
    public IncomeStatistics getStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
        FamilyGroup familyGroup = familyGroupService.getFamilyGroupByUserId(userId);
        List<Income> incomes;
        
        if (familyGroup != null) {
            if (startDate != null && endDate != null) {
                incomes = incomeMapper.findByFamilyGroupIdAndDateRange(familyGroup.getId(), startDate, endDate);
            } else {
                incomes = incomeMapper.findByFamilyGroupIdOrderByDateDesc(familyGroup.getId());
            }
        } else {
            // 用户不属于任何家庭组，按userId查询
            if (startDate != null && endDate != null) {
                incomes = incomeMapper.findByUserIdAndDateRange(userId, startDate, endDate);
            } else {
                incomes = incomeMapper.findByUserIdOrderByDateDesc(userId);
            }
        }
        
        IncomeStatistics statistics = new IncomeStatistics();
        statistics.setTotalIncome(incomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        statistics.setRecordCount((long) incomes.size());
        
        if (!incomes.isEmpty()) {
            BigDecimal avgIncome = statistics.getTotalIncome().divide(
                    new BigDecimal(incomes.size()), 2, java.math.RoundingMode.HALF_UP);
            statistics.setAvgIncome(avgIncome);
            
            BigDecimal maxIncome = incomes.stream()
                    .map(Income::getAmount)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            statistics.setMaxIncome(maxIncome);
        } else {
            statistics.setAvgIncome(BigDecimal.ZERO);
            statistics.setMaxIncome(BigDecimal.ZERO);
        }
        
        statistics.setStartDate(startDate);
        statistics.setEndDate(endDate);
        
        return statistics;
    }
}
