package com.billmanager.jizhang.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 家庭组实体
 */
@Data
public class FamilyGroup {
    private Long id;
    private String code;               // 6位编号，如：FA8K3M
    private String name;               // 家庭组名称
    private String description;        // 家庭介绍
    private Long creatorId;            // 创建者ID
    private Integer status;            // 1正常、0禁用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
