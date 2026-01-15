package com.billmanager.jizhang.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 加入家庭组请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinFamilyGroupRequest {
    /**
     * 家庭组编号
     */
    private String code;
    
    /**
     * 是否将现有数据带入家庭组
     * true: 将收入、支出、分类等数据同步到家庭组中
     * false: 保留为个人数据（family_group_id = 0）
     */
    private Boolean bringExistingData;
}
