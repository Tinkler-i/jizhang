<template>
  <div class="captcha-wrapper">
    <div v-if="showCaptcha" class="captcha-modal-overlay" @click="closeCaptcha">
      <div class="captcha-modal" @click.stop>
        <div class="captcha-header">
          <h3>人机验证</h3>
          <button class="close-btn" @click="closeCaptcha">×</button>
        </div>
        
        <div class="captcha-content">
          <mi-captcha
            ref="captchaRef"
            :init-action="initAction"
            :verify-action="dynamicVerifyAction"
            :verify-params="verifyParams"
            @init="handleCaptchaInit"
            @success="handleCaptchaSuccess"
            primary-color="#667eea"
            theme-color="#764ba2"
          />
        </div>
      </div>
    </div>
    
    <!-- 触发验证按钮 - 根据 showTriggerButton 属性显示 -->
    <button 
      v-if="showTriggerButton"
      class="captcha-trigger-btn"
      @click="openCaptcha"
    >
      🔐 开始验证
    </button>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useUIStore } from '../stores/ui'

const props = defineProps({
  // 是否显示触发按钮
  showTriggerButton: {
    type: Boolean,
    default: false
  },
  // 初始化 API 地址
  initAction: {
    type: String,
    default: '/api/captcha/init'
  },
  // 验证 API 地址
  verifyAction: {
    type: String,
    default: '/api/captcha/verify'
  },
  // 打开验证码前的验证逻辑（返回 true 表示通过，返回 false 或字符串表示验证失败）
  beforeOpen: {
    type: Function,
    default: null
  }
})

const emit = defineEmits(['success', 'close'])

const uiStore = useUIStore()
const captchaRef = ref(null)
const showCaptcha = ref(false)
const verifyParams = ref({ key: null })
const captchaId = ref(null)  // 单独存储 captchaId

// 计算动态的验证 action URL，添加 captchaId 作为查询参数
const dynamicVerifyAction = computed(() => {
  if (captchaId.value) {
    return `${props.verifyAction}?captchaId=${captchaId.value}`
  }
  return props.verifyAction
})

// 初始化验证码
const handleCaptchaInit = (res) => {
  console.log('【验证码】初始化回调:', res)
  console.log('【验证码】初始化 URL:', props.initAction)
  console.log('【验证码】初始化响应完整数据:', JSON.stringify(res, null, 2))
  if (res?.data?.key) {
    // 创建新对象而不是修改现有对象，确保响应式更新
    verifyParams.value = {
      key: res.data.key
    }
    // 分别存储 captchaId
    captchaId.value = res.data.captchaId
    console.log('【验证码】已更新 verifyParams:', JSON.stringify(verifyParams.value, null, 2))
    console.log('【验证码】已获取 key:', res.data.key)
    console.log('【验证码】已获取 captchaId:', res.data.captchaId)
  }
}

// 验证码验证成功
const handleCaptchaSuccess = (data) => {
  console.log('【验证码】验证成功回调:', data)
  console.log('【验证码】验证成功响应完整数据:', JSON.stringify(data, null, 2))
  
  // 库会将后端 res?.data 传过来，其中包含 token 字段
  const token = data?.token
  console.log('【验证码】验证成功，token:', token)
  
  if (token) {
    // 触发成功事件，传递 token
    emit('success', {
      token: token,
      timestamp: Date.now()
    })
    
    // 显示提示
    uiStore.showNotification('人机验证成功', 'success')
    
    // 延迟关闭
    setTimeout(() => {
      closeCaptcha()
    }, 500)
  } else {
    const errorMsg = data?.message || '验证失败，请重试'
    console.warn('【验证码】验证失败:', errorMsg)
    console.error('【验证码】验证失败的完整响应:', JSON.stringify(data, null, 2))
    uiStore.showNotification(errorMsg, 'error')
  }
}

// 打开验证码
const openCaptcha = async () => {
  console.log('【验证码】准备打开验证码模态框')
  
  // 如果提供了 beforeOpen 钩子，先执行验证
  if (props.beforeOpen) {
    const result = await props.beforeOpen()
    if (result === false) {
      // 验证失败，不打开模态框
      console.warn('【验证码】beforeOpen 验证失败，取消打开模态框')
      return
    }
    if (typeof result === 'string') {
      // 如果返回字符串，显示错误信息
      uiStore.showNotification(result, 'error')
      console.warn('【验证码】beforeOpen 验证失败:', result)
      return
    }
  }
  
  console.log('【验证码】打开验证码模态框')
  showCaptcha.value = true
}

// 关闭验证码
const closeCaptcha = () => {
  console.log('【验证码】关闭验证码模态框')
  showCaptcha.value = false
  emit('close')
}

// 导出方法给父组件使用
defineExpose({
  openCaptcha,
  closeCaptcha
})
</script>

<style scoped lang="scss">
.captcha-wrapper {
  .captcha-modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 9999;
    animation: fadeIn 0.3s ease;
  }

  .captcha-modal {
    background: white;
    border-radius: 12px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
    max-width: 400px;
    width: 90%;
    animation: slideUp 0.3s ease;
  }

  .captcha-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px;
    border-bottom: 1px solid #e8e8e8;

    h3 {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
      color: #333;
    }

    .close-btn {
      background: none;
      border: none;
      font-size: 28px;
      color: #999;
      cursor: pointer;
      padding: 0;
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      transition: all 0.3s;

      &:hover {
        background: #f0f0f0;
        color: #333;
      }
    }
  }

  .captcha-content {
    padding: 20px;
  }

  .captcha-trigger-btn {
    padding: 12px 20px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-size: 15px;
    font-weight: 500;
    transition: all 0.3s;
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
    }

    &:active {
      transform: translateY(0);
    }
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

// 深色主题支持
@media (prefers-color-scheme: dark) {
  .captcha-modal {
    background: #2a2a2a;
    color: #fff;
  }

  .captcha-header {
    border-bottom-color: #444;

    h3 {
      color: #fff;
    }

    .close-btn {
      color: #aaa;

      &:hover {
        background: #444;
        color: #fff;
      }
    }
  }
}
</style>
