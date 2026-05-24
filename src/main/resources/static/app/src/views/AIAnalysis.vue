<template>
  <div class="ai-analysis-page">
    <!-- 页面header -->
    <div class="page-header">
      <h2>🤖 AI财务顾问</h2>
      <button class="clear-btn" @click="clearChat">清空对话</button>
    </div>
    
    <!-- 对话框 -->
    <div class="ai-chat-wrapper">
      <AIChat ref="aiChatRef" />
    </div>
  </div>
</template>

<script setup>
import AIChat from '../components/AIChat.vue'
import { ref } from 'vue'

const aiChatRef = ref(null)

const clearChat = () => {
  aiChatRef.value?.clearMessages?.()
}
</script>

<style scoped>
.ai-analysis-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  border-bottom: 1px solid #e5e7eb;
  background: white;
  flex-shrink: 0;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.clear-btn {
  padding: 8px 16px;
  background: #f3f4f6;
  color: #666;
  border: 1px solid #ddd;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.clear-btn:hover {
  background: #e5e7eb;
  color: #333;
  border-color: #ccc;
}

.ai-chat-wrapper {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}
</style>
