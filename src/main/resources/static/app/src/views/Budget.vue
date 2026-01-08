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
        label="预算月份"
        required
      />
      <template #footer>
        <Button type="secondary" @click="showModal = false">取消</Button>
        <Button type="primary" @click="handleSave">保存</Button>
      </template>
    </Modal>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { budgetAPI, expenseCategoryAPI } from '../api'
import Card from '../components/Card.vue'
import Button from '../components/Button.vue'
import Input from '../components/Input.vue'
import Select from '../components/Select.vue'
import Modal from '../components/Modal.vue'

const budgets = ref([])
const categories = ref([])
const loading = ref(false)
const showModal = ref(false)
const editingId = ref(null)

const currentMonth = ref(new Date().toISOString().slice(0, 7))

const form = reactive({
  categoryId: '',
  amount: '',
  budgetMonth: new Date().toISOString().slice(0, 7)
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
      await budgetAPI.update(editingId.value, form)
    } else {
      await budgetAPI.create(form)
    }
    showModal.value = false
    loadBudgets()
  } catch (error) {
    console.error('Failed to save budget:', error)
  }
}

const deleteBudget = async (id) => {
  if (confirm('确定删除此预算吗？')) {
    try {
      await budgetAPI.delete(id)
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
  align-items: flex-start;
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
