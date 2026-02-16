package com.billmanager.jizhang.service;

import com.billmanager.jizhang.entity.UserTarget;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户目标服务接口
 * 处理用户收入目标的业务逻辑
 */
public interface UserTargetService {
    
    /**
     * 按 ID 查询目标
     * @param id 目标ID
     * @return 目标对象
     */
    UserTarget findById(Long id);
    
    /**
     * 查询用户指定月份的目标
     * @param userId 用户ID
     * @param targetMonth 目标年月（格式：YYYY-MM）
     * @return 目标对象，不存在返回 null
     */
    UserTarget findByUserIdAndMonth(Long userId, String targetMonth);
    
    /**
     * 查询用户的所有目标
     * @param userId 用户ID
     * @return 目标列表
     */
    List<UserTarget> findByUserId(Long userId);
    
    /**
     * 创建或更新目标
     * @param userId 用户ID
     * @param targetMonth 目标年月（格式：YYYY-MM）
     * @param incomeTarget 收入目标
     * @return 目标对象
     */
    UserTarget createOrUpdate(Long userId, String targetMonth, BigDecimal incomeTarget);
    
    /**
     * 创建目标
     * @param userTarget 目标对象
     * @return 创建后的目标对象
     */
    UserTarget create(UserTarget userTarget);
    
    /**
     * 更新目标
     * @param userTarget 目标对象
     * @return 更新后的目标对象
     */
    UserTarget update(UserTarget userTarget);
    
    /**
     * 删除目标
     * @param id 目标ID
     * @param userId 用户ID（用于权限验证）
     */
    void delete(Long id, Long userId);
    
    /**
     * 删除目标
     * @param id 目标ID
     */
    void delete(Long id);
    
    /**
     * 查询用户在某个月份范围内的目标
     * @param userId 用户ID
     * @param startMonth 起始年月（格式：YYYY-MM）
     * @param endMonth 结束年月（格式：YYYY-MM）
     * @return 目标列表
     */
    List<UserTarget> findByUserIdAndMonthRange(Long userId, String startMonth, String endMonth);
}
