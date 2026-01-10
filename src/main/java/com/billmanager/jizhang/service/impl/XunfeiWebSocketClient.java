package com.billmanager.jizhang.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.*;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.net.URLEncoder;

/**
 * 讯飞WebSocket客户端 - 基于OkHttp3实现
 * 参照官方Demo改造
 */
@Slf4j
public class XunfeiWebSocketClient {
    
    private final String base64Image;
    private final String accountType;
    private final String appId;
    private final String apiKey;
    private final String apiSecret;
    private final long timeout;
    
    @Getter
    private String response;
    
    @Getter
    private String error;
    
    private final CountDownLatch completeLatch = new CountDownLatch(1);
    private StringBuilder responseBuffer = new StringBuilder();
    private WebSocket webSocket;
    
    public XunfeiWebSocketClient(String appId, String apiKey, String apiSecret, 
                                 String base64Image, String accountType, long timeout) {
        this.appId = appId;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.base64Image = base64Image;
        this.accountType = accountType;
        this.timeout = timeout;
        
        // 启动连接
        connect();
    }
    
    /**
     * 连接到讯飞API
     */
    private void connect() {
        new Thread(() -> {
            try {
                log.info("开始初始化OkHttp3 WebSocket客户端...");
                
                // 生成鉴权URL
                String authUrl = getAuthUrl();
                log.debug("鉴权URL生成完成");
                
                // 转换为 wss:// 协议
                String wsUrl = authUrl.replace("https://", "wss://");
                log.debug("WebSocket URL: {}", wsUrl.substring(0, Math.min(100, wsUrl.length())) + "...");
                
                // 创建信任所有证书的SSL Socket Factory
                TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }
                        
                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }
                        
                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
                };
                
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                
                // 构建OkHttp客户端
                OkHttpClient client = new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();
                
                // 创建请求
                Request request = new Request.Builder()
                    .url(wsUrl)
                    .build();
                
                log.info("正在连接到讯飞API WebSocket...");
                
                // 创建WebSocket连接
                webSocket = client.newWebSocket(request, new WebSocketListener() {
                    @Override
                    public void onOpen(WebSocket webSocket, Response response) {
                        log.info("✅ WebSocket连接已建立");
                        // 发送识别请求
                        sendRecognitionRequest(webSocket);
                    }
                    
                    @Override
                    public void onMessage(WebSocket webSocket, String text) {
                        log.debug("收到WebSocket消息，长度: {} 字符", text.length());
                        handleMessage(text);
                    }
                    
                    @Override
                    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                        log.error("❌ WebSocket连接失败: {}", t.getMessage(), t);
                        try {
                            if (response != null) {
                                int code = response.code();
                                log.error("HTTP响应码: {}", code);
                                if (response.body() != null) {
                                    log.error("响应体: {}", response.body().string());
                                }
                            }
                        } catch (IOException e) {
                            log.error("读取响应失败", e);
                        }
                        XunfeiWebSocketClient.this.error = "WebSocket连接失败: " + t.getMessage();
                        completeLatch.countDown();
                    }
                    
                    @Override
                    public void onClosed(WebSocket webSocket, int code, String reason) {
                        log.debug("WebSocket已关闭: code={}, reason={}", code, reason);
                    }
                });
                
            } catch (Exception e) {
                log.error("❌ 初始化WebSocket客户端失败: {}", e.getMessage(), e);
                this.error = "WebSocket初始化失败: " + e.getMessage();
                completeLatch.countDown();
            }
        }).start();
    }
    
    /**
     * 发送识别请求
     */
    private void sendRecognitionRequest(WebSocket webSocket) {
        try {
            log.info("开始构建识别请求...");
            
            JSONObject requestJson = new JSONObject();
            
            // header参数
            JSONObject header = new JSONObject();
            header.put("app_id", appId);
            header.put("uid", UUID.randomUUID().toString().substring(0, 10));
            
            // parameter参数
            JSONObject parameter = new JSONObject();
            JSONObject chat = new JSONObject();
            chat.put("domain", "imagev3");
            chat.put("temperature", 0.5);
            chat.put("max_tokens", 4096);
            chat.put("auditing", "default");
            parameter.put("chat", chat);
            
            // payload参数
            JSONObject payload = new JSONObject();
            JSONObject message = new JSONObject();
            JSONArray text = new JSONArray();
            
            // 构建图像和提示词
            JSONObject imageContent = new JSONObject();
            imageContent.put("role", "user");
            imageContent.put("content", base64Image);
            imageContent.put("content_type", "image");
            text.add(imageContent);
            
            JSONObject textContent = new JSONObject();
            textContent.put("role", "user");
            textContent.put("content", buildPrompt());
            textContent.put("content_type", "text");
            text.add(textContent);
            
            message.put("text", text);
            payload.put("message", message);
            
            requestJson.put("header", header);
            requestJson.put("parameter", parameter);
            requestJson.put("payload", payload);
            
            String requestBody = requestJson.toString();
            log.debug("请求体长度: {} 字符", requestBody.length());
            
            webSocket.send(requestBody);
            log.info("✅ 请求已发送到讯飞API");
            
        } catch (Exception e) {
            log.error("❌ 构建识别请求失败: {}", e.getMessage(), e);
            this.error = "构建请求失败: " + e.getMessage();
            completeLatch.countDown();
        }
    }
    
    /**
     * 处理WebSocket消息
     */
    private void handleMessage(String text) {
        try {
            JSONObject jsonObject = JSON.parseObject(text);
            
            JSONObject header = jsonObject.getJSONObject("header");
            if (header == null) {
                return;
            }
            
            int code = header.getIntValue("code");
            if (code != 0) {
                log.error("❌ 讯飞API错误，code: {}, sid: {}", code, header.getString("sid"));
                this.error = "讯飞API错误，code: " + code;
                completeLatch.countDown();
                return;
            }
            
            // 处理响应内容
            JSONObject payload = jsonObject.getJSONObject("payload");
            if (payload != null) {
                JSONObject choices = payload.getJSONObject("choices");
                if (choices != null) {
                    JSONArray textArray = choices.getJSONArray("text");
                    if (textArray != null && textArray.size() > 0) {
                        for (int i = 0; i < textArray.size(); i++) {
                            JSONObject item = textArray.getJSONObject(i);
                            String content = item.getString("content");
                            if (content != null) {
                                responseBuffer.append(content);
                                log.debug("追加响应内容，当前长度: {}", responseBuffer.length());
                            }
                        }
                    }
                }
            }
            
            // 检查是否响应完成
            int status = header.getIntValue("status");
            if (status == 2) {
                this.response = responseBuffer.toString().trim();
                log.info("✅ 讯飞API响应完成，总长度: {} 字符", this.response.length());
                completeLatch.countDown();
                
                if (webSocket != null) {
                    webSocket.close(1000, "completed");
                }
            }
            
        } catch (Exception e) {
            log.error("❌ 处理WebSocket消息失败: {}", e.getMessage(), e);
            this.error = "处理响应失败: " + e.getMessage();
            completeLatch.countDown();
        }
    }
    
    /**
     * 生成鉴权URL
     */
    private String getAuthUrl() throws Exception {
        URL url = new URL("https://spark-api.cn-huabei-1.xf-yun.com/v2.1/image");
        
        // 生成时间戳
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        
        log.debug("生成鉴权URL - 日期: {}", date);
        
        // 拼接签名字符串
        String preStr = "host: " + url.getHost() + "\n" +
                "date: " + date + "\n" +
                "GET " + url.getPath() + " HTTP/1.1";
        
        log.debug("签名原始字符串:\n{}", preStr);
        
        // HMAC-SHA256签名
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
        mac.init(spec);
        
        byte[] hexDigits = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));
        String sha = Base64.getEncoder().encodeToString(hexDigits);
        
        log.debug("签名结果: {}", sha);
        
        // 拼接授权字符串
        String authorization = String.format(
            "api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
            apiKey, "hmac-sha256", "host date request-line", sha
        );
        
        log.debug("授权字符串: {}", authorization);
        
        // 拼接最终URL
        String authUrl = "https://" + url.getHost() + url.getPath() +
                "?authorization=" + URLEncoder.encode(Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8) +
                "&date=" + URLEncoder.encode(date, StandardCharsets.UTF_8) +
                "&host=" + url.getHost();
        
        log.debug("最终鉴权URL: {}...{}", 
            authUrl.substring(0, Math.min(100, authUrl.length())),
            authUrl.length() > 100 ? authUrl.substring(authUrl.length() - 50) : "");
        
        return authUrl;
    }
    
    /**
     * 构建系统提示词
     */
    private String buildPrompt() {
        return "你是一个专业的账单数据提取助手。你的任务是从账单截图中准确提取交易信息。\n" +
                "\n请从图像中提取以下信息:\n" +
                "- type: 交易类型，必须是 'INCOME'(收入) 或 'EXPENSE'(支出)\n" +
                "- amount: 交易金额，必须是数字，例如 1000.00\n" +
                "- transactionDate: 交易日期，格式必须是 YYYY-MM-DD\n" +
                "- categoryName: 交易分类，例如 '工资', '餐饮', '购物', '转账', '红包'等\n" +
                "- description: 交易说明或备注（可选）\n" +
                "\n重要要求:\n" +
                "1. 如果图像中有多条交易记录，请全部提取并返回\n" +
                "2. 金额必须是正数\n" +
                "3. 日期格式必须严格按照 YYYY-MM-DD\n" +
                "4. 必须返回标准的JSON数组格式，每条记录一个对象\n" +
                "5. 不要返回任何其他文本，只返回JSON数组\n" +
                "\n返回格式示例:\n" +
                "[{\"type\": \"INCOME\", \"amount\": 5000.00, \"transactionDate\": \"2026-01-10\", \"categoryName\": \"工资\", \"description\": \"一月工资\"}]";
    }
    
    /**
     * 等待响应完成
     */
    public boolean waitForComplete(long timeout, TimeUnit unit) {
        try {
            return completeLatch.await(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}

