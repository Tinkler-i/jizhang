/**
 * 仪表盘 JavaScript
 */

let charts = {};

document.addEventListener('DOMContentLoaded', function() {
    loadDashboardData();
});

function loadDashboardData() {
    fetch('/jizhang/api/analysis/dashboard')
        .then(response => response.json())
        .then(data => {
            if (data.code === 200 && data.data) {
                const dashboard = data.data;
                updateMetrics(dashboard);
                updateSummary(dashboard);
                updateCharts(dashboard);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('加载数据失败，请刷新重试');
        });
    
    loadHealthScore();
    loadRecommendation();
}

function updateMetrics(dashboard) {
    document.getElementById('thisMonthIncome').textContent = '¥ ' + formatNumber(dashboard.thisMonthIncome);
    document.getElementById('thisMonthExpense').textContent = '¥ ' + formatNumber(dashboard.thisMonthExpense);
    document.getElementById('thisMonthBalance').textContent = '¥ ' + formatNumber(dashboard.thisMonthBalance);
    document.getElementById('targetIncome').textContent = '¥ ' + formatNumber(dashboard.targetIncome);
    document.getElementById('targetExpense').textContent = '¥ ' + formatNumber(dashboard.targetExpense);
    
    if (dashboard.thisMonthIncome > 0) {
        const savingRate = ((dashboard.thisMonthBalance / dashboard.thisMonthIncome) * 100).toFixed(1);
        document.getElementById('savingRate').textContent = savingRate + '%';
    }
    
    const budgetUtilization = dashboard.thisMonthBudgetUtilization || 0;
    document.getElementById('budgetUtilization').textContent = budgetUtilization.toFixed(1) + '%';
    
    const progressFill = document.getElementById('budgetProgressFill');
    if (progressFill) {
        let percentage = Math.min(budgetUtilization, 100);
        progressFill.style.width = percentage + '%';
        if (percentage > 100) {
            progressFill.style.backgroundColor = '#e74c3c';
        } else if (percentage > 80) {
            progressFill.style.backgroundColor = '#f39c12';
        } else {
            progressFill.style.backgroundColor = '#27ae60';
        }
    }
}

function updateSummary(dashboard) {
    document.getElementById('last30DaysIncome').textContent = '¥ ' + formatNumber(dashboard.last30DaysIncome);
    document.getElementById('last30DaysExpense').textContent = '¥ ' + formatNumber(dashboard.last30DaysExpense);
    document.getElementById('last30DaysBalance').textContent = '¥ ' + formatNumber(dashboard.last30DaysBalance);
    
    document.getElementById('yearToDateIncome').textContent = '¥ ' + formatNumber(dashboard.yearToDateIncome);
    document.getElementById('yearToDateExpense').textContent = '¥ ' + formatNumber(dashboard.yearToDateExpense);
    document.getElementById('yearToDateBalance').textContent = '¥ ' + formatNumber(dashboard.yearToDateBalance);
}

function updateCharts(dashboard) {
    if (!dashboard.chartData) return;
    
    const chartData = dashboard.chartData;
    
    if (chartData.dates && chartData.incomeData && chartData.expenseData) {
        drawTrendChart(chartData.dates, chartData.incomeData, chartData.expenseData);
    }
    
    if (chartData.incomeCategory && chartData.incomeCategory.length > 0) {
        drawPieChart('incomeChart', chartData.incomeCategory);
    } else {
        document.getElementById('incomeChart').parentElement.style.display = 'none';
    }
    
    if (chartData.expenseCategory && chartData.expenseCategory.length > 0) {
        drawPieChart('expenseChart', chartData.expenseCategory);
    } else {
        document.getElementById('expenseChart').parentElement.style.display = 'none';
    }
    
    if (chartData.budgetComparison && chartData.budgetComparison.length > 0) {
        drawBudgetComparisonChart(chartData.budgetComparison);
    } else {
        document.getElementById('budgetChart').parentElement.style.display = 'none';
    }
}

function drawTrendChart(dates, incomeData, expenseData) {
    const ctx = document.getElementById('trendChart');
    if (!ctx) return;
    
    if (charts.trend) charts.trend.destroy();
    
    const incomeValues = incomeData.map(v => parseFloat(v) || 0);
    const expenseValues = expenseData.map(v => parseFloat(v) || 0);
    
    charts.trend = new Chart(ctx, {
        type: 'line',
        data: {
            labels: dates || [],
            datasets: [
                {
                    label: '收入',
                    data: incomeValues,
                    borderColor: '#27ae60',
                    backgroundColor: 'rgba(39, 174, 96, 0.1)',
                    borderWidth: 2,
                    tension: 0.4,
                    fill: true
                },
                {
                    label: '支出',
                    data: expenseValues,
                    borderColor: '#e74c3c',
                    backgroundColor: 'rgba(231, 76, 60, 0.1)',
                    borderWidth: 2,
                    tension: 0.4,
                    fill: true
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: { display: true, position: 'top' }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return '¥' + value.toFixed(0);
                        }
                    }
                }
            }
        }
    });
}

function drawPieChart(canvasId, data) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return;
    
    if (charts[canvasId]) charts[canvasId].destroy();
    
    const labels = data.map(item => item.name);
    const values = data.map(item => parseFloat(item.value) || 0);
    const colors = generateColors(data.length);
    
    charts[canvasId] = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: values,
                backgroundColor: colors,
                borderColor: '#fff',
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: { position: 'bottom' }
            }
        }
    });
}

function drawBudgetComparisonChart(data) {
    const ctx = document.getElementById('budgetChart');
    if (!ctx) return;
    
    if (charts.budget) charts.budget.destroy();
    
    const labels = data.map(item => item.category);
    const budgets = data.map(item => parseFloat(item.budget) || 0);
    const actuals = data.map(item => parseFloat(item.actual) || 0);
    
    charts.budget = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [
                {
                    label: '预算',
                    data: budgets,
                    backgroundColor: '#3498db'
                },
                {
                    label: '实际支出',
                    data: actuals,
                    backgroundColor: '#e74c3c'
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return '¥' + value.toFixed(0);
                        }
                    }
                }
            }
        }
    });
}

function loadHealthScore() {
    fetch('/jizhang/api/analysis/health-score')
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                const score = data.data || 0;
                document.getElementById('healthScore').textContent = score;
                
                let status = '优秀';
                if (score < 40) status = '需要改进';
                else if (score < 60) status = '一般';
                else if (score < 80) status = '良好';
                
                document.getElementById('healthStatus').textContent = status;
            }
        })
        .catch(error => console.error('Error:', error));
}

function loadRecommendation() {
    fetch('/jizhang/api/analysis/recommendation')
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                const recommendation = data.data || '暂无建议';
                const box = document.getElementById('recommendationBox');
                if (box) {
                    box.innerHTML = '<p>' + recommendation.replace(/\n/g, '</p><p>') + '</p>';
                }
            }
        })
        .catch(error => console.error('Error:', error));
}

function loadFinancialReport() {
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    const startDate = formatDate(firstDay);
    const endDate = formatDate(today);
    
    fetch(`/jizhang/api/reports/financial?startDate=${startDate}&endDate=${endDate}`)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                displayFinancialReport(data.data);
            }
        })
        .catch(error => console.error('Error:', error));
}

function loadBudgetReport() {
    const today = new Date();
    const budgetMonth = today.getFullYear() + '-' + String(today.getMonth() + 1).padStart(2, '0');
    
    fetch(`/jizhang/api/reports/budget?budgetMonth=${budgetMonth}`)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                displayBudgetReport(data.data);
            }
        })
        .catch(error => console.error('Error:', error));
}

function loadCashFlowAnalysis() {
    const today = new Date();
    const startDate = formatDate(new Date(today.getTime() - 30 * 24 * 60 * 60 * 1000));
    const endDate = formatDate(today);
    
    fetch(`/jizhang/api/reports/cashflow?startDate=${startDate}&endDate=${endDate}`)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                displayCashFlowAnalysis(data.data);
            }
        })
        .catch(error => console.error('Error:', error));
}

function loadTrendAnalysis() {
    fetch('/jizhang/api/reports/trend?monthCount=6')
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                displayTrendAnalysis(data.data);
            }
        })
        .catch(error => console.error('Error:', error));
}

function displayFinancialReport(report) {
    let html = '<div class="report-content">';
    html += '<h3>收支总览</h3>';
    html += '<table class="report-table"><tr><td>总收入</td><td>¥ ' + formatNumber(report.totalIncome) + '</td></tr>';
    html += '<tr><td>总支出</td><td>¥ ' + formatNumber(report.totalExpense) + '</td></tr>';
    html += '<tr><td>净利润</td><td>¥ ' + formatNumber(report.netProfit) + '</td></tr></table>';
    html += '</div>';
    showModal('财务报表', html);
}

function displayBudgetReport(report) {
    let html = '<div class="report-content">';
    html += '<h3>预算月份: ' + report.budgetMonth + '</h3>';
    html += '<table class="report-table"><tr><td>预算总额</td><td>¥ ' + formatNumber(report.totalBudget) + '</td></tr>';
    html += '<tr><td>已支出</td><td>¥ ' + formatNumber(report.totalSpent) + '</td></tr>';
    html += '<tr><td>使用率</td><td>' + (report.budgetUtilizationRate || 0).toFixed(2) + '%</td></tr></table>';
    html += '</div>';
    showModal('预算执行报表', html);
}

function displayCashFlowAnalysis(analysis) {
    let html = '<div class="report-content">';
    html += '<table class="report-table"><tr><td>总收入</td><td>¥ ' + formatNumber(analysis.totalIncome) + '</td></tr>';
    html += '<tr><td>总支出</td><td>¥ ' + formatNumber(analysis.totalExpense) + '</td></tr>';
    html += '<tr><td>净现金流</td><td>¥ ' + formatNumber(analysis.netCashFlow) + '</td></tr>';
    html += '<tr><td>健康度</td><td>' + analysis.healthScore + '分 (' + analysis.healthStatus + ')</td></tr></table>';
    html += '</div>';
    showModal('现金流分析', html);
}

function displayTrendAnalysis(analysis) {
    let html = '<div class="report-content">';
    html += '<table class="report-table"><tr><td>收入趋势</td><td>' + (analysis.incomeTrend || '稳定') + '</td></tr>';
    html += '<tr><td>支出趋势</td><td>' + (analysis.expenseTrend || '稳定') + '</td></tr></table>';
    html += '</div>';
    showModal('趋势预测', html);
}

function showModal(title, content) {
    document.getElementById('modalTitle').textContent = title;
    document.getElementById('modalBody').innerHTML = content;
    document.getElementById('reportModal').style.display = 'block';
}

function closeModal() {
    document.getElementById('reportModal').style.display = 'none';
}

function downloadReport() {
    const title = document.getElementById('modalTitle').textContent;
    const content = document.getElementById('modalBody').innerHTML;
    const element = document.createElement('a');
    element.setAttribute('href', 'data:text/html;charset=utf-8,' + encodeURIComponent(content));
    element.setAttribute('download', title + '.html');
    element.style.display = 'none';
    document.body.appendChild(element);
    element.click();
    document.body.removeChild(element);
}

function formatNumber(num) {
    if (typeof num !== 'number') {
        num = parseFloat(num) || 0;
    }
    return num.toFixed(2);
}

function formatDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return year + '-' + month + '-' + day;
}

function generateColors(count) {
    const colors = [
        '#FF6B6B', '#4ECDC4', '#45B7D1', '#FFA07A', '#98D8C8',
        '#F7DC6F', '#BB8FCE', '#85C1E2', '#F8B88B', '#82E0AA'
    ];
    const result = [];
    for (let i = 0; i < count; i++) {
        result.push(colors[i % colors.length]);
    }
    return result;
}

window.onclick = function(event) {
    const modal = document.getElementById('reportModal');
    if (event.target == modal) {
        closeModal();
    }
}
