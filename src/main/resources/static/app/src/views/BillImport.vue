<template>
  <div class="bill-import-container">
    <div class="page-header">
      <h1>AI账单导入</h1>
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
                  <path d="M12 2v16m8-8H4"/>
                </svg>
                <h3>点击或拖拽上传图片</h3>
                <p>支持 JPG、PNG 等常见图片格式</p>
              </div>

              <div v-else class="upload-preview">
                <img :src="previewUrl" alt="上传预览" />
                <p class="file-name">{{ selectedFile.name }}</p>
              </div>
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

            <div v-else>
              <!-- 状态说明 -->
              <div class="status-legend">
                <div class="legend-item">
                  <span class="legend-color auto-matched-color"></span>
                  <span class="legend-text">自动匹配成功</span>
                </div>
                <div class="legend-item">
                  <span class="legend-color manual-match-color"></span>
                  <span class="legend-text">待手动匹配</span>
                </div>
                <div class="legend-item">
                  <span class="legend-color manually-added-color"></span>
                  <span class="legend-text">手动添加</span>
                </div>
              </div>

              <div class="table-wrapper">
              <table class="confirm-table">
                <thead>
                  <tr>
                    <th width="5%">操作</th>
                    <th width="12%">类型</th>
                    <th width="12%">金额</th>
                    <th width="12%">日期</th>
                    <th width="12%">分类</th>
                    <th width="8%">说明</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(record, index) in recognizedRecords" :key="index" class="editable-row" :class="getRowClass(record)">
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
                      <div class="category-cell">
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
                      </div>
                    </td>
                    <td>
                      <div class="description-cell" @click="openDescriptionModal(index, record)">
                        <span class="description-preview">{{ record.description || '(无)' }}</span>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
              </div>
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

  <!-- 说明编辑模态框 -->
  <Modal 
    v-model="showDescriptionModal"
    title="账单详情"
  >
    <div class="modal-content">
      <!-- 账单详情显示 -->
      <div class="bill-details">
        <div class="detail-row">
          <span class="detail-label">交易类型:</span>
          <span class="detail-value">{{ currentEditRecord?.type === 'INCOME' ? '收入' : '支出' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">金额:</span>
          <span class="detail-value">{{ currentEditRecord?.amount }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">日期:</span>
          <span class="detail-value">{{ currentEditRecord?.transactionDate }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">分类:</span>
          <span class="detail-value">{{ currentEditRecord?.categoryName }}</span>
        </div>
      </div>

      <!-- 说明编辑区域 -->
      <div class="description-edit-section">
        <label class="description-label">说明信息:</label>
        <Textarea 
          v-model="currentEditRecord.description"
          placeholder="输入说明信息..."
          class="description-textarea"
        />
      </div>
    </div>
    <template #footer>
      <button @click="showDescriptionModal = false" class="btn btn-secondary">取消</button>
      <button @click="saveDescription" class="btn btn-primary">保存</button>
    </template>
  </Modal>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'
import Card from '../components/Card.vue'
import Input from '../components/Input.vue'
import Select from '../components/Select.vue'
import Modal from '../components/Modal.vue'
import Textarea from '../components/Textarea.vue'
import { billImportAPI, incomeCategoryAPI, expenseCategoryAPI } from '../api'

const router = useRouter()

// 当前步骤
const currentStep = ref(1)

// 文件和图片处理
const fileInput = ref(null)
const selectedFile = ref(null)
const previewUrl = ref(null)
const isDragging = ref(false)

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

// 说明编辑模态框
const showDescriptionModal = ref(false)
const currentEditRecord = ref({})
const currentEditIndex = ref(-1)

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
        
        // 获取当前日期，格式为 YYYY-MM-DD
        const today = new Date()
        const currentDate = today.toISOString().split('T')[0]
        console.log('当前日期:', currentDate)
        
        const response = await billImportAPI.recognize(base64Image, currentDate)
        
        console.log('API响应:', response)

        if (response.code === 200) {
          console.log('识别成功，记录数:', response.data.records.length)
          
          // 加载分类
          await loadCategories()
          
          // 格式化识别到的记录，并尝试自动匹配分类
          recognizedRecords.value = response.data.records.map(record => {
            let categoryId = ''
            let isMatched = false
            const cleanCategoryName = record.categoryName?.trim() || ''
            
            // 如果后端返回了categoryId，直接使用
            if (record.categoryId) {
              categoryId = record.categoryId
              // 检查是否是"待分类"（需要找到这个分类的ID来确认）
              const categories = record.type === 'INCOME' ? incomeCategories.value : expenseCategories.value
              const unclassifiedCategory = categories.find(cat => cat.name === '待分类')
              // 如果分配的分类ID是待分类的ID，则不标记为已匹配
              isMatched = !(unclassifiedCategory && categoryId === unclassifiedCategory.id)
              console.log(`记录自动匹配分类: ${cleanCategoryName} -> ID: ${categoryId}, 已匹配: ${isMatched}, 待分类ID: ${unclassifiedCategory?.id}`)
            } else {
              // 否则尝试手动匹配
              const categories = record.type === 'INCOME' ? incomeCategories.value : expenseCategories.value
              const matched = categories.find(cat => cat.name === cleanCategoryName)
              if (matched) {
                categoryId = matched.id
                isMatched = true
                console.log(`记录手动匹配分类: ${cleanCategoryName} -> ID: ${categoryId}`)
              } else {
                // 查找"待分类"分类
                const unclassified = categories.find(cat => cat.name === '待分类')
                if (unclassified) {
                  categoryId = unclassified.id
                  isMatched = false
                  console.log(`记录无法匹配，使用待分类: ${cleanCategoryName} -> ID: ${categoryId}`)
                }
              }
            }
            
            return {
              type: record.type,
              amount: record.amount,
              transactionDate: record.transactionDate,
              categoryName: cleanCategoryName,
              categoryId: categoryId,
              description: record.description || '',
              isMatched: isMatched, // 仅当真正匹配到非待分类分类时才标记为已匹配
              isManuallyAdded: false // 标记是否手动添加
            }
          })

          // 对识别结果进行排序：待分类的在前，已匹配的在后，手动添加的在最后
          recognizedRecords.value.sort((a, b) => {
            // 待分类（isMatched === false）排在最前
            if (!a.isMatched && b.isMatched) return -1
            if (a.isMatched && !b.isMatched) return 1
            
            // 手动添加的排在最后
            if (!a.isManuallyAdded && b.isManuallyAdded) return -1
            if (a.isManuallyAdded && !b.isManuallyAdded) return 1
            
            return 0
          })

          // 检查是否有记录自动匹配了
          const matchedCount = recognizedRecords.value.filter(r => r.categoryId).length
          
          if (matchedCount > 0) {
            // 有记录已自动匹配，显示提示
            successMessage.value = `已自动匹配 ${matchedCount} 条记录，请确认信息后导入`
          }
          
          // 无论是否自动匹配，都进入确认步骤，让用户人工审核
          currentStep.value = 2
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
    amount: parseFloat(newRecord.value.amount),
    isMatched: false,
    isManuallyAdded: true // 标记为手动添加
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
  successMessage.value = ''

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
      successMessage.value = `成功导入 ${response.data.importedIds?.length || 0} 条记录`
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
  importResult.value = null
  newRecord.value = {
    type: '',
    amount: '',
    transactionDate: '',
    categoryId: '',
    description: ''
  }
}

// 获取行的CSS class
const getRowClass = (record) => {
  if (record.isManuallyAdded) {
    return 'manually-added'
  } else if (record.isMatched) {
    return 'auto-matched'
  } else {
    return 'manual-match'
  }
}

// 打开说明编辑模态框
const openDescriptionModal = (index, record) => {
  console.log('打开模态框:', index, record)
  currentEditIndex.value = index
  currentEditRecord.value = { ...record }
  showDescriptionModal.value = true
  console.log('showDescriptionModal.value:', showDescriptionModal.value)
}

// 保存说明
const saveDescription = () => {
  if (currentEditIndex.value >= 0 && currentEditRecord.value) {
    recognizedRecords.value[currentEditIndex.value].description = currentEditRecord.value.description
  }
  showDescriptionModal.value = false
}
</script>

<style scoped>
.bill-import-container {
  width: 100%;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px 12px;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
  padding: 0 12px;
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
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

/* 上传部分 */
.upload-section {
  padding: 24px 20px;
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

/* 确认部分 */
.confirm-section {
  padding: 24px 20px;
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

/* 状态图例 */
.status-legend {
  display: flex;
  gap: 24px;
  margin-bottom: 20px;
  padding: 12px 0;
  font-size: 13px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 2px;
}

.auto-matched-color {
  background-color: #d4edda;
  border: 1px solid #28a745;
}

.manual-match-color {
  background-color: #f8d7da;
  border: 1px solid #f5c6cb;
}

.manually-added-color {
  background-color: #e2e3e5;
  border: 1px solid #d3d3d3;
}

.legend-text {
  color: #555;
  font-weight: 500;
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
  table-layout: fixed;
}

.confirm-table th {
  background: #f5f5f5;
  border-bottom: 2px solid #e0e0e0;
  padding: 10px 8px;
  text-align: center;
  font-weight: 600;
  color: #333;
  font-size: 13px;
  height: auto;
  vertical-align: middle;
}

.confirm-table td {
  padding: 6px 4px;
  border-bottom: 1px solid #e8e8e8;
  font-size: 14px;
  vertical-align: middle;
  height: 38px;
}

.confirm-table tbody tr:hover {
  background: #f9f9f9;
}

/* 说明单元格 */
.description-cell {
  cursor: pointer;
  padding: 6px 8px;
  border-radius: 4px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  height: 100%;
  background: white;
  border: 1px solid #ddd;
  box-sizing: border-box;
  min-height: 32px;
  overflow: hidden;
}

.description-cell:hover {
  background-color: white;
  border-color: #999;
}

.description-preview {
  display: block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 13px;
  padding: 0;
  color: #333;
  white-space: nowrap;
  color: #333;
  font-size: 14px;
  line-height: 1.5;
}

.editable-row {
  transition: background-color 0.3s;
}

/* 自动匹配成功 - 绿色 */
.editable-row.auto-matched {
  background-color: #d4edda !important;
}

.editable-row.auto-matched:hover {
  background-color: #c3e6cb !important;
}

/* 待手动匹配 - 粉红色 */
.editable-row.manual-match {
  background-color: #f8d7da !important;
}

.editable-row.manual-match:hover {
  background-color: #f5c6cb !important;
}

/* 手动添加 - 灰色 */
.editable-row.manually-added {
  background-color: #e2e3e5 !important;
}

.editable-row.manually-added:hover {
  background-color: #d3d3d3 !important;
}

.category-cell {
  display: flex;
  align-items: center;
  gap: 0;
  position: relative;
  width: 100%;
}

.action-cell {
  text-align: center;
  padding: 0 !important;
  margin: 0 !important;
  min-width: 36px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-bottom: none !important;
  width: 100%;
  box-sizing: border-box;
}

.btn-delete {
  background: #ff5252;
  color: white;
  border: none;
  border-radius: 3px;
  width: 28px;
  height: 28px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.2s;
  padding: 0;
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.btn-delete:hover {
  background: #ff1744;
  transform: scale(1.05);
}

.inline-select,
.inline-input {
  width: 100%;
  padding: 6px 8px;
  height: 100%;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 13px;
  line-height: 1.4;
  box-sizing: border-box;
  background: white;
  transition: all 0.2s;
  color: #333;
  min-height: 32px;
}

.inline-select {
  cursor: pointer;
}

.inline-input {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.inline-select:hover,
.inline-input:hover {
  border-color: #999;
}

.inline-select:focus,
.inline-input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.1);
}

.add-record-section {
  background: #f9f9f9;
  padding: 20px 16px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.add-record-section h3 {
  color: #333;
  margin: 0 0 20px 0;
  font-size: 16px;
  font-weight: 600;
}

.form-row {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
  align-items: flex-end;
  flex-wrap: wrap;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.label-text {
  font-size: 13px;
  font-weight: 500;
  color: #555;
  min-width: 50px;
}

.form-input {
  width: 100%;
  padding: 8px 12px;
  min-height: 36px;
  border: none;
  border-radius: 0;
  font-size: 14px;
  line-height: 1.5;
  box-sizing: border-box;
  background: transparent;
}

.form-input:focus {
  outline: none;
  border-bottom: 2px solid #007bff;
}

.btn-add {
  padding: 8px 24px;
  min-height: 36px;
  background: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s;
  white-space: nowrap;
  font-size: 14px;
  font-weight: 500;
  align-self: flex-end;
}

.btn-add:hover {
  background: #218838;
}

/* 成功部分 */
.success-section {
  padding: 60px 20px;
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

/* 模态框样式 */
.modal-content {
  padding: 20px 0;
}

.description-textarea {
  width: 100%;
  min-height: 150px;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  resize: vertical;
  box-sizing: border-box;
}

.description-textarea:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
}

/* 账单详情显示 */
.bill-details {
  background: #f9f9f9;
  padding: 16px;
  border-radius: 6px;
  margin-bottom: 20px;
  border-left: 4px solid #007bff;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
  border-bottom: 1px solid #e0e0e0;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-label {
  font-weight: 600;
  color: #555;
  min-width: 80px;
}

.detail-value {
  color: #333;
  text-align: right;
  flex: 1;
  padding-left: 12px;
}

/* 说明编辑区域 */
.description-edit-section {
  margin-bottom: 20px;
}

.description-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #555;
  margin-bottom: 8px;
}

.modal-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
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
