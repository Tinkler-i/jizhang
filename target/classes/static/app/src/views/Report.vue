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
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { reportAPI, analysisAPI } from '../api'
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
  } catch (error) {
    console.error('Failed to load report data:', error)
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

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
