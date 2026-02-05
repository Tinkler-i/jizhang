<template>
  <div class="page">
    <div class="page-header">
      <h1>预算管理</h1>
      <div class="header-actions">
        <Input
          v-model="currentMonth"
          type="month"
          @change="loadBudgets"
        />
        <Button type="primary" @click="openAddModal">+ 添加预算</Button>
      </div>
    </div>

    <!-- 预算卡片列表 -->
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="budgets.length === 0" class="empty-state">
      <p>暂无预算记录</p>
      <Button type="primary" @click="openAddModal">添加预算</Button>
    </div>
    <div v-else class="budget-grid">
      <Card v-for="budget in budgets" :key="budget.id" class="budget-card">
        <template #header>
          <div class="budget-header">
            <h3>{{ getCategoryName(budget.categoryId) }}</h3>
            <div class="budget-actions">
              <button class="link-btn" @click="editBudget(budget)">编辑</button>
              <button class="link-btn danger" @click="deleteBudget(budget.id)">删除</button>
            </div>
          </div>
        </template>
        <div class="budget-progress">
          <div class="progress-info">
            <span>预算: ¥ {{ (budget.budgetAmount || budget.amount || 0).toFixed(2) }}</span>
            <span class="actual">已用: ¥ {{ (budget.spentAmount || budget.actualAmount || 0).toFixed(2) }}</span>
          </div>
          <div class="progress-bar">
            <div
              class="progress-fill"
              :style="{ width: getProgressPercentage(budget) + '%' }"
            ></div>
          </div>
          <div class="progress-percentage">{{ getProgressPercentage(budget) }}%</div>
        </div>
      </Card>
    </div>

    <!-- 添加/编辑模态框 -->
    <Modal
      v-model="showModal"
      :title="editingId ? '编辑预算' : '添加预算'"
      size="md"
    >
      <Select
        v-model="form.categoryId"
        label="分类"
        placeholder="请选择分类"
        :options="categoryOptions"
        required
      />
      <Input
        v-model="form.amount"
        type="number"
        label="预算金额"
        placeholder="请输入预算金额"
        step="0.01"
        min="0"
        required
      />
      <Input
        v-model="form.budgetMonth"
        type="month"
        label="起始月份"
        required
      />
      <div v-if="!editingId" class="form-group">
        <label>应用到多少个月</label>
        <div class="month-selector">
          <Input
            v-model.number="form.monthCount"
            type="number"
            min="1"
            max="12"
            placeholder="1"
            style="flex: 1"
          />
          <span class="month-info">月（从 {{ form.budgetMonth }} 开始）</span>
        </div>
      </div>
      <template #footer>
        <Button type="secondary" @click="showModal = false">取消</Button>
        <Button type="primary" @click="handleSave">
          {{ editingId ? '保存' : `创建 ${form.monthCount} 个月的预算` }}
        </Button>
      </template>
    </Modal>

    <!-- 预算冲突确认对话框 -->
    <Modal
      v-model="showConfirmDialog"
      title="预算已存在"
      size="md"
    >
      <div class="conflict-content">
        <p class="conflict-message">
          检测到以下月份的预算已存在，请选择处理方式：
        </p>
        <div class="conflict-list">
          <div v-for="conflict in conflictBudgets" :key="conflict.month" class="conflict-item">
            <span class="conflict-month">{{ conflict.month }}</span>
            <span class="conflict-amount">
              {{ getCategoryName(parseInt(conflict.categoryId)) }}
              当前: ¥{{ parseFloat(conflict.existingAmount).toFixed(2) }}
              新增: ¥{{ parseFloat(conflict.newAmount).toFixed(2) }}
            </span>
          </div>
        </div>
        <div class="conflict-options">
          <label class="option-label">
            <input
              v-model="conflictAction"
              type="radio"
              value="override"
              name="action"
            />
            <span>覆盖原预算（新预算将替换原预算）</span>
          </label>
          <label class="option-label">
            <input
              v-model="conflictAction"
              type="radio"
              value="add"
              name="action"
            />
            <span>仅添加金额（新预算金额 = 原预算 + 新增）</span>
          </label>
        </div>
      </div>
      <template #footer>
        <Button type="secondary" @click="showConfirmDialog = false">取消</Button>
        <Button type="primary" @click="handleConfirm">
          {{ conflictAction === 'override' ? '覆盖' : '添加' }}
        </Button>
      </template>
    </Modal>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { budgetAPI, expenseCategoryAPI } from '../api'
import { useUIStore } from '../stores/ui'
import Card from '../components/Card.vue'
import Button from '../components/Button.vue'
import Input from '../components/Input.vue'
import Select from '../components/Select.vue'
import Modal from '../components/Modal.vue'

const uiStore = useUIStore()
const budgets = ref([])
const categories = ref([])
const loading = ref(false)
const showModal = ref(false)
const showConfirmDialog = ref(false)
const editingId = ref(null)

// 存储冲突的预算信息
const conflictBudgets = ref([])
const conflictAction = ref('override') // 'override' 或 'add'

const currentMonth = ref(new Date().toISOString().slice(0, 7))

const form = reactive({
  categoryId: '',
  amount: '',
  budgetMonth: new Date().toISOString().slice(0, 7),
  monthCount: 1
})

const categoryOptions = computed(() =>
  categories.value.map(c => ({ value: c.id, label: c.name }))
)

const getCategoryName = (categoryId) => {
  const category = categories.value.find(c => c.id === categoryId)
  return category?.name || '未分类'
}

const getProgressPercentage = (budget) => {
  // 支持多种字段名
  const budgetAmount = budget.budgetAmount || budget.amount || 0
  const spentAmount = budget.spentAmount || budget.actualAmount || budget.spent || 0
  
  if (budgetAmount === 0) return 0
  
  // 优先使用后端返回的 percentageUsed
  if (budget.percentageUsed !== undefined && budget.percentageUsed !== null) {
    return Math.round(budget.percentageUsed)
  }
  
  const percentage = (spentAmount / budgetAmount) * 100
  return Math.min(Math.round(percentage), 100)
}

const loadCategories = async () => {
  try {
    console.log('【预算】加载支出分类...')
    const response = await expenseCategoryAPI.getList()
    console.log('【预算】分类响应:', JSON.stringify(response, null, 2))
    
    if (response && response.code === 200 && response.data) {
      categories.value = response.data
      console.log('【预算】加载分类数:', response.data.length)
    } else if (response && Array.isArray(response)) {
      categories.value = response
      console.log('【预算】直接返回数组，分类数:', response.length)
    }
  } catch (error) {
    console.error('【预算】加载分类失败:', error)
  }
}

const loadBudgets = async () => {
  loading.value = true
  try {
    console.log('【预算】加载月份预算，月份:', currentMonth.value)
    const response = await budgetAPI.getByMonth(currentMonth.value)
    console.log('【预算】原始响应:', JSON.stringify(response, null, 2))
    
    if (response && response.code === 200) {
      budgets.value = response.data || []
      console.log('【预算】成功加载预算数:', budgets.value.length)
      
      // 详细日志：每个预算记录
      budgets.value.forEach((budget, index) => {
        console.log(`【预算第 ${index + 1} 条】:`, JSON.stringify(budget, null, 2))
        console.log(`【预算第 ${index + 1} 条字段】:`, Object.keys(budget))
      })
    } else {
      console.warn('【预算】响应格式错误:', response)
    }
  } catch (error) {
    console.error('【预算】加载失败:', error)
  } finally {
    loading.value = false
  }
}

const openAddModal = () => {
  console.log('【预算】打开添加模态框')
  editingId.value = null
  form.categoryId = ''
  form.amount = ''
  form.budgetMonth = currentMonth.value
  form.monthCount = 1
  showModal.value = true
  console.log('【预算】showModal 状态:', showModal.value)
}

const editBudget = (budget) => {
  console.log('【预算】编辑预算:', JSON.stringify(budget, null, 2))
  editingId.value = budget.id
  form.categoryId = budget.categoryId
  form.amount = (budget.budgetAmount || budget.amount || 0).toString()
  form.budgetMonth = budget.budgetMonth
  showModal.value = true
  console.log('【预算】编辑表单:', JSON.stringify(form, null, 2))
}

const handleSave = async () => {
  try {
    if (editingId.value) {
      // 编辑模式下只更新单个预算
      await budgetAPI.update(editingId.value, {
        categoryId: parseInt(form.categoryId),
        amount: parseFloat(form.amount).toString(),
        budgetMonth: form.budgetMonth
      })
    } else {
      // 添加模式下，检查是否有冲突
      const monthCount = parseInt(form.monthCount) || 1
      const categoryId = parseInt(form.categoryId)
      const [year, month] = form.budgetMonth.split('-').map(Number)
      const conflicts = []
      
      // 生成所有要处理的月份并检查冲突
      const months = []
      for (let i = 0; i < monthCount; i++) {
        let currentMonth = month + i
        let currentYear = year
        if (currentMonth > 12) {
          currentYear += Math.floor((currentMonth - 1) / 12)
          currentMonth = ((currentMonth - 1) % 12) + 1
        }
        const monthStr = `${currentYear}-${String(currentMonth).padStart(2, '0')}`
        months.push(monthStr)
        
        const existing = budgets.value.find(
          b => b.categoryId === categoryId && b.budgetMonth === monthStr
        )
        
        if (existing) {
          conflicts.push({
            month: monthStr,
            categoryId: categoryId,
            existingAmount: existing.budgetAmount || existing.amount || 0,
            newAmount: form.amount
          })
        }
      }
      
      // 如果有冲突，显示确认对话框
      if (conflicts.length > 0) {
        console.log('【预算】检测到冲突:', conflicts)
        conflictBudgets.value = conflicts
        conflictAction.value = 'override'
        showConfirmDialog.value = true
        return
      }
      
      // 没有冲突，直接创建
      console.log('【预算】无冲突，直接创建', monthCount, '个月份的预算')
      await createBudgets()
    }
    
    showModal.value = false
    await loadBudgets()
  } catch (error) {
    console.error('Failed to save budget:', error)
    ElMessage.error('保存失败: ' + error.message)
  }
}

const createBudgets = async () => {
  try {
    const monthCount = parseInt(form.monthCount) || 1
    const categoryId = parseInt(form.categoryId)
    const amount = parseFloat(form.amount)
    
    // 生成月份列表
    const months = []
    const [year, month] = form.budgetMonth.split('-').map(Number)
    for (let i = 0; i < monthCount; i++) {
      let currentMonth = month + i
      let currentYear = year
      if (currentMonth > 12) {
        currentYear += Math.floor((currentMonth - 1) / 12)
        currentMonth = ((currentMonth - 1) % 12) + 1
      }
      const monthStr = `${currentYear}-${String(currentMonth).padStart(2, '0')}`
      months.push(monthStr)
    }
    
    console.log(`【预算】开始处理 ${months.length} 个月份:`, months)
    
    // 逐月处理
    for (const monthStr of months) {
      console.log(`【预算】处理月份: ${monthStr}`)
      
      // 先加载该月的预算，确保拿到最新数据
      const response = await budgetAPI.getByMonth(monthStr)
      const monthBudgets = response.data || []
      console.log(`【预算】${monthStr} 现有预算:`, monthBudgets)
      
      const existing = monthBudgets.find(b => b.categoryId === categoryId)
      
      if (existing) {
        let updateAmount = amount
        if (conflictAction.value === 'add') {
          updateAmount = parseFloat(existing.budgetAmount || existing.amount || 0) + amount
        }
        
        console.log(`【预算】更新预算 ${monthStr}: ID=${existing.id}, 原金额=${existing.budgetAmount || existing.amount}, 新金额=${updateAmount}`)
        await budgetAPI.update(existing.id, {
          categoryId: categoryId,
          amount: updateAmount.toString(),
          budgetMonth: monthStr
        })
      } else {
        console.log(`【预算】创建新预算 ${monthStr}: 分类=${categoryId}, 金额=${amount}`)
        const createResult = await budgetAPI.create({
          categoryId: categoryId,
          amount: amount.toString(),
          budgetMonth: monthStr
        })
        console.log(`【预算】创建结果:`, createResult)
      }
    }
    
    console.log(`【预算】所有月份处理完成`)
  } catch (error) {
    console.error('Failed to create budgets:', error)
    throw error
  }
}

const handleConfirm = async () => {
  showConfirmDialog.value = false
  try {
    await createBudgets()
    await loadBudgets()  // 等待重新加载完成
    showModal.value = false
    ElMessage.success('预算创建成功')
  } catch (error) {
    ElMessage.error('预算创建失败: ' + error.message)
    console.error('Error:', error)
  }
}

const deleteBudget = async (id) => {
  const confirmed = await uiStore.showConfirm('确定删除此预算吗？', '删除确认', 'danger')
  if (confirmed) {
    try {
      await budgetAPI.delete(id)
      uiStore.showNotification('删除成功', 'success')
      loadBudgets()
    } catch (error) {
      console.error('Failed to delete budget:', error)
    }
  }
}

onMounted(() => {
  loadCategories()
  loadBudgets()
})
</script>

<style scoped>
.page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  flex-wrap: wrap;
  gap: 20px;
}

.page-header h1 {
  font-size: 28px;
  color: #333;
  margin: 0;
  flex: 1;
  min-width: 200px;
}

.header-actions {
  display: flex;
  gap: 15px;
  align-items: center;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.header-actions :deep(.form-group) {
  margin-bottom: 0;
}

.header-actions :deep(input) {
  padding: 10px 12px;
  height: 40px;
}

.month-selector {
  display: flex;
  align-items: center;
  gap: 10px;
}

.month-selector :deep(input) {
  max-width: 100px;
}

.month-info {
  color: #999;
  font-size: 13px;
  white-space: nowrap;
}

.conflict-content {
  margin-bottom: 20px;
}

.conflict-message {
  color: #e74c3c;
  font-weight: 500;
  margin-bottom: 15px;
}

.conflict-list {
  background: #fff8f7;
  border: 1px solid #f5e6e3;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 20px;
  max-height: 200px;
  overflow-y: auto;
}

.conflict-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0e0dd;
  font-size: 13px;

  &:last-child {
    border-bottom: none;
  }
}

.conflict-month {
  font-weight: 600;
  color: #333;
  min-width: 100px;
}

.conflict-amount {
  color: #666;
  flex: 1;
}

.conflict-options {
  background: #f5f5f5;
  border-radius: 6px;
  padding: 15px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-label {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  font-size: 14px;
  color: #333;

  input[type="radio"] {
    cursor: pointer;
  }

  &:hover {
    color: #667eea;
  }
}

.loading {
  text-align: center;
  padding: 40px;
  color: #999;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-state p {
  margin-bottom: 20px;
}

.budget-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.budget-card {
  transition: all 0.3s;
}

.budget-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.budget-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.budget-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.budget-actions {
  display: flex;
  gap: 10px;
}

.budget-progress {
  margin-top: 15px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
  color: #666;
}

.progress-info .actual {
  color: #e74c3c;
}

.progress-bar {
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 8px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  transition: width 0.3s;
}

.progress-percentage {
  text-align: right;
  font-size: 12px;
  color: #999;
}

.link-btn {
  background: none;
  border: none;
  color: #667eea;
  cursor: pointer;
  font-size: 12px;
  padding: 0;
  text-decoration: none;

  &:hover {
    color: #764ba2;
    text-decoration: underline;
  }

  &.danger {
    color: #e74c3c;

    &:hover {
      color: #c0392b;
    }
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .header-actions {
    width: 100%;
    flex-direction: column;
  }

  .budget-grid {
    grid-template-columns: 1fr;
  }
}
</style>
