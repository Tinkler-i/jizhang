<template>
  <div class="dashboard">
    <div class="page-header">
      <h1>仪表盘</h1>
      <p class="page-subtitle">{{ currentMonthDisplay }}</p>
    </div>

    <!-- 关键指标卡片区 -->
    <section class="metrics-section">
      <h2>本月关键指标</h2>
      <div class="metrics-grid">
        <Card variant="metric">
          <div class="metric-card">
            <div class="metric-icon income">💵</div>
            <div class="metric-content">
              <p class="metric-label">本月收入</p>
              <p class="metric-value">¥ {{ metrics.income || '0.00' }}</p>
              <p class="metric-target">
                目标: ¥ {{ metrics.targetIncome || '0.00' }}
                <button class="edit-btn" @click="showEditTargetModal = true" title="编辑目标">✏️</button>
              </p>
            </div>
          </div>
        </Card>

        <Card variant="metric">
          <div class="metric-card">
            <div class="metric-icon expense">💸</div>
            <div class="metric-content">
              <p class="metric-label">本月支出</p>
              <p class="metric-value">¥ {{ metrics.expense || '0.00' }}</p>
              <p class="metric-target">预算: ¥ {{ metrics.budgetExpense || '0.00' }}</p>
            </div>
          </div>
        </Card>

        <Card variant="metric">
          <div class="metric-card">
            <div class="metric-icon balance">💰</div>
            <div class="metric-content">
              <p class="metric-label">净收入</p>
              <p class="metric-value" :class="{ 'text-danger': metrics.balance < 0 }">
                ¥ {{ metrics.balance || '0.00' }}
              </p>
              <p class="metric-target">收入比: {{ metrics.profitRate || '0%' }}</p>
            </div>
          </div>
        </Card>

        <Card variant="metric">
          <div class="metric-card">
            <div class="metric-icon budget">📊</div>
            <div class="metric-content">
              <p class="metric-label">预算消耗</p>
              <p class="metric-value">{{ metrics.budgetUsage || '0%' }}</p>
              <p class="metric-target">
                <span v-if="metrics.budgetStatus === 'over'" class="text-danger">超出预算</span>
                <span v-else class="text-success">在预算内</span>
              </p>
            </div>
          </div>
        </Card>
      </div>
    </section>

    <!-- 图表区域 -->
    <div class="charts-section">
      <div class="chart-container">
        <Card>
          <template #header>
            <h3>收入支出趋势</h3>
          </template>
          <div class="chart-placeholder">
            <canvas ref="trendChart"></canvas>
          </div>
        </Card>
      </div>
    </div>

    <!-- 分类分布图表区域 -->
    <div class="category-charts-section">
      <div class="category-chart-container">
        <Card>
          <template #header>
            <h3>收入分类分布</h3>
            <span class="chart-total">总计: ¥{{ incomeCategoryTotal.toFixed(2) }}</span>
          </template>
          <div class="category-content">
            <div class="chart-placeholder">
              <canvas ref="incomeCategoryChart"></canvas>
            </div>
            <div class="category-details">
              <div 
                v-for="item in incomeCategoryDetails"
                :key="item.categoryId"
                class="category-item"
                @click="selectedIncomeCategory = item"
                :class="{ active: selectedIncomeCategory?.categoryId === item.categoryId }"
              >
                <div class="category-name">{{ item.categoryName }}</div>
                <div class="category-amount">¥{{ item.amount.toFixed(2) }}</div>
                <div class="category-percentage">{{ item.percentage.toFixed(2) }}%</div>
              </div>
            </div>
          </div>
        </Card>
      </div>

      <div class="category-chart-container">
        <Card>
          <template #header>
            <h3>支出分类分布</h3>
            <span class="chart-total">总计: ¥{{ expenseCategoryTotal.toFixed(2) }}</span>
          </template>
          <div class="category-content">
            <div class="chart-placeholder">
              <canvas ref="expenseCategoryChart"></canvas>
            </div>
            <div class="category-details">
              <div 
                v-for="item in expenseCategoryDetails"
                :key="item.categoryId"
                class="category-item"
                @click="selectedExpenseCategory = item"
                :class="{ active: selectedExpenseCategory?.categoryId === item.categoryId }"
              >
                <div class="category-name">{{ item.categoryName }}</div>
                <div class="category-amount">¥{{ item.amount.toFixed(2) }}</div>
                <div class="category-percentage">{{ item.percentage.toFixed(2) }}%</div>
              </div>
            </div>
          </div>
        </Card>
      </div>
    </div>

    <!-- 快速操作 -->
    <section class="quick-actions">
      <h2>快速操作</h2>
      <div class="action-buttons">
        <Button type="primary" @click="$router.push('/expense')">
          📝 记录支出
        </Button>
        <Button type="primary" @click="$router.push('/income')">
          📊 记录收入
        </Button>
        <Button type="primary" @click="$router.push('/budget')">
          💹 管理预算
        </Button>
        <Button type="primary" @click="$router.push('/report')">
          📈 查看报表
        </Button>
      </div>
    </section>

    <!-- 编辑目标模态框 -->
    <Modal
      v-model:visible="showEditTargetModal"
      title="编辑本月收入目标"
      @confirm="saveIncomeTarget"
    >
      <div class="form-group">
        <label>本月收入目标（¥）</label>
        <Input
          v-model="targetForm.incomeTarget"
          type="number"
          placeholder="请输入本月收入目标"
        />
      </div>
    </Modal>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { analysisAPI, reportAPI, userTargetAPI } from '../api'
import Card from '../components/Card.vue'
import Button from '../components/Button.vue'
import Modal from '../components/Modal.vue'
import Input from '../components/Input.vue'
import Chart from 'chart.js/auto'

const trendChart = ref(null)
const incomeCategoryChart = ref(null)
const expenseCategoryChart = ref(null)
let trendChartInstance = null
let incomeCategoryChartInstance = null
let expenseCategoryChartInstance = null

const showEditTargetModal = ref(false)
const targetForm = reactive({
  incomeTarget: '0'
})

const incomeCategoryDetails = ref([])
const expenseCategoryDetails = ref([])
const selectedIncomeCategory = ref(null)
const selectedExpenseCategory = ref(null)
const incomeCategoryTotal = ref(0)
const expenseCategoryTotal = ref(0)

const metrics = reactive({
  income: '0.00',
  expense: '0.00',
  balance: '0.00',
  targetIncome: '0.00',
  budgetExpense: '0.00',
  budgetUsage: '0%',
  budgetStatus: 'normal',
  profitRate: '0%'
})

const currentMonthDisplay = computed(() => {
  const now = new Date()
  return `${now.getFullYear()}年${now.getMonth() + 1}月`
})

const loadDashboardData = async () => {
  try {
    const month = new Date().toISOString().slice(0, 7)
    console.log('【仪表盘】加载月份数据，月份:', month)
    const response = await reportAPI.getSummary({ month })
    console.log('【仪表盘】原始响应:', JSON.stringify(response, null, 2))
    
    if (response && response.code === 200 && response.data) {
      const data = response.data
      console.log('【仪表盘】解析数据:', JSON.stringify(data, null, 2))
      
      metrics.income = (data.totalIncome || 0).toFixed(2)
      metrics.expense = (data.totalExpense || 0).toFixed(2)
      metrics.balance = ((data.totalIncome || 0) - (data.totalExpense || 0)).toFixed(2)
      metrics.targetIncome = (data.targetIncome || 0).toFixed(2)
      metrics.budgetExpense = (data.budgetExpense || 0).toFixed(2)
      metrics.budgetUsage = data.budgetUsage || '0%'
      metrics.profitRate = data.profitRate || '0%'
      
      // 设置编辑表单的初始值
      targetForm.incomeTarget = data.targetIncome || '0'
      
      console.log('【仪表盘】更新后的 metrics:', JSON.stringify(metrics, null, 2))
    } else {
      console.warn('【仪表盘】响应格式错误或无数据:', response)
    }
  } catch (error) {
    console.error('【仪表盘】加载失败:', error)
  }
}

const loadCharts = async () => {
  try {
    const month = new Date().toISOString().slice(0, 7)
    console.log('【图表】加载图表数据，月份:', month)
    
    // 加载趋势图数据
    const trendData = await reportAPI.getIncomeChart(month)
    console.log('【趋势图】原始响应:', JSON.stringify(trendData, null, 2))
    if (trendData && trendData.code === 200 && trendData.data) {
      console.log('【趋势图】绘制数据:', JSON.stringify(trendData.data, null, 2))
      drawTrendChart(trendData.data)
    } else {
      console.warn('【趋势图】数据格式错误:', trendData)
    }

    // 加载收入分类分布图
    const incomeCategoryData = await reportAPI.getIncomeCategoryChart(month)
    console.log('【收入分类图】原始响应:', JSON.stringify(incomeCategoryData, null, 2))
    if (incomeCategoryData && incomeCategoryData.code === 200 && incomeCategoryData.data) {
      console.log('【收入分类图】绘制数据:', JSON.stringify(incomeCategoryData.data, null, 2))
      drawIncomeCategoryChart(incomeCategoryData.data)
    } else {
      console.warn('【收入分类图】数据格式错误:', incomeCategoryData)
    }

    // 加载支出分类分布图
    const expenseCategoryData = await reportAPI.getExpenseChart(month)
    console.log('【支出分类图】原始响应:', JSON.stringify(expenseCategoryData, null, 2))
    if (expenseCategoryData && expenseCategoryData.code === 200 && expenseCategoryData.data) {
      console.log('【支出分类图】绘制数据:', JSON.stringify(expenseCategoryData.data, null, 2))
      drawExpenseCategoryChart(expenseCategoryData.data)
    } else {
      console.warn('【支出分类图】数据格式错误:', expenseCategoryData)
    }
  } catch (error) {
    console.error('【图表】加载失败:', error)
  }
}

const drawTrendChart = (data) => {
  if (trendChartInstance) trendChartInstance.destroy()
  
  const ctx = trendChart.value
  if (!ctx) return

  trendChartInstance = new Chart(ctx, {
    type: 'line',
    data: {
      labels: data.labels || [],
      datasets: [
        {
          label: '收入',
          data: data.income || [],
          borderColor: '#27ae60',
          backgroundColor: 'rgba(39, 174, 96, 0.1)',
          tension: 0.3
        },
        {
          label: '支出',
          data: data.expense || [],
          borderColor: '#e74c3c',
          backgroundColor: 'rgba(231, 76, 60, 0.1)',
          tension: 0.3
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: true,
      plugins: {
        legend: {
          display: true,
          position: 'top'
        }
      }
    }
  })
}

const drawIncomeCategoryChart = (data) => {
  if (incomeCategoryChartInstance) incomeCategoryChartInstance.destroy()
  
  const ctx = incomeCategoryChart.value
  if (!ctx) return

  // 存储详细数据用于交互
  if (data.details) {
    incomeCategoryDetails.value = data.details.map(d => ({
      ...d,
      amount: Number(d.amount),
      percentage: Number(d.percentage)
    }))
  }
  if (data.total) {
    incomeCategoryTotal.value = Number(data.total)
  }

  incomeCategoryChartInstance = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: data.labels || [],
      datasets: [
        {
          data: data.values || [],
          backgroundColor: [
            '#667eea',
            '#764ba2',
            '#f093fb',
            '#4facfe',
            '#00f2fe',
            '#43e97b',
            '#fa709a',
            '#fee140',
            '#34c759',
            '#ff3b30'
          ],
          borderWidth: 2,
          borderColor: '#fff'
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: true,
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            padding: 15,
            font: {
              size: 12
            }
          }
        }
      }
    }
  })
}

const drawExpenseCategoryChart = (data) => {
  if (expenseCategoryChartInstance) expenseCategoryChartInstance.destroy()
  
  const ctx = expenseCategoryChart.value
  if (!ctx) return

  // 存储详细数据用于交互
  if (data.details) {
    expenseCategoryDetails.value = data.details.map(d => ({
      ...d,
      amount: Number(d.amount),
      percentage: Number(d.percentage)
    }))
  }
  if (data.total) {
    expenseCategoryTotal.value = Number(data.total)
  }

  expenseCategoryChartInstance = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: data.labels || [],
      datasets: [
        {
          data: data.values || [],
          backgroundColor: [
            '#667eea',
            '#764ba2',
            '#f093fb',
            '#4facfe',
            '#00f2fe',
            '#43e97b',
            '#fa709a',
            '#fee140',
            '#34c759',
            '#ff3b30'
          ],
          borderWidth: 2,
          borderColor: '#fff'
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: true,
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            padding: 15,
            font: {
              size: 12
            }
          }
        }
      }
    }
  })
}

const saveIncomeTarget = async () => {
  try {
    const month = new Date().toISOString().slice(0, 7)
    console.log('【仪表盘】保存收入目标，月份:', month, '目标:', targetForm.incomeTarget)
    
    const response = await userTargetAPI.update(month, targetForm.incomeTarget)
    console.log('【仪表盘】保存响应:', JSON.stringify(response, null, 2))
    
    if (response && response.code === 200) {
      metrics.targetIncome = parseFloat(targetForm.incomeTarget).toFixed(2)
      showEditTargetModal.value = false
      console.log('【仪表盘】保存成功')
    } else {
      console.error('【仪表盘】保存失败:', response)
    }
  } catch (error) {
    console.error('【仪表盘】保存异常:', error)
  }
}

onMounted(() => {
  loadDashboardData()
  setTimeout(() => loadCharts(), 500)
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  color: #333;
  margin-bottom: 5px;
}

.page-subtitle {
  color: #999;
  font-size: 14px;
}

.metrics-section {
  margin-bottom: 40px;
}

.metrics-section h2 {
  font-size: 18px;
  color: #333;
  margin-bottom: 20px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.metric-card {
  display: flex;
  align-items: center;
  gap: 20px;
}

.metric-icon {
  font-size: 32px;
  flex-shrink: 0;
}

.metric-content {
  flex: 1;
}

.metric-label {
  font-size: 12px;
  color: #999;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin: 0 0 8px 0;
}

.metric-value {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin: 0 0 4px 0;
}

.metric-target {
  font-size: 12px;
  color: #999;
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.edit-btn {
  background: none;
  border: none;
  padding: 0;
  cursor: pointer;
  font-size: 12px;
  opacity: 0.6;
  transition: opacity 0.2s;
}

.edit-btn:hover {
  opacity: 1;
}

.charts-section {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
  margin-bottom: 40px;
}

.chart-container {
  width: 100%;
}

.chart-placeholder {
  position: relative;
  height: 300px;
}

.category-charts-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.category-chart-container {
  width: 100%;
}

.category-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  align-items: flex-start;
}

.category-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 300px;
  overflow-y: auto;
  padding-right: 10px;
}

.category-item {
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-left: 4px solid #f0f0f0;
}

.category-item:hover {
  background-color: #f0f0f0;
  transform: translateX(4px);
}

.category-item.active {
  background-color: #e8f5e9;
  border-left-color: #27ae60;
}

.category-name {
  font-size: 13px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.category-amount {
  font-size: 14px;
  font-weight: 700;
  color: #27ae60;
  margin-bottom: 2px;
}

.category-percentage {
  font-size: 12px;
  color: #999;
}

.chart-total {
  font-size: 12px;
  color: #666;
  margin-left: 10px;
  font-weight: normal;
}

.quick-actions {
  margin-bottom: 20px;
}

.quick-actions h2 {
  font-size: 18px;
  color: #333;
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.text-danger {
  color: #e74c3c !important;
}

.text-success {
  color: #27ae60 !important;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

@media (max-width: 768px) {
  .metrics-grid {
    grid-template-columns: 1fr;
  }

  .category-charts-section {
    grid-template-columns: 1fr;
  }

  .category-content {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    flex-direction: column;
  }
}
</style>
