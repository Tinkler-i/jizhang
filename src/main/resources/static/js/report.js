/**
 * 报表分析 JavaScript
 */

let currentTab = 'financial';
let reportChart = null;

document.addEventListener('DOMContentLoaded', function() {
    // 设置默认日期
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    
    document.getElementById('startDate').value = formatDateInput(firstDay);
    document.getElementById('endDate').value = formatDateInput(today);
    
    loadReport();
});

function formatDateInput(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return year + '-' + month + '-' + day;
}

function formatDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return year + '-' + month + '-' + day;
}

function formatNumber(num) {
    if (typeof num !== 'number') {
        num = parseFloat(num) || 0;
    }
    return num.toFixed(2);
}

function switchTab(tabName) {
    currentTab = tabName;
    
    // 隐藏所有 tab
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    
    // 显示选中的 tab
    document.getElementById(tabName + '-tab').classList.add('active');
    
    // 更新按钮状态
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    event.target.classList.add('active');
    
    loadReport();
}

function loadReport() {
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;
    
    if (!startDate || !endDate) {
        alert('请选择日期范围');
        return;
    }
    
    switch(currentTab) {
        case 'financial':
            loadFinancialReport(startDate, endDate);
            break;
        case 'budget':
            loadBudgetReport(startDate, endDate);
            break;
        case 'cashflow':
            loadCashFlowReport(startDate, endDate);
            break;
        case 'trend':
            loadTrendReport();
            break;
    }
}

// 财务报表
function loadFinancialReport(startDate, endDate) {
    fetch(`/jizhang/api/reports/financial?startDate=${startDate}&endDate=${endDate}`)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                const report = data.data;
                
                // 更新总览数据
                document.getElementById('financial-income').textContent = '¥ ' + formatNumber(report.totalIncome);
                document.getElementById('financial-expense').textContent = '¥ ' + formatNumber(report.totalExpense);
                document.getElementById('financial-profit').textContent = '¥ ' + formatNumber(report.netProfit);
                document.getElementById('financial-rate').textContent = formatNumber(report.savingRate) + '%';
                
                // 更新收入分类
                const incomeTable = document.getElementById('financial-income-detail');
                if (report.incomeByCategory && report.incomeByCategory.length > 0) {
                    incomeTable.innerHTML = report.incomeByCategory.map(cat => `
                        <tr>
                            <td>${cat.categoryName}</td>
                            <td>¥ ${formatNumber(cat.amount)}</td>
                            <td>${cat.count}</td>
                            <td>${formatNumber(cat.percentage)}%</td>
                        </tr>
                    `).join('');
                } else {
                    incomeTable.innerHTML = '<tr><td colspan="4" class="text-center">暂无数据</td></tr>';
                }
                
                // 更新支出分类
                const expenseTable = document.getElementById('financial-expense-detail');
                if (report.expenseByCategory && report.expenseByCategory.length > 0) {
                    expenseTable.innerHTML = report.expenseByCategory.map(cat => `
                        <tr>
                            <td>${cat.categoryName}</td>
                            <td>¥ ${formatNumber(cat.amount)}</td>
                            <td>${cat.count}</td>
                            <td>${formatNumber(cat.percentage)}%</td>
                        </tr>
                    `).join('');
                } else {
                    expenseTable.innerHTML = '<tr><td colspan="4" class="text-center">暂无数据</td></tr>';
                }
                
                // 绘制图表
                drawFinancialChart(report);
            } else {
                alert('加载失败: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('加载报表失败');
        });
}

function drawFinancialChart(report) {
    const ctx = document.getElementById('reportChart');
    
    if (reportChart) {
        reportChart.destroy();
    }
    
    const incomeLabels = report.incomeByCategory ? report.incomeByCategory.map(c => c.categoryName) : [];
    const incomeValues = report.incomeByCategory ? report.incomeByCategory.map(c => parseFloat(c.amount)) : [];
    const expenseLabels = report.expenseByCategory ? report.expenseByCategory.map(c => c.categoryName) : [];
    const expenseValues = report.expenseByCategory ? report.expenseByCategory.map(c => parseFloat(c.amount)) : [];
    
    const allLabels = [...new Set([...incomeLabels, ...expenseLabels])];
    const incomeData = allLabels.map(label => {
        const item = report.incomeByCategory.find(c => c.categoryName === label);
        return item ? parseFloat(item.amount) : 0;
    });
    const expenseData = allLabels.map(label => {
        const item = report.expenseByCategory.find(c => c.categoryName === label);
        return item ? parseFloat(item.amount) : 0;
    });
    
    reportChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: allLabels,
            datasets: [
                {
                    label: '收入',
                    data: incomeData,
                    backgroundColor: '#27ae60',
                    borderColor: '#229954',
                    borderWidth: 1
                },
                {
                    label: '支出',
                    data: expenseData,
                    backgroundColor: '#e74c3c',
                    borderColor: '#c0392b',
                    borderWidth: 1
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

// 预算报表
function loadBudgetReport(startDate, endDate) {
    const budgetMonth = new Date(startDate).getFullYear() + '-' + 
                       String(new Date(startDate).getMonth() + 1).padStart(2, '0');
    
    fetch(`/jizhang/api/reports/budget?budgetMonth=${budgetMonth}`)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                const report = data.data;
                
                // 更新总览
                document.getElementById('budget-total').textContent = '¥ ' + formatNumber(report.totalBudget);
                document.getElementById('budget-spent').textContent = '¥ ' + formatNumber(report.totalSpent);
                document.getElementById('budget-remaining').textContent = '¥ ' + formatNumber(report.totalRemaining);
                document.getElementById('budget-rate').textContent = formatNumber(report.budgetUtilizationRate) + '%';
                
                // 更新分类预算
                const budgetTable = document.getElementById('budget-detail');
                if (report.budgetItems && report.budgetItems.length > 0) {
                    budgetTable.innerHTML = report.budgetItems.map(item => `
                        <tr>
                            <td>${item.categoryName}</td>
                            <td>¥ ${formatNumber(item.budgetAmount)}</td>
                            <td>¥ ${formatNumber(item.spentAmount)}</td>
                            <td>¥ ${formatNumber(item.remainingAmount)}</td>
                            <td>${formatNumber(item.utilizationRate)}%</td>
                            <td><span class="status ${item.isOverBudget ? 'over' : 'normal'}">${item.isOverBudget ? '超预算' : '正常'}</span></td>
                        </tr>
                    `).join('');
                } else {
                    budgetTable.innerHTML = '<tr><td colspan="6" class="text-center">暂无数据</td></tr>';
                }
                
                drawBudgetChart(report);
            } else {
                alert('加载失败: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('加载报表失败');
        });
}

function drawBudgetChart(report) {
    const ctx = document.getElementById('reportChart');
    
    if (reportChart) {
        reportChart.destroy();
    }
    
    const labels = report.budgetItems ? report.budgetItems.map(item => item.categoryName) : [];
    const budgetData = report.budgetItems ? report.budgetItems.map(item => parseFloat(item.budgetAmount)) : [];
    const spentData = report.budgetItems ? report.budgetItems.map(item => parseFloat(item.spentAmount)) : [];
    
    reportChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [
                {
                    label: '预算',
                    data: budgetData,
                    backgroundColor: '#3498db',
                    borderColor: '#2980b9',
                    borderWidth: 1
                },
                {
                    label: '已支出',
                    data: spentData,
                    backgroundColor: '#e74c3c',
                    borderColor: '#c0392b',
                    borderWidth: 1
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

// 现金流分析
function loadCashFlowReport(startDate, endDate) {
    fetch(`/jizhang/api/reports/cashflow?startDate=${startDate}&endDate=${endDate}`)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                const report = data.data;
                
                // 更新统计数据
                document.getElementById('cashflow-income').textContent = '¥ ' + formatNumber(report.totalIncome);
                document.getElementById('cashflow-expense').textContent = '¥ ' + formatNumber(report.totalExpense);
                document.getElementById('cashflow-net').textContent = '¥ ' + formatNumber(report.netCashFlow);
                document.getElementById('cashflow-volatility').textContent = formatNumber(report.volatility);
                
                // 更新健康评分
                document.getElementById('cashflow-score').textContent = report.healthScore;
                document.getElementById('cashflow-status').textContent = report.healthStatus;
                
                const descMap = {
                    'EXCELLENT': '现金流充足，财务状况良好',
                    'GOOD': '现金流良好，财务状况稳定',
                    'FAIR': '现金流一般，需要关注',
                    'POOR': '现金流不足，需要改善'
                };
                document.getElementById('cashflow-desc').textContent = descMap[report.healthStatus] || '财务状况评估中...';
                
                // 日均数据
                const days = report.dailyCashFlows ? report.dailyCashFlows.length : 1;
                document.getElementById('cashflow-avg-income').textContent = '¥ ' + formatNumber(report.totalIncome / days);
                document.getElementById('cashflow-avg-expense').textContent = '¥ ' + formatNumber(report.totalExpense / days);
                
                drawCashFlowChart(report);
            } else {
                alert('加载失败: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('加载报表失败');
        });
}

function drawCashFlowChart(report) {
    const ctx = document.getElementById('reportChart');
    
    if (reportChart) {
        reportChart.destroy();
    }
    
    const dates = report.dailyCashFlows ? report.dailyCashFlows.map(dcf => dcf.date) : [];
    const cashFlows = report.dailyCashFlows ? report.dailyCashFlows.map(dcf => parseFloat(dcf.cumulativeBalance)) : [];
    
    reportChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: dates,
            datasets: [
                {
                    label: '累计净现金流',
                    data: cashFlows,
                    borderColor: '#27ae60',
                    backgroundColor: 'rgba(39, 174, 96, 0.1)',
                    borderWidth: 2,
                    fill: true,
                    tension: 0.4
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

// 趋势预测
function loadTrendReport() {
    fetch(`/jizhang/api/reports/trend?monthCount=6`)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200) {
                const report = data.data;
                
                // 更新趋势数据
                const trendMap = {
                    'UP': '📈 上升',
                    'DOWN': '📉 下降',
                    'STABLE': '➡️ 稳定'
                };
                
                document.getElementById('trend-income-trend').textContent = trendMap[report.incomeTrend] || report.incomeTrend;
                document.getElementById('trend-expense-trend').textContent = trendMap[report.expenseTrend] || report.expenseTrend;
                document.getElementById('trend-income-growth').textContent = formatNumber(report.incomeGrowthRate) + '%';
                document.getElementById('trend-expense-growth').textContent = formatNumber(report.expenseGrowthRate) + '%';
                
                // 更新预测表格
                const forecastTable = document.getElementById('trend-forecast');
                if (report.forecastData && report.forecastData.length > 0) {
                    forecastTable.innerHTML = report.forecastData.map(forecast => `
                        <tr>
                            <td>${forecast.month}</td>
                            <td>¥ ${formatNumber(forecast.predictedIncome)}</td>
                            <td>¥ ${formatNumber(forecast.predictedExpense)}</td>
                            <td>¥ ${formatNumber(forecast.predictedNetProfit)}</td>
                            <td>${trendMap[forecast.trend] || forecast.trend}</td>
                        </tr>
                    `).join('');
                } else {
                    forecastTable.innerHTML = '<tr><td colspan="5" class="text-center">暂无预测数据</td></tr>';
                }
                
                drawTrendChart(report);
            } else {
                alert('加载失败: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('加载报表失败');
        });
}

function drawTrendChart(report) {
    const ctx = document.getElementById('reportChart');
    
    if (reportChart) {
        reportChart.destroy();
    }
    
    const allData = [...(report.historicalData || []), ...(report.forecastData || [])];
    const months = allData.map(d => d.month);
    const incomes = allData.map(d => parseFloat(d.income || d.predictedIncome || 0));
    const expenses = allData.map(d => parseFloat(d.expense || d.predictedExpense || 0));
    
    reportChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: months,
            datasets: [
                {
                    label: '收入',
                    data: incomes,
                    borderColor: '#27ae60',
                    backgroundColor: 'rgba(39, 174, 96, 0.1)',
                    borderWidth: 2,
                    fill: false,
                    tension: 0.4
                },
                {
                    label: '支出',
                    data: expenses,
                    borderColor: '#e74c3c',
                    backgroundColor: 'rgba(231, 76, 60, 0.1)',
                    borderWidth: 2,
                    fill: false,
                    tension: 0.4
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

function showFinancialReport() {
    switchTab('financial');
}

function showBudgetReport() {
    switchTab('budget');
}

function showCashFlowReport() {
    switchTab('cashflow');
}

function showTrendReport() {
    switchTab('trend');
}

function downloadReport() {
    const content = document.querySelector('.report-content-section').innerHTML;
    const title = document.querySelector('h2').textContent;
    
    const htmlContent = `
        <html>
        <head>
            <meta charset="UTF-8">
            <title>${title}</title>
            <style>
                body { font-family: Arial, sans-serif; margin: 20px; }
                h2, h3 { color: #333; }
                table { width: 100%; border-collapse: collapse; margin: 20px 0; }
                th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
                th { background-color: #f5f5f5; }
                .metrics-row { display: flex; gap: 20px; margin: 20px 0; }
                .metric { flex: 1; padding: 15px; background: #f9f9f9; border-radius: 5px; }
                .value { font-size: 24px; font-weight: bold; }
                .income { color: #27ae60; }
                .expense { color: #e74c3c; }
            </style>
        </head>
        <body>
            <h1>${title}</h1>
            <p>生成时间: ${new Date().toLocaleString('zh-CN')}</p>
            ${content}
        </body>
        </html>
    `;
    
    const element = document.createElement('a');
    element.setAttribute('href', 'data:text/html;charset=utf-8,' + encodeURIComponent(htmlContent));
    element.setAttribute('download', title + '.html');
    element.style.display = 'none';
    
    document.body.appendChild(element);
    element.click();
    document.body.removeChild(element);
}
