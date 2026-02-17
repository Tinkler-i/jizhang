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
          @click="handleLogin"
        >
          登录
        </Button>
      </form>

      <div class="login-footer">
        <p>还没有账户？<router-link to="/register" class="register-link">立即注册</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { authAPI } from '../api'
import Button from '../components/Button.vue'
import Input from '../components/Input.vue'

const router = useRouter()
const authStore = useAuthStore()

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

const handleLogin = async () => {
  if (!validateForm()) return

  loading.value = true
  try {
    const response = await authAPI.login(form.username, form.password)
    if (response.code === 200 && response.data) {
      const { token } = response.data
      authStore.setAuth(token, form.username)
      router.push('/')
    } else {
      // 显示后端返回的错误信息
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
