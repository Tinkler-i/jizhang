import axios from 'axios'
import { useUIStore } from '../stores/ui'

const api = axios.create({
  baseURL: '/jizhang/api',
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器 - 统一处理会话过期和权限错误
api.interceptors.response.use(
  response => {
    // 检查返回数据中是否包含错误且是认证相关错误
    if (response.data && response.data.code && response.data.code !== 0) {
      // 如果是认证相关错误，清除登录信息并跳转到登录页
      if (response.data.code === 401 || response.data.message?.includes('未登录') || response.data.message?.includes('会话已过期')) {
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        // 使用 window.location 确保强制刷新
        window.location.href = '/jizhang/login'
      }
    }
    return response.data
  },
  error => {
    console.log('【API错误】状态码:', error.response?.status, '消息:', error.response?.data?.message)
    // 处理HTTP状态码错误
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      window.location.href = '/jizhang/login'
    }
    // 处理403权限不足错误
    if (error.response?.status === 403) {
      console.log('【API错误】检测到403权限错误，准备显示通知')
      try {
        const uiStore = useUIStore()
        const message = error.response?.data?.message || '您没有权限执行此操作'
        console.log('【API错误】显示通知:', message)
        uiStore.showNotification(message, 'error', 5000)
      } catch (e) {
        console.error('无法显示权限错误提示:', e)
      }
      return Promise.reject(new Error(error.response?.data?.message || '权限不足'))
    }
    // 处理网络错误或其他错误
    if (error.response?.status === 500) {
      // 检查是否是认证相关的500错误
      if (error.response?.data?.message?.includes('未登录') || error.response?.data?.message?.includes('会话已过期')) {
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        window.location.href = '/jizhang/login'
      }
    }
    return Promise.reject(error)
  }
)

// Auth API
export const authAPI = {
  login: (username, password) =>
    api.post('/auth/login', { username, password }),
  logout: () => api.post('/auth/logout'),
  getProfile: () => api.get('/user/profile'),
  updateProfile: (data) => api.put('/user/profile', data),
  updateNickname: (nickname) => api.put('/user/nickname', { nickname }),
  sendVerificationCode: (data) => api.post('/auth/send-verification-code', data),
  verifyCode: (data) => api.post('/auth/verify-code', data),
  register: (data) => api.post('/auth/register', data),
  // 忘记密码 API
  sendResetPasswordCode: (data) => api.post('/auth/send-reset-password-code', data),
  verifyResetPasswordCode: (data) => api.post('/auth/verify-reset-password-code', data),
  resetPassword: (data) => api.post('/auth/reset-password', data)
}

// Income API
export const incomeAPI = {
  getList: (params) => api.get('/income', { params }),
  getById: (id) => api.get(`/income/${id}`),
  create: (data) => api.post('/income', data),
  update: (id, data) => api.put(`/income/${id}`, data),
  delete: (id) => api.delete(`/income/${id}`),
  getByMonth: (month) => api.get(`/income/month/${month}`)
}

// Expense API
export const expenseAPI = {
  getList: (params) => api.get('/expense', { params }),
  getById: (id) => api.get(`/expense/${id}`),
  create: (data) => api.post('/expense', data),
  update: (id, data) => api.put(`/expense/${id}`, data),
  delete: (id) => api.delete(`/expense/${id}`),
  getByMonth: (month) => api.get(`/expense/month/${month}`)
}

// Income Category API
export const incomeCategoryAPI = {
  getList: () => api.get('/income-category'),
  getById: (id) => api.get(`/income-category/${id}`),
  create: (data) => api.post('/income-category', data),
  update: (id, data) => api.put(`/income-category/${id}`, data),
  delete: (id) => api.delete(`/income-category/${id}`)
}

// Expense Category API
export const expenseCategoryAPI = {
  getList: () => api.get('/expense-category'),
  getById: (id) => api.get(`/expense-category/${id}`),
  create: (data) => api.post('/expense-category', data),
  update: (id, data) => api.put(`/expense-category/${id}`, data),
  delete: (id) => api.delete(`/expense-category/${id}`)
}

// Budget API
export const budgetAPI = {
  getList: (params) => api.get('/budget', { params }),
  getById: (id) => api.get(`/budget/${id}`),
  create: (data) => api.post('/budget', data),
  update: (id, data) => api.put(`/budget/${id}`, data),
  delete: (id) => api.delete(`/budget/${id}`),
  getByMonth: (month) => api.get(`/budget/month/${month}`)
}

// Report API
export const reportAPI = {
  getSummary: (params) => api.get('/reports/summary', { params }),
  getIncomeChart: (month) => api.get(`/reports/income-chart/${month}`),
  getIncomeCategoryChart: (month) => api.get(`/reports/income-category-chart/${month}`),
  getExpenseChart: (month) => api.get(`/reports/expense-chart/${month}`),
  getAnalysis: (params) => api.get('/reports/analysis', { params })
}

// Analysis API
export const analysisAPI = {
  getMonthlyTrend: (params) => api.get('/analysis/monthly-trend', { params }),
  getCategoryAnalysis: (params) => api.get('/analysis/category', { params }),
  getBudgetVsActual: (month) => api.get(`/analysis/budget-vs-actual/${month}`)
}

// User Target API - 用户收入目标
export const userTargetAPI = {
  getByMonth: (month) => api.get(`/user-target/${month}`),
  update: (month, incomeTarget) => api.put(`/user-target/${month}`, { incomeTarget }),
  create: (targetMonth, incomeTarget) => api.post('/user-target', { targetMonth, incomeTarget })
}

// Bill Import API - 账单导入
export const billImportAPI = {
  recognize: (image, currentDate) => api.post('/bill-import/recognize', { image, currentDate }, { timeout: 120000 }),
  confirm: (records) => api.post('/bill-import/confirm', { records })
}

// 通用 API 调用函数 - 用于需要灵活请求的场景（如FamilyManagement）
export const apiCall = (url, method = 'GET', data = null) => {
  const config = {
    method: method.toUpperCase(),
    url: url
  }
  
  if (data && (method.toUpperCase() === 'POST' || method.toUpperCase() === 'PUT')) {
    config.data = data
  } else if (data && method.toUpperCase() === 'GET') {
    config.params = data
  }
  
  return api.request(config)
}

export default api
