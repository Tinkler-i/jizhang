package com.billmanager.jizhang.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 退出家庭组请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExitFamilyGroupRequest {
    /**
     * 是否删除在家庭组中的数据
     * true: 删除该用户在此家庭组中的所有数据（收入、支出、分类）
     * false: 将数据转换为个人数据（family_group_id 改为 0）
     */
    private Boolean deleteData;
}
