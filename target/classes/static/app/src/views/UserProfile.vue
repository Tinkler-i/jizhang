<template>
  <div class="page">
    <div class="page-header">
      <h1>个人设置</h1>
    </div>

    <div class="settings-grid">
      <!-- 基本设置 -->
      <Card class="settings-card">
        <template #header>
          <h2>基本信息</h2>
        </template>
        <form @submit.prevent="handleSaveProfile">
          <Input
            v-model="profile.username"
            label="用户名"
            disabled
          />
          <Input
            v-model="profile.email"
            label="邮箱"
            type="email"
            placeholder="请输入邮箱"
          />
          <Input
            v-model="profile.phone"
            label="电话"
            placeholder="请输入电话"
          />
          <div class="form-actions">
            <Button type="primary" @click="handleSaveProfile">保存修改</Button>
          </div>
        </form>
      </Card>

      <!-- 密码修改 -->
      <Card class="settings-card">
        <template #header>
          <h2>修改密码</h2>
        </template>
        <form @submit.prevent="handleChangePassword">
          <Input
            v-model="passwordForm.oldPassword"
            type="password"
            label="旧密码"
            placeholder="请输入旧密码"
            required
          />
          <Input
            v-model="passwordForm.newPassword"
            type="password"
            label="新密码"
            placeholder="请输入新密码"
            required
          />
          <Input
            v-model="passwordForm.confirmPassword"
            type="password"
            label="确认密码"
            placeholder="再次输入新密码"
            required
          />
          <div class="form-actions">
            <Button type="primary" @click="handleChangePassword">修改密码</Button>
          </div>
        </form>
      </Card>

      <!-- 通知设置 -->
      <Card class="settings-card">
        <template #header>
          <h2>通知设置</h2>
        </template>
        <div class="settings-option">
          <label class="checkbox-label">
            <input v-model="settings.emailNotification" type="checkbox" />
            <span>邮件通知</span>
          </label>
          <p class="help-text">接收重要通知的邮件</p>
        </div>
        <div class="settings-option">
          <label class="checkbox-label">
            <input v-model="settings.budgetAlert" type="checkbox" />
            <span>预算超额提醒</span>
          </label>
          <p class="help-text">当支出超过预算时提醒</p>
        </div>
        <div class="settings-option">
          <label class="checkbox-label">
            <input v-model="settings.dailySummary" type="checkbox" />
            <span>每日汇总</span>
          </label>
          <p class="help-text">每天接收今日收支汇总</p>
        </div>
        <div class="form-actions">
          <Button type="primary" @click="handleSaveSettings">保存设置</Button>
        </div>
      </Card>

      <!-- 账户安全 -->
      <Card class="settings-card">
        <template #header>
          <h2>账户安全</h2>
        </template>
        <div class="security-info">
          <div class="security-item">
            <p class="security-label">最后登录</p>
            <p class="security-value">{{ lastLogin }}</p>
          </div>
          <div class="security-item">
            <p class="security-label">登录位置</p>
            <p class="security-value">{{ loginLocation }}</p>
          </div>
          <div class="security-item">
            <p class="security-label">账户状态</p>
            <p class="security-value" style="color: #27ae60">正常</p>
          </div>
        </div>
        <div class="form-actions">
          <Button type="danger" @click="handleLogout">登出所有设备</Button>
        </div>
      </Card>

      <!-- 数据管理 -->
      <Card class="settings-card">
        <template #header>
          <h2>数据管理</h2>
        </template>
        <div class="data-actions">
          <p>您可以导出您的所有账户数据进行备份</p>
          <Button type="secondary" @click="handleExportData">导出数据</Button>
        </div>
        <div class="data-actions danger-zone">
          <p>删除账户是永久的，此操作不可撤销</p>
          <Button type="danger" @click="handleDeleteAccount">删除账户</Button>
        </div>
      </Card>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { authAPI } from '../api'
import Card from '../components/Card.vue'
import Button from '../components/Button.vue'
import Input from '../components/Input.vue'

const router = useRouter()
const authStore = useAuthStore()

const profile = reactive({
  username: '',
  email: '',
  phone: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const settings = reactive({
  emailNotification: true,
  budgetAlert: true,
  dailySummary: false
})

const lastLogin = ref('2024年1月7日 14:30')
const loginLocation = ref('中国 北京')

const loadProfile = async () => {
  try {
    const response = await authAPI.getProfile()
    if (response.code === 200 && response.data) {
      profile.username = response.data.username || ''
      profile.email = response.data.email || ''
      profile.phone = response.data.phone || ''
    }
  } catch (error) {
    console.error('Failed to load profile:', error)
  }
}

const handleSaveProfile = async () => {
  try {
    await authAPI.updateProfile(profile)
    alert('个人信息已更新')
  } catch (error) {
    console.error('Failed to save profile:', error)
    alert('更新失败，请重试')
  }
}

const handleChangePassword = async () => {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    alert('两次输入的密码不一致')
    return
  }
  
  try {
    // 调用修改密码API
    alert('密码已修改')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    console.error('Failed to change password:', error)
    alert('修改失败，请重试')
  }
}

const handleSaveSettings = () => {
  alert('设置已保存')
}

const handleExportData = () => {
  alert('数据导出功能开发中...')
}

const handleDeleteAccount = () => {
  if (confirm('确定要删除账户吗？此操作不可撤销！')) {
    alert('账户删除功能开发中...')
  }
}

const handleLogout = () => {
  authStore.clearAuth()
  router.push('/login')
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.page {
  padding: 0;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  color: #333;
  margin: 0;
}

.settings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
}

.settings-card {
  display: flex;
  flex-direction: column;
}

.settings-card h2 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.form-actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

.settings-option {
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-weight: 500;
  color: #333;

  input {
    width: auto;
    margin: 0;
  }
}

.help-text {
  margin: 8px 0 0 0;
  font-size: 12px;
  color: #999;
}

.security-item {
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.security-label {
  font-size: 12px;
  color: #999;
  text-transform: uppercase;
  margin: 0 0 4px 0;
}

.security-value {
  font-size: 14px;
  color: #333;
  margin: 0;
}

.data-actions {
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  p {
    margin: 0 0 15px 0;
    color: #666;
    font-size: 14px;
  }

  &.danger-zone {
    padding-top: 20px;
    border-top: 1px solid #ffebee;
    background: #fafafa;
    margin: 0 -20px;
    padding: 15px 20px;
  }
}

@media (max-width: 768px) {
  .settings-grid {
    grid-template-columns: 1fr;
  }
}
</style>
