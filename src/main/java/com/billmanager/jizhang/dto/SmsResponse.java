package com.billmanager.jizhang.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 阿里云短信 API 响应
 */
@Data
public class SmsResponse {
    
    /**
     * 请求 ID
     */
    @JsonProperty("request_id")
    private String requestId;
    
    /**
     * 响应状态：OK（成功）或其他错误码
     */
    private String status;
    
    /**
     * 错误原因描述（失败时）
     */
    private String reason;
    
    /**
     * 检查响应是否成功
     */
    public boolean isSuccess() {
        return "OK".equals(this.status);
    }
    
    @Override
    public String toString() {
        return "SmsResponse{" +
                "requestId='" + requestId + '\'' +
                ", status='" + status + '\'' +
                ", reason='" + reason + '\'' +
                '}';
    }
}
