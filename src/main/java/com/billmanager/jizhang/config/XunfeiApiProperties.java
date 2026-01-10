package com.billmanager.jizhang.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 讯飞大模型API配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "xunfei.api")
public class XunfeiApiProperties {
    
    /**
     * 讯飞Spark API的AppId
     */
    private String appId;
    
    /**
     * 讯飞API Key
     */
    private String apiKey;
    
    /**
     * 讯飞API Secret
     */
    private String apiSecret;
    
    /**
     * 图像理解API的请求URL
     */
    private String imageUnderstandingUrl;
    
    /**
     * API调用超时时间（毫秒）
     */
    private long timeout = 30000;
}
