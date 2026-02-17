<template>
  <div class="page">
    <div class="page-header">
      <h1>目标管理</h1>
      <div class="header-actions">
        <Input
          v-model="currentYear"
          type="number"
          min="2020"
          max="2099"
          @change="loadYearTargets"
        />
        <Button type="primary" @click="openAddModal">+ 添加目标</Button>
      </div>
    </div>

    <!-- 年度目标网格 -->
    <div class="section">
      <h2>{{ currentYear }} 年度目标</h2>
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="yearTargets.length === 0" class="empty-state">
        <p>该年度暂无目标记录</p>
        <Button type="primary" @click="openAddModal">添加目标</Button>
      </div>
      <div v-else class="year-target-grid">
        <div
          v-for="target in yearTargets"
          :key="target.targetMonth"
          class="year-target-item"
          :class="{ 'no-target': target.isDefault }"
        >
          <div class="item-month">{{ target.targetMonth.substring(5) }} 月</div>
          <div v-if="target.isDefault" class="item-empty">
            <p class="empty-text">暂未设置目标</p>
            <p class="empty-tip">点击编辑添加目标</p>
          </div>
          <div v-else class="item-info">
            <div class="info-row">
              <span class="label">目标:</span>
              <span class="value">¥ {{ target.incomeTarget?.toFixed(2) || '0.00' }}</span>
            </div>
            <div class="info-row">
              <span class="label">攒下:</span>
              <span class="value">¥ {{ target.actualIncome?.toFixed(2) || '0.00' }}</span>
            </div>
          </div>
          <div v-if="!target.isDefault" class="progress-container">
            <div class="progress-bar">
              <div 
                class="progress-fill"
                :style="{ width: target.progressPercent + '%' }"
                :class="{ 'progress-complete': target.progressPercent >= 100 }"
              ></div>
            </div>
            <div class="progress-text">{{ target.progressPercent }}%</div>
          </div>
          <div class="item-actions">
            <button class="link-btn" @click="editTarget(target)">{{ target.isDefault ? '设置' : '编辑' }}</button>
            <button v-if="!target.isDefault" class="link-btn danger" @click="deleteTarget(target.id)">删除</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加/编辑模态框 -->
    <Modal
      v-model="showModal"
      :title="editingId ? '编辑目标' : '添加目标'"
      size="md"
    >
      <Select
        v-model="form.selectedMonth"
        label="选择月份"
        required
        @change="updateFormMonth"
      >
        <option value="" disabled>请选择月份</option>
        <option v-for="month in 12" :key="month" :value="month">
          {{ String(month).padStart(2, '0') }} 月
        </option>
      </Select>
      <Input
        v-model="form.incomeTarget"
        type="number"
        label="收入目标 (¥)"
        placeholder="请输入收入目标"
        step="0.01"
        min="0.01"
        required
      />
      <template #footer>
        <Button type="secondary" @click="showModal = false">取消</Button>
        <Button type="primary" @click="handleSave">
          {{ editingId ? '保存' : '创建' }}
        </Button>
      </template>
    </Modal>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { userTargetAPI, incomeAPI, expenseAPI } from '../api'
import { useUIStore } from '../stores/ui'
import Button from '../components/Button.vue'
import Input from '../components/Input.vue'
import Select from '../components/Select.vue'
import Modal from '../components/Modal.vue'

const uiStore = useUIStore()
const allTargets = ref([])
const monthlySavings = ref({}) // 存储每个月份的实际攒下金额（收入-支出）
const loading = ref(false)
const showModal = ref(false)
const editingId = ref(null)

const currentYear = ref(new Date().getFullYear())

const form = reactive({
  targetMonth: '',
  incomeTarget: '',
  selectedMonth: ''
})

// 计算包含进度信息的年度目标（包含未设置的月份）
const yearTargets = computed(() => {
  const year = currentYear.value.toString()
  
  // 从API获取的目标
  const apiTargets = allTargets.value.filter(t => t.targetMonth.startsWith(year))
  const targetMap = new Map(apiTargets.map(t => [t.targetMonth, t]))
  
  // 生成全年12个月的目标列表
  const allMonths = []
  for (let month = 1; month <= 12; month++) {
    const monthStr = String(month).padStart(2, '0')
    const targetMonth = `${year}-${monthStr}`
    
    if (targetMap.has(targetMonth)) {
      // 如果有设置目标，使用API数据
      const target = targetMap.get(targetMonth)
      const actualSavings = monthlySavings.value[targetMonth] || 0
      const progressPercent = target.incomeTarget > 0 
        ? Math.min(Math.round((actualSavings / target.incomeTarget) * 100), 100)
        : 0
      allMonths.push({
        ...target,
        actualIncome: actualSavings,
        progressPercent,
        isDefault: false
      })
    } else {
      // 如果没有设置目标，使用默认值
      const actualSavings = monthlySavings.value[targetMonth] || 0
      allMonths.push({
        id: null,
        targetMonth,
        incomeTarget: 0,
        actualIncome: actualSavings,
        progressPercent: 0,
        isDefault: true
      })
    }
  }
  
  return allMonths.sort((a, b) => a.targetMonth.localeCompare(b.targetMonth))
})

const loadAllTargets = async () => {
  loading.value = true
  try {
    console.log('【目标】加载所有目标')
    const response = await userTargetAPI.getAll()
    console.log('【目标】所有目标响应:', response)
    
    if (response?.code === 0 || response?.code === 200) {
      allTargets.value = response.data || []
      console.log('【目标】加载的目标数:', allTargets.value.length)
      // 加载所有月份的攒下数据
      await loadMonthlySavings()
    } else {
      console.warn('【目标】响应格式异常:', response)
      allTargets.value = []
    }
  } catch (error) {
    console.error('【目标】加载所有目标失败:', error)
    allTargets.value = []
  } finally {
    loading.value = false
  }
}

// 加载每个月份的攒下数据（收入 - 支出）
const loadMonthlySavings = async () => {
  const year = currentYear.value.toString()
  
  console.log('【目标】加载月度攒下数据，年份:', year)
  monthlySavings.value = {}

  // 对每个月（包括没有设置目标的月份）都计算攒下金额
  for (let month = 1; month <= 12; month++) {
    const monthStr = String(month).padStart(2, '0')
    const monthKey = `${year}-${monthStr}`
    
    try {
      // 获取该月的日期范围
      const startDate = `${year}-${monthStr}-01`
      const lastDay = new Date(year, month, 0).getDate()
      const endDate = `${year}-${monthStr}-${lastDay}`
      
      // 并行加载收入和支出
      const [incomeRes, expenseRes] = await Promise.all([
        incomeAPI.getList({ startDate, endDate }),
        expenseAPI.getList({ startDate, endDate })
      ])
      
      console.log(`【目标】${monthKey} 的收入响应:`, incomeRes)
      console.log(`【目标】${monthKey} 的支出响应:`, expenseRes)
      
      // 计算该月的总收入
      let totalIncome = 0
      if (incomeRes?.code === 200 && incomeRes?.data && Array.isArray(incomeRes.data)) {
        totalIncome = incomeRes.data.reduce((sum, item) => sum + (item.amount || 0), 0)
      } else if (Array.isArray(incomeRes)) {
        totalIncome = incomeRes.reduce((sum, item) => sum + (item.amount || 0), 0)
      }
      console.log(`【目标】${monthKey} 的总收入: ¥${totalIncome}`)
      
      // 计算该月的总支出
      let totalExpense = 0
      if (expenseRes?.code === 200 && expenseRes?.data && Array.isArray(expenseRes.data)) {
        totalExpense = expenseRes.data.reduce((sum, item) => sum + (item.amount || 0), 0)
      } else if (Array.isArray(expenseRes)) {
        totalExpense = expenseRes.reduce((sum, item) => sum + (item.amount || 0), 0)
      }
      console.log(`【目标】${monthKey} 的总支出: ¥${totalExpense}`)
      
      // 攒下金额 = 收入 - 支出
      const savings = totalIncome - totalExpense
      monthlySavings.value[monthKey] = savings
      console.log(`【目标】${monthKey} 的攒下金额: ¥${savings}`)
    } catch (error) {
      console.error(`【目标】加载 ${monthKey} 的数据失败:`, error)
      monthlySavings.value[monthKey] = 0
    }
  }
}

const loadYearTargets = async () => {
  console.log('【目标】加载年度目标，年份:', currentYear.value)
  loading.value = true
  try {
    if (allTargets.value.length === 0) {
      await loadAllTargets()
    } else {
      await loadMonthlySavings()
    }
  } finally {
    loading.value = false
  }
}

const openAddModal = () => {
  console.log('【目标】打开添加模态框')
  editingId.value = null
  form.selectedMonth = (new Date().getMonth() + 1).toString()
  updateFormMonth()
  form.incomeTarget = ''
  showModal.value = true
}

const updateFormMonth = () => {
  // 根据选中的月份更新form.targetMonth
  const month = form.selectedMonth
  const monthStr = String(month).padStart(2, '0')
  form.targetMonth = `${currentYear.value}-${monthStr}`
  console.log('【目标】更新表单月份:', form.targetMonth)
}

const editTarget = (target) => {
  console.log('【目标】编辑目标:', target)
  editingId.value = target.id
  form.targetMonth = target.targetMonth
  form.selectedMonth = target.targetMonth.substring(5)
  form.incomeTarget = target.isDefault ? '' : target.incomeTarget.toString()
  showModal.value = true
}

const handleSave = async () => {
  try {
    if (!form.targetMonth || !form.incomeTarget) {
      uiStore.showNotification('请填写所有必需字段', 'warning', 3000)
      return
    }

    const incomeTarget = parseFloat(form.incomeTarget)
    if (incomeTarget <= 0) {
      uiStore.showNotification('收入目标必须大于 0', 'warning', 3000)
      return
    }

    if (editingId.value) {
      // 编辑模式
      console.log('【目标】更新目标:', editingId.value)
      const response = await userTargetAPI.update(form.targetMonth, incomeTarget)
      console.log('【目标】更新响应:', response)
      
      if (response?.code === 0 || response?.code === 200) {
        uiStore.showNotification('更新成功', 'success', 2000)
        showModal.value = false
        await loadAllTargets()
      } else {
        uiStore.showNotification(response?.message || '更新失败', 'error', 3000)
      }
    } else {
      // 添加模式
      console.log('【目标】创建目标:', form)
      const response = await userTargetAPI.create(form.targetMonth, incomeTarget)
      console.log('【目标】创建响应:', response)
      
      if (response?.code === 0 || response?.code === 200) {
        uiStore.showNotification('创建成功', 'success', 2000)
        showModal.value = false
        await loadAllTargets()
      } else {
        uiStore.showNotification(response?.message || '创建失败', 'error', 3000)
      }
    }
  } catch (error) {
    console.error('【目标】保存失败:', error)
    uiStore.showNotification(error.message || '保存失败，请重试', 'error', 3000)
  }
}

const deleteTarget = async (targetId) => {
  try {
    if (!confirm('确定要删除此目标吗？')) {
      return
    }

    console.log('【目标】删除目标:', targetId)
    const response = await userTargetAPI.delete(targetId)
    console.log('【目标】删除响应:', response)
    
    if (response?.code === 0 || response?.code === 200) {
      uiStore.showNotification('删除成功', 'success', 2000)
      await loadAllTargets()
    } else {
      uiStore.showNotification(response?.message || '删除失败', 'error', 3000)
    }
  } catch (error) {
    console.error('【目标】删除失败:', error)
    uiStore.showNotification(error.message || '删除失败，请重试', 'error', 3000)
  }
}

onMounted(async () => {
  console.log('【目标】页面已挂载，开始加载数据')
  await loadAllTargets()
})
</script>

<style scoped>
.page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 15px;
}

.page-header h1 {
  font-size: 28px;
  font-weight: bold;
  margin: 0;
  color: #333;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
  height: 40px;
}

.header-actions input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  min-width: 140px;
  height: 40px;
  box-sizing: border-box;
  vertical-align: middle;
}

/* 覆盖 form-group 默认样式，用于 header-actions 中的 Input 组件 */
.header-actions :deep(.form-group) {
  margin-bottom: 0 !important;
  display: flex;
  align-items: center;
}

.header-actions :deep(.form-group label) {
  display: none;
}

.header-actions :deep(.form-group input) {
  margin-bottom: 0;
  height: 40px;
  padding: 8px 12px;
}

/* 强制对齐按钮和输入框高度 */
.header-actions :deep(.btn) {
  padding: 8px 16px !important;
  min-height: 40px !important;
  height: 40px !important;
}

.section {
  margin-bottom: 40px;
}

.section h2 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 15px;
  color: #333;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #999;
  font-size: 14px;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  background: #f9f9f9;
  border-radius: 8px;
  color: #999;
}

.empty-state p {
  margin-bottom: 15px;
  font-size: 14px;
}

.link-btn {
  background: none;
  border: none;
  color: #1890ff;
  cursor: pointer;
  font-size: 14px;
  padding: 0;
}

.link-btn:hover {
  color: #40a9ff;
  text-decoration: underline;
}

.link-btn.danger {
  color: #ff4d4f;
}

.link-btn.danger:hover {
  color: #ff7875;
}

/* 年度模式网格样式 */
.year-target-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
}

.year-target-item {
  padding: 20px;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  background: #fff;
  transition: all 0.3s;
}

.year-target-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-color: #1890ff;
}

.item-month {
  font-size: 16px;
  font-weight: 600;
  color: #666;
  margin-bottom: 12px;
}

.item-info {
  margin-bottom: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
  font-size: 13px;
}

.info-row .label {
  color: #999;
}

.info-row .value {
  font-weight: 600;
  color: #1890ff;
}

.progress-container {
  margin-bottom: 12px;
}

.progress-bar {
  height: 6px;
  background: #f0f0f0;
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 6px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(to right, #1890ff, #40a9ff);
  transition: width 0.3s ease;
  border-radius: 3px;
}

.progress-fill.progress-complete {
  background: linear-gradient(to right, #52c41a, #85ce61);
}

.progress-text {
  text-align: center;
  font-size: 12px;
  color: #666;
  font-weight: 600;
}

/* 暂未设置目标的卡片样式 */
.year-target-item.no-target {
  background: #fafafa;
  border-color: #f0f0f0;
  opacity: 0.9;
}

.year-target-item.no-target:hover {
  border-color: #ffa940;
  background: #fffbe6;
}

.item-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60px;
  padding: 12px 0;
}

.item-empty .empty-text {
  font-size: 14px;
  color: #999;
  margin: 0;
  font-weight: 500;
}

.item-empty .empty-tip {
  font-size: 12px;
  color: #ccc;
  margin: 4px 0 0 0;
}

.item-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}
</style>
