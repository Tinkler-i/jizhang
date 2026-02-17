package com.billmanager.jizhang.service.impl;

import com.billmanager.jizhang.service.LoginAttemptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 登录尝试管理服务实现
 * 基于浏览器指纹（IP + UserAgent）的防暴力破解机制
 */
@Slf4j
@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {
    
    // 失败尝试次数记录：browserFingerprint -> 失败次数
    private final ConcurrentHashMap<String, AtomicInteger> failedAttempts = new ConcurrentHashMap<>();
    
    // 锁定时间记录：browserFingerprint -> 锁定解除时间戳
    private final ConcurrentHashMap<String, Long> lockTimeMap = new ConcurrentHashMap<>();
    
    // 验证码验证通过标记：browserFingerprint -> 验证通过时间戳
    private final ConcurrentHashMap<String, Long> captchaPassedMap = new ConcurrentHashMap<>();
    
    // 配置参数
    private static final int MAX_ATTEMPTS_BEFORE_CAPTCHA = 3;  // 触发验证码的尝试次数
    private static final int MAX_ATTEMPTS_BEFORE_LOCK = 6;     // 触发锁定的尝试次数（验证码通过后还允许 3 次）
    private static final long INITIAL_LOCK_TIME = 5 * 60 * 1000; // 初始锁定 5 分钟
    private static final long CAPTCHA_PASS_VALIDITY = 30 * 60 * 1000; // 验证码通过后 30 分钟内可尝试
    
    @Override
    public void addFailedAttempt(String browserFingerprint, String username) {
        failedAttempts.computeIfAbsent(browserFingerprint, k -> new AtomicInteger(0)).incrementAndGet();
        int attempts = failedAttempts.get(browserFingerprint).get();
        log.warn("【登录防护】浏览器 {} 登录尝试失败（用户: {}），失败次数: {}", 
                browserFingerprint, username, attempts);
    }
    
    @Override
    public int getFailedAttempts(String browserFingerprint) {
        AtomicInteger attempts = failedAttempts.get(browserFingerprint);
        return attempts != null ? attempts.get() : 0;
    }
    
    @Override
    public boolean isLocked(String browserFingerprint) {
        Long lockUntil = lockTimeMap.get(browserFingerprint);
        if (lockUntil == null) {
            return false;
        }
        
        long now = System.currentTimeMillis();
        boolean locked = now < lockUntil;
        
        if (!locked) {
            // 锁定时间已过期，清除记录
            lockTimeMap.remove(browserFingerprint);
            failedAttempts.remove(browserFingerprint);
            captchaPassedMap.remove(browserFingerprint);
            log.info("【登录防护】浏览器 {} 的锁定时间已到期，已解除锁定", browserFingerprint);
        }
        
        return locked;
    }
    
    @Override
    public long getLockTimeRemaining(String browserFingerprint) {
        Long lockUntil = lockTimeMap.get(browserFingerprint);
        if (lockUntil == null) {
            return 0;
        }
        
        long now = System.currentTimeMillis();
        long remaining = lockUntil - now;
        
        return Math.max(0, remaining);
    }
    
    @Override
    public boolean needsCaptcha(String browserFingerprint) {
        // 如果被锁定则不需要验证码（直接拒绝）
        if (isLocked(browserFingerprint)) {
            return false;
        }
        
        // 如果已经通过验证码，则不需要再验证
        Long captchaPassTime = captchaPassedMap.get(browserFingerprint);
        if (captchaPassTime != null && System.currentTimeMillis() - captchaPassTime < CAPTCHA_PASS_VALIDITY) {
            return false;
        }
        
        // 失败次数超过 3 次则需要验证码
        int attempts = getFailedAttempts(browserFingerprint);
        return attempts >= MAX_ATTEMPTS_BEFORE_CAPTCHA;
    }
    
    @Override
    public void resetAttempts(String browserFingerprint) {
        log.info("【登录防护】验证码验证成功，浏览器 {} 继续可尝试登录，失败计数保留", browserFingerprint);
        
        // 只标记验证码通过的时间，不清除失败计数
        // 这样验证码通过后还能继续尝试 3 次，然后才会触发锁定
        captchaPassedMap.put(browserFingerprint, System.currentTimeMillis());
    }
    
    @Override
    public void clearAttempts(String browserFingerprint) {
        log.info("【登录防护】登录成功，清除浏览器 {} 的所有登录防护记录", browserFingerprint);
        
        failedAttempts.remove(browserFingerprint);
        lockTimeMap.remove(browserFingerprint);
        captchaPassedMap.remove(browserFingerprint);
    }
    
    /**
     * 内部方法：执行登录失败后的处理，包括锁定逻辑
     * 第一阶段（0-3 次失败）：需要验证码
     * 第二阶段（3-6 次失败）：验证码通过后，再失败 3 次直接锁定
     */
    public void handleLoginFailure(String browserFingerprint) {
        int attempts = getFailedAttempts(browserFingerprint);
        
        // 只有在达到 6 次失败才锁定
        // 之前的 3 次失败会触发验证码要求
        if (attempts >= MAX_ATTEMPTS_BEFORE_LOCK) {
            long lockDuration = INITIAL_LOCK_TIME;
            
            // 计算锁定轮次（每 3 次失败一个轮次）
            // 6 次：第 1 轮锁定 5 分钟
            // 9 次：第 2 轮锁定 10 分钟
            // 12 次：第 3 轮锁定 20 分钟
            int lockCycles = (attempts - MAX_ATTEMPTS_BEFORE_LOCK) / MAX_ATTEMPTS_BEFORE_CAPTCHA + 1;
            lockDuration = INITIAL_LOCK_TIME * (long) Math.pow(2, lockCycles - 1);
            
            long lockUntil = System.currentTimeMillis() + lockDuration;
            lockTimeMap.put(browserFingerprint, lockUntil);
            
            log.warn("【登录防护】浏览器 {} 已超过尝试限制（失败 {} 次），已被锁定 {} 分钟", 
                    browserFingerprint, attempts, lockDuration / 60 / 1000);
        }
    }
}

