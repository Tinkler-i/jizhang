package com.billmanager.jizhang.service.impl;

import com.billmanager.jizhang.dto.CaptchaInitResponse;
import com.billmanager.jizhang.dto.CaptchaVerifyRequest;
import com.billmanager.jizhang.dto.CaptchaVerifyResponse;
import com.billmanager.jizhang.service.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 人机验证码服务实现
 * 
 * 采用简单的内存存储方案（生产环境建议使用 Redis）
 */
@Slf4j
@Service
public class CaptchaServiceImpl implements CaptchaService {
    
    /**
     * 存储验证码信息的 Map
     * key: captchaId
     * value: CaptchaInfo
     */
    private static final Map<String, CaptchaInfo> CAPTCHA_STORE = new ConcurrentHashMap<>();
    
    /**
     * 存储有效的 token
     * key: token
     * value: 过期时间
     */
    private static final Map<String, Long> TOKEN_STORE = new ConcurrentHashMap<>();
    
    /**
     * 验证码有效期（5分钟）
     */
    private static final long CAPTCHA_EXPIRE_TIME = 5 * 60 * 1000;
    
    /**
     * Token 有效期（30分钟）
     */
    private static final long TOKEN_EXPIRE_TIME = 30 * 60 * 1000;
    
    /**
     * 允许的误差范围（像素）
     */
    private static final int TOLERANCE = 10;
    
    @Override
    public CaptchaInitResponse initCaptcha() {
        String captchaId = UUID.randomUUID().toString();
        String key = UUID.randomUUID().toString();
        
        // 生成随机滑块位置（假设验证码宽度为 310px，滑块宽度为 50px）
        int sliderPosition = new Random().nextInt(260) + 25;
        
        CaptchaInfo info = new CaptchaInfo();
        info.setCaptchaId(captchaId);
        info.setKey(key);
        info.setSliderPosition(sliderPosition);
        info.setCreatedTime(System.currentTimeMillis());
        info.setExpireTime(System.currentTimeMillis() + CAPTCHA_EXPIRE_TIME);
        
        CAPTCHA_STORE.put(captchaId, info);
        
        // 清理过期的验证码
        cleanExpiredCaptcha();
        
        log.info("【人机验证】初始化验证码 - captchaId: {}, key: {}, sliderPosition: {}", captchaId, key, sliderPosition);
        
        CaptchaInitResponse response = new CaptchaInitResponse();
        response.setKey(key);
        response.setCaptchaId(captchaId);
        response.setMessage("验证码初始化成功");
        
        return response;
    }
    
    @Override
    public CaptchaVerifyResponse verifyCaptcha(CaptchaVerifyRequest request) {
        CaptchaVerifyResponse response = new CaptchaVerifyResponse();
        
        try {
            // 参数检查
            if (request == null || request.getCaptchaId() == null) {
                response.setSuccess(false);
                response.setCode(1);
                response.setMessage("验证码 ID 不能为空");
                log.warn("【人机验证】验证失败：captchaId 为空");
                return response;
            }
            
            if (request.getKey() == null) {
                response.setSuccess(false);
                response.setCode(1);
                response.setMessage("验证码 key 不能为空");
                log.warn("【人机验证】验证失败：key 为空");
                return response;
            }
            
            String captchaId = request.getCaptchaId();
            String key = request.getKey();
            
            // 获取验证码信息
            CaptchaInfo info = CAPTCHA_STORE.get(captchaId);
            if (info == null) {
                response.setSuccess(false);
                response.setCode(1);
                response.setMessage("验证码不存在或已过期");
                log.warn("【人机验证】验证失败：验证码不存在 - captchaId: {}", captchaId);
                return response;
            }
            
            // 检查过期时间
            if (System.currentTimeMillis() > info.getExpireTime()) {
                CAPTCHA_STORE.remove(captchaId);
                response.setSuccess(false);
                response.setCode(1);
                response.setMessage("验证码已过期");
                log.warn("【人机验证】验证失败：验证码已过期 - captchaId: {}", captchaId);
                return response;
            }
            
            // 检查 key 是否匹配（Makeit Captcha 库已在前端进行了滑块位置验证，此处只需验证 key 的有效性）
            if (!key.equals(info.getKey())) {
                response.setSuccess(false);
                response.setCode(1);
                response.setMessage("验证码 key 不匹配");
                log.warn("【人机验证】验证失败：key 不匹配 - 期望: {}, 实际: {}", info.getKey(), key);
                return response;
            }
            
            // 验证成功，生成 token
            String token = UUID.randomUUID().toString();
            response.setKey(request.getKey());
            response.setToken(token);
            response.setSuccess(true);
            response.setCode(0);  // 成功用 code=0
            response.setMessage("验证码验证成功");
            
            // 保存 token
            TOKEN_STORE.put(token, System.currentTimeMillis() + TOKEN_EXPIRE_TIME);
            
            // 从存储中移除已使用的验证码
            CAPTCHA_STORE.remove(captchaId);
            
            log.info("【人机验证】验证成功 - captchaId: {}, token: {}", captchaId, token);
            
        } catch (Exception e) {
            log.error("【人机验证】验证过程异常", e);
            response.setSuccess(false);
            response.setCode(1);
            response.setMessage("验证码验证异常，请重试");
        }
        
        return response;
    }
    
    @Override
    public boolean isValidCaptchaToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        Long expireTime = TOKEN_STORE.get(token);
        if (expireTime == null) {
            return false;
        }
        
        if (System.currentTimeMillis() > expireTime) {
            TOKEN_STORE.remove(token);
            return false;
        }
        
        return true;
    }
    
    @Override
    public void useCaptchaToken(String token) {
        if (token != null && !token.isEmpty()) {
            TOKEN_STORE.remove(token);
            log.info("【人机验证】Token 已使用 - token: {}", token);
        }
    }
    
    /**
     * 清理过期的验证码
     */
    private void cleanExpiredCaptcha() {
        long currentTime = System.currentTimeMillis();
        CAPTCHA_STORE.entrySet().removeIf(entry -> entry.getValue().getExpireTime() < currentTime);
        TOKEN_STORE.entrySet().removeIf(entry -> entry.getValue() < currentTime);
    }
    
    /**
     * 验证码信息内部类
     */
    private static class CaptchaInfo {
        private String captchaId;
        private String key;
        private int sliderPosition;
        private long createdTime;
        private long expireTime;
        
        // Getters and Setters
        public String getCaptchaId() {
            return captchaId;
        }
        
        public void setCaptchaId(String captchaId) {
            this.captchaId = captchaId;
        }
        
        public String getKey() {
            return key;
        }
        
        public void setKey(String key) {
            this.key = key;
        }
        
        public int getSliderPosition() {
            return sliderPosition;
        }
        
        public void setSliderPosition(int sliderPosition) {
            this.sliderPosition = sliderPosition;
        }
        
        public long getCreatedTime() {
            return createdTime;
        }
        
        public void setCreatedTime(long createdTime) {
            this.createdTime = createdTime;
        }
        
        public long getExpireTime() {
            return expireTime;
        }
        
        public void setExpireTime(long expireTime) {
            this.expireTime = expireTime;
        }
    }
}
