package com.billmanager.jizhang.service;

/**
 * 登录尝试管理服务
 * 用于防止暴力破解密码（基于浏览器标识而非用户名）
 * 这样可以防止恶意用户通过攻击某个用户名来锁定正常用户账户
 * 
 * 【关键限制】此服务【仅用于登录端点】，不应影响其他业务流程如注册、忘记密码等。
 * 
 * 防护机制：
 * - 记录来自同一浏览器的登录失败次数
 * - 失败3次后，要求进行人机验证
 * - 失败6次后，锁定该浏览器（初始5分钟，后续失败将指数增加锁定时间）
 * - 验证码通过后或登录成功后，重置计数器
 * 
 * 使用场景：登录防护（LoginController 中的 login() 和 authLogin() 方法）
 * 不使用场景：注册、忘记密码、其他业务流程
 */
public interface LoginAttemptService {
    
    /**
     * 增加登录失败次数
     * @param browserFingerprint 浏览器指纹（IP+UserAgent）
     * @param username 用户名（用于日志记录）
     */
    void addFailedAttempt(String browserFingerprint, String username);
    
    /**
     * 获取失败次数
     * @param browserFingerprint 浏览器指纹
     * @return 失败次数
     */
    int getFailedAttempts(String browserFingerprint);
    
    /**
     * 检查浏览器是否被锁定
     * @param browserFingerprint 浏览器指纹
     * @return true 表示被锁定，false 表示未锁定
     */
    boolean isLocked(String browserFingerprint);
    
    /**
     * 获取锁定剩余时间（毫秒）
     * @param browserFingerprint 浏览器指纹
     * @return 如果被锁定返回剩余时间，否则返回 0
     */
    long getLockTimeRemaining(String browserFingerprint);
    
    /**
     * 检查是否需要人机验证
     * @param browserFingerprint 浏览器指纹
     * @return true 表示需要验证码，false 表示不需要
     */
    boolean needsCaptcha(String browserFingerprint);
    
    /**
     * 重置登录失败计数（验证码验证成功后调用）
     * @param browserFingerprint 浏览器指纹
     */
    void resetAttempts(String browserFingerprint);
    
    /**
     * 清除浏览器的所有尝试记录（登录成功后调用）
     * @param browserFingerprint 浏览器指纹
     */
    void clearAttempts(String browserFingerprint);
}

