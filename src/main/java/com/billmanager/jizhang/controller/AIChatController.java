package com.billmanager.jizhang.controller;

import com.billmanager.jizhang.dto.ApiResponse;
import com.billmanager.jizhang.service.AIChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AI聊天Controller - 处理AI对话请求
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
public class AIChatController {

    @Autowired
    private AIChatService aiChatService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * 普通对话API - 返回完整响应
     * POST /api/ai/chat
     */
    @PostMapping("/chat")
    public ApiResponse<String> chat(@RequestBody ChatRequest request, HttpSession session) {
        Long userId = getUserId(session);
        
        if (userId == null) {
            return ApiResponse.error(401, "请先登录");
        }
        
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return ApiResponse.error(400, "问题不能为空");
        }
        
        try {
            String response = aiChatService.chat(userId, request.getMessage());
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("聊天出错", e);
            return ApiResponse.error("AI处理出错: " + e.getMessage());
        }
    }

    /**
     * 流式对话API - 实时返回响应 (Server-Sent Events)
     * POST /api/ai/chat-stream
     */
    @PostMapping("/chat-stream")
    public SseEmitter chatStream(@RequestBody ChatRequest request, HttpSession session) {
        Long userId = getUserId(session);
        
        SseEmitter emitter = new SseEmitter(30000L);
        
        if (userId == null) {
            try {
                emitter.send(SseEmitter.event().data("error:请先登录").build());
            } catch (IOException e) {
                log.error("SSE发送错误", e);
            }
            emitter.complete();
            return emitter;
        }
        
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            try {
                emitter.send(SseEmitter.event().data("error:问题不能为空").build());
            } catch (IOException e) {
                log.error("SSE发送错误", e);
            }
            emitter.complete();
            return emitter;
        }
        
        // 异步处理流式响应
        executorService.execute(() -> {
            try {
                log.info("用户ID: {} 开始流式对话", userId);
                String response = aiChatService.chatStream(userId, request.getMessage());
                
                // 将响应按字符逐个发送
                for (char c : response.toCharArray()) {
                    emitter.send(SseEmitter.event()
                            .id(String.valueOf(System.nanoTime()))
                            .data(String.valueOf(c))
                            .build());
                    Thread.sleep(10); // 模拟流式效果
                }
                
                emitter.send(SseEmitter.event().data("[DONE]").build());
                emitter.complete();
                
            } catch (Exception e) {
                log.error("SSE处理出错", e);
                try {
                    emitter.send(SseEmitter.event().data("error:" + e.getMessage()).build());
                } catch (IOException ioException) {
                    log.error("SSE发送错误", ioException);
                }
                emitter.completeWithError(e);
            }
        });
        
        return emitter;
    }

    /**
     * 从Session中获取用户ID
     */
    private Long getUserId(HttpSession session) {
        if (session == null) {
            return null;
        }
        
        // 先尝试从session中获取user对象
        Object userObj = session.getAttribute("user");
        if (userObj != null) {
            try {
                // 使用反射获取ID，避免依赖User类
                Object id = userObj.getClass().getMethod("getId").invoke(userObj);
                if (id instanceof Long) {
                    return (Long) id;
                }
                if (id instanceof Integer) {
                    return ((Integer) id).longValue();
                }
            } catch (Exception e) {
                log.error("从User对象获取ID失败", e);
            }
        }
        
        // 备选方案：直接查找userId
        Object userId = session.getAttribute("userId");
        if (userId instanceof Long) {
            return (Long) userId;
        }
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        
        return null;
    }

    /**
     * 聊天请求DTO
     */
    public static class ChatRequest {
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return "ChatRequest{" +
                    "message='" + message + '\'' +
                    '}';
        }
    }
}
