package com.billmanager.jizhang.service.impl;

import com.billmanager.jizhang.dto.*;
import com.billmanager.jizhang.entity.Expense;
import com.billmanager.jizhang.entity.Income;
import com.billmanager.jizhang.exception.BusinessException;
import com.billmanager.jizhang.mapper.ExpenseCategoryMapper;
import com.billmanager.jizhang.mapper.ExpenseMapper;
import com.billmanager.jizhang.mapper.IncomeCategoryMapper;
import com.billmanager.jizhang.mapper.IncomeMapper;
import com.billmanager.jizhang.service.BillImportService;
import com.billmanager.jizhang.service.XunfeiApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 账单导入服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillImportServiceImpl implements BillImportService {
    
    private final XunfeiApiService xunfeiApiService;
    private final ExpenseMapper expenseMapper;
    private final IncomeMapper incomeMapper;
    private final ExpenseCategoryMapper expenseCategoryMapper;
    private final IncomeCategoryMapper incomeCategoryMapper;
    
    /**
     * 识别账单图像
     */
    @Override
    public BillImportResponse recognize(String base64Image, String accountType) {
        log.info("开始识别账单图像，账单类型: {}", accountType);
        
        // 验证图像数据
        if (base64Image == null || base64Image.trim().isEmpty()) {
            throw new BusinessException("图像数据不能为空");
        }
        
        // 调用讯飞API识别
        List<BillRecordDTO> records = xunfeiApiService.recognizeBillFromImage(base64Image, accountType);
        
        log.info("成功识别 {} 条账单记录", records.size());
        
        return BillImportResponse.builder()
                .records(records)
                .build();
    }
    
    /**
     * 确认并导入账单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillImportConfirmResponse confirm(Long userId, BillImportConfirmRequest request) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        
        if (request.getRecords() == null || request.getRecords().isEmpty()) {
            throw new BusinessException("导入的记录不能为空");
        }
        
        List<Long> importedIds = new ArrayList<>();
        
        for (BillImportConfirmRequest.BillRecordConfirmDTO record : request.getRecords()) {
            try {
                // 验证记录数据
                validateRecord(record);
                
                // 根据类型导入
                Long id = importRecord(userId, record);
                if (id != null) {
                    importedIds.add(id);
                }
            } catch (Exception e) {
                log.error("导入单条记录失败: {}", record, e);
                // 继续导入其他记录，记录失败但不中断整个流程
                // 如需要全部成功或全部失败，可将此改为throw
            }
        }
        
        log.info("账单导入完成，成功导入 {} 条记录", importedIds.size());
        
        return BillImportConfirmResponse.builder()
                .importedIds(importedIds)
                .build();
    }
    
    /**
     * 验证单条记录
     */
    private void validateRecord(BillImportConfirmRequest.BillRecordConfirmDTO record) {
        if (record.getAmount() == null || record.getAmount().signum() <= 0) {
            throw new BusinessException("金额必须大于0");
        }
        
        if (record.getTransactionDate() == null) {
            throw new BusinessException("交易日期不能为空");
        }
        
        if (record.getTransactionDate().isAfter(LocalDate.now().plusDays(1))) {
            throw new BusinessException("交易日期不能是未来日期");
        }
        
        if (record.getCategoryId() == null) {
            throw new BusinessException("分类ID不能为空");
        }
        
        String type = record.getType();
        if (type == null || (!type.equals("INCOME") && !type.equals("EXPENSE"))) {
            throw new BusinessException("交易类型必须是INCOME或EXPENSE");
        }
    }
    
    /**
     * 导入单条记录
     */
    private Long importRecord(Long userId, BillImportConfirmRequest.BillRecordConfirmDTO record) {
        String type = record.getType();
        
        if ("INCOME".equals(type)) {
            // 验证收入分类是否存在
            if (incomeCategoryMapper.findById(record.getCategoryId()) == null) {
                throw new BusinessException("收入分类不存在，分类ID: " + record.getCategoryId());
            }
            
            Income income = new Income();
            income.setUserId(userId);
            income.setCategoryId(record.getCategoryId());
            income.setAmount(record.getAmount());
            income.setTransactionDate(record.getTransactionDate());
            income.setDescription(record.getDescription());
            income.setCreateTime(LocalDateTime.now());
            income.setUpdateTime(LocalDateTime.now());
            
            incomeMapper.insert(income);
            return income.getId();
            
        } else if ("EXPENSE".equals(type)) {
            // 验证支出分类是否存在
            if (expenseCategoryMapper.findById(record.getCategoryId()) == null) {
                throw new BusinessException("支出分类不存在，分类ID: " + record.getCategoryId());
            }
            
            Expense expense = new Expense();
            expense.setUserId(userId);
            expense.setCategoryId(record.getCategoryId());
            expense.setAmount(record.getAmount());
            expense.setTransactionDate(record.getTransactionDate());
            expense.setDescription(record.getDescription());
            expense.setCreateTime(LocalDateTime.now());
            expense.setUpdateTime(LocalDateTime.now());
            
            expenseMapper.insert(expense);
            return expense.getId();
        }
        
        return null;
    }
}
