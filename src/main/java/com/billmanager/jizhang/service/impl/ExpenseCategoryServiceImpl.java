package com.billmanager.jizhang.service.impl;

import com.billmanager.jizhang.dto.ExpenseCategoryRequest;
import com.billmanager.jizhang.entity.ExpenseCategory;
import com.billmanager.jizhang.exception.BusinessException;
import com.billmanager.jizhang.mapper.ExpenseCategoryMapper;
import com.billmanager.jizhang.service.ExpenseCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseCategoryServiceImpl implements ExpenseCategoryService {
    
    private final ExpenseCategoryMapper expenseCategoryMapper;
    
    @Override
    public ExpenseCategory add(ExpenseCategoryRequest request, Long userId) {
        ExpenseCategory existing = expenseCategoryMapper.findByUserIdAndName(userId, request.getName());
        if (existing != null) {
            throw new BusinessException("分类名称已存在");
        }
        
        ExpenseCategory category = new ExpenseCategory();
        category.setUserId(userId);
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        
        expenseCategoryMapper.insert(category);
        return category;
    }
    
    @Override
    public ExpenseCategory update(Long id, ExpenseCategoryRequest request, Long userId) {
        ExpenseCategory category = expenseCategoryMapper.findById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        if (!category.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此分类");
        }
        
        ExpenseCategory existing = expenseCategoryMapper.findByUserIdAndName(userId, request.getName());
        if (existing != null && !existing.getId().equals(id)) {
            throw new BusinessException("分类名称已存在");
        }
        
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        
        expenseCategoryMapper.update(category);
        return category;
    }
    
    @Override
    public void delete(Long id, Long userId) {
        ExpenseCategory category = expenseCategoryMapper.findById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        if (!category.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此分类");
        }
        
        expenseCategoryMapper.deleteById(id);
    }
    
    @Override
    public ExpenseCategory findById(Long id, Long userId) {
        ExpenseCategory category = expenseCategoryMapper.findById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        if (!category.getUserId().equals(userId)) {
            throw new BusinessException("无权查看此分类");
        }
        return category;
    }
    
    @Override
    public List<ExpenseCategory> findByUserId(Long userId) {
        return expenseCategoryMapper.findByUserId(userId);
    }
}
