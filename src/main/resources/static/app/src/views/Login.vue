<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="login-icon">💰</div>
        <h1>AI记账管家</h1>
        <p>欢迎回来，请登录您的账户</p>
      </div>

      <form class="login-form" @submit.prevent="handleLogin">
        <Input
          v-model="form.username"
          type="text"
          label="用户名"
          placeholder="请输入用户名"
          :error="errors.username"
          required
        />

        <Input
          v-model="form.password"
          type="password"
          label="密码"
          placeholder="请输入密码"
          :error="errors.password"
          required
        />

        <!-- 需要人机验证时显示提示 -->
        <div v-if="needsCaptcha" class="captcha-notice">
          <span>⚠️ 登录尝试过多，请先进行人机验证</span>
        </div>

        <!-- 需要人机验证时显示按钮 -->
        <div v-if="needsCaptcha" class="captcha-button-container">
          <Button
            type="primary"
            block
            @click="openCaptcha"
          >
            🔐 进行人机验证
          </Button>
        </div>

        <div class="form-options">
          <label class="remember-me">
            <input v-model="form.rememberMe" type="checkbox" />
            <span>记住我</span>
          </label>
          <router-link to="/forgot-password" class="forgot-password">忘记密码？</router-link>
        </div>

        <Button
          type="primary"
          block
          :loading="loading"
          :disabled="needsCaptcha"
          @click="handleLogin"
        >
          登录
        </Button>
      </form>

      <div class="login-footer">
        <p>还没有账户？<router-link to="/register" class="register-link">立即注册</router-link></p>
      </div>
    </div>

    <!-- 人机验证码组件 -->
    <Captcha 
      ref="captchaRef"
      :show-trigger-button="false"
      @success="handleCaptchaSuccess"
      @close="handleCaptchaClose"
    />
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useUIStore } from '../stores/ui'
import { authAPI } from '../api'
import Button from '../components/Button.vue'
import Input from '../components/Input.vue'
import Captcha from '../components/Captcha.vue'

const router = useRouter()
const authStore = useAuthStore()
const uiStore = useUIStore()

const form = reactive({
  username: '',
  password: '',
  rememberMe: false
})

const errors = reactive({
  username: '',
  password: ''
})

const loading = ref(false)
const needsCaptcha = ref(false)
const captchaToken = ref(null)
const captchaRef = ref(null)

const validateForm = () => {
  errors.username = ''
  errors.password = ''

  if (!form.username.trim()) {
    errors.username = '请输入用户名'
    return false
  }
  if (!form.password) {
    errors.password = '请输入密码'
    return false
  }
  return true
}

const openCaptcha = () => {
  console.log('【登录】打开人机验证码')
  captchaRef.value?.openCaptcha()
}

const handleCaptchaSuccess = (data) => {
  console.log('【登录】人机验证成功，token:', data.token)
  captchaToken.value = data.token
  needsCaptcha.value = false
  uiStore.showNotification('验证成功，请继续登录', 'success')
}

const handleCaptchaClose = () => {
  console.log('【登录】人机验证已关闭')
}

const handleLogin = async () => {
  if (!validateForm()) return

  loading.value = true
  try {
    // 如果需要验证码但未验证，则提示
    if (needsCaptcha.value && !captchaToken.value) {
      errors.username = '请先完成人机验证'
      loading.value = false
      return
    }

    // 调用登录接口，如果有验证码 token 则一起发送
    const response = await authAPI.login(
      form.username, 
      form.password,
      captchaToken.value
    )
    
    if (response.code === 200 && response.data) {
      // 登录成功
      authStore.setAuth(form.username, form.username)
      uiStore.showNotification('登录成功！', 'success')
      router.push('/')
    } else if (response.code === 428) {
      // 需要人机验证
      console.log('【登录】需要人机验证')
      needsCaptcha.value = true
      captchaToken.value = null
      errors.username = response.message || '需要人机验证'
      uiStore.showNotification('检测到异常登录，需要进行人机验证', 'warning')
    } else if (response.code === 429) {
      // 被锁定
      console.log('【登录】账户被锁定')
      needsCaptcha.value = false
      errors.username = response.message || '登录尝试过多，请稍后再试'
      uiStore.showNotification(response.message || '登录尝试过多，账户已被锁定', 'error')
    } else {
      // 其他错误
      errors.username = response.message || '登录失败，请检查用户名和密码'
      console.error('Login failed:', response.message)
    }
  } catch (error) {
    errors.username = '登录失败，请稍后重试'
    console.error('Login error:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-box {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  width: 100%;
  max-width: 400px;
  padding: 40px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.login-header h1 {
  font-size: 24px;
  color: #333;
  margin: 10px 0;
}

.login-header p {
  color: #999;
  font-size: 14px;
}

.login-form {
  margin-bottom: 20px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-size: 14px;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #666;

  input {
    width: auto;
    margin: 0;
  }
}

.forgot-password {
  color: #667eea;
  text-decoration: none;

  &:hover {
    color: #764ba2;
  }
}

.captcha-notice {
  background-color: #fff3cd;
  border: 1px solid #ffc107;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 16px;
  color: #856404;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  animation: slideDown 0.3s ease;
  width: 100%;
  box-sizing: border-box;
}

.captcha-button-container {
  margin-bottom: 16px;
  width: 100%;
  display: block;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-footer {
  text-align: center;
  color: #999;
  font-size: 14px;

  a {
    color: #667eea;
    text-decoration: none;

    &:hover {
      color: #764ba2;
    }
  }
}
</style>
