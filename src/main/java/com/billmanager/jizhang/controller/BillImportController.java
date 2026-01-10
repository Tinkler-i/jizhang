package com.billmanager.jizhang.controller;

import com.billmanager.jizhang.dto.*;
import com.billmanager.jizhang.entity.User;
import com.billmanager.jizhang.mapper.UserMapper;
import com.billmanager.jizhang.service.BillImportService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 账单导入控制器
 * 处理账单图像上传、识别和确认导入
 */
@Slf4j
@RestController
@RequestMapping("/api/bill-import")
@RequiredArgsConstructor
public class BillImportController {
    
    private final BillImportService billImportService;
    private final UserMapper userMapper;
    
    /**
     * 获取当前登录用户
     */
    private User getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            return user;
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String username = auth.getName();
            user = userMapper.findByUsername(username);
            if (user != null) {
                session.setAttribute("user", user);
                return user;
            }
        }
        
        return null;
    }
    
    /**
     * 识别账单图像
     *
     * @param request 包含Base64编码的图像数据和账单类型提示
     * @param session HTTP会话
     * @return 识别到的账单记录列表
     */
    @PostMapping("/recognize")
    public ApiResponse<BillImportResponse> recognize(
            @Valid @RequestBody BillImportRequest request,
            HttpSession session) {
        
        User user = getCurrentUser(session);
        if (user == null) {
            log.warn("识别请求失败：用户未登录");
            return ApiResponse.error("用户未登录");
        }
        
        log.info("用户 {} 提交账单识别请求，账单类型: {}, 图片大小: {} 字节", 
            user.getId(), 
            request.getAccountType() != null ? request.getAccountType() : "未指定",
            request.getImage() != null ? request.getImage().length() : 0);
        
        try {
            // 验证图片数据
            if (request.getImage() == null || request.getImage().isEmpty()) {
                log.error("识别失败：图片数据为空");
                return ApiResponse.error("图片数据为空");
            }
            
            BillImportResponse response = billImportService.recognize(
                    request.getImage(),
                    request.getAccountType()
            );
            
            log.info("识别成功，识别到 {} 条记录", response.getRecords().size());
            return ApiResponse.success(response);
            
        } catch (Exception e) {
            log.error("账单识别失败 - 错误类型: {}, 错误信息: {}", 
                e.getClass().getSimpleName(), 
                e.getMessage(), 
                e);
            return ApiResponse.error("识别失败: " + e.getMessage());
        }
    }
    
    /**
     * 确认并导入账单
     *
     * @param request 包含确认的账单记录列表
     * @param session HTTP会话
     * @return 导入结果（成功导入的记录ID列表）
     */
    @PostMapping("/confirm")
    public ApiResponse<BillImportConfirmResponse> confirm(
            @Valid @RequestBody BillImportConfirmRequest request,
            HttpSession session) {
        
        User user = getCurrentUser(session);
        if (user == null) {
            return ApiResponse.error("用户未登录");
        }
        
        log.info("用户 {} 提交账单确认导入，共 {} 条记录", user.getId(), request.getRecords().size());
        
        try {
            BillImportConfirmResponse response = billImportService.confirm(user.getId(), request);
            
            log.info("用户 {} 的账单导入成功，导入 {} 条记录", user.getId(), response.getImportedIds().size());
            
            return ApiResponse.success(response);
            
        } catch (RuntimeException e) {
            log.error("账单导入失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }
}
