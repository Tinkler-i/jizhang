<template>
  <div class="forgot-password-container">
    <div class="forgot-password-box">
      <div class="forgot-password-header">
        <div class="back-button" @click="goBack">
          <span>← 返回</span>
        </div>
        <h1>忘记密码</h1>
        <p>输入您的邮箱或手机号，我们将为您发送重置密码的验证码</p>
      </div>

      <!-- 步骤 1: 选择验证方式和输入邮箱/手机号 -->
      <div v-if="currentStep === 1" class="step-content">
        <div class="verification-type-selector">
          <button
            :class="['type-btn', { active: verifyType === 'EMAIL' }]"
            @click="verifyType = 'EMAIL'"
          >
            📧 邮箱验证
          </button>
          <button
            :class="['type-btn', { active: verifyType === 'SMS' }]"
            @click="verifyType = 'SMS'"
          >
            📱 短信验证
          </button>
        </div>

        <!-- 邮箱输入 -->
        <div v-if="verifyType === 'EMAIL'" class="input-group">
          <Input
            v-model="form.email"
            type="email"
            label="邮箱地址"
            placeholder="请输入注册时使用的邮箱"
            :error="errors.email"
            required
          />
        </div>

        <!-- 手机号输入 -->
        <div v-if="verifyType === 'SMS'" class="input-group">
          <Input
            v-model="form.phone"
            type="tel"
            label="手机号"
            placeholder="请输入注册时使用的手机号"
            :error="errors.phone"
            required
          />
        </div>

        <!-- 人机验证组件 -->
        <div class="input-group">
          <label>人机验证</label>
          <div class="captcha-container">
            <Captcha
              ref="captchaRef"
              :before-open="handleValidateBeforeCaptcha"
              @success="handleCaptchaSuccess"
              @close="handleCaptchaClose"
            />
          </div>
          <div v-if="errors.captcha" class="error-message">{{ errors.captcha }}</div>
        </div>

        <div class="step-indicator">
          <span class="step active">1</span>
          <span class="separator"></span>
          <span class="step">2</span>
          <span class="separator"></span>
          <span class="step">3</span>
        </div>
      </div>

      <!-- 步骤 2: 输入验证码 -->
      <div v-if="currentStep === 2" class="step-content">
        <div class="step-title">
          <h2>验证您的{{ verifyType === 'EMAIL' ? '邮箱' : '手机号' }}</h2>
          <p>我们已将验证码发送至 {{ verifyType === 'EMAIL' ? form.email : form.phone }}</p>
        </div>

        <div class="input-group">
          <label>验证码</label>
          <div class="code-input-group">
            <Input
              v-model="form.code"
              type="text"
              placeholder="请输入 4 位验证码"
              maxlength="4"
              :error="errors.code"
              required
            />
            <Button
              type="secondary"
              :disabled="countdown > 0 || resendLoading"
              :loading="resendLoading"
              @click="handleResendCode"
            >
              {{ countdown > 0 ? `${countdown}s` : '重新发送' }}
            </Button>
          </div>
        </div>

        <Button
          type="primary"
          block
          :loading="verifyCodeLoading"
          @click="handleVerifyCode"
        >
          验证
        </Button>

        <div class="step-indicator">
          <span class="step completed">✓</span>
          <span class="separator"></span>
          <span class="step active">2</span>
          <span class="separator"></span>
          <span class="step">3</span>
        </div>

        <button class="back-link" @click="currentStep = 1">
          ← 返回上一步
        </button>
      </div>

      <!-- 步骤 3: 重置密码 -->
      <div v-if="currentStep === 3" class="step-content">
        <div class="step-title">
          <h2>设置新密码</h2>
          <p>请输入您的新密码</p>
        </div>

        <div class="input-group">
          <Input
            v-model="form.newPassword"
            type="password"
            label="新密码"
            placeholder="至少 6 位密码"
            :error="errors.newPassword"
            required
          />
          <div class="password-strength">
            <div
              :class="['strength-bar', getPasswordStrength(form.newPassword).class]"
            ></div>
            <span class="strength-text">{{ getPasswordStrength(form.newPassword).text }}</span>
          </div>
        </div>

        <div class="input-group">
          <Input
            v-model="form.confirmPassword"
            type="password"
            label="确认密码"
            placeholder="请再次输入新密码"
            :error="errors.confirmPassword"
            required
          />
        </div>

        <Button
          type="primary"
          block
          :loading="resetPasswordLoading"
          @click="handleResetPassword"
        >
          重置密码
        </Button>

        <div class="step-indicator">
          <span class="step completed">✓</span>
          <span class="separator"></span>
          <span class="step completed">✓</span>
          <span class="separator"></span>
          <span class="step active">3</span>
        </div>

        <button class="back-link" @click="currentStep = 2">
          ← 返回上一步
        </button>
      </div>

      <!-- 成功提示 -->
      <div v-if="currentStep === 4" class="step-content success-content">
        <div class="success-icon">✓</div>
        <h2>密码重置成功</h2>
        <p>您的密码已成功重置，请使用新密码登录</p>
        <Button type="primary" block @click="goToLogin">
          返回登录
        </Button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { authAPI } from '../api'
import Button from '../components/Button.vue'
import Input from '../components/Input.vue'
import Captcha from '../components/Captcha.vue'
import { useUIStore } from '../stores/ui'

const router = useRouter()
const uiStore = useUIStore()
const captchaRef = ref(null)

const currentStep = ref(1)
const verifyType = ref('EMAIL')
const sendCodeLoading = ref(false)
const verifyCodeLoading = ref(false)
const resendLoading = ref(false)
const resetPasswordLoading = ref(false)
const countdown = ref(0)
const captchaToken = ref('')

const form = reactive({
  email: '',
  phone: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const errors = reactive({
  email: '',
  phone: '',
  code: '',
  newPassword: '',
  confirmPassword: '',
  captcha: ''
})

// 获取密码强度
const getPasswordStrength = (password) => {
  if (!password) return { class: '', text: '' }
  if (password.length < 6) {
    return { class: 'weak', text: '密码过弱' }
  }
  if (password.length < 8) {
    return { class: 'medium', text: '密码强度中等' }
  }
  if (/[a-z]/.test(password) && /[A-Z]/.test(password) && /[0-9]/.test(password)) {
    return { class: 'strong', text: '密码强度强' }
  }
  return { class: 'medium', text: '密码强度中等' }
}

// 验证邮箱格式
const validateEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

// 验证手机号格式
const validatePhone = (phone) => {
  const phoneRegex = /^1[3-9]\d{9}$/
  return phoneRegex.test(phone)
}

// 验证是否可以开启人机验证（返回 true 表示可以，返回错误信息字符串表示验证失败）
const handleValidateBeforeCaptcha = () => {
  console.log('【ForgotPassword】验证是否可以开启人机验证')
  errors.email = ''
  errors.phone = ''
  
  // 根据选择的验证类型进行验证
  if (verifyType.value === 'EMAIL') {
    if (!form.email) {
      errors.email = '请输入邮箱地址'
      console.warn('【ForgotPassword】邮箱为空')
      return '请先输入邮箱地址'
    }
    if (!validateEmail(form.email)) {
      errors.email = '邮箱格式不正确'
      console.warn('【ForgotPassword】邮箱格式不正确:', form.email)
      return '邮箱格式不正确，请重新输入'
    }
  } else {
    if (!form.phone) {
      errors.phone = '请输入手机号'
      console.warn('【ForgotPassword】手机号为空')
      return '请先输入手机号'
    }
    if (!validatePhone(form.phone)) {
      errors.phone = '手机号格式不正确'
      console.warn('【ForgotPassword】手机号格式不正确:', form.phone)
      return '手机号格式不正确，请输入11位手机号'
    }
  }
  
  console.log('【ForgotPassword】验证通过，可以开启人机验证')
  return true
}

// 处理人机验证成功
const handleCaptchaSuccess = (data) => {
  console.log('【ForgotPassword】人机验证成功', data)
  captchaToken.value = data.token
  errors.captcha = ''
  uiStore.showNotification('✓ 人机验证已通过，正在为您发送验证码...', 'success')
  setTimeout(() => {
    handleSendCode()
  }, 800)
}

// 处理人机验证关闭
const handleCaptchaClose = () => {
  console.log('【ForgotPassword】人机验证模态框已关闭')
}

// 第一步：发送验证码
const handleSendCode = async () => {
  errors.email = ''
  errors.phone = ''
  errors.captcha = ''

  if (verifyType.value === 'EMAIL') {
    if (!form.email.trim()) {
      errors.email = '请输入邮箱地址'
      return
    }
    if (!validateEmail(form.email)) {
      errors.email = '邮箱格式不正确'
      return
    }
  } else {
    if (!form.phone.trim()) {
      errors.phone = '请输入手机号'
      return
    }
    if (!validatePhone(form.phone)) {
      errors.phone = '手机号格式不正确'
      return
    }
  }

  // 检查人机验证
  if (!captchaToken.value) {
    errors.captcha = '请先完成人机验证'
    return
  }

  sendCodeLoading.value = true
  try {
    const response = await authAPI.sendResetPasswordCode({
      type: verifyType.value,
      email: form.email,
      phone: form.phone,
      captchaToken: captchaToken.value
    })

    if (response.code === 200) {
      uiStore.showNotification('验证码已发送，请检查您的' + (verifyType.value === 'EMAIL' ? '邮箱' : '短信'), 'success')
      currentStep.value = 2
      startCountdown()
      // 重置 captcha token，下次需要重新验证
      captchaToken.value = ''
    } else {
      uiStore.showNotification(response.message, 'error')
    }
  } catch (error) {
    uiStore.showNotification('发送验证码失败: ' + error.message, 'error')
  } finally {
    sendCodeLoading.value = false
  }
}

// 重新发送验证码
const handleResendCode = async () => {
  // 需要重新进行人机验证
  if (!captchaToken.value) {
    errors.captcha = '请先完成人机验证'
    captchaRef.value?.openCaptcha()
    return
  }

  resendLoading.value = true
  try {
    const response = await authAPI.sendResetPasswordCode({
      type: verifyType.value,
      email: form.email,
      phone: form.phone,
      captchaToken: captchaToken.value
    })

    if (response.code === 200) {
      uiStore.showNotification('验证码已重新发送', 'success')
      startCountdown()
      captchaToken.value = ''
    } else {
      uiStore.showNotification(response.message, 'error')
    }
  } catch (error) {
    uiStore.showNotification('重新发送失败: ' + error.message, 'error')
  } finally {
    resendLoading.value = false
  }
}

// 开始倒计时
const startCountdown = () => {
  countdown.value = 60
  const interval = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(interval)
    }
  }, 1000)
}

// 第二步：验证验证码
const handleVerifyCode = async () => {
  errors.code = ''

  if (!form.code.trim()) {
    errors.code = '请输入验证码'
    return
  }

  if (form.code.length !== 4) {
    errors.code = '验证码必须是 4 位数字'
    return
  }

  verifyCodeLoading.value = true
  try {
    const response = await authAPI.verifyResetPasswordCode({
      type: verifyType.value,
      email: form.email,
      phone: form.phone,
      code: form.code
    })

    if (response.code === 200 && response.data.success) {
      uiStore.showNotification('验证成功', 'success')
      currentStep.value = 3
    } else {
      errors.code = response.message || '验证失败'
      uiStore.showNotification(errors.code, 'error')
    }
  } catch (error) {
    errors.code = '验证失败: ' + error.message
    uiStore.showNotification(errors.code, 'error')
  } finally {
    verifyCodeLoading.value = false
  }
}

// 第三步：重置密码
const handleResetPassword = async () => {
  errors.newPassword = ''
  errors.confirmPassword = ''

  if (!form.newPassword) {
    errors.newPassword = '请输入新密码'
    return
  }

  if (form.newPassword.length < 6) {
    errors.newPassword = '密码长度至少为 6 位'
    return
  }

  if (!form.confirmPassword) {
    errors.confirmPassword = '请确认密码'
    return
  }

  if (form.newPassword !== form.confirmPassword) {
    errors.confirmPassword = '两次输入的密码不一致'
    return
  }

  resetPasswordLoading.value = true
  try {
    const response = await authAPI.resetPassword({
      type: verifyType.value,
      email: form.email,
      phone: form.phone,
      code: form.code,
      newPassword: form.newPassword,
      confirmPassword: form.confirmPassword
    })

    if (response.code === 200) {
      uiStore.showNotification('密码重置成功', 'success')
      currentStep.value = 4
      setTimeout(() => {
        goToLogin()
      }, 2000)
    } else {
      uiStore.showNotification(response.message, 'error')
    }
  } catch (error) {
    uiStore.showNotification('密码重置失败: ' + error.message, 'error')
  } finally {
    resetPasswordLoading.value = false
  }
}

// 返回上一步
const goBack = () => {
  if (currentStep.value === 1) {
    router.back()
  } else {
    currentStep.value = 1
  }
}

// 返回登录
const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.forgot-password-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.forgot-password-box {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 450px;
  padding: 40px;
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.forgot-password-header {
  margin-bottom: 30px;
  text-align: center;
}

.back-button {
  display: inline-block;
  margin-bottom: 15px;
  cursor: pointer;
  color: #667eea;
  font-weight: 500;
  font-size: 14px;
  transition: color 0.3s;
}

.back-button:hover {
  color: #764ba2;
}

.forgot-password-header h1 {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin: 0 0 10px 0;
}

.forgot-password-header p {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.step-content {
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.verification-type-selector {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
  margin-bottom: 25px;
}

.type-btn {
  padding: 12px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
  color: #666;
}

.type-btn:hover {
  border-color: #667eea;
  color: #667eea;
}

.type-btn.active {
  border-color: #667eea;
  background: #f0f4ff;
  color: #667eea;
}

.input-group {
  margin-bottom: 20px;
}

.input-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.code-input-group {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  align-items: stretch;
}

.code-input-group :deep(.input-wrapper) {
  display: flex;
  align-items: center;
}

.captcha-success-tip {
  color: #51cf66;
  font-size: 14px;
  margin-top: 8px;
  font-weight: 500;
}

.error-message {
  color: #ff4757;
  font-size: 14px;
  margin-top: 8px;
}

.captcha-container {
  margin-top: 8px;
}

.code-input-group :deep(input) {
  text-align: center;
  letter-spacing: 2px;
  font-size: 18px;
  font-weight: bold;
  height: 44px;
}

.code-input-group :deep(button) {
  height: 44px;
  min-width: 100px;
  white-space: nowrap;
  display: flex;
  align-items: center;
  justify-content: center;
}

.password-strength {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.strength-bar {
  height: 4px;
  width: 100px;
  border-radius: 2px;
  background: #f0f0f0;
}

.strength-bar.weak {
  background: #ff6b6b;
}

.strength-bar.medium {
  background: #ffa94d;
}

.strength-bar.strong {
  background: #51cf66;
}

.strength-text {
  font-size: 12px;
  color: #666;
}

.step-title {
  text-align: center;
  margin-bottom: 25px;
}

.step-title h2 {
  font-size: 20px;
  color: #333;
  margin: 0 0 10px 0;
}

.step-title p {
  font-size: 14px;
  color: #999;
  margin: 0;
  word-break: break-all;
}

.success-content {
  text-align: center;
}

.success-icon {
  font-size: 60px;
  margin-bottom: 20px;
  animation: scaleIn 0.3s ease-out;
}

@keyframes scaleIn {
  from {
    transform: scale(0);
  }
  to {
    transform: scale(1);
  }
}

.success-content h2 {
  font-size: 24px;
  color: #333;
  margin: 0 0 10px 0;
}

.success-content p {
  font-size: 14px;
  color: #666;
  margin: 0 0 30px 0;
}

.step-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 30px;
  gap: 10px;
}

.step {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
  font-size: 12px;
  background: #f0f0f0;
  color: #999;
  transition: all 0.3s;
}

.step.active {
  background: #667eea;
  color: white;
}

.step.completed {
  background: #51cf66;
  color: white;
}

.separator {
  width: 20px;
  height: 2px;
  background: #e0e0e0;
}

.back-link {
  display: block;
  margin-top: 20px;
  width: 100%;
  padding: 12px;
  border: none;
  background: none;
  color: #667eea;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: color 0.3s;
}

.back-link:hover {
  color: #764ba2;
}

@media (max-width: 480px) {
  .forgot-password-box {
    padding: 30px 20px;
  }

  .forgot-password-header h1 {
    font-size: 24px;
  }

  .type-btn {
    font-size: 12px;
    padding: 10px 12px;
  }
}
</style>
