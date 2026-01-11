package com.billmanager.jizhang.mapper;

import com.billmanager.jizhang.entity.VerificationCode;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface VerificationCodeMapper {
    
    /**
     * 新增验证码
     */
    @Insert("INSERT INTO verification_code (email, phone, code, type, created_time, ttl, is_used) " +
            "VALUES (#{email}, #{phone}, #{code}, #{type}, #{createdTime}, #{ttl}, #{isUsed})")
    void insert(VerificationCode verificationCode);
    
    /**
     * 根据邮箱获取最新的验证码
     */
    @Select("SELECT * FROM verification_code WHERE email = #{email} ORDER BY created_time DESC LIMIT 1")
    VerificationCode findLatestByEmail(String email);
    
    /**
     * 根据手机号获取最新的验证码
     */
    @Select("SELECT * FROM verification_code WHERE phone = #{phone} ORDER BY created_time DESC LIMIT 1")
    VerificationCode findLatestByPhone(String phone);
    
    /**
     * 根据邮箱和验证码查询
     */
    @Select("SELECT * FROM verification_code WHERE email = #{email} AND code = #{code} AND is_used = 0 ORDER BY created_time DESC LIMIT 1")
    VerificationCode findByEmailAndCode(String email, String code);
    
    /**
     * 根据手机号和验证码查询
     */
    @Select("SELECT * FROM verification_code WHERE phone = #{phone} AND code = #{code} AND is_used = 0 ORDER BY created_time DESC LIMIT 1")
    VerificationCode findByPhoneAndCode(String phone, String code);
    
    /**
     * 标记验证码为已使用
     */
    @Update("UPDATE verification_code SET is_used = 1 WHERE id = #{id}")
    void markAsUsed(Long id);
}
