<template>
  <div class="page">
    <div class="page-header">
      <h1>支出管理</h1>
      <Button type="primary" @click="openAddModal">+ 添加支出</Button>
    </div>

    <!-- 筛选区域 -->
    <Card class="filters-card">
      <div class="filter-row">
        <Input
          v-model="filters.keyword"
          placeholder="搜索描述..."
          @input="handleSearch"
        />
        <Select
          v-model="filters.categoryId"
          placeholder="选择分类"
          :options="categoryOptions"
          @change="loadExpenses"
        />
        <Input
          v-model="filters.month"
          type="month"
          @change="loadExpenses"
        />
      </div>
    </Card>

    <!-- 数据表格 -->
    <Card>
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="expenses.length === 0" class="empty-state">
        <p>暂无支出记录</p>
        <Button type="primary" @click="openAddModal">添加支出</Button>
      </div>
      <table v-else class="table">
        <thead>
          <tr>
            <th>日期</th>
            <th>分类</th>
            <th>描述</th>
            <th>金额</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in expenses" :key="item.id">
            <td>{{ formatDate(item.transactionDate) }}</td>
            <td>{{ getCategoryName(item.categoryId) }}</td>
            <td>{{ item.description }}</td>
            <td>¥ {{ item.amount.toFixed(2) }}</td>
            <td class="actions">
              <button class="link-btn" @click="editExpense(item)">编辑</button>
              <button class="link-btn danger" @click="deleteExpense(item.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </Card>

    <!-- 添加/编辑模态框 -->
    <Modal
      v-model="showModal"
      :title="editingId ? '编辑支出' : '添加支出'"
      size="md"
    >
      <div>
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
          label="金额"
          placeholder="请输入金额"
          step="0.01"
          min="0"
          required
        />
        <Input
          v-model="form.transactionDate"
          type="date"
          label="日期"
          required
        />
        <Input
          v-model="form.description"
          label="描述"
          placeholder="请输入描述（可选）"
        />
      </div>
      <template #footer>
        <Button type="secondary" @click="showModal = false">取消</Button>
        <Button type="primary" @click="handleSave">保存</Button>
      </template>
    </Modal>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { expenseAPI, expenseCategoryAPI } from '../api'
import Card from '../components/Card.vue'
import Button from '../components/Button.vue'
import Input from '../components/Input.vue'
import Select from '../components/Select.vue'
import Modal from '../components/Modal.vue'

const expenses = ref([])
const categories = ref([])
const loading = ref(false)
const showModal = ref(false)
const editingId = ref(null)

const filters = reactive({
  keyword: '',
  categoryId: '',
  month: new Date().toISOString().slice(0, 7)
})

const form = reactive({
  categoryId: '',
  amount: '',
  transactionDate: new Date().toISOString().slice(0, 10),
  description: ''
})

const categoryOptions = computed(() =>
  categories.value.map(c => ({ value: c.id, label: c.name }))
)

const getCategoryName = (categoryId) => {
  const category = categories.value.find(c => c.id === categoryId)
  return category?.name || '未分类'
}

const formatDate = (dateStr) => {
  if (!dateStr) return '未知'
  try {
    // 处理多种日期格式
    let dateObj
    
    if (typeof dateStr === 'string') {
      // 如果是 ISO 格式 (2026-01-08) 或 ISO 完整格式
      if (dateStr.includes('T')) {
        dateObj = new Date(dateStr)
      } else {
        // 处理 YYYY-MM-DD 格式
        const [year, month, day] = dateStr.split('-')
        dateObj = new Date(year, parseInt(month) - 1, day)
      }
    } else if (dateStr instanceof Date) {
      dateObj = dateStr
    } else {
      return '未知'
    }

    // 检查日期是否有效
    if (isNaN(dateObj.getTime())) {
      console.warn('无效的日期:', dateStr)
      return dateStr
    }

    return dateObj.toLocaleDateString('zh-CN')
  } catch (error) {
    console.error('日期解析错误:', error, dateStr)
    return dateStr || '未知'
  }
}

const loadCategories = async () => {
  try {
    const response = await expenseCategoryAPI.getList()
    console.log('支出分类响应:', response)
    if (response && response.code === 200 && response.data) {
      categories.value = response.data
      console.log('加载的分类:', categories.value)
    } else if (response && Array.isArray(response)) {
      // 如果后端直接返回数组
      categories.value = response
      console.log('直接返回数组的分类:', categories.value)
    } else {
      console.warn('分类响应格式不正确:', response)
    }
  } catch (error) {
    console.error('Failed to load categories:', error)
  }
}

const loadExpenses = async () => {
  loading.value = true
  try {
    const params = {
      month: filters.month,
      ...(filters.categoryId && { categoryId: filters.categoryId }),
      ...(filters.keyword && { keyword: filters.keyword })
    }
    console.log('加载支出，参数:', params)
    const response = await expenseAPI.getList(params)
    console.log('【原始响应】', JSON.stringify(response, null, 2))
    
    if (response && response.code === 200 && response.data) {
      expenses.value = response.data || []
      console.log('成功加载支出数:', expenses.value.length)
      
      // 详细日志：打印每条记录的所有字段
      expenses.value.forEach((item, index) => {
        console.log(`【第 ${index + 1} 条记录的所有字段】:`, JSON.stringify(item, null, 2))
        console.log(`【第 ${index + 1} 条记录的 Object.keys】:`, Object.keys(item))
      })
    } else if (response && Array.isArray(response)) {
      expenses.value = response
      console.log('直接返回数组，支出数:', response.length)
      response.forEach((item, index) => {
        console.log(`【第 ${index + 1} 条记录】:`, JSON.stringify(item, null, 2))
      })
    } else {
      console.warn('响应格式不正确:', response)
      expenses.value = []
    }
  } catch (error) {
    console.error('Failed to load expenses:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  loadExpenses()
}

const openAddModal = () => {
  editingId.value = null
  form.categoryId = ''
  form.amount = ''
  form.transactionDate = new Date().toISOString().slice(0, 10)
  form.description = ''
  showModal.value = true
}

const editExpense = (item) => {
  editingId.value = item.id
  form.categoryId = item.categoryId
  form.amount = item.amount.toString()
  form.transactionDate = item.transactionDate
  form.description = item.description
  showModal.value = true
}

const handleSave = async () => {
  // 表单验证
  if (!form.categoryId) {
    alert('请选择分类')
    return
  }
  if (!form.amount || parseFloat(form.amount) <= 0) {
    alert('请输入有效的金额')
    return
  }
  if (!form.transactionDate) {
    alert('请选择日期')
    return
  }

  try {
    // 准备数据，确保类型正确
    const data = {
      categoryId: parseInt(form.categoryId),
      amount: parseFloat(form.amount),
      transactionDate: form.transactionDate,  // 后端字段名是 transactionDate
      description: form.description || ''
    }

    if (editingId.value) {
      await expenseAPI.update(editingId.value, data)
      alert('修改成功')
    } else {
      await expenseAPI.create(data)
      alert('添加成功')
    }
    showModal.value = false
    loadExpenses()
  } catch (error) {
    console.error('Failed to save expense:', error)
    alert('保存失败: ' + (error.response?.data?.message || error.message))
  }
}

const deleteExpense = async (id) => {
  if (confirm('确定删除此记录吗？')) {
    try {
      await expenseAPI.delete(id)
      loadExpenses()
    } catch (error) {
      console.error('Failed to delete expense:', error)
    }
  }
}

onMounted(() => {
  loadCategories()
  loadExpenses()
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
}

.page-header h1 {
  font-size: 28px;
  color: #333;
  margin: 0;
}

.filters-card {
  margin-bottom: 20px;
}

.filter-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 15px;
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

.table {
  width: 100%;
  border-collapse: collapse;
  background: white;
}

.table thead {
  background: #fafafa;
  border-bottom: 2px solid #f0f0f0;
}

.table th {
  padding: 12px 16px;
  text-align: left;
  font-weight: 600;
  color: #333;
  font-size: 13px;
}

.table td {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  color: #666;
}

.table tbody tr:hover {
  background: #f9f9f9;
}

.actions {
  display: flex;
  gap: 12px;
}

.link-btn {
  background: none;
  border: none;
  color: #667eea;
  cursor: pointer;
  font-size: 13px;
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

  .filter-row {
    grid-template-columns: 1fr;
  }
}
</style>
