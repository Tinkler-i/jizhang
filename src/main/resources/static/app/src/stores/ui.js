import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUIStore = defineStore('ui', () => {
  const sidebarCollapsed = ref(false)
  const notification = ref(null)
  const loading = ref(false)

  const showNotification = (message, type = 'info', duration = 3000) => {
    notification.value = { message, type }
    if (duration > 0) {
      setTimeout(() => {
        notification.value = null
      }, duration)
    }
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
    showNotification,
    toggleSidebar,
    setLoading
  }
})
