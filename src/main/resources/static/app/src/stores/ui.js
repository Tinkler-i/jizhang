import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUIStore = defineStore('ui', () => {
  const sidebarCollapsed = ref(false)
  const notification = ref(null)
  const loading = ref(false)
  let notificationTimer = null  // 保存定时器引用

  // 确认对话框状态
  const confirmDialog = ref(null)
  let confirmResolve = null

  const showNotification = (message, type = 'info', duration = 3000) => {
    // 清除之前的定时器，防止提前关闭新通知
    if (notificationTimer) {
      clearTimeout(notificationTimer)
      notificationTimer = null
    }
    
    notification.value = { message, type }
    if (duration > 0) {
      notificationTimer = setTimeout(() => {
        notification.value = null
        notificationTimer = null
      }, duration)
    }
  }

  // 显示确认对话框，返回 Promise
  const showConfirm = (message, title = '确认操作', type = 'warning') => {
    return new Promise((resolve) => {
      confirmResolve = resolve
      confirmDialog.value = { message, title, type }
    })
  }

  // 确认对话框 - 确定
  const handleConfirm = () => {
    if (confirmResolve) {
      confirmResolve(true)
      confirmResolve = null
    }
    confirmDialog.value = null
  }

  // 确认对话框 - 取消
  const handleCancel = () => {
    if (confirmResolve) {
      confirmResolve(false)
      confirmResolve = null
    }
    confirmDialog.value = null
  }

  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  const setLoading = (isLoading) => {
    loading.value = isLoading
  }

  return {
    sidebarCollapsed,
    notification,
    loading,
    confirmDialog,
    showNotification,
    showConfirm,
    handleConfirm,
    handleCancel,
    toggleSidebar,
    setLoading
  }
})
