package com.billmanager.jizhang.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Enumeration;

/**
 * 请求日志拦截器 - 记录所有进入后端的请求信息
 */
@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullUrl = queryString != null ? uri + "?" + queryString : uri;
        
        log.info("【请求开始】{} {}", method, fullUrl);
        
        // 仅在 DEBUG 级别打印详细头信息
        if (log.isDebugEnabled()) {
            log.debug("【请求头信息】");
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                log.debug("  {}: {}", headerName, headerValue);
            }
        }
        
        // 存储请求开始时间
        request.setAttribute("requestStartTime", System.currentTimeMillis());
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();
        
        Long startTime = (Long) request.getAttribute("requestStartTime");
        long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;
        
        log.info("【请求完成】{} {} - Status: {} - 耗时: {}ms", method, uri, status, duration);
        
        if (ex != null) {
            log.error("【请求异常】{} {} - 异常: {}", method, uri, ex.getMessage(), ex);
        }
    }
}
