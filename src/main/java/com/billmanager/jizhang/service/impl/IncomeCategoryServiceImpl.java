package com.billmanager.jizhang.service.impl;

import com.billmanager.jizhang.dto.IncomeCategoryRequest;
import com.billmanager.jizhang.entity.IncomeCategory;
import com.billmanager.jizhang.entity.FamilyGroup;
import com.billmanager.jizhang.exception.BusinessException;
import com.billmanager.jizhang.exception.FamilyPermissionException;
import com.billmanager.jizhang.mapper.IncomeCategoryMapper;
import com.billmanager.jizhang.service.IncomeCategoryService;
import com.billmanager.jizhang.service.FamilyGroupService;
import com.billmanager.jizhang.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncomeCategoryServiceImpl implements IncomeCategoryService {
    
    private final IncomeCategoryMapper incomeCategoryMapper;
    private final FamilyGroupService familyGroupService;
    private final PermissionService permissionService;
    
    @Override
    public IncomeCategory add(IncomeCategoryRequest request, Long userId) {
        // 检查编辑权限（收入分类使用收入权限）
        if (!permissionService.canEdit(userId, "income")) {
            throw new FamilyPermissionException("没有创建收入分类的权限");
        }
        
        FamilyGroup familyGroup = familyGroupService.getFamilyGroupByUserId(userId);
        Long familyGroupId = (familyGroup != null) ? familyGroup.getId() : 0L;
        
        // 检查分类名称是否已存在
        IncomeCategory existing;
        if (familyGroup != null) {
            // 在家庭组中检查重名
            existing = incomeCategoryMapper.findByFamilyGroupIdAndName(
                    familyGroup.getId(), request.getName());
        } else {
            // 在个人数据中检查重名
            existing = incomeCategoryMapper.findByUserIdAndName(userId, request.getName());
        }
        
        if (existing != null) {
            throw new BusinessException("分类名称已存在");
        }
        
        IncomeCategory category = new IncomeCategory();
        category.setUserId(userId);
        category.setFamilyGroupId(familyGroupId);
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        
        incomeCategoryMapper.insert(category);
        return category;
    }
    
    @Override
    public IncomeCategory update(Long id, IncomeCategoryRequest request, Long userId) {
        // 检查编辑权限
        if (!permissionService.canEdit(userId, "income")) {
            throw new FamilyPermissionException("没有编辑收入分类的权限");
        }
        
        IncomeCategory category = incomeCategoryMapper.findById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        if (!category.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此分类");
        }
        
        IncomeCategory existing = incomeCategoryMapper.findByFamilyGroupIdAndName(
                category.getFamilyGroupId(), request.getName());
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
        // 检查编辑权限
        if (!permissionService.canEdit(userId, "income")) {
            throw new FamilyPermissionException("没有删除收入分类的权限");
        }
        
        IncomeCategory category = incomeCategoryMapper.findById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        if (!category.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此分类");
        }
        
        // 检查是否是系统内置分类，不允许删除
        if (category.getIsBuiltIn() != null && category.getIsBuiltIn() == 1) {
            throw new BusinessException("系统内置分类不能删除");
        }
        
        incomeCategoryMapper.deleteById(id);
    }
    
    @Override
    public IncomeCategory findById(Long id, Long userId) {
        // 检查查看权限
        if (!permissionService.canView(userId, "income")) {
            throw new FamilyPermissionException("没有查看收入分类的权限");
        }
        
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
        // 检查查看权限
        if (!permissionService.canView(userId, "income")) {
            throw new FamilyPermissionException("没有查看收入分类的权限");
        }
        
        FamilyGroup familyGroup = familyGroupService.getFamilyGroupByUserId(userId);
        if (familyGroup != null) {
            // 用户属于某个家庭组，按familyGroupId查询
            return incomeCategoryMapper.findByFamilyGroupId(familyGroup.getId());
        } else {
            // 用户不属于任何家庭组，按userId查询（完全访问权限）
            log.info("【收入分类】用户{}不属于任何家庭组，按个人身份查询分类", userId);
            return incomeCategoryMapper.findByUserId(userId);
        }
    }
}
