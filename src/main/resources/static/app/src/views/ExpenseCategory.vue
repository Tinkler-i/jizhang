<template>
  <div class="page">
    <div class="page-header">
      <h1>支出分类</h1>
      <Button type="primary" @click="openAddModal">+ 添加分类</Button>
    </div>

    <!-- 分类列表 -->
    <div class="category-grid">
      <Card 
        v-for="category in categories" 
        :key="category.id" 
        class="category-card clickable-card"
        @click="viewCategoryDetails(category)"
      >
        <template #header>
          <div class="category-header">
            <h3>{{ category.name }}</h3>
            <div class="category-actions" @click.stop>
              <button class="link-btn" @click="editCategory(category)">编辑</button>
              <button class="link-btn danger" @click="deleteCategory(category.id)">删除</button>
            </div>
          </div>
        </template>
        <p v-if="category.description" class="category-description">
          {{ category.description }}
        </p>
        <p class="category-stats">
          该分类下共有 <strong>{{ getCategoryCount(category.id) }}</strong> 条记录
        </p>
      </Card>
    </div>

    <!-- 添加/编辑模态框 -->
    <Modal
      v-model="showModal"
      :title="editingId ? '编辑分类' : '添加分类'"
      size="sm"
    >
      <Input
        v-model="form.name"
        label="分类名称"
        placeholder="请输入分类名称"
        required
      />
      <Input
        v-model="form.description"
        label="描述"
        placeholder="请输入描述（可选）"
      />
      <template #footer>
        <Button type="secondary" @click="showModal = false">取消</Button>
        <Button type="primary" @click="handleSave">保存</Button>
      </template>
    </Modal>

    <!-- 分类详情模态框 -->
    <Modal
      v-model="showDetailsModal"
      :title="`${selectedCategory?.name} - 详细记录`"
      size="lg"
    >
      <div v-if="categoryDetails.length === 0" class="empty-state">
        <p>暂无数据</p>
      </div>
      <div v-else class="details-table">
        <table>
          <thead>
            <tr>
              <th>日期</th>
              <th>金额</th>
              <th>描述</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in categoryDetails" :key="item.id">
              <td>{{ formatDate(item.transactionDate) }}</td>
              <td>¥{{ item.amount }}</td>
              <td>{{ item.description || '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <template #footer>
        <Button type="secondary" @click="showDetailsModal = false">关闭</Button>
      </template>
    </Modal>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { expenseCategoryAPI, expenseAPI } from '../api'
import Card from '../components/Card.vue'
import Button from '../components/Button.vue'
import Input from '../components/Input.vue'
import Modal from '../components/Modal.vue'

const categories = ref([])
const expenses = ref([])
const showModal = ref(false)
const showDetailsModal = ref(false)
const editingId = ref(null)
const selectedCategory = ref(null)
const categoryDetails = ref([])

const form = reactive({
  name: '',
  description: ''
})

const getCategoryCount = (categoryId) => {
  // 统计该分类下的支出数量
  return expenses.value.filter(item => item.categoryId === categoryId).length
}

const loadExpenses = async () => {
  try {
    console.log('【分类页】加载所有支出用于计数...')
    const response = await expenseAPI.getList({ month: '' })
    console.log('【分类页】支出列表响应:', response)
    
    if (response && response.code === 200 && response.data) {
      expenses.value = response.data
      console.log('【分类页】加载支出数:', response.data.length)
    } else if (response && Array.isArray(response)) {
      expenses.value = response
      console.log('【分类页】直接返回数组，支出数:', response.length)
    }
  } catch (error) {
    console.warn('【分类页】加载支出失败，记录数将显示为 0:', error)
  }
}

const loadCategories = async () => {
  try {
    console.log('开始加载支出分类...')
    const response = await expenseCategoryAPI.getList()
    console.log('【原始响应】', JSON.stringify(response, null, 2))
    
    if (response && response.code === 200 && response.data) {
      categories.value = response.data
      console.log('成功加载分类数:', response.data.length)
      response.data.forEach((item, index) => {
        console.log(`【第 ${index + 1} 个分类】:`, JSON.stringify(item, null, 2))
      })
    } else if (response && Array.isArray(response)) {
      // 如果后端直接返回数组
      categories.value = response
      console.log('直接返回数组，分类数:', response.length)
      response.forEach((item, index) => {
        console.log(`【第 ${index + 1} 个分类】:`, JSON.stringify(item, null, 2))
      })
    } else {
      console.warn('分类响应格式不正确:', response)
      alert('分类加载失败：响应格式错误')
    }
  } catch (error) {
    console.error('Failed to load categories:', error)
    alert('加载失败: ' + (error.response?.data?.message || error.message))
  }
}

const openAddModal = () => {
  editingId.value = null
  form.name = ''
  form.description = ''
  showModal.value = true
}

const editCategory = (category) => {
  editingId.value = category.id
  form.name = category.name
  form.description = category.description || ''
  showModal.value = true
}

const handleSave = async () => {
  try {
    if (editingId.value) {
      await expenseCategoryAPI.update(editingId.value, form)
    } else {
      await expenseCategoryAPI.create(form)
    }
    showModal.value = false
    loadCategories()
  } catch (error) {
    console.error('Failed to save category:', error)
  }
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

const viewCategoryDetails = (category) => {
  selectedCategory.value = category
  categoryDetails.value = expenses.value.filter(item => item.categoryId === category.id)
  showDetailsModal.value = true
}

const deleteCategory = async (id) => {
  if (confirm('确定删除此分类吗？')) {
    try {
      await expenseCategoryAPI.delete(id)
      loadCategories()
    } catch (error) {
      console.error('Failed to delete category:', error)
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

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.category-card {
  transition: all 0.3s;
  cursor: pointer;
}

.category-card.clickable-card {
  cursor: pointer;
}

.category-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.category-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.category-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.category-actions {
  display: flex;
  gap: 10px;
}

.category-description {
  color: #999;
  font-size: 13px;
  margin: 10px 0 0 0;
}

.category-stats {
  color: #999;
  font-size: 13px;
  margin: 10px 0 0 0;
}

.details-table {
  width: 100%;
}

.details-table table {
  width: 100%;
  border-collapse: collapse;
}

.details-table thead {
  background-color: #f5f5f5;
  border-bottom: 2px solid #e0e0e0;
}

.details-table th {
  padding: 12px;
  text-align: left;
  font-weight: 600;
  font-size: 13px;
  color: #333;
}

.details-table td {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 13px;
}

.details-table tbody tr:hover {
  background-color: #fafafa;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
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

  .category-grid {
    grid-template-columns: 1fr;
  }
}
</style>
