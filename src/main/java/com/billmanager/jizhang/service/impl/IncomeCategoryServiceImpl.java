package com.billmanager.jizhang.service.impl;

import com.billmanager.jizhang.dto.IncomeCategoryRequest;
import com.billmanager.jizhang.entity.IncomeCategory;
import com.billmanager.jizhang.exception.BusinessException;
import com.billmanager.jizhang.mapper.IncomeCategoryMapper;
import com.billmanager.jizhang.service.IncomeCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeCategoryServiceImpl implements IncomeCategoryService {
    
    private final IncomeCategoryMapper incomeCategoryMapper;
    
    @Override
    public IncomeCategory add(IncomeCategoryRequest request, Long userId) {
        IncomeCategory existing = incomeCategoryMapper.findByUserIdAndName(userId, request.getName());
        if (existing != null) {
            throw new BusinessException("分类名称已存在");
        }
        
        IncomeCategory category = new IncomeCategory();
        category.setUserId(userId);
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        
        incomeCategoryMapper.insert(category);
        return category;
    }
    
    @Override
    public IncomeCategory update(Long id, IncomeCategoryRequest request, Long userId) {
        IncomeCategory category = incomeCategoryMapper.findById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        if (!category.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此分类");
        }
        
        IncomeCategory existing = incomeCategoryMapper.findByUserIdAndName(userId, request.getName());
        if (existing != null && !existing.getId().equals(id)) {
            throw new BusinessException("分类名称已存在");
        }
        
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        
        incomeCategoryMapper.update(category);
        return category;
    }
    
    @Override
    public void delete(Long id, Long userId) {
        IncomeCategory category = incomeCategoryMapper.findById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        if (!category.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此分类");
        }
        
        incomeCategoryMapper.deleteById(id);
    }
    
    @Override
    public IncomeCategory findById(Long id, Long userId) {
        IncomeCategory category = incomeCategoryMapper.findById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        if (!category.getUserId().equals(userId)) {
            throw new BusinessException("无权查看此分类");
        }
        return category;
    }
    
    @Override
    public List<IncomeCategory> findByUserId(Long userId) {
        return incomeCategoryMapper.findByUserId(userId);
    }
}
