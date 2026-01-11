<template>
  <div class="app">
    <MainLayout v-if="$route.path !== '/login' && $route.path !== '/register'">
      <RouterView />
    </MainLayout>
    <RouterView v-else />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import MainLayout from './layouts/MainLayout.vue'

const router = useRouter()

// 检查认证状态
const checkAuth = () => {
  const token = localStorage.getItem('token')
  if (!token && router.currentRoute.value.path !== '/login' && router.currentRoute.value.path !== '/register') {
    router.push('/login')
  }
}

// 监听路由变化
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login' || to.path === '/register') {
    next()
  } else if (!token) {
    next('/login')
  } else {
    next()
  }
})

checkAuth()
</script>

<style scoped>
.app {
  height: 100vh;
  overflow: hidden;
}
</style>
