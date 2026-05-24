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
            v-model="profile.nickname"
            label="昵称"
            placeholder="请输入昵称（用于家庭组成员管理中显示）"
            @blur="validateNickname"
          />
          <p v-if="nicknameError" class="error-text">{{ nicknameError }}</p>
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
            <Button type="primary" @click="handleSaveProfile" :disabled="!!nicknameError">
              保存修改
            </Button>
            <Button 
              v-if="nicknameChanged"
              type="secondary" 
              @click="handleResetNickname"
            >
              重置昵称
            </Button>
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

      <!-- 通知设置 (暂时不实现)
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
      -->
      
      <!-- 账户安全 (暂时不实现)
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
      -->

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

    <!-- 成功/错误提示 -->
    <div v-if="message" class="message" :class="messageType">
      {{ message }}
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useUIStore } from '../stores/ui'
import { authAPI } from '../api'
import Card from '../components/Card.vue'
import Button from '../components/Button.vue'
import Input from '../components/Input.vue'

const router = useRouter()
const authStore = useAuthStore()
const uiStore = useUIStore()

const profile = reactive({
  username: '',
  nickname: '',
  email: '',
  phone: ''
})

const originalNickname = ref('')
const originalEmail = ref('')
const originalPhone = ref('')
const nicknameError = ref('')
const nicknameChanged = ref(false)
const message = ref('')
const messageType = ref('success')

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
      profile.nickname = response.data.nickname || response.data.username || ''
      originalNickname.value = profile.nickname
      profile.email = response.data.email || ''
      originalEmail.value = profile.email
      profile.phone = response.data.phone || ''
      originalPhone.value = profile.phone
    }
  } catch (error) {
    console.error('Failed to load profile:', error)
  }
}

const handleSaveProfile = async () => {
  try {
    // 检查是否有任何字段被修改
    const hasChanges = 
      profile.nickname !== originalNickname.value ||
      profile.email !== originalEmail.value ||
      profile.phone !== originalPhone.value
    
    if (!hasChanges) {
      showMessage('未修改任何内容', 'success')
      return
    }
    
    // 调用更新API
    const result = await authAPI.updateProfile({
      nickname: profile.nickname,
      email: profile.email,
      phone: profile.phone
    })
    
    if (result.code === 200 || result.code === 0) {
      // 更新成功后，保存原始值
      originalNickname.value = profile.nickname
      originalEmail.value = profile.email
      originalPhone.value = profile.phone
      nicknameChanged.value = false
      
      showMessage('个人信息已更新', 'success')
    } else {
      showMessage(result.message || '更新失败，请重试', 'error')
    }
  } catch (error) {
    console.error('Failed to save profile:', error)
    showMessage('更新失败，请重试', 'error')
  }
}

const updateNickname = async () => {
  try {
    const result = await authAPI.updateNickname(profile.nickname)
    
    if (result.code === 200 || result.code === 0) {
      console.log('昵称已更新:', result.data.nickname)
      // 更新 authStore 中的用户信息
      if (authStore.user) {
        authStore.user.nickname = result.data.nickname
      }
      return true
    } else {
      throw new Error(result.message || '更新昵称失败')
    }
  } catch (error) {
    console.error('Failed to update nickname:', error)
    throw error
  }
}

const validateNickname = () => {
  nicknameError.value = ''
  nicknameChanged.value = profile.nickname !== originalNickname.value
  
  if (profile.nickname && profile.nickname.trim() === '') {
    nicknameError.value = '昵称不能只包含空格'
    return
  }
  
  if (profile.nickname && profile.nickname.length > 50) {
    nicknameError.value = '昵称不超过 50 个字符'
    return
  }
}

const handleResetNickname = () => {
  profile.nickname = originalNickname.value
  nicknameChanged.value = false
  nicknameError.value = ''
}

const handleChangePassword = async () => {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    showMessage('两次输入的密码不一致', 'error')
    return
  }
  
  try {
    // 调用修改密码API
    showMessage('密码已修改', 'success')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    console.error('Failed to change password:', error)
    showMessage('修改失败，请重试', 'error')
  }
}

const handleSaveSettings = () => {
  showMessage('设置已保存', 'success')
}

const handleExportData = () => {
  showMessage('数据导出功能开发中...', 'success')
}

const handleDeleteAccount = async () => {
  const confirmed = await uiStore.showConfirm('确定要删除账户吗？此操作不可撤销！', '危险操作', 'danger')
  if (confirmed) {
    showMessage('账户删除功能开发中...', 'success')
  }
}

const handleLogout = () => {
  authStore.clearAuth()
  router.push('/login')
}

const showMessage = (msg, type = 'success') => {
  message.value = msg
  messageType.value = type
  setTimeout(() => {
    message.value = ''
  }, 3000)
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

.error-text {
  color: #ff4d4f;
  font-size: 12px;
  margin-top: -10px;
  margin-bottom: 10px;
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

/* 消息提示 */
.message {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 12px 20px;
  border-radius: 4px;
  color: white;
  font-size: 14px;
  z-index: 2000;
  animation: slideIn 0.3s ease;
}

.message.success {
  background: #52c41a;
}

.message.error {
  background: #ff4d4f;
}

@keyframes slideIn {
  from {
    transform: translateX(400px);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@media (max-width: 768px) {
  .settings-grid {
    grid-template-columns: 1fr;
  }
}
</style>
