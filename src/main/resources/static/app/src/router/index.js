import { createRouter, createWebHistory } from 'vue-router'
import { authAPI } from '../api'
import Login from '../views/Login.vue'
import Dashboard from '../views/Dashboard.vue'
import Income from '../views/Income.vue'
import IncomeCategory from '../views/IncomeCategory.vue'
import Expense from '../views/Expense.vue'
import ExpenseCategory from '../views/ExpenseCategory.vue'
import Budget from '../views/Budget.vue'
import Report from '../views/Report.vue'
import UserProfile from '../views/UserProfile.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
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
    console.log('【路由守卫】验证成功，用户:', response)
    next()
  } catch (error) {
    console.warn('【路由守卫】token 验证失败，清除登录信息:', error)
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    next('/login')
  } finally {
    isVerifying = false
  }
})

export default router
