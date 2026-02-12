import { createRouter, createWebHistory } from 'vue-router'
import { authAPI } from '../api'
import { useUIStore } from '../stores/ui'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import ForgotPassword from '../views/ForgotPassword.vue'
import Dashboard from '../views/Dashboard.vue'
import Income from '../views/Income.vue'
import IncomeCategory from '../views/IncomeCategory.vue'
import Expense from '../views/Expense.vue'
import ExpenseCategory from '../views/ExpenseCategory.vue'
import Budget from '../views/Budget.vue'
import Report from '../views/Report.vue'
import UserProfile from '../views/UserProfile.vue'
import BillImport from '../views/BillImport.vue'
import FamilyManagement from '../views/FamilyManagement.vue'
import AIAnalysis from '../views/AIAnalysis.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { requiresAuth: false }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: ForgotPassword,
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    name: 'Dashboard',
    component: Dashboard,
    meta: { requiresAuth: true, title: '仪表盘' }
  },
  {
    path: '/income',
    name: 'Income',
    component: Income,
    meta: { requiresAuth: true, title: '收入管理' }
  },
  {
    path: '/income-category',
    name: 'IncomeCategory',
    component: IncomeCategory,
    meta: { requiresAuth: true, title: '收入分类' }
  },
  {
    path: '/expense',
    name: 'Expense',
    component: Expense,
    meta: { requiresAuth: true, title: '支出管理' }
  },
  {
    path: '/expense-category',
    name: 'ExpenseCategory',
    component: ExpenseCategory,
    meta: { requiresAuth: true, title: '支出分类' }
  },
  {
    path: '/budget',
    name: 'Budget',
    component: Budget,
    meta: { requiresAuth: true, title: '预算管理' }
  },
  {
    path: '/report',
    name: 'Report',
    component: Report,
    meta: { requiresAuth: true, title: '报表分析' }
  },
  {
    path: '/profile',
    name: 'UserProfile',
    component: UserProfile,
    meta: { requiresAuth: true, title: '个人设置' }
  },
  {
    path: '/bill-import',
    name: 'BillImport',
    component: BillImport,
    meta: { requiresAuth: true, title: '账单导入' }
  },
  {
    path: '/family-management',
    name: 'FamilyManagement',
    component: FamilyManagement,
    meta: { requiresAuth: true, title: '家庭组管理' }
  },
  {
    path: '/ai-analysis',
    name: 'AIAnalysis',
    component: AIAnalysis,
    meta: { requiresAuth: true, title: 'AI财务顾问' }
  }
]

const router = createRouter({
  history: createWebHistory('/jizhang/'),
  routes
})

let isVerifying = false

router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token')
  const username = localStorage.getItem('username')
  const uiStore = useUIStore()
  
  // 如果是登录页，直接放行
  if (to.meta.requiresAuth === false) {
    next()
    return
  }
  
  // 如果没有 token，重定向到登录
  if (!token) {
    console.log('【路由守卫】没有 token，重定向到登录页')
    next('/login')
    return
  }
  
  // 防止重复验证
  if (isVerifying) {
    next()
    return
  }
  
  // 验证 token 有效性，确保后端 session 也存在
  isVerifying = true
  try {
    console.log('【路由守卫】验证 token 有效性...')
    const response = await authAPI.getProfile()
    
    // 检查响应是否成功
    if (response.code === 0 && response.data) {
      console.log('【路由守卫】验证成功，用户:', response.data)
      next()
    } else if (response.code === 401 || response.message?.includes('未登录') || response.message?.includes('会话已过期')) {
      console.warn('【路由守卫】会话已过期，用户信息:', response)
      // 清除登录信息
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      // 显示提示信息
      if (uiStore && uiStore.showNotification) {
        uiStore.showNotification('登录已过期，请重新登录', 'warning', 3000)
      }
      // 重定向到登录页
      next('/login')
    } else {
      console.warn('【路由守卫】未知响应:', response)
      next()
    }
  } catch (error) {
    console.warn('【路由守卫】token 验证失败:', error.message)
    
    // 检查是否是401错误
    if (error.response?.status === 401) {
      console.warn('【路由守卫】收到 401 错误，会话已过期')
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      if (uiStore && uiStore.showNotification) {
        uiStore.showNotification('登录已过期，请重新登录', 'warning', 3000)
      }
      next('/login')
    } else {
      // 其他错误，允许继续（会在页面请求时再次处理）
      console.warn('【路由守卫】验证出错，继续导航:', error)
      next()
    }
  } finally {
    isVerifying = false
  }
})

export default router
