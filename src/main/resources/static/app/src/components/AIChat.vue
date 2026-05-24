<template>
  <div class="ai-chat-container">
    <div class="chat-messages" ref="messagesContainer">
      <!-- 欢迎消息 -->
      <div v-if="messages.length === 0" class="welcome-message">
        <p>👋 你好！我是你的财务顾问AI助手</p>
        <p>你可以问我关于你的账单、支出、收入等问题</p>
        <div class="example-questions">
          <p>试试问我：</p>
          <button @click="sendPredefinedMessage('我这个月花了多少钱？')" class="example-btn">
            我这个月花了多少钱？
          </button>
          <button @click="sendPredefinedMessage('我的支出主要在哪些方面？')" class="example-btn">
            我的支出主要在哪些方面？
          </button>
          <button @click="sendPredefinedMessage('对比我的收入和支出')" class="example-btn">
            对比我的收入和支出
          </button>
        </div>
      </div>

      <!-- 对话消息 -->
      <div v-for="(msg, index) in messages" :key="`msg-${index}`" class="message-item" :class="msg.role">
        <div class="message-avatar">
          <span v-if="msg.role === 'user'">👤</span>
          <span v-else>🤖</span>
        </div>
        <div class="message-content">
          <div v-if="msg.role === 'user'" class="message-text">{{ msg.content }}</div>
          <!-- AI回复：同时尝试ref和v-html双保险 -->
          <div v-if="msg.role === 'assistant'" :ref="`aiMsg${index}`" class="message-markdown">
            <div v-if="msg.renderedContent" v-html="msg.renderedContent"></div>
            <div v-else>{{ msg.content }}</div>
          </div>
        </div>
      </div>

      <!-- 加载指示 -->
      <div v-if="loading" class="loading-indicator">
        <div class="spinner"></div>
        <p>AI正在思考...</p>
      </div>
    </div>

    <div class="chat-input-area">
      <textarea 
        v-model="inputText" 
        @keydown.ctrl.enter="sendMessage"
        @keydown.enter="sendMessage"
        placeholder="输入你的问题... (Ctrl+Enter 或 Enter 发送)"
        :disabled="loading"
        class="input-field"
      ></textarea>
      <button @click="sendMessage" :disabled="loading || !inputText.trim()" class="send-btn">
        <span v-if="!loading">发送</span>
        <span v-else>发送中...</span>
      </button>
    </div>
  </div>
</template>

<script>
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { useUIStore } from '../stores/ui'

// 配置marked以支持表格
marked.use({
  breaks: true,
  gfm: true
})

export default {
  name: 'AIChat',
  setup() {
    const uiStore = useUIStore()
    return { uiStore }
  },
  data() {
    return {
      messages: [],
      inputText: '',
      loading: false
    };
  },
  watch: {
    messages: {
      handler(newMessages) {
        // 仅在开发时输出消息数量
        if (process.env.NODE_ENV === 'development') {
          console.debug('【消息更新】共', newMessages.length, '条')
        }
      },
      deep: true
    }
  },
  computed: {},
  methods: {
    renderMarkdown(content) {
      try {
        // 直接用 marked 生成 HTML，不用 DOMPurify（marked 输出已经足够安全）
        const html = marked(content)
        return html
      } catch (e) {
        console.error('【Markdown渲染失败】', e.message)
        return `<p>${content}</p>`
      }
    },
    async sendMessage() {
      if (!this.inputText.trim()) {
        return;
      }

      const userMessage = this.inputText.trim();
      
      // 添加用户消息到界面
      this.messages.push({
        role: 'user',
        content: userMessage
      });
      
      this.inputText = '';
      this.loading = true;
      
      // 滚动到最新消息
      this.$nextTick(() => {
        this.scrollToBottom();
      });

      try {
        // 调用后端API
        const response = await fetch('/jizhang/api/ai/chat', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            message: userMessage
          })
        });

        const data = await response.json();
        
        if (response.ok && data && (data.code === 0 || data.code === 200)) {
          // 添加AI回复
          const aiContent = data.data || data.message || '获得回复'
          console.log('【用户问题】', userMessage)
          console.log('【AI回复】', aiContent)
          
          const rendered = this.renderMarkdown(aiContent)
          
          this.messages.push({
            role: 'assistant',
            content: aiContent,
            renderedContent: rendered
          });
        } else {
          // 添加错误消息
          const errorMsg = '❌ 出错: ' + (data?.message || data?.msg || '未知错误')
          console.error('【AI错误】', errorMsg)
          this.messages.push({
            role: 'assistant',
            content: errorMsg,
            renderedContent: errorMsg
          });
        }
      } catch (error) {
        console.error('【请求失败】', error.message)
        const errorMsg = '❌ 网络错误: ' + error.message
        this.messages.push({
          role: 'assistant',
          content: errorMsg,
          renderedContent: errorMsg
        });
      } finally {
        this.loading = false;
        this.$nextTick(() => {
          this.scrollToBottom();
          
          // 直接用ref设置AI消息的HTML内容
          this.messages.forEach((msg, index) => {
            if (msg.role === 'assistant' && msg.renderedContent) {
              const refKey = `aiMsg${index}`
              const element = this.$refs[refKey]?.[0]
              if (element) {
                element.innerHTML = msg.renderedContent
              }
            }
          })
        });
      }
    },

    async sendPredefinedMessage(message) {
      this.inputText = message;
      await this.sendMessage();
    },

    clearMessages() {
      this.uiStore.showConfirm('确定要清空所有对话吗？', '清空对话', 'warning').then(confirmed => {
        if (confirmed) {
          this.messages = []
        }
      })
    },

    scrollToBottom() {
      const container = this.$refs.messagesContainer;
      if (container) {
        setTimeout(() => {
          container.scrollTop = container.scrollHeight;
        }, 0);
      }
    }
  }
};
</script>

<style scoped>
.ai-chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
  overflow: hidden;
}

.chat-header {
  display: none;
}

.chat-header h3 {
  display: none;
}

.clear-btn {
  display: none;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: white;
}

.welcome-message {
  text-align: center;
  padding: 40px 20px;
  color: #666;
}

.welcome-message p {
  margin: 10px 0;
  font-size: 16px;
}

.example-questions {
  margin-top: 30px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.example-questions p {
  margin: 15px 0 10px;
  font-weight: 600;
  color: #333;
}

.example-btn {
  padding: 10px 16px;
  background: #f0f0f0;
  border: 1px solid #ddd;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #333;
  transition: all 0.3s ease;
}

.example-btn:hover {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
  font-size: 24px;
  display: flex;
  align-items: center;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  word-wrap: break-word;
  line-height: 1.5;
}

.message-text {
  margin: 0;
}

.message-markdown {
  margin: 0;
}

.message-markdown h1,
.message-markdown h2,
.message-markdown h3 {
  margin: 12px 0 8px 0;
  font-weight: 600;
}

.message-markdown h1 {
  font-size: 18px;
}

.message-markdown h2 {
  font-size: 16px;
}

.message-markdown h3 {
  font-size: 14px;
}

.message-markdown p {
  margin: 8px 0;
  font-size: 14px;
}

.message-markdown ul,
.message-markdown ol {
  margin: 8px 0;
  padding-left: 24px;
}

.message-markdown li {
  margin: 4px 0;
  font-size: 14px;
}

.message-markdown strong {
  font-weight: 600;
  color: #333;
}

.message-markdown em {
  font-style: italic;
  color: #666;
}

.message-markdown code {
  background: rgba(0, 0, 0, 0.1);
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
}

.message-markdown pre {
  background: rgba(0, 0, 0, 0.1);
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
  margin: 8px 0;
}

/* Markdown表格样式 */
.message-markdown table {
  border-collapse: collapse;
  margin: 12px 0;
  font-size: 13px;
  width: 100%;
}

.message-markdown table thead {
  background: #f0f0f0;
}

.message-markdown table th {
  padding: 8px 12px;
  border: 1px solid #ddd;
  text-align: left;
  font-weight: 600;
  color: #333;
}

.message-markdown table td {
  padding: 8px 12px;
  border: 1px solid #ddd;
  color: #666;
}

.message-markdown table tbody tr:nth-child(even) {
  background: #fafafa;
}

.message-markdown table tbody tr:hover {
  background: #f0f0f0;
}

.message-item.user .message-markdown table th {
  background: rgba(102, 126, 234, 0.2);
  color: white;
}

.message-item.user .message-markdown table th,
.message-item.user .message-markdown table td {
  border-color: rgba(102, 126, 234, 0.3);
}

.message-item.user .message-content {
  background: #667eea;
  color: white;
  border-bottom-right-radius: 4px;
}

.message-item.assistant .message-content {
  background: #f0f0f0;
  color: #333;
  border-bottom-left-radius: 4px;
}

.message-item.assistant .message-markdown strong {
  color: #333;
}

.message-item.user .message-markdown strong {
  color: white;
}

.message-item.user .message-markdown table {
  color: white;
}

.message-item.user .message-markdown table th {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.message-item.user .message-markdown table td {
  color: white;
  border-color: rgba(255, 255, 255, 0.3);
}

.message-item.user .message-markdown table tbody tr:nth-child(even) {
  background: rgba(255, 255, 255, 0.1);
}

.message-item.user .message-markdown table tbody tr:hover {
  background: rgba(255, 255, 255, 0.15);
}

.loading-indicator {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #999;
  font-size: 14px;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid #f0f0f0;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.chat-input-area {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  background: white;
  border-top: 1px solid #eee;
}

.input-field {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
  max-height: 120px;
  min-height: 44px;
  transition: border-color 0.3s ease;
}

.input-field:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.input-field:disabled {
  background: #f5f5f5;
  color: #ccc;
}

.send-btn {
  height: 40px;
  padding: 0 22px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: 1px solid rgba(102, 126, 234, 0.3);
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: transform 0.2s ease, box-shadow 0.2s ease, opacity 0.2s ease;
  white-space: nowrap;
  align-self: center;
  box-shadow: 0 6px 14px rgba(118, 75, 162, 0.25);
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 10px 18px rgba(118, 75, 162, 0.32);
}

.send-btn:disabled {
  background: #e5e7eb;
  border-color: #e5e7eb;
  color: #9ca3af;
  box-shadow: none;
  cursor: not-allowed;
  opacity: 0.9;
}

/* 滚动条美化 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: #f0f0f0;
  border-radius: 10px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #667eea;
  border-radius: 10px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #764ba2;
}

/* v-html渲染内容的表格样式（确保显示） */
:deep(table) {
  border-collapse: collapse !important;
  margin: 12px 0 !important;
  font-size: 13px !important;
  width: 100% !important;
  display: table !important;
}

:deep(thead) {
  display: table-header-group !important;
  background: #f0f0f0 !important;
}

:deep(tbody) {
  display: table-row-group !important;
}

:deep(tr) {
  display: table-row !important;
}

:deep(th) {
  padding: 8px 12px !important;
  border: 1px solid #ddd !important;
  text-align: left !important;
  font-weight: 600 !important;
  color: #333 !important;
  display: table-cell !important;
}

:deep(td) {
  padding: 8px 12px !important;
  border: 1px solid #ddd !important;
  color: #666 !important;
  display: table-cell !important;
}

:deep(tbody tr:nth-child(even)) {
  background: #fafafa !important;
}

:deep(hr) {
  display: block !important;
  margin: 12px 0 !important;
  border: none !important;
  border-top: 1px solid #ddd !important;
}

:deep(h3) {
  font-size: 14px !important;
  margin: 8px 0 !important;
}

:deep(p) {
  margin: 8px 0 !important;
}
</style>
