<template>
  <div class="page">
    <div class="page-header">
      <h1>收入分类</h1>
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
import { incomeCategoryAPI, incomeAPI } from '../api'
import { useUIStore } from '../stores/ui'
import Card from '../components/Card.vue'
import Button from '../components/Button.vue'
import Input from '../components/Input.vue'
import Modal from '../components/Modal.vue'

const uiStore = useUIStore()
const categories = ref([])
const incomes = ref([])
const showModal = ref(false)
const showDetailsModal = ref(false)
const editingId = ref(null)
const selectedCategory = ref(null)
const categoryDetails = ref([])
const allRawCategories = ref([]) // 存储去重前的所有分类数据
const categoryNameToIds = new Map() // 分类名称 -> [所有ID数组]

const form = reactive({
  name: '',
  description: ''
})

const getCategoryCount = (categoryId) => {
  // 找到该分类的名称，然后统计所有相同名称分类的收入数量
  const category = allRawCategories.value.find(c => c.id === categoryId)
  if (!category) return 0
  
  const categoryIds = categoryNameToIds.get(category.name) || [categoryId]
  return incomes.value.filter(item => categoryIds.includes(item.categoryId)).length
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
    
    let data = []
    if (response && response.code === 200 && response.data) {
      data = response.data
      console.log('成功加载分类数:', response.data.length)
      response.data.forEach((item, index) => {
        console.log(`【第 ${index + 1} 个分类】:`, JSON.stringify(item, null, 2))
      })
    } else if (response && Array.isArray(response)) {
      // 如果后端直接返回数组
      data = response
      console.log('直接返回数组，分类数:', response.length)
      response.forEach((item, index) => {
        console.log(`【第 ${index + 1} 个分类】:`, JSON.stringify(item, null, 2))
      })
    } else {
      console.warn('分类响应格式不正确:', response)
      uiStore.showNotification('分类加载失败：响应格式错误', 'error')
      return
    }
    
    // 保存所有原始分类数据
    allRawCategories.value = data
    
    // 建立分类名称到ID数组的映射
    categoryNameToIds.clear()
    for (const category of data) {
      const categoryIds = categoryNameToIds.get(category.name) || []
      categoryIds.push(category.id)
      categoryNameToIds.set(category.name, categoryIds)
    }
    
    // 按分类名称去重（家庭组场景中，相同名称的分类只显示一个）
    const uniqueCategories = new Map()
    for (const category of data) {
      uniqueCategories.set(category.name, category)
    }
    categories.value = Array.from(uniqueCategories.values())
    console.log('去重后分类数:', categories.value.length)
  } catch (error) {
    console.error('Failed to load categories:', error)
    uiStore.showNotification('加载失败: ' + (error.response?.data?.message || error.message), 'error')
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
      // 获取要编辑的分类名字
      const categoryToEdit = allRawCategories.value.find(c => c.id === editingId.value)
      if (categoryToEdit) {
        // 获取所有相同名字的分类ID
        const categoryIds = categoryNameToIds.get(categoryToEdit.name) || [editingId.value]
        // 对所有相同名字的分类一起更新
        await Promise.all(categoryIds.map(id => incomeCategoryAPI.update(id, form)))
        console.log(`已更新所有相同名字的分类，共 ${categoryIds.length} 个`)
      } else {
        // 如果找不到，就只更新当前分类
        await incomeCategoryAPI.update(editingId.value, form)
      }
    } else {
      await incomeCategoryAPI.create(form)
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
  // 获取该分类名下所有相同名称的分类的所有收入数据
  const categoryIds = categoryNameToIds.get(category.name) || [category.id]
  categoryDetails.value = incomes.value.filter(item => categoryIds.includes(item.categoryId))
  showDetailsModal.value = true
}

const deleteCategory = async (id) => {
  // 找到该分类及所有相同名字的分类
  const categoryToDelete = allRawCategories.value.find(c => c.id === id)
  let categoryIds = [id]
  let warningMsg = '确定删除此收入分类吗？'
  
  if (categoryToDelete) {
    categoryIds = categoryNameToIds.get(categoryToDelete.name) || [id]
    if (categoryIds.length > 1) {
      warningMsg = `确定删除此收入分类吗？该分类在家庭组中有 ${categoryIds.length} 个相同名字的分类，将全部删除。`
    }
  }
  
  const confirmed = await uiStore.showConfirm(warningMsg, '删除确认', 'danger')
  if (confirmed) {
    try {
      // 删除所有相同名字的分类
      await Promise.all(categoryIds.map(cid => incomeCategoryAPI.delete(cid)))
      uiStore.showNotification('删除成功', 'success')
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
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    gap: 10px;
  }

  .page-header h1 {
    margin-bottom: 0;
  }

  .category-grid {
    grid-template-columns: 1fr;
  }
}
</style>
