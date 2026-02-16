<template>
  <div class="page">
    <div class="page-header">
      <h1>报表分析</h1>
      <Input
        v-model="selectedMonth"
        type="month"
        @change="loadReportData"
      />
    </div>

    <!-- 汇总统计 -->
    <div class="summary-grid">
      <Card>
        <div class="summary-item">
          <div class="summary-icon">💵</div>
          <div class="summary-content">
            <p class="summary-label">本月收入</p>
            <p class="summary-value">¥ {{ summary.totalIncome || '0.00' }}</p>
          </div>
        </div>
      </Card>
      <Card>
        <div class="summary-item">
          <div class="summary-icon">💸</div>
          <div class="summary-content">
            <p class="summary-label">本月支出</p>
            <p class="summary-value">¥ {{ summary.totalExpense || '0.00' }}</p>
          </div>
        </div>
      </Card>
      <Card>
        <div class="summary-item">
          <div class="summary-icon">💰</div>
          <div class="summary-content">
            <p class="summary-label">净收入</p>
            <p class="summary-value" :class="{ 'text-danger': summary.balance < 0 }">
              ¥ {{ summary.balance || '0.00' }}
            </p>
          </div>
        </div>
      </Card>
    </div>

    <!-- 分类分析 -->
    <Card class="analysis-card">
      <template #header>
        <h2>支出分类分析</h2>
      </template>
      <div class="table-container">
        <table class="analysis-table" v-if="categoryAnalysis.length > 0">
          <thead>
            <tr>
              <th>分类</th>
              <th>金额</th>
              <th>占比</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in categoryAnalysis" :key="item.categoryId">
              <td>{{ item.categoryName }}</td>
              <td>¥ {{ item.amount.toFixed(2) }}</td>
              <td>{{ item.percentage }}%</td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-message">
          本月暂无支出数据
        </div>
      </div>
    </Card>

    <!-- 预算对比 -->
    <Card class="analysis-card">
      <template #header>
        <h2>预算执行对比</h2>
      </template>
      <div class="table-container">
        <table class="analysis-table" v-if="budgetAnalysis.length > 0">
          <thead>
            <tr>
              <th>分类</th>
              <th>预算</th>
              <th>实际</th>
              <th>执行率</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in budgetAnalysis" :key="item.categoryId">
              <td>{{ item.categoryName }}</td>
              <td>¥ {{ item.budgetAmount.toFixed(2) }}</td>
              <td>¥ {{ item.actualAmount.toFixed(2) }}</td>
              <td>{{ item.percentage }}%</td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-message">
          本月暂无预算数据
        </div>
      </div>
    </Card>

    <!-- 年度目标进度表 -->
    <Card class="analysis-card">
      <template #header>
        <div class="yearly-targets-header">
          <h2>{{ selectedYearForTargets }} 年目标进度分析</h2>
          <div class="yearly-summary-stats">
            <span>年度目标: <strong>¥{{ yearlyTotalTarget.toFixed(2) }}</strong></span>
            <span class="separator">|</span>
            <span>年度实际: <strong class="highlight">¥{{ yearlyTotalSavings.toFixed(2) }}</strong></span>
            <span class="separator">|</span>
            <span>完成度: <strong :class="{ 'text-success': yearlyAchievementRate >= 100, 'text-danger': yearlyAchievementRate < 50 }">{{ yearlyAchievementRate }}%</strong></span>
          </div>
        </div>
      </template>
      <div class="table-container">
        <table class="yearly-targets-table" v-if="yearlyTargetsData.length > 0">
          <thead>
            <tr>
              <th class="col-month">月份</th>
              <th class="col-target">目标（¥）</th>
              <th class="col-savings">实际（¥）</th>
              <th class="col-progress">进度</th>
              <th class="col-status">状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in yearlyTargetsData" :key="index" :class="['target-row', { 'achieved': item.achieved, 'not-achieved': !item.achieved }]">
              <td class="col-month">{{ item.month }} 月</td>
              <td class="col-target">¥{{ item.target.toFixed(2) }}</td>
              <td class="col-savings" :class="{ 'text-success': item.savings >= 0 }">¥{{ item.savings.toFixed(2) }}</td>
              <td class="col-progress">
                <div class="progress-bar-container">
                  <div class="progress-bar">
                    <div 
                      class="progress-fill"
                      :style="{ width: item.percentage + '%' }"
                      :class="{ 'progress-complete': item.percentage >= 100 }"
                    ></div>
                  </div>
                  <span class="progress-text">{{ item.percentage }}%</span>
                </div>
              </td>
              <td class="col-status">
                <span v-if="item.achieved" class="status-badge achieved">✓ 达成</span>
                <span v-else class="status-badge not-achieved">✗ 未达成</span>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-message">
          暂无年度目标数据
        </div>
      </div>
    </Card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { reportAPI, analysisAPI, userTargetAPI, incomeAPI, expenseAPI } from '../api'
import Card from '../components/Card.vue'
import Input from '../components/Input.vue'

const selectedMonth = ref(new Date().toISOString().slice(0, 7))
const summary = reactive({
  totalIncome: '0.00',
  totalExpense: '0.00',
  balance: '0.00'
})
const categoryAnalysis = ref([])
const budgetAnalysis = ref([])

// 年度目标数据
const yearlyTargetsData = ref([])
const selectedYearForTargets = computed(() => {
  return new Date().getFullYear()
})

const loadReportData = async () => {
  try {
    // 加载汇总数据
    const summaryResponse = await reportAPI.getSummary({ month: selectedMonth.value })
    if (summaryResponse.code === 200 && summaryResponse.data) {
      summary.totalIncome = (summaryResponse.data.totalIncome || 0).toFixed(2)
      summary.totalExpense = (summaryResponse.data.totalExpense || 0).toFixed(2)
      summary.balance = ((summaryResponse.data.totalIncome || 0) - (summaryResponse.data.totalExpense || 0)).toFixed(2)
    }

    // 加载分类分析
    const categoryResponse = await analysisAPI.getCategoryAnalysis({ month: selectedMonth.value })
    if (categoryResponse.code === 200) {
      categoryAnalysis.value = categoryResponse.data || []
    }

    // 加载预算对比
    const budgetResponse = await analysisAPI.getBudgetVsActual(selectedMonth.value)
    if (budgetResponse.code === 200) {
      budgetAnalysis.value = budgetResponse.data || []
    }

    // 加载年度目标进度
    await loadYearlyTargetsProgress()
  } catch (error) {
    console.error('Failed to load report data:', error)
  }
}

// 年度目标相关计算属性
const yearlyTotalTarget = computed(() => {
  return yearlyTargetsData.value.reduce((sum, item) => sum + item.target, 0)
})

const yearlyTotalSavings = computed(() => {
  return yearlyTargetsData.value.reduce((sum, item) => sum + item.savings, 0)
})

const yearlyAchievementRate = computed(() => {
  const total = yearlyTotalTarget.value
  const actual = yearlyTotalSavings.value
  if (total <= 0) return 0
  const rate = Math.round((actual / total) * 100)
  return Math.max(0, rate)
})

// 加载年度目标进度数据
const loadYearlyTargetsProgress = async () => {
  try {
    console.log('【报表】加载年度目标进度，年份:', selectedYearForTargets.value)
    
    // 获取该年份所有目标
    const targetsResponse = await userTargetAPI.getAll()
    const allTargets = (targetsResponse?.code === 200 || targetsResponse?.code === 0) ? (targetsResponse.data || []) : []
    
    const year = selectedYearForTargets.value.toString()
    const yearTargets = allTargets.filter(t => t.targetMonth.startsWith(year))
    
    console.log(`【报表】该年份的目标数: ${yearTargets.length}`, yearTargets)
    
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
        
        console.log(`【报表】${monthStr}: 目标=¥${target.incomeTarget}, 攒下=¥${savings}, 进度=${percentage}%`)
      } catch (error) {
        console.error(`【报表】加载 ${monthStr} 数据失败:`, error)
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
    
    console.log('【报表】年度目标进度表:', yearlyTargetsData.value)
  } catch (error) {
    console.error('【报表】加载年度目标进度失败:', error)
    yearlyTargetsData.value = []
  }
}

onMounted(() => {
  loadReportData()
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

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 20px;
}

.summary-icon {
  font-size: 32px;
  flex-shrink: 0;
}

.summary-content {
  flex: 1;
}

.summary-label {
  font-size: 12px;
  color: #999;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin: 0 0 8px 0;
}

.summary-value {
  font-size: 24px;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.analysis-card {
  margin-bottom: 20px;
}

.analysis-card h2 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.table-container {
  overflow-x: auto;
}

.analysis-table {
  width: 100%;
  border-collapse: collapse;
}

.analysis-table thead {
  background: #fafafa;
  border-bottom: 2px solid #f0f0f0;
}

.analysis-table th {
  padding: 12px 16px;
  text-align: left;
  font-weight: 600;
  color: #333;
  font-size: 13px;
}

.analysis-table td {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  color: #666;
}

.analysis-table tbody tr:hover {
  background: #f9f9f9;
}

.empty-message {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.text-danger {
  color: #e74c3c !important;
}

.text-success {
  color: #52c41a !important;
}

/* 年度目标表格样式 */
.yearly-targets-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  gap: 20px;
}

.yearly-targets-header h2 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.yearly-summary-stats {
  display: flex;
  gap: 16px;
  align-items: center;
  font-size: 13px;
  color: #666;
  flex-shrink: 0;
}

.yearly-summary-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}

.yearly-summary-stats strong {
  color: #333;
  font-weight: 600;
  font-size: 14px;
}

.yearly-summary-stats .highlight {
  color: #52c41a;
}

.yearly-summary-stats .separator {
  color: #ddd;
}

.yearly-targets-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 16px;
}

.yearly-targets-table thead {
  background: #fafafa;
}

.yearly-targets-table th {
  padding: 14px 16px;
  text-align: left;
  font-weight: 600;
  font-size: 13px;
  color: #666;
  border-bottom: 2px solid #e8e8e8;
}

.yearly-targets-table tbody tr {
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s ease;
}

.yearly-targets-table tbody tr:hover {
  background-color: #fafafa;
}

.yearly-targets-table tbody tr.achieved {
  background: rgba(82, 196, 26, 0.05);
}

.yearly-targets-table tbody tr.not-achieved {
  background: rgba(255, 77, 79, 0.02);
}

.yearly-targets-table td {
  padding: 14px 16px;
  font-size: 13px;
}

.col-month {
  font-weight: 600;
  color: #333;
  width: 80px;
}

.col-target {
  text-align: right;
  width: 120px;
  color: #666;
}

.col-savings {
  text-align: right;
  width: 120px;
  font-weight: 600;
  color: #1890ff;
}

.col-savings.text-success {
  color: #52c41a;
}

.col-progress {
  width: 200px;
}

.progress-bar-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.progress-bar {
  flex: 1;
  height: 6px;
  background: #f0f0f0;
  border-radius: 3px;
  overflow: hidden;
  min-width: 80px;
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
  width: 40px;
  text-align: right;
  font-weight: 600;
  color: #333;
  font-size: 12px;
}

.col-status {
  text-align: center;
  width: 100px;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.status-badge.achieved {
  background: rgba(82, 196, 26, 0.2);
  color: #52c41a;
}

.status-badge.not-achieved {
  background: rgba(255, 77, 79, 0.2);
  color: #ff4d4f;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }

  .yearly-targets-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .yearly-summary-stats {
    flex-direction: column;
    align-items: flex-start;
  }

  .col-month,
  .col-target,
  .col-savings,
  .col-progress,
  .col-status {
    width: auto;
  }
}
</style>
