package com.billmanager.jizhang.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HTTP 请求工具类（用于调用阿里云 API）
 * 参考：https://github.com/aliyun/api-gateway-demo-sign-java
 */
@Slf4j
public class HttpUtils {
    
    /**
     * 执行 POST 请求
     *
     * @param host     API 主机地址
     * @param path     API 路径
     * @param method   HTTP 方法（通常是 POST）
     * @param headers  请求头
     * @param querys   查询参数（URL 参数）
     * @param bodys    请求体参数（表单参数）
     * @return         HTTP 响应
     */
    public static HttpResponse doPost(String host, String path, String method,
                                      Map<String, String> headers,
                                      Map<String, String> querys,
                                      Map<String, String> bodys) {
        try {
            // 构建完整的 URL
            String url = buildUrl(host, path, querys);
            
            // 构建请求
            ClassicRequestBuilder requestBuilder = ClassicRequestBuilder.post(url);
            
            // 设置请求头
            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    requestBuilder.addHeader(entry.getKey(), entry.getValue());
                }
            }
            
            // 设置请求体（表单参数）
            if (bodys != null && !bodys.isEmpty()) {
                List<NameValuePair> nameValuePairs = new ArrayList<>();
                for (Map.Entry<String, String> entry : bodys.entrySet()) {
                    nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                // 使用 ClassicRequestBuilder 的 addParameters 方法
                for (NameValuePair pair : nameValuePairs) {
                    requestBuilder.addParameter(pair.getName(), pair.getValue());
                }
            }
            
            // 执行请求
            HttpClient httpClient = HttpClientBuilder.create().build();
            ClassicHttpResponse response = httpClient.executeOpen(null, requestBuilder.build(), null);
            
            return new HttpResponse(response);
        } catch (Exception e) {
            log.error("HTTP POST 请求失败", e);
            throw new RuntimeException("HTTP 请求失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 构建完整的 URL
     */
    private static String buildUrl(String host, String path, Map<String, String> querys) {
        StringBuilder url = new StringBuilder();
        url.append(host).append(path);
        
        if (querys != null && !querys.isEmpty()) {
            url.append("?");
            boolean first = true;
            for (Map.Entry<String, String> entry : querys.entrySet()) {
                if (!first) {
                    url.append("&");
                }
                url.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
        }
        
        return url.toString();
    }
    
    /**
     * 简化的 POST 请求方法（已弃用，使用 doPost 方法）
     * 
     * @deprecated 请使用 doPost() 方法代替，以获得更好的控制和错误处理
     */
    @Deprecated
    public static HttpResponse post(String host, String path, String appcode, Map<String, String> bodys) {
        Map<String, String> headers = new java.util.HashMap<>();
        headers.put("Authorization", "APPCODE " + appcode);
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        
        return doPost(host, path, "POST", headers, null, bodys);
    }
    
    /**
     * HTTP 响应包装类
     */
    public static class HttpResponse {
        private final ClassicHttpResponse response;
        private String body;
        
        public HttpResponse(ClassicHttpResponse response) {
            this.response = response;
        }
        
        /**
         * 获取响应状态码
         */
        public int getStatusCode() {
            return response.getCode();
        }
        
        /**
         * 获取响应体内容
         */
        public String getBody() {
            if (body == null) {
                try {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        body = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                    }
                } catch (Exception e) {
                    log.error("获取响应体失败", e);
                }
            }
            return body;
        }
        
        @Override
        public String toString() {
            return "HttpResponse{" +
                    "statusCode=" + getStatusCode() +
                    ", body='" + getBody() + '\'' +
                    '}';
        }
    }
}
