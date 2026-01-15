package com.billmanager.jizhang.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 家庭成员信息传输对象
 * 用于返回成员及其对应的用户信息（包括昵称）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyMemberDTO {
    // 成员信息
    private Long id;
    private Long familyGroupId;
    private Long userId;
    private String role;
    private String permissions;
    private LocalDateTime joinTime;
    private Integer status;
    
    // 用户信息
    private String username;
    private String nickname;
    private String email;
    private String phone;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
