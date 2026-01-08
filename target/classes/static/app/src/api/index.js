import axios from 'axios'

const api = axios.create({
  baseURL: '/jizhang/api',
  timeout: 10000,
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

// 响应拦截器
api.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/jizhang/login'
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
  updateProfile: (data) => api.put('/user/profile', data)
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

export default api
