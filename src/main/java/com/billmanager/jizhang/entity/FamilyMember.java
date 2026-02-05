package com.billmanager.jizhang.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 家庭成员实体
 */
@Data
public class FamilyMember {
    private Long id;
    private Long familyGroupId;        // 家庭组ID
    private Long userId;               // 用户ID
    private String role;               // ADMIN或MEMBER
    private String permissions;        // JSON格式的权限配置
    private LocalDateTime joinTime;    // 加入时间
    private Integer status;            // 1正常、0禁用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
