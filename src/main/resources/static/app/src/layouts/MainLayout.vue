<template>
  <div class="main-layout" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
    <!-- 全局通知 -->
    <transition name="notification-fade">
      <div v-if="notification" class="global-notification" :class="notification.type">
        <span class="notification-icon">{{ notificationIcon }}</span>
        <span class="notification-message">{{ notification.message }}</span>
      </div>
    </transition>

    <!-- 全局确认对话框 -->
    <transition name="confirm-fade">
      <div v-if="confirmDialog" class="confirm-overlay" @click.self="uiStore.handleCancel()">
        <div class="confirm-dialog" :class="confirmDialog.type">
          <div class="confirm-header">
            <span class="confirm-icon">{{ confirmIcon }}</span>
            <span class="confirm-title">{{ confirmDialog.title }}</span>
          </div>
          <div class="confirm-body">
            {{ confirmDialog.message }}
          </div>
          <div class="confirm-footer">
            <button class="confirm-btn cancel" @click="uiStore.handleCancel()">取消</button>
            <button class="confirm-btn ok" @click="uiStore.handleConfirm()">确定</button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 顶部导航栏 -->
    <header class="navbar">
      <div class="navbar-content">
        <div class="navbar-left">
          <button class="menu-toggle" @click="toggleSidebar">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <line x1="3" y1="6" x2="21" y2="6"></line>
              <line x1="3" y1="12" x2="21" y2="12"></line>
              <line x1="3" y1="18" x2="21" y2="18"></line>
            </svg>
          </button>
          <div class="brand">
            <span class="brand-icon">💰</span>
            <span class="brand-text">记账系统</span>
          </div>
        </div>

        <div class="navbar-right">
          <div class="user-info">
            <span class="username">{{ username }}</span>
          </div>
          <button class="logout-btn" @click="handleLogout">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
              <polyline points="16 17 21 12 16 7"></polyline>
              <line x1="21" y1="12" x2="9" y2="12"></line>
            </svg>
            登出
          </button>
        </div>
      </div>
    </header>

    <div class="main-wrapper">
      <!-- 侧边栏菜单 -->
      <aside class="sidebar">
        <nav class="sidebar-nav">
          <RouterLink
            v-for="item in menuItems"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            :class="{ active: $route.path === item.path }"
          >
            <span class="nav-icon" v-html="item.icon"></span>
            <span class="nav-text">{{ item.label }}</span>
          </RouterLink>
        </nav>
      </aside>

      <!-- 主内容区 -->
      <main class="content">
        <slot></slot>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useUIStore } from '../stores/ui'

const router = useRouter()
const authStore = useAuthStore()
const uiStore = useUIStore()
const sidebarCollapsed = ref(false)

const username = computed(() => localStorage.getItem('username') || '用户')
const notification = computed(() => uiStore.notification)
const confirmDialog = computed(() => uiStore.confirmDialog)
const notificationIcon = computed(() => {
  if (!notification.value) return ''
  const icons = {
    success: '✓',
    error: '✕',
    warning: '⚠',
    info: 'ℹ'
  }
  return icons[notification.value.type] || icons.info
})
const confirmIcon = computed(() => {
  if (!confirmDialog.value) return ''
  const icons = {
    warning: '⚠',
    danger: '⚠',
    info: 'ℹ'
  }
  return icons[confirmDialog.value.type] || icons.warning
})

const menuItems = [
  {
    path: '/',
    label: '仪表盘',
    icon: '📊'
  },
  {
    path: '/income',
    label: '收入管理',
    icon: '💵'
  },
  {
    path: '/income-category',
    label: '收入分类',
    icon: '🏷️'
  },
  {
    path: '/expense',
    label: '支出管理',
    icon: '💸'
  },
  {
    path: '/expense-category',
    label: '支出分类',
    icon: '🏷️'
  },
  {
    path: '/budget',
    label: '预算管理',
    icon: '💹'
  },
  {
    path: '/report',
    label: '报表分析',
    icon: '📈'
  },
  {
    path: '/bill-import',
    label: '账单导入',
    icon: '📸'
  },
  {
    path: '/family-management',
    label: '家庭组管理',
    icon: '👨‍👩‍👧‍👦'
  },
  {
    path: '/ai-analysis',
    label: 'AI财务顾问',
    icon: '🤖'
  },
  {
    path: '/profile',
    label: '个人设置',
    icon: '⚙️'
  }
]

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  authStore.clearAuth()
  router.push('/login')
}
</script>

<style scoped>
.main-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f7fa;
}

/* 顶部导航栏 */
.navbar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.navbar-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 70px;
  padding: 0 20px;
  max-width: 100%;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.menu-toggle {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  padding: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  transition: background 0.3s;
}

.menu-toggle:hover {
  background: rgba(255, 255, 255, 0.1);
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 700;
}

.brand-icon {
  font-size: 24px;
}

.brand-text {
  white-space: nowrap;
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
}

.username {
  font-size: 14px;
  font-weight: 500;
}

.logout-btn {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

/* 主容器 */
.main-wrapper {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* 侧边栏 */
.sidebar {
  width: 250px;
  background: white;
  border-right: 1px solid #e0e0e0;
  overflow-y: auto;
  transition: width 0.3s ease;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
}

.main-layout.sidebar-collapsed .sidebar {
  width: 70px;
}

.sidebar-nav {
  list-style: none;
  padding: 20px 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 12px 20px;
  color: #666;
  text-decoration: none;
  transition: all 0.3s;
  border-left: 3px solid transparent;
  font-size: 14px;
  font-weight: 500;
}

.main-layout.sidebar-collapsed .nav-item {
  padding: 12px 10px;
  justify-content: center;
}

.nav-item:hover {
  background: #f5f5f5;
  color: #667eea;
}

.nav-item.active {
  background: #f0f4ff;
  color: #667eea;
  border-left-color: #667eea;
}

.nav-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.nav-text {
  white-space: nowrap;
}

.main-layout.sidebar-collapsed .nav-text {
  display: none;
}

/* 主内容区 */
.content {
  flex: 1;
  overflow-y: auto;
  padding: 30px 40px;
  background: #f5f7fa;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .navbar-content {
    padding: 0 15px;
    height: 60px;
  }

  .brand-text {
    display: none;
  }

  .sidebar {
    position: absolute;
    left: -250px;
    height: calc(100vh - 60px);
    z-index: 999;
    transition: left 0.3s ease;
  }

  .main-layout.sidebar-collapsed .sidebar {
    left: 0;
    width: 250px;
  }

  .content {
    padding: 20px 15px;
  }

  .navbar-right {
    gap: 10px;
  }
}

/* 滚动条样式 */
.sidebar::-webkit-scrollbar,
.content::-webkit-scrollbar {
  width: 6px;
}

.sidebar::-webkit-scrollbar-track,
.content::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.sidebar::-webkit-scrollbar-thumb,
.content::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 3px;
}

.sidebar::-webkit-scrollbar-thumb:hover,
.content::-webkit-scrollbar-thumb:hover {
  background: #555;
}

/* 全局通知样式 */
.global-notification {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 14px 20px;
  border-radius: 8px;
  color: white;
  font-size: 14px;
  font-weight: 500;
  z-index: 9999;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  max-width: 400px;
}

.global-notification.success {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
}

.global-notification.error {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
}

.global-notification.warning {
  background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);
}

.global-notification.info {
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
}

.notification-icon {
  font-size: 18px;
  font-weight: bold;
}

.notification-message {
  flex: 1;
}

/* 通知动画 */
.notification-fade-enter-active {
  animation: notificationSlideIn 0.3s ease;
}

.notification-fade-leave-active {
  animation: notificationSlideOut 0.3s ease;
}

@keyframes notificationSlideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@keyframes notificationSlideOut {
  from {
    transform: translateX(0);
    opacity: 1;
  }
  to {
    transform: translateX(100%);
    opacity: 0;
  }
}

/* 确认对话框样式 */
.confirm-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  backdrop-filter: blur(2px);
}

.confirm-dialog {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  min-width: 360px;
  max-width: 480px;
  overflow: hidden;
}

.confirm-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.confirm-icon {
  font-size: 24px;
}

.confirm-dialog.warning .confirm-icon {
  color: #faad14;
}

.confirm-dialog.danger .confirm-icon {
  color: #ff4d4f;
}

.confirm-dialog.info .confirm-icon {
  color: #1890ff;
}

.confirm-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.confirm-body {
  padding: 24px;
  font-size: 15px;
  color: #666;
  line-height: 1.6;
}

.confirm-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  background: #fafafa;
}

.confirm-btn {
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.confirm-btn.cancel {
  background: #f0f0f0;
  color: #666;
}

.confirm-btn.cancel:hover {
  background: #e0e0e0;
}

.confirm-btn.ok {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.confirm-btn.ok:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

/* 确认对话框动画 */
.confirm-fade-enter-active {
  animation: confirmFadeIn 0.25s ease;
}

.confirm-fade-leave-active {
  animation: confirmFadeOut 0.2s ease;
}

.confirm-fade-enter-active .confirm-dialog {
  animation: confirmSlideIn 0.25s ease;
}

.confirm-fade-leave-active .confirm-dialog {
  animation: confirmSlideOut 0.2s ease;
}

@keyframes confirmFadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes confirmFadeOut {
  from { opacity: 1; }
  to { opacity: 0; }
}

@keyframes confirmSlideIn {
  from {
    transform: scale(0.9) translateY(-20px);
    opacity: 0;
  }
  to {
    transform: scale(1) translateY(0);
    opacity: 1;
  }
}

@keyframes confirmSlideOut {
  from {
    transform: scale(1) translateY(0);
    opacity: 1;
  }
  to {
    transform: scale(0.9) translateY(-20px);
    opacity: 0;
  }
}
</style>
