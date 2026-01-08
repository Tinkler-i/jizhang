<template>
  <div class="page">
    <div class="page-header">
      <h1>收入分类</h1>
      <Button type="primary" @click="openAddModal">+ 添加分类</Button>
    </div>

    <!-- 分类列表 -->
    <div class="category-grid">
      <Card v-for="category in categories" :key="category.id" class="category-card">
        <template #header>
          <div class="category-header">
            <h3>{{ category.name }}</h3>
            <div class="category-actions">
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
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { incomeCategoryAPI, incomeAPI } from '../api'
import Card from '../components/Card.vue'
import Button from '../components/Button.vue'
import Input from '../components/Input.vue'
import Modal from '../components/Modal.vue'

const categories = ref([])
const incomes = ref([])
const showModal = ref(false)
const editingId = ref(null)

const form = reactive({
  name: '',
  description: ''
})

const getCategoryCount = (categoryId) => {
  // 统计该分类下的收入数量
  return incomes.value.filter(item => item.categoryId === categoryId).length
}

const loadIncomes = async () => {
  try {
    console.log('【分类页】加载所有收入用于计数...')
    const response = await incomeAPI.getList({ month: '' })
    console.log('【分类页】收入列表响应:', response)
    
    if (response && response.code === 200 && response.data) {
      incomes.value = response.data
      console.log('【分类页】加载收入数:', response.data.length)
    } else if (response && Array.isArray(response)) {
      incomes.value = response
      console.log('【分类页】直接返回数组，收入数:', response.length)
    }
  } catch (error) {
    console.warn('【分类页】加载收入失败，记录数将显示为 0:', error)
  }
}

const loadCategories = async () => {
  try {
    console.log('开始加载收入分类...')
    const response = await incomeCategoryAPI.getList()
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
      await incomeCategoryAPI.update(editingId.value, form)
    } else {
      await incomeCategoryAPI.create(form)
    }
    showModal.value = false
    loadCategories()
  } catch (error) {
    console.error('Failed to save category:', error)
  }
}

const deleteCategory = async (id) => {
  if (confirm('确定删除此分类吗？')) {
    try {
      await incomeCategoryAPI.delete(id)
      loadCategories()
    } catch (error) {
      console.error('Failed to delete category:', error)
    }
  }
}

onMounted(() => {
  loadCategories()
  loadIncomes()
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
