package com.billmanager.jizhang.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 短信服务配置
 * 
 * 支持的短信服务商配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "aliyun.sms")
public class SmsConfig {
    
    /**
     * API 主机地址
     * 示例：https://dfsns.market.alicloudapi.com
     */
    private String host;
    
    /**
     * API 路径
     * 示例：/data/send_sms
     */
    private String path;
    
    /**
     * AppCode（阿里云 API 网关的授权码）
     */
    private String appcode;
    
    /**
     * 短信模板 ID
     * 注意：不同的短信服务商使用不同的参数名
     * - 某些使用 templateid
     * - 某些使用 template_id
     * 
     * 当前新接口使用：template_id
     */
    private String templateId;
    
    /**
     * 短信签名（可选）
     */
    private String signature;
    
    /**
     * 请求超时时间（毫秒）
     */
    private int timeout = 5000;
    
    /**
     * 是否启用短信功能
     */
    private boolean enabled = false;
}
