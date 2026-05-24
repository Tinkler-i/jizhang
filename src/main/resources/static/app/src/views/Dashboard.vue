<template>
  <div class="dashboard">
    <div class="page-header">
      <h1>仪表盘</h1>
      <p class="page-subtitle">{{ pageSubtitle }}</p>
    </div>

    <!-- 模式切换和时间选择 -->
    <div class="mode-selector">
      <div class="mode-buttons">
        <button 
          :class="['mode-btn', { active: modeType === 'monthly' }]"
          @click="modeType = 'monthly'">
          📅 月度模式
        </button>
        <button 
          :class="['mode-btn', { active: modeType === 'yearly' }]"
          @click="modeType = 'yearly'">
          📊 年度模式
        </button>
      </div>
      
      <!-- 月份选择器（仅月度模式显示） -->
      <div v-if="modeType === 'monthly'" class="month-selector">
        <input 
          type="month" 
          v-model="selectedMonth"
          @change="handleMonthChange"
          class="month-input"
        >
        <button class="quick-btn" @click="selectCurrentMonth">本月</button>
      </div>

      <!-- 年份选择器（仅年度模式显示） -->
      <div v-if="modeType === 'yearly'" ref="yearSelectorRef" class="year-selector">
        <div class="year-input-group">
          <input 
            type="text"
            :value="`${selectedYear}年`"
            @click="toggleYearPicker"
            class="year-input"
            readonly
          >
          <span class="year-icon" @click="toggleYearPicker">📅</span>
          
          <!-- 年份选择面板 -->
          <div v-if="showYearPicker" class="year-picker">
            <div class="year-picker-header">
              <button class="year-nav-btn" @click="previousYearRange">❮</button>
              <span class="year-range-text">{{ yearRangeStart }}-{{ yearRangeEnd }}</span>
              <button class="year-nav-btn" @click="nextYearRange">❯</button>
            </div>
            <div class="year-picker-grid">
              <button 
                v-for="year in displayYears" 
                :key="year"
                :class="['year-button', { active: year === selectedYear }]"
                @click="selectYearFromPicker(year)"
              >
                {{ year }}
              </button>
            </div>
          </div>
        </div>
        <button class="quick-btn" @click="selectCurrentYear">本年</button>
      </div>
    </div>

    <!-- 关键指标卡片区 -->
    <section class="metrics-section">
      <h2>{{ metricsTitle }}</h2>
      <div class="metrics-grid">
        <Card variant="metric">
          <div class="metric-card">
            <div class="metric-icon income">💵</div>
            <div class="metric-content">
              <p class="metric-label">{{ incomeLabel }}</p>
              <p class="metric-value">¥ {{ metrics.income || '0.00' }}</p>
            </div>
          </div>
        </Card>

        <Card variant="metric">
          <div class="metric-card">
            <div class="metric-icon expense">💸</div>
            <div class="metric-content">
              <p class="metric-label">{{ expenseLabel }}</p>
              <p class="metric-value">¥ {{ metrics.expense || '0.00' }}</p>
              <p class="metric-target">预算: ¥ {{ metrics.budgetExpense || '0.00' }}</p>
            </div>
          </div>
        </Card>

        <Card variant="metric">
          <div class="metric-card">
            <div class="metric-icon balance">💰</div>
            <div class="metric-content">
              <p class="metric-label">攒下</p>
              <p class="metric-value" :class="{ 'text-danger': metrics.balance < 0 }">
                ¥ {{ metrics.balance || '0.00' }}
              </p>
              <p class="metric-target">
                达成度: <span :class="{ 'text-success': achievementRate >= 100, 'text-danger': achievementRate < 50 }">{{ achievementRate }}%</span> (¥{{ metrics.targetIncome || '0.00' }})
              </p>
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
          <div v-show="hasTrendData" class="chart-placeholder">
            <canvas ref="trendChart"></canvas>
          </div>
          <div v-show="!hasTrendData" class="empty-state-compact">
            <p>📊 此{{ modeType === 'monthly' ? '月份' : '年份' }}暂时没有财务数据</p>
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
            <span v-show="hasIncomeCategoryData" class="chart-total">总计: ¥{{ incomeCategoryTotal.toFixed(2) }}</span>
          </template>
          <div v-show="hasIncomeCategoryData" class="category-content">
            <div class="chart-placeholder">
              <canvas ref="incomeCategoryChart"></canvas>
            </div>
            <div class="category-details">
              <div 
                v-for="item in incomeCategoryDetails"
                :key="item.categoryId"
                class="category-item"
                @click="selectedIncomeCategory = selectedIncomeCategory?.categoryId === item.categoryId ? null : item"
                :class="{ active: selectedIncomeCategory?.categoryId === item.categoryId }"
              >
                <div class="category-name">{{ item.categoryName }}</div>
                <div class="category-amount">¥{{ item.amount.toFixed(2) }}</div>
                <div class="category-percentage">{{ item.percentage.toFixed(2) }}%</div>
              </div>
            </div>
          </div>
          <div v-show="!hasIncomeCategoryData && !incomeCategoryDetails.length" class="empty-state-compact">
            <p>📊 此{{ modeType === 'monthly' ? '月份' : '年份' }}暂时没有收入数据</p>
          </div>
        </Card>
      </div>

      <div class="category-chart-container">
        <Card>
          <template #header>
            <h3>支出分类分布</h3>
            <span v-show="hasExpenseCategoryData" class="chart-total">总计: ¥{{ expenseCategoryTotal.toFixed(2) }}</span>
          </template>
          <div v-show="hasExpenseCategoryData" class="category-content">
            <div class="chart-placeholder">
              <canvas ref="expenseCategoryChart"></canvas>
            </div>
            <div class="category-details">
              <div 
                v-for="item in expenseCategoryDetails"
                :key="item.categoryId"
                class="category-item"
                @click="selectedExpenseCategory = selectedExpenseCategory?.categoryId === item.categoryId ? null : item"
                :class="{ active: selectedExpenseCategory?.categoryId === item.categoryId }"
              >
                <div class="category-name">{{ item.categoryName }}</div>
                <div class="category-amount">¥{{ item.amount.toFixed(2) }}</div>
                <div class="category-percentage">{{ item.percentage.toFixed(2) }}%</div>
              </div>
            </div>
          </div>
          <div v-show="!hasExpenseCategoryData && !expenseCategoryDetails.length" class="empty-state-compact">
            <p>📊 此{{ modeType === 'monthly' ? '月份' : '年份' }}暂时没有支出数据</p>
          </div>
        </Card>
      </div>
    </div>

    <!-- 预算执行情况 -->
    <div class="analysis-charts-section">
      <div class="analysis-chart-container">
        <Card>
          <template #header>
            <h3>预算执行情况</h3>
          </template>
          <div v-show="hasBudgetData" class="chart-placeholder">
            <canvas ref="budgetChart"></canvas>
          </div>
          <div v-show="!hasBudgetData" class="empty-state-compact">
            <p>📊 此{{ modeType === 'monthly' ? '月份' : '年份' }}暂时没有预算数据</p>
          </div>
        </Card>
      </div>
    </div>


    <!-- 年度模式目标进度图表 -->
    <div v-if="modeType === 'yearly'" class="yearly-targets-chart-section">
      <Card>
        <template #header>
          <h3>{{ selectedYear }} 年目标进度对比</h3>
        </template>
        <div v-if="yearlyTargetsData.length > 0" class="chart-placeholder">
          <canvas ref="yearlyTargetsChart"></canvas>
        </div>
        <div v-else class="empty-state-compact">
          <p>📊 暂无年度目标数据，请先在目标管理中设置目标</p>
        </div>
      </Card>
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
      v-model="showEditTargetModal"
      :title="modeType === 'monthly' ? '编辑本月攒下的目标' : '编辑年度攒下的目标'"
    >
      <div class="form-group">
        <label>{{ modeType === 'monthly' ? '本月' : '年度' }}攒下的目标（¥）</label>
        <Input
          v-model="targetForm.incomeTarget"
          type="number"
          :placeholder="modeType === 'monthly' ? '请输入本月攒下的目标' : '请输入年度攒下的目标'"
        />
        <p v-if="modeType === 'yearly'" class="target-tip">
          💡 提示：该目标金额将被平均分配到全年 12 个月。例如输入 120,000 时，每个月的目标将为 10,000。
        </p>
      </div>
      <template #footer>
        <Button type="secondary" @click="showEditTargetModal = false">取消</Button>
        <Button type="primary" @click="saveIncomeTarget">保存</Button>
      </template>
    </Modal>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { analysisAPI, reportAPI, userTargetAPI, incomeAPI, expenseAPI } from '../api'
import Card from '../components/Card.vue'
import Button from '../components/Button.vue'
import Modal from '../components/Modal.vue'
import Input from '../components/Input.vue'
import Chart from 'chart.js/auto'

const trendChart = ref(null)
const incomeCategoryChart = ref(null)
const expenseCategoryChart = ref(null)
const budgetChart = ref(null)
const yearlyTargetsChart = ref(null)
let trendChartInstance = null
let incomeCategoryChartInstance = null
let expenseCategoryChartInstance = null
let budgetChartInstance = null
let yearlyTargetsChartInstance = null

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
const incomeCategoryRawData = ref(null)
const expenseCategoryRawData = ref(null)

// 图表数据状态标志
const hasTrendData = ref(false)
const hasIncomeCategoryData = ref(false)
const hasExpenseCategoryData = ref(false)
const hasBudgetData = ref(false)

// 年度目标进度数据
const yearlyTargetsData = ref([])

// 年份选择器ref
const yearSelectorRef = ref(null)

// 模式相关
const modeType = ref('monthly') // 'monthly' 或 'yearly'
const selectedMonth = ref('')
const selectedYear = ref(new Date().getFullYear())
const currentYear = new Date().getFullYear()

// 请求序号用于避免模式切换时旧请求回写新状态
let dashboardRequestSeq = 0
let chartsRequestSeq = 0

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

const pageSubtitle = computed(() => {
  if (modeType.value === 'monthly') {
    const date = new Date(selectedMonth.value)
    return `${date.getFullYear()}年${date.getMonth() + 1}月`
  } else {
    return `${selectedYear.value}年度`
  }
})

const metricsTitle = computed(() => {
  return modeType.value === 'monthly' ? '本月关键指标' : '年度关键指标'
})

const incomeLabel = computed(() => {
  return modeType.value === 'monthly' ? '本月收入' : '年度收入'
})

const expenseLabel = computed(() => {
  return modeType.value === 'monthly' ? '本月支出' : '年度支出'
})

const currentMonthDisplay = computed(() => {
  return pageSubtitle.value
})

const achievementRate = computed(() => {
  const balance = parseFloat(metrics.balance) || 0
  const target = parseFloat(metrics.targetIncome) || 0
  if (target <= 0) return 0
  const rate = Math.round((balance / target) * 100)
  return Math.max(0, rate)
})

const getCurrentPeriod = () => {
  return modeType.value === 'monthly' ? selectedMonth.value : `${selectedYear.value}`
}

// 年度目标相关计算属性 - 已移至Report.vue
// const yearlyTotalTarget = computed(() => {
//   return yearlyTargetsData.value.reduce((sum, item) => sum + item.target, 0)
// })

// const yearlyTotalSavings = computed(() => {
//   return yearlyTargetsData.value.reduce((sum, item) => sum + item.savings, 0)
// })

// const yearlyAchievementRate = computed(() => {
//   const total = yearlyTotalTarget.value
//   const actual = yearlyTotalSavings.value
//   if (total <= 0) return 0
//   const rate = Math.round((actual / total) * 100)
//   return Math.max(0, rate)
// })

const selectCurrentMonth = () => {
  const now = new Date()
  selectedMonth.value = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
  handleMonthChange()
}

const handleMonthChange = async () => {
  selectedIncomeCategory.value = null
  selectedExpenseCategory.value = null
  loadDashboardData()
  await new Promise(resolve => setTimeout(resolve, 100))
  await loadCharts()
}

// 年份选择器相关
const showYearPicker = ref(false)
const yearPickerStart = ref(new Date().getFullYear() - 6)

const displayYears = computed(() => {
  const years = []
  for (let i = 0; i < 12; i++) {
    years.push(yearPickerStart.value + i)
  }
  return years
})

const yearRangeStart = computed(() => yearPickerStart.value)
const yearRangeEnd = computed(() => yearPickerStart.value + 11)

const toggleYearPicker = () => {
  showYearPicker.value = !showYearPicker.value
}

const previousYearRange = () => {
  yearPickerStart.value -= 12
}

const nextYearRange = () => {
  yearPickerStart.value += 12
}

const selectYearFromPicker = (year) => {
  selectedYear.value = year
  showYearPicker.value = false
  handleYearChange()
}

const selectCurrentYear = () => {
  selectedYear.value = new Date().getFullYear()
  handleYearChange()
}

const handleYearChange = async () => {
  selectedIncomeCategory.value = null
  selectedExpenseCategory.value = null
  loadDashboardData()
  await new Promise(resolve => setTimeout(resolve, 100))
  await loadCharts()
  // 年度模式下加载并绘制年度目标图表
  if (modeType.value === 'yearly') {
    await loadYearlyTargetsProgress()
    await new Promise(resolve => setTimeout(resolve, 100))
    drawYearlyTargetsChart()
  }
}

const loadDashboardData = async () => {
  const requestId = ++dashboardRequestSeq
  const requestMode = modeType.value
  const requestPeriod = getCurrentPeriod()

  try {
    const month = requestPeriod
    
    console.log('【仪表盘】加载数据，模式:', modeType.value, '时间:', month)
    const response = await reportAPI.getSummary({ month })

    // 若模式或时间已切换，丢弃旧请求结果
    if (requestId !== dashboardRequestSeq || requestMode !== modeType.value || requestPeriod !== getCurrentPeriod()) {
      console.log('【仪表盘】丢弃过期摘要响应，模式/时间已变更')
      return
    }

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
      metrics.income = '0.00'
      metrics.expense = '0.00'
      metrics.balance = '0.00'
      metrics.targetIncome = '0.00'
      metrics.budgetExpense = '0.00'
      metrics.budgetUsage = '0%'
      metrics.profitRate = '0%'
    }
  } catch (error) {
    console.error('【仪表盘】加载失败:', error)
    metrics.income = '0.00'
    metrics.expense = '0.00'
    metrics.balance = '0.00'
    metrics.targetIncome = '0.00'
    metrics.budgetExpense = '0.00'
    metrics.budgetUsage = '0%'
    metrics.profitRate = '0%'
  }
}

const loadCharts = async () => {
  const requestId = ++chartsRequestSeq
  const requestMode = modeType.value
  const requestPeriod = getCurrentPeriod()

  const isStaleRequest = () => {
    return requestId !== chartsRequestSeq || requestMode !== modeType.value || requestPeriod !== getCurrentPeriod()
  }

  try {
    const month = requestPeriod
    console.log('【图表】加载图表数据，时间:', month)
    
    // 重置总计值，确保数据更新时显示新数据
    incomeCategoryTotal.value = 0
    expenseCategoryTotal.value = 0
    
    // 重置数据状态标志
    hasTrendData.value = false
    hasIncomeCategoryData.value = false
    hasExpenseCategoryData.value = false
    hasBudgetData.value = false
    
    // 加载趋势图数据
    const trendData = await reportAPI.getIncomeChart(month)
    if (isStaleRequest()) return
    console.log('【趋势图】原始响应:', JSON.stringify(trendData, null, 2))
    if (trendData && trendData.code === 200 && trendData.data) {
      console.log('【趋势图】绘制数据:', JSON.stringify(trendData.data, null, 2))
      drawTrendChart(trendData.data, modeType.value)
    } else {
      console.warn('【趋势图】数据格式错误:', trendData)
    }

    // 加载收入分类分布图
    const incomeCategoryData = await reportAPI.getIncomeCategoryChart(month)
    if (isStaleRequest()) return
    console.log('【收入分类图】原始响应:', JSON.stringify(incomeCategoryData, null, 2))
    if (incomeCategoryData && incomeCategoryData.code === 200 && incomeCategoryData.data) {
      console.log('【收入分类图】绘制数据:', JSON.stringify(incomeCategoryData.data, null, 2))
      drawIncomeCategoryChart(incomeCategoryData.data)
    } else {
      console.warn('【收入分类图】数据格式错误:', incomeCategoryData)
    }

    // 加载支出分类分布图
    const expenseCategoryData = await reportAPI.getExpenseChart(month)
    if (isStaleRequest()) return
    console.log('【支出分类图】原始响应:', JSON.stringify(expenseCategoryData, null, 2))
    if (expenseCategoryData && expenseCategoryData.code === 200 && expenseCategoryData.data) {
      console.log('【支出分类图】绘制数据:', JSON.stringify(expenseCategoryData.data, null, 2))
      drawExpenseCategoryChart(expenseCategoryData.data)
    } else {
      console.warn('【支出分类图】数据格式错误:', expenseCategoryData)
    }

    // 加载预算执行情况
    try {
      const budgetData = await analysisAPI.getBudgetVsActual(month)
      if (isStaleRequest()) return
      console.log('【预算图】原始响应:', JSON.stringify(budgetData, null, 2))
      if (budgetData && budgetData.code === 200 && budgetData.data) {
        console.log('【预算图】绘制数据:', JSON.stringify(budgetData.data, null, 2))
        drawBudgetChart(budgetData.data)
      } else {
        console.warn('【预算图】数据格式错误:', budgetData)
      }
    } catch (error) {
      console.error('【预算图】加载失败:', error)
    }
  } catch (error) {
    console.error('【图表】加载失败:', error)
  }
}

const drawTrendChart = (data, mode) => {
  try {
    if (trendChartInstance) trendChartInstance.destroy()
    
    const ctx = trendChart.value
    if (!ctx) {
      console.warn('【趋势图】canvas 上下文不存在')
      hasTrendData.value = false
      return
    }

    // 数据已经是按日期聚合的格式
    const labels = data.labels || []
    const incomeValues = data.income || []
    const expenseValues = data.expense || []
    
    console.log('【趋势图】准备绘制，数据:', { labelsCount: labels.length })
    
    // 只有当真的有标签时才绘制
    if (!labels || labels.length === 0) {
      console.warn('【趋势图】没有数据：labels 为空')
      hasTrendData.value = false
      return
    }
    
    // 检查是否有有效的数据（至少有一个非零的收入或支出）
    const hasValidData = incomeValues.some(v => v !== 0) || expenseValues.some(v => v !== 0)
    if (!hasValidData) {
      console.warn('【趋势图】没有有效数据：所有收入和支出都是0')
      hasTrendData.value = false
      return
    }
    
    // 计算每日盈余
    const surplusValues = labels.map((_, index) => {
      return (incomeValues[index] || 0) - (expenseValues[index] || 0)
    })

    trendChartInstance = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: labels,
        datasets: [
          {
            label: '收入',
            data: incomeValues,
            borderColor: '#27ae60',
            backgroundColor: 'rgba(39, 174, 96, 0.1)',
            borderWidth: 2,
            tension: 0.3,
            type: 'line',
            order: 1
          },
          {
            label: '支出',
            data: expenseValues,
            borderColor: '#e74c3c',
            backgroundColor: 'rgba(231, 76, 60, 0.1)',
            borderWidth: 2,
            tension: 0.3,
            type: 'line',
            order: 1
          },
          {
            label: '盈余',
            data: surplusValues,
            backgroundColor: surplusValues.map(v => v >= 0 ? '#3498db' : '#f39c12'),
            borderColor: surplusValues.map(v => v >= 0 ? '#2980b9' : '#e67e22'),
            borderWidth: 1,
            borderRadius: 2,
            type: 'bar',
            order: 2,
            hidden: mode === 'monthly'
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: true,
            position: 'top'
          },
          tooltip: {
            callbacks: {
              label: function(context) {
                let value = context.parsed.y
                return context.dataset.label + ': ¥' + value.toFixed(2)
              }
            }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              callback: function(value) {
                return '¥' + value.toFixed(0)
              }
            }
          }
        }
      }
    })
    
    hasTrendData.value = true
    console.log('【趋势图】绘制完成')
  } catch (error) {
    console.error('【趋势图】绘制出错:', error)
    hasTrendData.value = false
  }
}

const drawIncomeCategoryChart = (data) => {
  try {
    if (incomeCategoryChartInstance) incomeCategoryChartInstance.destroy()
    
    const ctx = incomeCategoryChart.value
    if (!ctx) {
      console.warn('【收入分类图】canvas 上下文不存在')
      hasIncomeCategoryData.value = false
      return
    }

    // 检查是否有有效的数据
    console.log('【收入分类图】检查数据:', { labels: data.labels?.length, values: data.values?.length })
    
    // 只要有 labels 就可以尝试绘制
    if (!data.labels || !data.labels.length) {
      console.warn('【收入分类图】无有效数据：labels 为空')
      hasIncomeCategoryData.value = false
      incomeCategoryDetails.value = []
      incomeCategoryTotal.value = 0
      return
    }
    
    console.log('【收入分类图】开始绘制')

    // 存储原始数据
    incomeCategoryRawData.value = JSON.parse(JSON.stringify(data))

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

    const labels = data.labels || []
    const values = data.values || []
    const bgColors = [
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
    ]

    // 初始的offset全为0，通过watch来动态修改
    const offset = values.map(() => 0)

    incomeCategoryChartInstance = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: labels,
        datasets: [
          {
            data: values,
            backgroundColor: bgColors,
            borderWidth: 2,
            borderColor: '#fff',
            offset: offset
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
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
    
    hasIncomeCategoryData.value = true
    console.log('【收入分类图】绘制完成')
  } catch (error) {
    console.error('【收入分类图】绘制出错:', error)
    hasIncomeCategoryData.value = false
  }
}

const drawExpenseCategoryChart = (data) => {
  try {
    if (expenseCategoryChartInstance) expenseCategoryChartInstance.destroy()
    
    const ctx = expenseCategoryChart.value
    if (!ctx) {
      console.warn('【支出分类图】canvas 上下文不存在')
      hasExpenseCategoryData.value = false
      return
    }

    // 检查是否有有效的数据
    console.log('【支出分类图】检查数据:', { labels: data.labels?.length, values: data.values?.length })
    
    // 只要有 labels 就可以尝试绘制
    if (!data.labels || !data.labels.length) {
      console.warn('【支出分类图】无有效数据：labels 为空')
      hasExpenseCategoryData.value = false
      expenseCategoryDetails.value = []
      expenseCategoryTotal.value = 0
      return
    }
    
    console.log('【支出分类图】开始绘制')

    // 存储原始数据
    expenseCategoryRawData.value = JSON.parse(JSON.stringify(data))

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

    const labels = data.labels || []
    const values = data.values || []
    const bgColors = [
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
    ]

    // 初始的offset全为0，通过watch来动态修改
    const offset = values.map(() => 0)

    expenseCategoryChartInstance = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: labels,
        datasets: [
          {
            data: values,
            backgroundColor: bgColors,
            borderWidth: 2,
            borderColor: '#fff',
            offset: offset
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
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
    
    hasExpenseCategoryData.value = true
    console.log('【支出分类图】绘制完成')
  } catch (error) {
    console.error('【支出分类图】绘制出错:', error)
    hasExpenseCategoryData.value = false
  }
}

const drawBudgetChart = (data) => {
  if (budgetChartInstance) budgetChartInstance.destroy()
  
  const ctx = budgetChart.value
  if (!ctx) {
    console.warn('【预算图】canvas 上下文不存在')
    hasBudgetData.value = false
    return
  }

  // 处理数据格式：如果data是数组对象，提取categories, actual, budget
  let categories, actualValues, budgetValues
  
  if (Array.isArray(data)) {
    // 如果是数组格式（来自API的原始格式）
    categories = data.map(item => item.categoryName)
    actualValues = data.map(item => item.actualAmount)
    budgetValues = data.map(item => item.budgetAmount)
  } else {
    // 如果是预处理的格式
    categories = data.categories || []
    actualValues = data.actualValues || []
    budgetValues = data.budgetValues || []
  }
  
  // 检查是否有有效的数据
  console.log('【预算图】检查数据:', { categories: categories?.length })
  
  // 只要有 categories 就可以尝试绘制
  if (!categories || !categories.length) {
    console.warn('【预算图】无有效数据：categories 为空')
    hasBudgetData.value = false
    return
  }
  
  console.log('【预算图】开始绘制')
  hasBudgetData.value = true

  budgetChartInstance = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: categories,
      datasets: [
        {
          label: '实际消费',
          data: actualValues,
          backgroundColor: '#e74c3c',
          borderColor: '#c0392b',
          borderWidth: 1,
          borderRadius: 4
        },
        {
          label: '预算',
          data: budgetValues,
          backgroundColor: '#27ae60',
          borderColor: '#229954',
          borderWidth: 1,
          borderRadius: 4
        }
      ]
    },
    options: {
      indexAxis: 'y',
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            padding: 15,
            font: {
              size: 12
            }
          }
        },
        tooltip: {
          callbacks: {
            label: function(context) {
              return context.dataset.label + ': ¥' + context.parsed.x.toFixed(2)
            }
          }
        }
      },
      scales: {
        x: {
          ticks: {
            callback: function(value) {
              return '¥' + value.toFixed(0)
            }
          }
        }
      }
    }
  })
}

// 加载年度目标进度数据
const loadYearlyTargetsProgress = async () => {
  try {
    console.log('【仪表盘】加载年度目标进度，年份:', selectedYear.value)
    
    // 获取该年份所有目标
    const targetsResponse = await userTargetAPI.getAll()
    const allTargets = (targetsResponse?.code === 200 || targetsResponse?.code === 0) ? (targetsResponse.data || []) : []
    
    const year = selectedYear.value.toString()
    const yearTargets = allTargets.filter(t => t.targetMonth.startsWith(year))
    
    console.log(`【仪表盘】该年份的目标数: ${yearTargets.length}`, yearTargets)
    
    // 为每个月份组织数据
    const tableData = []
    
    for (const target of yearTargets) {
      const month = parseInt(target.targetMonth.substring(5))
      const monthStr = target.targetMonth
      
      try {
        // 获取该月的日期范围
        const [year, monthNum] = monthStr.split('-')
        const startDate = `${year}-${monthNum}-01`
        const lastDay = new Date(year, monthNum, 0).getDate()
        const endDate = `${year}-${monthNum}-${lastDay}`
        
        // 并行加载收入和支出
        const [incomeRes, expenseRes] = await Promise.all([
          incomeAPI.getList({ startDate, endDate }),
          expenseAPI.getList({ startDate, endDate })
        ])
        
        // 计算总收入
        let totalIncome = 0
        if (incomeRes?.code === 200 && incomeRes?.data && Array.isArray(incomeRes.data)) {
          totalIncome = incomeRes.data.reduce((sum, item) => sum + (item.amount || 0), 0)
        } else if (Array.isArray(incomeRes)) {
          totalIncome = incomeRes.reduce((sum, item) => sum + (item.amount || 0), 0)
        }
        
        // 计算总支出
        let totalExpense = 0
        if (expenseRes?.code === 200 && expenseRes?.data && Array.isArray(expenseRes.data)) {
          totalExpense = expenseRes.data.reduce((sum, item) => sum + (item.amount || 0), 0)
        } else if (Array.isArray(expenseRes)) {
          totalExpense = expenseRes.reduce((sum, item) => sum + (item.amount || 0), 0)
        }
        
        // 计算攒下金额
        const savings = totalIncome - totalExpense
        const percentage = target.incomeTarget > 0 ? Math.min(Math.round((savings / target.incomeTarget) * 100), 100) : 0
        const achieved = savings >= target.incomeTarget
        
        tableData.push({
          month,
          target: target.incomeTarget,
          savings,
          percentage: Math.max(0, percentage),
          achieved
        })
        
        console.log(`【仪表盘】${monthStr}: 目标=¥${target.incomeTarget}, 攒下=¥${savings}, 进度=${percentage}%`)
      } catch (error) {
        console.error(`【仪表盘】加载 ${monthStr} 数据失败:`, error)
        tableData.push({
          month,
          target: target.incomeTarget,
          savings: 0,
          percentage: 0,
          achieved: false
        })
      }
    }
    
    // 按月份排序
    tableData.sort((a, b) => a.month - b.month)
    yearlyTargetsData.value = tableData
    
    console.log('【仪表盘】年度目标进度表:', yearlyTargetsData.value)
  } catch (error) {
    console.error('【仪表盘】加载年度目标进度失败:', error)
    yearlyTargetsData.value = []
  }
}

// 绘制年度目标进度图表
const drawYearlyTargetsChart = () => {
  try {
    if (!yearlyTargetsData.value || yearlyTargetsData.value.length === 0) {
      console.warn('【年度目标图表】无数据')
      return
    }

    if (yearlyTargetsChartInstance) yearlyTargetsChartInstance.destroy()
    
    const ctx = yearlyTargetsChart.value
    if (!ctx) {
      console.warn('【年度目标图表】canvas 上下文不存在')
      return
    }

    // 准备数据
    const months = yearlyTargetsData.value.map(item => item.month + '月')
    const targets = yearlyTargetsData.value.map(item => item.target)
    const savings = yearlyTargetsData.value.map(item => item.savings)
    const achieved = yearlyTargetsData.value.map(item => item.achieved)

    yearlyTargetsChartInstance = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: months,
        datasets: [
          {
            label: '目标金额',
            data: targets,
            backgroundColor: 'rgba(24, 144, 255, 0.6)',
            borderColor: 'rgba(24, 144, 255, 1)',
            borderWidth: 1,
            borderRadius: 4
          },
          {
            label: '实际攒下',
            data: savings,
            backgroundColor: savings.map((s, i) => achieved[i] ? 'rgba(82, 196, 26, 0.6)' : 'rgba(255, 193, 7, 0.6)'),
            borderColor: savings.map((s, i) => achieved[i] ? 'rgba(82, 196, 26, 1)' : 'rgba(255, 193, 7, 1)'),
            borderWidth: 1,
            borderRadius: 4
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              padding: 15,
              font: {
                size: 12
              }
            }
          },
          tooltip: {
            callbacks: {
              label: function(context) {
                return context.dataset.label + ': ¥' + context.parsed.y.toFixed(2)
              }
            }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              callback: function(value) {
                return '¥' + value.toFixed(0)
              }
            }
          }
        }
      }
    })

    console.log('【年度目标图表】绘制完成')
  } catch (error) {
    console.error('【年度目标图表】绘制出错:', error)
  }
}

const saveIncomeTarget = async () => {
  try {
    let month
    if (modeType.value === 'monthly') {
      month = selectedMonth.value
    } else {
      // 年度模式：保存每个月份的目标为年度目标 / 12
      const yearlyTarget = parseFloat(targetForm.incomeTarget) / 12
      const promises = []
      for (let i = 1; i <= 12; i++) {
        const monthStr = `${selectedYear.value}-${String(i).padStart(2, '0')}`
        promises.push(userTargetAPI.update(monthStr, yearlyTarget.toFixed(2)))
      }
      const responses = await Promise.all(promises)
      console.log('【仪表盘】年度目标已保存到所有月份')
      
      metrics.targetIncome = parseFloat(targetForm.incomeTarget).toFixed(2)
      showEditTargetModal.value = false
      console.log('【仪表盘】保存成功')
      return
    }
    
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

const handleClickOutside = (event) => {
  if (yearSelectorRef.value && !yearSelectorRef.value.contains(event.target)) {
    showYearPicker.value = false
  }
}

const openEditTargetModal = () => {
  if (modeType.value === 'monthly') {
    // 月度模式：直接显示当月的目标
    targetForm.incomeTarget = metrics.targetIncome || '0'
  } else {
    // 年度模式：显示年度目标（即当前的 targetIncome 是年度汇总）
    targetForm.incomeTarget = metrics.targetIncome || '0'
  }
  showEditTargetModal.value = true
}

// 监听模式改变，自动加载数据
watch(modeType, async () => {
  console.log('【仪表盘】模式改变为:', modeType.value)
  selectedIncomeCategory.value = null
  selectedExpenseCategory.value = null
  loadDashboardData()
  setTimeout(async () => {
    await loadCharts()
    // 如果切换到年度模式，加载并绘制年度目标图表
    if (modeType.value === 'yearly') {
      await loadYearlyTargetsProgress()
      await new Promise(resolve => setTimeout(resolve, 100))
      drawYearlyTargetsChart()
    }
  }, 500)
})

// 监听年份改变-仅在年度模式下
watch(selectedYear, async () => {
  if (modeType.value === 'yearly') {
    console.log('【仪表盘】年份改变为:', selectedYear.value)
    loadDashboardData()
    setTimeout(async () => {
      await loadCharts()
      await loadYearlyTargetsProgress()
      await new Promise(resolve => setTimeout(resolve, 100))
      drawYearlyTargetsChart()
    }, 500)
  }
})

// 监听月份改变-仅在月度模式下
watch(selectedMonth, async () => {
  if (modeType.value === 'monthly' && selectedMonth.value) {
    console.log('【仪表盘】月份改变为:', selectedMonth.value)
    loadDashboardData()
    setTimeout(async () => {
      await loadCharts()
    }, 500)
  }
})

// 监听收入分类选择变化
watch(selectedIncomeCategory, () => {
  console.log('【收入分类】选择变化:', selectedIncomeCategory.value)
  if (incomeCategoryRawData.value && incomeCategoryChartInstance) {
    try {
      const data = incomeCategoryRawData.value
      const offset = (data.values || []).map((_, index) => {
        if (selectedIncomeCategory.value && data.details) {
          const selectedIndex = data.details.findIndex(d => d.categoryId === selectedIncomeCategory.value.categoryId)
          return index === selectedIndex ? 30 : 0
        }
        return 0
      })
      incomeCategoryChartInstance.data.datasets[0].offset = offset
      incomeCategoryChartInstance.update('none')
      console.log('【收入分类】已更新偏移量')
    } catch (error) {
      console.error('【收入分类】更新失败:', error)
    }
  }
})

// 监听支出分类选择变化
watch(selectedExpenseCategory, () => {
  console.log('【支出分类】选择变化:', selectedExpenseCategory.value)
  if (expenseCategoryRawData.value && expenseCategoryChartInstance) {
    try {
      const data = expenseCategoryRawData.value
      const offset = (data.values || []).map((_, index) => {
        if (selectedExpenseCategory.value && data.details) {
          const selectedIndex = data.details.findIndex(d => d.categoryId === selectedExpenseCategory.value.categoryId)
          return index === selectedIndex ? 30 : 0
        }
        return 0
      })
      expenseCategoryChartInstance.data.datasets[0].offset = offset
      expenseCategoryChartInstance.update('none')
      console.log('【支出分类】已更新偏移量')
    } catch (error) {
      console.error('【支出分类】更新失败:', error)
    }
  }
})

onMounted(async () => {
  // 初始化月份为当前月份
  const now = new Date()
  selectedMonth.value = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
  
  // 添加点击外部关闭年份选择器的监听
  document.addEventListener('click', handleClickOutside)
  
  loadDashboardData()
  await new Promise(resolve => setTimeout(resolve, 100))
  await loadCharts()
})

onUnmounted(() => {
  // 移除事件监听
  document.removeEventListener('click', handleClickOutside)
  
  // 销毁所有图表实例
  if (trendChartInstance) trendChartInstance.destroy()
  if (incomeCategoryChartInstance) incomeCategoryChartInstance.destroy()
  if (expenseCategoryChartInstance) expenseCategoryChartInstance.destroy()
  if (budgetChartInstance) budgetChartInstance.destroy()
  if (yearlyTargetsChartInstance) yearlyTargetsChartInstance.destroy()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.page-header {
  margin-bottom: 20px;
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

/* 模式选择器样式 */
.mode-selector {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 16px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
}

.mode-buttons {
  display: flex;
  gap: 12px;
}

.mode-btn {
  padding: 10px 20px;
  border: 2px solid #e0e0e0;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  color: #666;
}

.mode-btn:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.mode-btn.active {
  background: #1890ff;
  border-color: #1890ff;
  color: white;
}

.month-selector {
  display: flex;
  gap: 10px;
  align-items: center;
}

.month-input {
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  font-family: inherit;
  cursor: pointer;
}

.month-input:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.year-selector {
  display: flex;
  gap: 10px;
  align-items: center;
}

.year-input-group {
  position: relative;
  display: flex;
  align-items: center;
}

.year-input {
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  font-family: inherit;
  cursor: pointer;
  background: white;
  min-width: 140px;
}

.year-input:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.year-icon {
  position: absolute;
  right: 8px;
  cursor: pointer;
  font-size: 16px;
  pointer-events: none;
}

.year-picker {
  position: absolute;
  top: 100%;
  left: 0;
  background: white;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  margin-top: 4px;
  padding: 12px;
  z-index: 100;
  min-width: 280px;
}

.year-picker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.year-range-text {
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.year-nav-btn {
  background: none;
  border: none;
  padding: 4px 8px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  border-radius: 4px;
  transition: all 0.2s;
}

.year-nav-btn:hover {
  background: #f0f0f0;
  color: #1890ff;
}

.year-picker-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 6px;
}

.year-button {
  padding: 8px 4px;
  border: 1px solid #d9d9d9;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s ease;
  color: #333;
}

.year-button:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.year-button.active {
  background: #1890ff;
  border-color: #1890ff;
  color: white;
  font-weight: 600;
}

.quick-btn {
  padding: 8px 16px;
  background: #f0f0f0;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s ease;
  color: #333;
}

.quick-btn:hover {
  background: #e6e6e6;
  border-color: #999;
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
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 40px;
}

.chart-container {
  width: 100%;
  grid-column: 1 / -1;
}

.chart-placeholder {
  position: relative;
  height: 400px;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 400px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 8px;
  margin: 0 auto;
}

.empty-state p {
  font-size: 16px;
  color: #666;
  text-align: center;
  margin: 0;
}

.empty-state-compact {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 120px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 8px;
  margin: 0 auto;
}

.empty-state-compact p {
  font-size: 14px;
  color: #666;
  text-align: center;
  margin: 0;
}

.category-charts-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 40px;
}

.category-chart-container {
  width: 100%;
}

.analysis-charts-section {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
  margin-bottom: 40px;
}

.analysis-chart-container {
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

.yearly-targets-chart-section {
  margin-bottom: 30px;
}

.yearly-targets-chart-section h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.yearly-targets-chart-section .chart-placeholder {
  position: relative;
  height: 360px;
}

.yearly-targets-chart-section canvas {
  max-height: 360px;
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

.target-tip {
  margin-top: 10px;
  padding: 10px 12px;
  background: #e6f7ff;
  border-left: 3px solid #1890ff;
  border-radius: 4px;
  font-size: 13px;
  color: #0050b3;
  line-height: 1.6;
  margin-bottom: 0;
}

/* 年度目标进度表样式 */
@media (max-width: 768px) {
  .mode-selector {
    flex-direction: column;
    gap: 16px;
  }

  .mode-buttons {
    width: 100%;
  }

  .mode-btn {
    flex: 1;
  }

  .month-selector {
    width: 100%;
  }

  .month-input {
    flex: 1;
  }

  .year-selector {
    width: 100%;
  }

  .year-input-group {
    flex: 1;
  }

  .year-input {
    width: 100%;
  }

  .year-picker {
    position: fixed;
    top: 50%;
    left: 50%;
    right: auto;
    transform: translate(-50%, -50%);
    min-width: 280px;
    max-width: 90vw;
  }

  .metrics-grid {
    grid-template-columns: 1fr;
  }

  .charts-section {
    grid-template-columns: 1fr;
  }

  .chart-placeholder {
    height: 320px;
  }

  .empty-state {
    height: 320px;
  }

  .category-charts-section {
    grid-template-columns: 1fr;
  }

  .analysis-charts-section {
    grid-template-columns: 1fr;
  }

  .category-content {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    flex-direction: column;
  }

  .yearly-targets-chart-section .chart-placeholder {
    height: 280px;
  }
}
</style>
