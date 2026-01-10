<template>
  <div class="bill-import-container">
    <div class="page-header">
      <h1>账单导入</h1>
      <p class="subtitle">上传账单截图，智能识别交易信息</p>
    </div>

      <!-- 错误信息提示 -->
      <div v-if="errorMessage" class="alert alert-error">
        <span>{{ errorMessage }}</span>
        <button @click="errorMessage = ''" class="close-btn">×</button>
      </div>

      <!-- 成功提示 -->
      <div v-if="successMessage" class="alert alert-success">
        <span>{{ successMessage }}</span>
        <button @click="successMessage = ''" class="close-btn">×</button>
      </div>

      <!-- 步骤指示器 -->
      <div class="steps">
        <div :class="['step', { active: currentStep >= 1, completed: currentStep > 1 }]">
          <div class="step-number">1</div>
          <div class="step-label">上传图片</div>
        </div>
        <div class="step-divider"></div>
        <div :class="['step', { active: currentStep >= 2, completed: currentStep > 2 }]">
          <div class="step-number">2</div>
          <div class="step-label">确认信息</div>
        </div>
        <div class="step-divider"></div>
        <div :class="['step', { active: currentStep >= 3 }]">
          <div class="step-number">3</div>
          <div class="step-label">导入完成</div>
        </div>
      </div>

      <!-- 第一步：上传图片 -->
      <div v-if="currentStep === 1" class="step-content">
        <Card>
          <div class="upload-section">
            <div class="upload-area" @click="triggerFileInput" :class="{ 'dragging': isDragging }">
              <input 
                ref="fileInput" 
                type="file" 
                accept="image/*" 
                @change="handleFileSelect"
                @dragover.prevent="isDragging = true"
                @dragleave.prevent="isDragging = false"
                @drop.prevent="handleFileDrop"
                style="display: none"
              />
              
              <div v-if="!selectedFile" class="upload-hint">
                <svg class="upload-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <path d="M12 2v16m8-8H4m-2 6h20"/>
                </svg>
                <h3>点击或拖拽上传图片</h3>
                <p>支持 JPG、PNG 等常见图片格式</p>
              </div>

              <div v-else class="upload-preview">
                <img :src="previewUrl" alt="上传预览" />
                <p class="file-name">{{ selectedFile.name }}</p>
              </div>
            </div>

            <div class="upload-options">
              <label class="form-group">
                <span class="label-text">账单类型（可选）：</span>
                <Select v-model="accountType" class="account-type-select">
                  <option value="">自动识别</option>
                  <option value="ALIPAY">支付宝</option>
                  <option value="WECHAT">微信</option>
                  <option value="BANK">银行卡</option>
                </Select>
              </label>
            </div>

            <div class="button-group">
              <button 
                @click="recognizeBill" 
                :disabled="!selectedFile || isRecognizing"
                class="btn btn-primary"
              >
                <span v-if="!isRecognizing">识别账单</span>
                <span v-else>识别中...</span>
              </button>
              <button 
                @click="resetUpload" 
                class="btn btn-secondary"
              >
                清除
              </button>
            </div>
          </div>
        </Card>
      </div>

      <!-- 第二步：确认信息 -->
      <div v-if="currentStep === 2" class="step-content">
        <Card>
          <div class="confirm-section">
            <h2>请确认识别的账单信息</h2>
            <p class="confirm-hint">您可以编辑、删除错误的记录，或添加遗漏的交易</p>

            <div v-if="recognizedRecords.length === 0" class="empty-state">
              <p>未识别到任何账单信息</p>
            </div>

            <div v-else class="table-wrapper">
              <table class="confirm-table">
                <thead>
                  <tr>
                    <th width="10%">操作</th>
                    <th width="10%">类型</th>
                    <th width="15%">金额</th>
                    <th width="15%">日期</th>
                    <th width="20%">分类</th>
                    <th width="30%">说明</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(record, index) in recognizedRecords" :key="index" class="editable-row">
                    <td class="action-cell">
                      <button @click="deleteRecord(index)" class="btn-delete" title="删除">×</button>
                    </td>
                    <td>
                      <Select v-model="record.type" class="inline-select">
                        <option value="INCOME">收入</option>
                        <option value="EXPENSE">支出</option>
                      </Select>
                    </td>
                    <td>
                      <Input 
                        v-model="record.amount" 
                        type="number" 
                        step="0.01" 
                        class="inline-input"
                      />
                    </td>
                    <td>
                      <Input 
                        v-model="record.transactionDate" 
                        type="date" 
                        class="inline-input"
                      />
                    </td>
                    <td>
                      <Select v-model="record.categoryId" class="inline-select">
                        <option value="">-- 选择分类 --</option>
                        <optgroup v-if="record.type === 'INCOME'" label="收入分类">
                          <option v-for="cat in incomeCategories" :key="cat.id" :value="cat.id">
                            {{ cat.name }}
                          </option>
                        </optgroup>
                        <optgroup v-if="record.type === 'EXPENSE'" label="支出分类">
                          <option v-for="cat in expenseCategories" :key="cat.id" :value="cat.id">
                            {{ cat.name }}
                          </option>
                        </optgroup>
                      </Select>
                    </td>
                    <td>
                      <Input 
                        v-model="record.description" 
                        class="inline-input"
                      />
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div class="add-record-section">
              <h3>添加更多记录</h3>
              <div class="form-row">
                <label class="form-group" style="flex: 1">
                  <span class="label-text">类型:</span>
                  <Select v-model="newRecord.type" class="form-input">
                    <option value="">-- 选择 --</option>
                    <option value="INCOME">收入</option>
                    <option value="EXPENSE">支出</option>
                  </Select>
                </label>
                <label class="form-group" style="flex: 1">
                  <span class="label-text">金额:</span>
                  <Input 
                    v-model="newRecord.amount" 
                    type="number" 
                    step="0.01"
                    placeholder="0.00"
                    class="form-input"
                  />
                </label>
                <label class="form-group" style="flex: 1">
                  <span class="label-text">日期:</span>
                  <Input 
                    v-model="newRecord.transactionDate" 
                    type="date"
                    class="form-input"
                  />
                </label>
              </div>
              <div class="form-row">
                <label class="form-group" style="flex: 1">
                  <span class="label-text">分类:</span>
                  <Select v-model="newRecord.categoryId" class="form-input">
                    <option value="">-- 选择分类 --</option>
                    <optgroup v-if="newRecord.type === 'INCOME'" label="收入分类">
                      <option v-for="cat in incomeCategories" :key="cat.id" :value="cat.id">
                        {{ cat.name }}
                      </option>
                    </optgroup>
                    <optgroup v-if="newRecord.type === 'EXPENSE'" label="支出分类">
                      <option v-for="cat in expenseCategories" :key="cat.id" :value="cat.id">
                        {{ cat.name }}
                      </option>
                    </optgroup>
                  </Select>
                </label>
                <label class="form-group" style="flex: 2">
                  <span class="label-text">说明:</span>
                  <Input 
                    v-model="newRecord.description" 
                    placeholder="可选"
                    class="form-input"
                  />
                </label>
                <button @click="addNewRecord" class="btn btn-add">+ 添加</button>
              </div>
            </div>

            <div class="button-group">
              <button @click="goBack" class="btn btn-secondary">上一步</button>
              <button 
                @click="confirmImport" 
                :disabled="recognizedRecords.length === 0 || isConfirming"
                class="btn btn-primary"
              >
                <span v-if="!isConfirming">确认导入</span>
                <span v-else>导入中...</span>
              </button>
            </div>
          </div>
        </Card>
      </div>

      <!-- 第三步：导入完成 -->
      <div v-if="currentStep === 3" class="step-content">
        <Card>
          <div class="success-section">
            <div class="success-icon">✓</div>
            <h2>导入成功！</h2>
            <div class="success-stats">
              <div class="stat">
                <div class="stat-value">{{ importResult.importedIds.length }}</div>
                <div class="stat-label">条记录已导入</div>
              </div>
            </div>
            <p class="success-message">账单信息已成功导入系统，您可以在相应的管理页面查看和编辑</p>

            <div class="button-group">
              <button @click="navigateToExpense" class="btn btn-primary">查看支出</button>
              <button @click="navigateToIncome" class="btn btn-secondary">查看收入</button>
              <button @click="startOver" class="btn btn-secondary">导入新文件</button>
            </div>
          </div>
        </Card>
      </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'
import Card from '../components/Card.vue'
import Input from '../components/Input.vue'
import Select from '../components/Select.vue'
import { billImportAPI, incomeCategoryAPI, expenseCategoryAPI } from '../api'

const router = useRouter()

// 当前步骤
const currentStep = ref(1)

// 文件和图片处理
const fileInput = ref(null)
const selectedFile = ref(null)
const previewUrl = ref(null)
const isDragging = ref(false)

// 账单类型选择
const accountType = ref('')

// 识别状态
const isRecognizing = ref(false)
const isConfirming = ref(false)

// 识别的记录
const recognizedRecords = ref([])

// 新增记录表单
const newRecord = ref({
  type: '',
  amount: '',
  transactionDate: '',
  categoryId: '',
  description: ''
})

// 分类列表
const incomeCategories = ref([])
const expenseCategories = ref([])

// 导入结果
const importResult = ref(null)

// 提示信息
const errorMessage = ref('')
const successMessage = ref('')

// 触发文件输入
const triggerFileInput = () => {
  fileInput.value?.click()
}

// 处理文件选择
const handleFileSelect = (e) => {
  const files = e.target.files
  if (files && files.length > 0) {
    processFile(files[0])
  }
}

// 处理文件拖拽
const handleFileDrop = (e) => {
  isDragging.value = false
  const files = e.dataTransfer.files
  if (files && files.length > 0) {
    processFile(files[0])
  }
}

// 处理文件
const processFile = (file) => {
  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    errorMessage.value = '请选择图片文件'
    return
  }

  // 验证文件大小（最大10MB）
  if (file.size > 10 * 1024 * 1024) {
    errorMessage.value = '图片文件不能超过10MB'
    return
  }

  selectedFile.value = file
  errorMessage.value = ''

  // 生成预览
  const reader = new FileReader()
  reader.onload = (e) => {
    previewUrl.value = e.target.result
  }
  reader.readAsDataURL(file)
}

// 重置上传
const resetUpload = () => {
  selectedFile.value = null
  previewUrl.value = null
  accountType.value = ''
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

// 识别账单
const recognizeBill = async () => {
  if (!selectedFile.value) {
    errorMessage.value = '请先选择图片'
    return
  }

  isRecognizing.value = true
  errorMessage.value = ''

  try {
    // 将图片转换为Base64
    const reader = new FileReader()
    reader.onload = async (e) => {
      try {
        const base64Image = e.target.result.split(',')[1]
        
        console.log('开始识别账单，图片大小:', base64Image.length, '字节')
        console.log('账单类型:', accountType.value || '未指定')
        
        const response = await billImportAPI.recognize(base64Image, accountType.value)
        
        console.log('API响应:', response)

        if (response.code === 200) {
          console.log('识别成功，记录数:', response.data.records.length)
          
          // 格式化识别到的记录
          recognizedRecords.value = response.data.records.map(record => ({
            type: record.type,
            amount: record.amount,
            transactionDate: record.transactionDate,
            categoryName: record.categoryName,
            categoryId: '',
            description: record.description || ''
          }))

          currentStep.value = 2
          await loadCategories()
        } else {
          const errorMsg = response.msg || '识别失败'
          console.error('识别失败:', errorMsg)
          errorMessage.value = `识别失败: ${errorMsg}`
        }
      } catch (error) {
        console.error('识别请求错误:', error)
        console.error('错误详情:', {
          message: error.message,
          response: error.response,
          data: error.response?.data
        })
        
        const errorMsg = error.response?.data?.msg || error.message || '识别请求失败'
        errorMessage.value = `识别失败: ${errorMsg}`
      } finally {
        isRecognizing.value = false
      }
    }
    
    reader.onerror = () => {
      console.error('文件读取失败')
      errorMessage.value = '文件读取失败，请重试'
      isRecognizing.value = false
    }
    
    reader.readAsDataURL(selectedFile.value)
  } catch (error) {
    console.error('处理图片失败:', error)
    errorMessage.value = '处理图片失败: ' + error.message
    isRecognizing.value = false
  }
}

// 加载分类
const loadCategories = async () => {
  try {
    console.log('开始加载分类...')
    const [incomeResp, expenseResp] = await Promise.all([
      incomeCategoryAPI.getList(),
      expenseCategoryAPI.getList()
    ])

    console.log('收入分类API响应:', incomeResp)
    console.log('支出分类API响应:', expenseResp)

    if (incomeResp.code === 200) {
      incomeCategories.value = incomeResp.data || []
      console.log('已加载收入分类:', incomeCategories.value)
      if (incomeCategories.value.length > 0) {
        console.log('第一条收入分类详情:', incomeCategories.value[0])
      }
    }
    if (expenseResp.code === 200) {
      expenseCategories.value = expenseResp.data || []
      console.log('已加载支出分类:', expenseCategories.value)
      if (expenseCategories.value.length > 0) {
        console.log('第一条支出分类详情:', expenseCategories.value[0])
      }
    }
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

// 删除记录
const deleteRecord = (index) => {
  recognizedRecords.value.splice(index, 1)
}

// 添加新记录
const addNewRecord = () => {
  if (!newRecord.value.type || !newRecord.value.amount || !newRecord.value.transactionDate || !newRecord.value.categoryId) {
    errorMessage.value = '请填写完整的记录信息'
    return
  }

  recognizedRecords.value.push({
    ...newRecord.value,
    amount: parseFloat(newRecord.value.amount)
  })

  newRecord.value = {
    type: '',
    amount: '',
    transactionDate: '',
    categoryId: '',
    description: ''
  }
  successMessage.value = '记录已添加'
}

// 返回上一步
const goBack = () => {
  currentStep.value = 1
  resetUpload()
}

// 确认导入
const confirmImport = async () => {
  // 验证所有记录的分类都已选择
  const hasEmptyCategory = recognizedRecords.value.some(r => !r.categoryId)
  if (hasEmptyCategory) {
    errorMessage.value = '所有记录的分类必须选择'
    return
  }

  isConfirming.value = true
  errorMessage.value = ''

  try {
    const records = recognizedRecords.value.map(r => ({
      type: r.type,
      amount: r.amount,
      transactionDate: r.transactionDate,
      categoryId: r.categoryId,
      description: r.description
    }))

    console.log('确认导入，发送数据:', records)
    const response = await billImportAPI.confirm(records)

    console.log('导入响应:', response)
    if (response.code === 200) {
      importResult.value = response.data
      currentStep.value = 3
    } else {
      errorMessage.value = response.msg || '导入失败'
    }
  } catch (error) {
    console.error('导入错误:', error)
    errorMessage.value = error.response?.data?.msg || '导入请求失败'
  } finally {
    isConfirming.value = false
  }
}

// 导航到支出页面
const navigateToExpense = () => {
  router.push('/expense')
}

// 导航到收入页面
const navigateToIncome = () => {
  router.push('/income')
}

// 重新开始
const startOver = () => {
  currentStep.value = 1
  selectedFile.value = null
  previewUrl.value = null
  recognizedRecords.value = []
  accountType.value = ''
  importResult.value = null
  newRecord.value = {
    type: '',
    amount: '',
    transactionDate: '',
    categoryId: '',
    description: ''
  }
}
</script>

<style scoped>
.bill-import-container {
  width: 100%;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px 0;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
  padding: 0 20px;
}

.page-header h1 {
  font-size: 32px;
  color: #333;
  margin: 0 0 10px 0;
}

.subtitle {
  color: #666;
  font-size: 16px;
  margin: 0;
}

/* 警告框 */
.alert {
  margin: 0 auto 20px;
  max-width: 900px;
  padding: 15px 20px;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.alert-error {
  background-color: #fee;
  color: #c33;
  border: 1px solid #fcc;
}

.alert-success {
  background-color: #efe;
  color: #3c3;
  border: 1px solid #cfc;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: inherit;
  padding: 0;
  line-height: 1;
}

/* 步骤指示器 */
.steps {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 40px;
  padding: 0 20px;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.step-number {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #ddd;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  transition: all 0.3s;
}

.step.active .step-number {
  background: #007bff;
  color: white;
}

.step.completed .step-number {
  background: #28a745;
  color: white;
}

.step-label {
  font-size: 14px;
  color: #999;
  text-align: center;
  min-width: 60px;
}

.step.active .step-label {
  color: #007bff;
  font-weight: bold;
}

.step-divider {
  flex: 1;
  max-width: 60px;
  height: 2px;
  background: #ddd;
  margin: 0 20px;
  transform: translateY(-24px);
}

/* 步骤内容 */
.step-content {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 上传部分 */
.upload-section {
  padding: 30px;
}

.upload-area {
  border: 2px dashed #ddd;
  border-radius: 8px;
  padding: 40px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 20px;
}

.upload-area:hover {
  border-color: #007bff;
  background: #f9f9f9;
}

.upload-area.dragging {
  border-color: #007bff;
  background: #e3f2fd;
}

.upload-icon {
  width: 60px;
  height: 60px;
  color: #ddd;
  margin-bottom: 15px;
}

.upload-hint h3 {
  color: #333;
  margin: 10px 0;
}

.upload-hint p {
  color: #999;
  margin: 0;
}

.upload-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.upload-preview img {
  max-width: 300px;
  max-height: 300px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.file-name {
  color: #666;
  font-size: 14px;
  margin: 0;
  word-break: break-all;
}

.upload-options {
  margin: 20px 0;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
}

.form-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.label-text {
  color: #333;
  font-weight: 500;
  min-width: 120px;
}

.account-type-select {
  flex: 1;
  max-width: 200px;
}

/* 确认部分 */
.confirm-section {
  padding: 30px;
}

.confirm-section h2 {
  color: #333;
  margin-top: 0;
}

.confirm-hint {
  color: #666;
  font-size: 14px;
  margin: 10px 0 20px 0;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
}

.table-wrapper {
  overflow-x: auto;
  margin-bottom: 30px;
  border: 1px solid #ddd;
  border-radius: 8px;
}

.confirm-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
}

.confirm-table th {
  background: #f9f9f9;
  border-bottom: 1px solid #ddd;
  padding: 12px;
  text-align: left;
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.confirm-table td {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}

.confirm-table tbody tr:hover {
  background: #f9f9f9;
}

.action-cell {
  text-align: center;
}

.btn-delete {
  background: #ff6b6b;
  color: white;
  border: none;
  border-radius: 4px;
  width: 32px;
  height: 32px;
  font-size: 18px;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-delete:hover {
  background: #ff5252;
}

.inline-select,
.inline-input {
  width: 100%;
  padding: 6px 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.inline-select:focus,
.inline-input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
}

.add-record-section {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.add-record-section h3 {
  color: #333;
  margin-top: 0;
}

.form-row {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
  align-items: flex-end;
}

.form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
}

.btn-add {
  padding: 8px 20px;
  background: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s;
  white-space: nowrap;
}

.btn-add:hover {
  background: #218838;
}

/* 成功部分 */
.success-section {
  padding: 60px 30px;
  text-align: center;
}

.success-icon {
  font-size: 64px;
  color: #28a745;
  margin-bottom: 20px;
}

.success-section h2 {
  color: #333;
  font-size: 28px;
  margin: 0 0 30px 0;
}

.success-stats {
  display: flex;
  justify-content: center;
  gap: 40px;
  margin-bottom: 30px;
}

.stat {
  background: #f9f9f9;
  padding: 20px 40px;
  border-radius: 8px;
  min-width: 150px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #007bff;
  margin-bottom: 5px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.success-message {
  color: #666;
  font-size: 16px;
  margin-bottom: 30px;
  line-height: 1.6;
}

/* 按钮组 */
.button-group {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 30px;
}

.btn {
  padding: 12px 30px;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background: #007bff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #0056b3;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 123, 255, 0.3);
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover:not(:disabled) {
  background: #545b62;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(108, 117, 125, 0.3);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header h1 {
    font-size: 24px;
  }

  .steps {
    flex-wrap: wrap;
  }

  .step-divider {
    display: none;
  }

  .upload-area {
    padding: 20px;
  }

  .upload-icon {
    width: 40px;
    height: 40px;
  }

  .table-wrapper {
    font-size: 12px;
  }

  .confirm-table th,
  .confirm-table td {
    padding: 8px 6px;
  }

  .form-row {
    flex-direction: column;
  }

  .button-group {
    flex-direction: column-reverse;
  }

  .btn {
    width: 100%;
  }
}
</style>
