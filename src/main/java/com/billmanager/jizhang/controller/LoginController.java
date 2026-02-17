package com.billmanager.jizhang.controller;

import com.billmanager.jizhang.dto.ApiResponse;
import com.billmanager.jizhang.dto.LoginRequest;
import com.billmanager.jizhang.entity.User;
import com.billmanager.jizhang.exception.BusinessException;
import com.billmanager.jizhang.mapper.UserMapper;
import com.billmanager.jizhang.service.CaptchaService;
import com.billmanager.jizhang.service.LoginAttemptService;
import com.billmanager.jizhang.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.billmanager.jizhang.service.impl.LoginAttemptServiceImpl;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {
    
    private final UserService userService;
    private final UserMapper userMapper;
    private final LoginAttemptService loginAttemptService;
    private final CaptchaService captchaService;
    
    /**
     * 【重要】登录防护说明
     * 
     * 此控制器使用 LoginAttemptService 和 browserFingerprint（IP + User-Agent）来实现
     * 登录防暴力破解保护。这个机制【仅影响登录相关端点】(/api/login 和 /api/auth/login)。
     * 
     * 注意事项：
     * 1. browserFingerprint 仅在 login() 和 authLogin() 方法中使用
     * 2. 注册、忘记密码等其他流程有独立的验证系统，不受登录防护限制
     * 3. 当用户被要求人机验证（code=428）时，他们应仍可在其他页面进行操作
     * 4. 如果浏览器被完全锁定，用户需要等待或换浏览器/IP
     * 
     * 防护层次：
     * - 第 1 层（1-3 次失败）：无限制
     * - 第 2 层（3-6 次失败）：需要人机验证
     * - 第 3 层（6+ 次失败）：完全锁定（5分钟起步，后续失败将延长锁定时间）
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
    
    private String getBrowserFingerprint(HttpServletRequest request) {
        // 获取客户端 IP
        String clientIp = getClientIP(request);
        
        // 获取 User-Agent
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            userAgent = "";
        }
        
        // 组合成浏览器指纹
        return clientIp + "|" + userAgent;
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For 可能包含多个 IP，取第一个
            return xForwardedFor.split(",")[0].trim();
        }
        
        String remoteAddr = request.getHeader("X-Real-IP");
        if (remoteAddr != null && !remoteAddr.isEmpty()) {
            return remoteAddr;
        }
        
        return request.getRemoteAddr();
    }
    
    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }
    
    @PostMapping("/api/login")
    @ResponseBody
    public ApiResponse<User> login(@Valid @RequestBody LoginRequest request, @RequestParam(required = false) String captchaToken, HttpSession session, HttpServletRequest httpRequest) {
        String username = request.getUsername();
        String browserFingerprint = getBrowserFingerprint(httpRequest);
        
        // 检查浏览器是否被锁定
        if (loginAttemptService.isLocked(browserFingerprint)) {
            long remainingTime = loginAttemptService.getLockTimeRemaining(browserFingerprint);
            long minutesRemaining = (remainingTime / 1000 + 59) / 60;
            String message = String.format("登录尝试过多，账户已被锁定，请在 %d 分钟后再试", minutesRemaining);
            log.warn("【登录防护】浏览器 {} 被锁定（尝试登录用户: {}），剩余时间: {} 秒", 
                    browserFingerprint, username, remainingTime / 1000);
            return ApiResponse.error(429, message);
        }
        
        // 检查是否需要人机验证
        if (loginAttemptService.needsCaptcha(browserFingerprint)) {
            // 如果未提供验证码 token，需要先验证
            if (captchaToken == null || !captchaService.isValidCaptchaToken(captchaToken)) {
                log.warn("【登录防护】浏览器 {} 需要人机验证（尝试登录用户: {}）", browserFingerprint, username);
                return ApiResponse.error(428, "需要人机验证");
            }
            // 标记验证码已使用
            captchaService.useCaptchaToken(captchaToken);
            // 重置失败次数
            loginAttemptService.resetAttempts(browserFingerprint);
        }
        
        try {
            User user = userService.login(request);
            session.setAttribute("user", user);
            
            // 创建自定义的用户Principal，包含userId
            com.billmanager.jizhang.security.CustomUserPrincipal principal = 
                new com.billmanager.jizhang.security.CustomUserPrincipal(user.getId(), user.getUsername());
            
            // 创建Spring Security认证对象，包含权限
            UsernamePasswordAuthenticationToken token = 
                new UsernamePasswordAuthenticationToken(
                    principal, 
                    null, 
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );
            
            // 设置到SecurityContextHolder
            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(token);
            
            // 显式保存到Session
            session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
            );
            
            // 登录成功，清除登录防护记录
            loginAttemptService.clearAttempts(browserFingerprint);
            log.info("【登录防护】用户 {} 登录成功，已清除防护记录", username);
            
            return ApiResponse.success("登录成功", user);
        } catch (BusinessException e) {
            // 登录失败，记录失败尝试
            loginAttemptService.addFailedAttempt(browserFingerprint, username);
            
            // 检查是否需要触发锁定
            int attempts = loginAttemptService.getFailedAttempts(browserFingerprint);
            if (attempts >= 3) {
                ((LoginAttemptServiceImpl) loginAttemptService).handleLoginFailure(browserFingerprint);
                
                if (loginAttemptService.isLocked(browserFingerprint)) {
                    long remainingTime = loginAttemptService.getLockTimeRemaining(browserFingerprint);
                    long minutesRemaining = (remainingTime / 1000 + 59) / 60;
                    log.warn("【登录防护】浏览器 {} 已被锁定 {} 分钟", browserFingerprint, minutesRemaining);
                    return ApiResponse.error(429, String.format("登录尝试过多，账户已被锁定，请在 %d 分钟后再试", minutesRemaining));
                }
            }
            
            log.warn("【登录防护】浏览器 {} 登录失败（用户: {}），失败次数: {}", browserFingerprint, username, attempts);
            return ApiResponse.error(e.getCode(), e.getMessage());
        }
    }
    
    @PostMapping("/api/auth/login")
    @ResponseBody
    public ApiResponse<User> authLogin(@Valid @RequestBody LoginRequest request, @RequestParam(required = false) String captchaToken, HttpSession session, HttpServletRequest httpRequest) {
        String username = request.getUsername();
        String browserFingerprint = getBrowserFingerprint(httpRequest);
        
        // 检查浏览器是否被锁定
        if (loginAttemptService.isLocked(browserFingerprint)) {
            long remainingTime = loginAttemptService.getLockTimeRemaining(browserFingerprint);
            long minutesRemaining = (remainingTime / 1000 + 59) / 60;
            String message = String.format("登录尝试过多，账户已被锁定，请在 %d 分钟后再试", minutesRemaining);
            log.warn("【登录防护】浏览器 {} 被锁定（尝试登录用户: {}），剩余时间: {} 秒", 
                    browserFingerprint, username, remainingTime / 1000);
            return ApiResponse.error(429, message);
        }
        
        // 检查是否需要人机验证
        if (loginAttemptService.needsCaptcha(browserFingerprint)) {
            // 如果未提供验证码 token，需要先验证
            if (captchaToken == null || !captchaService.isValidCaptchaToken(captchaToken)) {
                log.warn("【登录防护】浏览器 {} 需要人机验证（尝试登录用户: {}）", browserFingerprint, username);
                return ApiResponse.error(428, "需要人机验证");
            }
            // 标记验证码已使用
            captchaService.useCaptchaToken(captchaToken);
            // 重置失败次数
            loginAttemptService.resetAttempts(browserFingerprint);
        }
        
        try {
            User user = userService.login(request);
            session.setAttribute("user", user);
            
            // 创建自定义的用户Principal，包含userId
            com.billmanager.jizhang.security.CustomUserPrincipal principal = 
                new com.billmanager.jizhang.security.CustomUserPrincipal(user.getId(), user.getUsername());
            
            // 创建Spring Security认证对象，包含权限
            UsernamePasswordAuthenticationToken token = 
                new UsernamePasswordAuthenticationToken(
                    principal, 
                    null, 
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );
            
            // 设置到SecurityContextHolder
            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(token);
            
            // 显式保存到Session
            session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
            );
            
            // 登录成功，清除登录防护记录
            loginAttemptService.clearAttempts(browserFingerprint);
            log.info("【登录防护】用户 {} 登录成功，已清除防护记录", username);
            
            return ApiResponse.success("登录成功", user);
        } catch (BusinessException e) {
            // 登录失败，记录失败尝试
            loginAttemptService.addFailedAttempt(browserFingerprint, username);
            
            // 检查是否需要触发锁定
            int attempts = loginAttemptService.getFailedAttempts(browserFingerprint);
            if (attempts >= 3) {
                ((LoginAttemptServiceImpl) loginAttemptService).handleLoginFailure(browserFingerprint);
                
                if (loginAttemptService.isLocked(browserFingerprint)) {
                    long remainingTime = loginAttemptService.getLockTimeRemaining(browserFingerprint);
                    long minutesRemaining = (remainingTime / 1000 + 59) / 60;
                    log.warn("【登录防护】浏览器 {} 已被锁定 {} 分钟", browserFingerprint, minutesRemaining);
                    return ApiResponse.error(429, String.format("登录尝试过多，账户已被锁定，请在 %d 分钟后再试", minutesRemaining));
                }
            }
            
            log.warn("【登录防护】浏览器 {} 登录失败（用户: {}），失败次数: {}", browserFingerprint, username, attempts);
            return ApiResponse.error(e.getCode(), e.getMessage());
        }
    }
    
    /**
     * 获取当前用户信息，用于验证登录状态
     */
    @GetMapping("/api/user/profile")
    @ResponseBody
    public ApiResponse<User> getProfile(HttpSession session, HttpServletResponse response) {
        User user = getCurrentUser(session);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            System.out.println("【LoginController】验证失败：未登录或会话已过期");
            return ApiResponse.error("未登录或会话已过期");
        }
        System.out.println("【LoginController】获取用户信息成功: " + user.getUsername());
        return ApiResponse.success("获取成功", user);
    }
    
    /**
     * 获取当前用户信息，用于验证登录状态（auth 路径）
     */
    @GetMapping("/api/auth/profile")
    @ResponseBody
    public ApiResponse<User> getAuthProfile(HttpSession session, HttpServletResponse response) {
        User user = getCurrentUser(session);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            System.out.println("【LoginController】验证失败：未登录或会话已过期");
            return ApiResponse.error("未登录或会话已过期");
        }
        System.out.println("【LoginController】获取用户信息成功: " + user.getUsername());
        return ApiResponse.success("获取成功", user);
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "dashboard";
    }
    
    @GetMapping("/jizhang/dashboard")
    public String dashboardWithPrefix(HttpSession session, Model model) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "dashboard";
    }
    
    @GetMapping("/report")
    public String report(HttpSession session, Model model) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "report";
    }
    
    @GetMapping("/jizhang/report")
    public String reportWithPrefix(HttpSession session, Model model) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "report";
    }
    
    /**
     * 更新用户个人信息 (邮箱、电话等)
     * 
     * 请求体格式:
     * {
     *   "nickname": "新昵称",
     *   "email": "新邮箱",
     *   "phone": "新电话"
     * }
     * 
     * @param request 包含用户信息的请求
     * @param session HTTP会话
     * @return 修改结果
     */
    @PutMapping("/api/user/profile")
    @ResponseBody
    public ApiResponse<User> updateProfile(
            @RequestBody Map<String, String> request, 
            HttpSession session) {
        try {
            User user = getCurrentUser(session);
            if (user == null) {
                return ApiResponse.error("请先登录");
            }
            
            // 更新邮箱
            if (request.containsKey("email") && request.get("email") != null) {
                String email = request.get("email").trim();
                if (!email.isEmpty()) {
                    user.setEmail(email);
                }
            }
            
            // 更新电话
            if (request.containsKey("phone") && request.get("phone") != null) {
                String phone = request.get("phone").trim();
                user.setPhone(phone);
            }
            
            // 更新昵称
            if (request.containsKey("nickname") && request.get("nickname") != null) {
                String nickname = request.get("nickname").trim();
                if (!nickname.isEmpty()) {
                    user.setNickname(nickname);
                }
            }
            
            // 保存更新
            user.setUpdateTime(java.time.LocalDateTime.now());
            userMapper.update(user);
            
            // 更新 session 中的用户信息
            session.setAttribute("user", user);
            
            System.out.println("【LoginController】用户 " + user.getUsername() + " 个人信息已更新");
            return ApiResponse.success("个人信息已更新", user);
        } catch (Exception e) {
            System.err.println("【LoginController】更新个人信息失败: " + e.getMessage());
            return ApiResponse.error("更新个人信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 修改用户昵称
     * 
     * 请求体格式:
     * {
     *   "nickname": "新昵称"
     * }
     * 
     * @param request 包含新昵称的请求
     * @param session HTTP会话
     * @return 修改结果
     */
    @PutMapping("/api/user/nickname")
    @ResponseBody
    public ApiResponse<User> updateNickname(
            @RequestBody Map<String, String> request, 
            HttpSession session) {
        try {
            User user = getCurrentUser(session);
            if (user == null) {
                return ApiResponse.error("请先登录");
            }
            
            String nickname = request.get("nickname");
            if (nickname == null || nickname.trim().isEmpty()) {
                return ApiResponse.error("昵称不能为空");
            }
            
            // 更新昵称
            user.setNickname(nickname.trim());
            user.setUpdateTime(java.time.LocalDateTime.now());
            userMapper.update(user);
            
            // 更新 session 中的用户信息
            session.setAttribute("user", user);
            
            System.out.println("【LoginController】用户 " + user.getUsername() + " 更新昵称为: " + nickname);
            return ApiResponse.success("昵称已更新", user);
        } catch (Exception e) {
            System.err.println("【LoginController】修改昵称失败: " + e.getMessage());
            return ApiResponse.error("修改昵称失败: " + e.getMessage());
        }
    }
}
