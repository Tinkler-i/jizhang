<template>
  <div class="register-container">
    <div class="register-card">
      <h1>账户注册</h1>
      
      <!-- 标签页：邮箱/短信注册 -->
      <div class="tabs">
        <button 
          :class="['tab-btn', { active: registerType === 'EMAIL' }]"
          @click="registerType = 'EMAIL'">
          邮箱注册
        </button>
        <button 
          :class="['tab-btn', { active: registerType === 'SMS' }]"
          @click="registerType = 'SMS'">
          短信注册
        </button>
      </div>

      <!-- 邮箱/手机输入 -->
      <div class="form-group">
        <label>{{ registerType === 'EMAIL' ? '邮箱地址' : '手机号码' }}</label>
        <div class="input-wrapper">
          <input 
            v-model="email"
            v-if="registerType === 'EMAIL'"
            type="email" 
            placeholder="请输入邮箱地址"
            @blur="checkEmailRegistered">
          <input 
            v-model="phone"
            v-if="registerType === 'SMS'"
            type="tel" 
            placeholder="请输入手机号码"
            @blur="checkPhoneRegistered">
          <span v-if="registeredMessage" class="error-message">{{ registeredMessage }}</span>
          <span v-if="canRegisterMessage" class="success-message">{{ canRegisterMessage }}</span>
        </div>
      </div>

      <!-- 人机验证 - 简单版本 -->
      <div class="form-group" v-if="!userRegistered">
        <label>人机验证</label>
        <button 
          @click="handleCaptchaSuccess"
          :class="['captcha-btn', { verified: captchaVerified }]">
          {{ captchaVerified ? '✓ 已验证' : '点击验证' }}
        </button>
        <span v-if="captchaMessage" class="error-message">{{ captchaMessage }}</span>
      </div>

      <!-- 验证码输入和发送 -->
      <div class="form-group" v-if="!userRegistered && captchaVerified">
        <label>验证码</label>
        <div class="code-input-wrapper">
          <input 
            v-model="verificationCode"
            type="text" 
            placeholder="请输入验证码"
            maxlength="4">
          <button 
            :disabled="codeCountdown > 0 || !canSendCode || isLoadingSendCode"
            @click="sendVerificationCode"
            class="send-btn">
            <span v-if="isLoadingSendCode">发送中...</span>
            <span v-else-if="codeCountdown > 0">{{ codeCountdown }}秒后重新发送</span>
            <span v-else>发送验证码</span>
          </button>
        </div>
      </div>

      <!-- 注册信息（如果邮箱/手机未注册） -->
      <div v-if="!userRegistered && captchaVerified">
        <div class="form-group">
          <label>用户名</label>
          <input 
            v-model="username"
            type="text" 
            placeholder="请输入用户名">
        </div>

        <div class="form-group">
          <label>密码</label>
          <input 
            v-model="password"
            type="password" 
            placeholder="请输入密码（至少8位）">
        </div>

        <div class="form-group">
          <label>确认密码</label>
          <input 
            v-model="confirmPassword"
            type="password" 
            placeholder="请再次输入密码">
        </div>

        <button
          @click="register"
          :disabled="!canRegister || isLoadingRegister"
          class="register-btn">
          <span v-if="isLoadingRegister">注册中...</span>
          <span v-else>立即注册</span>
        </button>
        
        <!-- 验证失败提示 -->
        <div v-if="!canRegister && registerValidationErrors.length > 0" class="validation-hints">
          <div class="hint-title">⚠️ 无法注册，请满足以下要求：</div>
          <div class="hint-items">
            <div v-for="(error, index) in registerValidationErrors" :key="index" class="hint-item">
              {{ error }}
            </div>
          </div>
        </div>
      </div>

      <!-- 已注册用户提示 -->
      <div v-if="userRegistered" class="registered-prompt">
        <p>{{ registeredMessage }}</p>
        <div class="btn-group">
          <router-link to="/login" class="btn btn-primary">去登录</router-link>
          <button @click="resetForm" class="btn btn-secondary">修改密码</button>
        </div>
      </div>

      <!-- 登录链接 -->
      <p class="login-link" v-if="!userRegistered">
        已有账户？<router-link to="/login">立即登录</router-link>
      </p>

      <!-- 错误信息 -->
      <div v-if="errorMessage" class="error-alert">
        {{ errorMessage }}
      </div>
    </div>

    <!-- Toast 浮窗通知 -->
    <transition name="toast-fade">
      <div v-if="toast.show" :class="['toast', toast.type]">
        <div class="toast-content">
          {{ toast.message }}
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { authAPI } from '../api'

export default {
  name: 'Register',
  components: {},
  setup() {
    const router = useRouter()
    
    // 表单数据
    const registerType = ref('EMAIL')
    const email = ref('')
    const phone = ref('')
    const username = ref('')
    const password = ref('')
    const confirmPassword = ref('')
    const verificationCode = ref('')
    
    // UI 状态
    const errorMessage = ref('')
    const registeredMessage = ref('')
    const canRegisterMessage = ref('')
    const captchaMessage = ref('')
    const codeCountdown = ref(0)
    const userRegistered = ref(false)
    const captchaVerified = ref(false)
    const isLoadingSendCode = ref(false)
    const isLoadingRegister = ref(false)
    
    // Toast 通知
    const toast = ref({
      show: false,
      message: '',
      type: 'success' // 'success' 或 'error'
    })
    let toastTimer = null
    
    // 计算属性
    const canSendCode = computed(() => {
      if (registerType.value === 'EMAIL') {
        return email.value && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)
      } else {
        return phone.value && /^1[3-9]\d{9}$/.test(phone.value)
      }
    })
    
    const canRegister = computed(() => {
      return (
        username.value.trim().length > 0 &&
        password.value.length >= 8 &&
        password.value === confirmPassword.value &&
        verificationCode.value.length === 4
      )
    })
    
    const registerValidationErrors = computed(() => {
      const errors = []
      if (username.value.trim().length === 0) {
        errors.push('• 用户名不能为空')
      }
      if (password.value.length === 0) {
        errors.push('• 密码不能为空')
      } else if (password.value.length < 8) {
        errors.push('• 密码至少需要8个字符（当前' + password.value.length + '个）')
      }
      if (password.value.length > 0 && confirmPassword.value.length === 0) {
        errors.push('• 请再次输入密码进行确认')
      } else if (password.value !== confirmPassword.value && confirmPassword.value.length > 0) {
        errors.push('• 两次输入的密码不一致')
      }
      if (verificationCode.value.length === 0) {
        errors.push('• 请输入验证码')
      } else if (verificationCode.value.length < 4) {
        errors.push('• 验证码必须是4位数字（当前' + verificationCode.value.length + '位）')
      }
      return errors
    })
    
    // Toast 通知函数
    const showToast = (message, type = 'success', duration = 3000) => {
      if (toastTimer) {
        clearTimeout(toastTimer)
      }
      toast.value.message = message
      toast.value.type = type
      toast.value.show = true
      
      toastTimer = setTimeout(() => {
        toast.value.show = false
      }, duration)
    }
    
    // 清除消息
    const clearMessages = () => {
      errorMessage.value = ''
      registeredMessage.value = ''
      canRegisterMessage.value = ''
      captchaMessage.value = ''
    }
    
    // 检查邮箱是否已注册
    const checkEmailRegistered = async () => {
      if (!email.value || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)) {
        return
      }
      
      try {
        const response = await authAPI.verifyCode({
          email: email.value,
          type: 'EMAIL'
        })
        
        if (response.code === 200) {
          const result = response.data
          if (result.status === 'REGISTERED') {
            registeredMessage.value = result.message
            canRegisterMessage.value = ''
            userRegistered.value = true
          } else {
            userRegistered.value = false
            registeredMessage.value = ''
            canRegisterMessage.value = '可以注册'
          }
        }
      } catch (error) {
        console.error('检查邮箱失败:', error)
      }
    }
    
    // 检查手机号是否已注册
    const checkPhoneRegistered = async () => {
      if (!phone.value || !/^1[3-9]\d{9}$/.test(phone.value)) {
        return
      }
      
      try {
        const response = await authAPI.verifyCode({
          phone: phone.value,
          type: 'SMS'
        })
        
        if (response.code === 200) {
          const result = response.data
          if (result.status === 'REGISTERED') {
            registeredMessage.value = result.message
            canRegisterMessage.value = ''
            userRegistered.value = true
          } else {
            userRegistered.value = false
            registeredMessage.value = ''
            canRegisterMessage.value = '可以注册'
          }
        }
      } catch (error) {
        console.error('检查手机号失败:', error)
      }
    }
    
    // 处理人机验证
    const handleCaptchaSuccess = () => {
      captchaVerified.value = true
      captchaMessage.value = ''
      console.log('人机验证成功')
    }
    
    // 发送验证码
    const sendVerificationCode = async () => {
      errorMessage.value = ''
      isLoadingSendCode.value = true
      
      try {
        console.log('[注册] 📬 开始发送验证码...')
        const payload = {
          type: registerType.value,
          captchaToken: 'temp-token'
        }
        
        if (registerType.value === 'EMAIL') {
          payload.email = email.value
          console.log('[注册] 📧 邮箱：' + email.value)
        } else {
          payload.phone = phone.value
          console.log('[注册] 📱 手机号：' + phone.value)
        }
        
        console.log('[注册] 📨 请求数据：', JSON.stringify(payload))
        const response = await authAPI.sendVerificationCode(payload)
        console.log('[注册] 📨 响应状态：code=' + response.code + ', message=' + response.message)
        console.log('[注册] 📨 完整响应：', response)
        
        if (response && response.code === 200) {
          showToast('✅ 验证码已发送，请查收邮件', 'success', 4000)
          startCodeCountdown()
          console.log('[注册] ✅ 验证码发送成功')
        } else {
          const msg = response?.message || '未知错误'
          showToast('❌ 发送失败：' + msg, 'error', 4000)
          console.error('[注册] ❌ 发送失败，状态码：' + response?.code + '，消息：' + msg)
        }
      } catch (error) {
        console.error('[注册] ❌ 请求异常捕获')
        console.error('[注册] 错误对象：', error)
        console.error('[注册] 响应数据：', error.response?.data)
        console.error('[注册] HTTP状态码：', error.response?.status)
        
        let errorMsg = '网络错误'
        if (error.response?.data?.message) {
          errorMsg = error.response.data.message
        } else if (error.message) {
          errorMsg = error.message
        }
        
        showToast('❌ 发送失败：' + errorMsg, 'error', 4000)
        console.error('[注册] ❌ 最终错误消息：' + errorMsg)
      } finally {
        isLoadingSendCode.value = false
      }
    }
    
    // 启动验证码倒计时
    const startCodeCountdown = () => {
      codeCountdown.value = 60
      const interval = setInterval(() => {
        codeCountdown.value--
        if (codeCountdown.value <= 0) {
          clearInterval(interval)
        }
      }, 1000)
    }
    
    // 用户注册
    const register = async () => {
      console.log('[注册] 检查注册条件')
      console.log('[注册] username.length=' + username.value.trim().length + ', 需要>0')
      console.log('[注册] password.length=' + password.value.length + ', 需要>=8')
      console.log('[注册] confirmPassword相等=' + (password.value === confirmPassword.value))
      console.log('[注册] verificationCode.length=' + verificationCode.value.length + ', 需要=4')
      console.log('[注册] canRegister=' + canRegister.value)
      
      if (!canRegister.value) {
        const reasons = []
        if (username.value.trim().length === 0) reasons.push('用户名为空')
        if (password.value.length < 8) reasons.push('密码少于8个字符')
        if (password.value !== confirmPassword.value) reasons.push('两次密码不匹配')
        if (verificationCode.value.length !== 4) reasons.push('验证码必须是4位数字')
        
        const msg = '❌ 注册信息不完整：' + reasons.join('、')
        errorMessage.value = msg
        console.warn('[注册] ' + msg)
        return
      }
      
      errorMessage.value = ''
      isLoadingRegister.value = true
      
      try {
        console.log('[注册] 👤 开始注册用户...')
        const payload = {
          username: username.value,
          password: password.value,
          confirmPassword: confirmPassword.value,
          type: registerType.value,
          code: verificationCode.value,
          captchaToken: 'temp-token'
        }
        
        if (registerType.value === 'EMAIL') {
          payload.email = email.value
          console.log('[注册] 📧 邮箱：' + email.value)
        } else {
          payload.phone = phone.value
          console.log('[注册] 📱 手机号：' + phone.value)
        }
        
        console.log('[注册] 👤 注册数据：', { ...payload, password: '***' })
        const response = await authAPI.register(payload)
        console.log('[注册] 👤 注册响应状态：code=' + response.code + ', message=' + response.message)
        console.log('[注册] 👤 完整响应：', response)
        
        if (response && response.code === 200) {
          showToast('✅ 注册成功！即将跳转到登录页...', 'success', 2000)
          console.log('[注册] ✅ 注册成功')
          // 2秒后跳转到登录页
          setTimeout(() => {
            router.push('/login')
          }, 2000)
        } else {
          const msg = response?.message || '未知错误'
          showToast('❌ 注册失败：' + msg, 'error', 4000)
          console.error('[注册] ❌ 注册失败，状态码：' + response?.code + '，消息：' + msg)
        }
      } catch (error) {
        console.error('[注册] ❌ 注册异常捕获')
        console.error('[注册] 错误对象：', error)
        console.error('[注册] 响应数据：', error.response?.data)
        console.error('[注册] HTTP状态码：', error.response?.status)
        
        let errorMsg = '网络错误'
        if (error.response?.data?.message) {
          errorMsg = error.response.data.message
        } else if (error.message) {
          errorMsg = error.message
        }
        
        showToast('❌ 注册失败：' + errorMsg, 'error', 4000)
        console.error('[注册] ❌ 最终错误消息：' + errorMsg)
      } finally {
        isLoadingRegister.value = false
      }
    }
    
    // 重置表单
    const resetForm = () => {
      email.value = ''
      phone.value = ''
      username.value = ''
      password.value = ''
      confirmPassword.value = ''
      verificationCode.value = ''
      registeredMessage.value = ''
      canRegisterMessage.value = ''
      userRegistered.value = false
      captchaVerified.value = false
      clearMessages()
    }
    
    return {
      registerType,
      email,
      phone,
      username,
      password,
      confirmPassword,
      verificationCode,
      errorMessage,
      registeredMessage,
      canRegisterMessage,
      captchaMessage,
      codeCountdown,
      userRegistered,
      captchaVerified,
      isLoadingSendCode,
      isLoadingRegister,
      canSendCode,
      canRegister,
      registerValidationErrors,
      toast,
      showToast,
      checkEmailRegistered,
      checkPhoneRegistered,
      handleCaptchaSuccess,
      sendVerificationCode,
      register,
      resetForm
    }
  }
}
</script>

<style scoped>
.register-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  padding-top: 40px;
  overflow-y: auto;
}

.register-card {
  background: white;
  border-radius: 10px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  padding: 40px;
  max-width: 450px;
  width: 100%;
  margin-bottom: 40px;
  flex-shrink: 0;
}

h1 {
  text-align: center;
  color: #333;
  margin-bottom: 30px;
  font-size: 28px;
}

.tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 30px;
  border-bottom: 2px solid #eee;
}

.tab-btn {
  flex: 1;
  padding: 12px 0;
  border: none;
  background: none;
  color: #999;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  border-bottom: 3px solid transparent;
  margin-bottom: -2px;
}

.tab-btn.active {
  color: #667eea;
  border-bottom-color: #667eea;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 14px;
  transition: border-color 0.3s;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.input-wrapper {
  position: relative;
}

.error-message {
  display: block;
  color: #ff4757;
  font-size: 12px;
  margin-top: 5px;
}

.success-message {
  display: block;
  color: #2ed573;
  font-size: 12px;
  margin-top: 5px;
}

.code-input-wrapper {
  display: flex;
  gap: 10px;
}

.code-input-wrapper input {
  flex: 1;
  font-size: 18px;
  letter-spacing: 5px;
  text-align: center;
}

.send-btn {
  padding: 12px 15px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 12px;
  white-space: nowrap;
  cursor: pointer;
  transition: background 0.3s;
}

.send-btn:hover:not(:disabled) {
  background: #5568d3;
}

.send-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.captcha-btn {
  width: 100%;
  padding: 12px;
  background: #f0f0f0;
  color: #333;
  border: 2px solid #ddd;
  border-radius: 5px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.captcha-btn:hover {
  border-color: #667eea;
  background: #f5f5ff;
}

.captcha-btn.verified {
  background: #d4edda;
  border-color: #28a745;
  color: #155724;
}

.register-btn {
  width: 100%;
  padding: 12px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.3s;
  margin-top: 10px;
}

.register-btn:hover:not(:disabled) {
  background: #5568d3;
}

.register-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.registered-prompt {
  background: #fff3cd;
  border: 1px solid #ffc107;
  border-radius: 5px;
  padding: 20px;
  text-align: center;
}

.registered-prompt p {
  color: #856404;
  margin-bottom: 15px;
}

.btn-group {
  display: flex;
  gap: 10px;
}

.btn {
  flex: 1;
  padding: 10px;
  border: none;
  border-radius: 5px;
  font-size: 14px;
  cursor: pointer;
  text-decoration: none;
  display: inline-block;
  text-align: center;
  transition: all 0.3s;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover {
  background: #5568d3;
}

.btn-secondary {
  background: #f1f3f4;
  color: #333;
  border: 1px solid #ddd;
}

.btn-secondary:hover {
  background: #e8eaed;
}

.login-link {
  text-align: center;
  margin-top: 20px;
  color: #999;
  font-size: 14px;
}

.login-link a {
  color: #667eea;
  text-decoration: none;
}

.login-link a:hover {
  text-decoration: underline;
}

.error-alert {
  background: #ffe4e6;
  color: #ff4757;
  padding: 12px;
  border-radius: 5px;
  margin-top: 20px;
  font-size: 14px;
  text-align: center;
}

.success-alert {
  background: #d4edda;
  color: #2ed573;
  padding: 12px;
  border-radius: 5px;
  margin-top: 20px;
  font-size: 14px;
  text-align: center;
}

.validation-hints {
  background: #fff3cd;
  border: 1px solid #ffc107;
  border-radius: 5px;
  padding: 15px;
  margin-top: 15px;
  font-size: 13px;
  color: #856404;
}

.hint-title {
  font-weight: 600;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.hint-items {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.hint-item {
  padding-left: 0;
  line-height: 1.5;
}

/* Toast 通知样式 */
.toast {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 16px 24px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 9999;
  max-width: 400px;
  word-wrap: break-word;
  animation: slideInRight 0.3s ease-in-out;
}

.toast.success {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.toast.error {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.toast-content {
  font-size: 14px;
  line-height: 1.5;
}

@keyframes slideInRight {
  from {
    transform: translateX(400px);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: all 0.3s ease-in-out;
}

.toast-fade-enter-from {
  transform: translateX(400px);
  opacity: 0;
}

.toast-fade-leave-to {
  transform: translateX(400px);
  opacity: 0;
}

@media (max-width: 600px) {
  .toast {
    top: 10px;
    right: 10px;
    left: 10px;
    max-width: none;
  }
}
</style>
