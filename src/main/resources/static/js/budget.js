// Budget Management JavaScript

let currentEditingId = null;
let currentMonth = new Date().toISOString().slice(0, 7);
let categories = [];

// 初始化
document.addEventListener('DOMContentLoaded', function() {
    // 初始化月份选择器
    const monthPicker = document.getElementById('monthPicker');
    const budgetMonth = document.getElementById('budgetMonth');
    
    const today = new Date();
    const currentMonthStr = today.getFullYear() + '-' + String(today.getMonth() + 1).padStart(2, '0');
    monthPicker.value = currentMonthStr;
    budgetMonth.value = currentMonthStr;
    
    // 加载分类
    loadCategories();
    
    // 加载预算数据
    loadBudgets();
    
    // 事件监听
    document.getElementById('addBudgetBtn').addEventListener('click', openAddModal);
    document.getElementById('monthPicker').addEventListener('change', function() {
        currentMonth = this.value;
        loadBudgets();
    });
    
    // 模态框关闭按钮
    const modal = document.getElementById('budgetModal');
    const closeBtn = modal.querySelector('.close');
    closeBtn.addEventListener('click', closeModal);
    
    // 表单提交
    document.getElementById('budgetForm').addEventListener('submit', saveBudget);
    
    // 点击模态框外部关闭
    window.addEventListener('click', function(event) {
        if (event.target === modal) {
            closeModal();
        }
        const detailModal = document.getElementById('detailModal');
        if (event.target === detailModal) {
            closeDetailModal();
        }
    });
});

// 加载分类
function loadCategories() {
    fetch('/jizhang/api/expense-category')
        .then(response => response.json())
        .then(data => {
            if (data.code === 200 || (data && data.data)) {
                categories = data.data || [];
                populateCategorySelect();
            }
        })
        .catch(error => console.error('Error loading categories:', error));
}

// 填充分类下拉框
function populateCategorySelect() {
    const select = document.getElementById('categoryId');
    select.innerHTML = '<option value="">请选择分类</option>';
    
    categories.forEach(category => {
        const option = document.createElement('option');
        option.value = category.id;
        option.textContent = category.name;
        select.appendChild(option);
    });
}

// 加载预算数据
function loadBudgets() {
    const monthStr = currentMonth || new Date().toISOString().slice(0, 7);
    
    fetch(`/jizhang/api/budget/month/${monthStr}`)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200 && data.data) {
                renderBudgets(data.data);
            } else {
                showEmptyState();
            }
        })
        .catch(error => {
            console.error('Error loading budgets:', error);
            showEmptyState();
        });
}

// 渲染预算卡片
function renderBudgets(budgets) {
    const container = document.getElementById('budgetContainer');
    const emptyState = document.getElementById('emptyState');
    
    if (budgets.length === 0) {
        showEmptyState();
        return;
    }
    
    container.innerHTML = '';
    emptyState.style.display = 'none';
    
    budgets.forEach(budget => {
        const card = createBudgetCard(budget);
        container.appendChild(card);
    });
}

// 创建预算卡片
function createBudgetCard(budget) {
    const card = document.createElement('div');
    card.className = 'budget-card';
    
    const percentageUsed = parseFloat(budget.percentageUsed) || 0;
    const progressClass = percentageUsed > 100 ? 'danger' : percentageUsed > 80 ? 'warning' : '';
    
    const spent = parseFloat(budget.spentAmount) || 0;
    const remaining = parseFloat(budget.remainingAmount) || 0;
    const budgetAmount = parseFloat(budget.budgetAmount) || 0;
    
    card.innerHTML = `
        <div class="budget-card-header">
            <div class="budget-card-title">${escapeHtml(budget.categoryName || '未分类')}</div>
            <div class="budget-card-month">${budget.yearMonth}</div>
        </div>
        
        <div class="budget-card-content">
            <div class="budget-item">
                <span class="budget-item-label">预算金额：</span>
                <span class="budget-item-value">¥${budgetAmount.toFixed(2)}</span>
            </div>
            
            <div class="budget-item">
                <span class="budget-item-label">已支出：</span>
                <span class="budget-item-value budget-item-spent">¥${spent.toFixed(2)}</span>
            </div>
            
            <div class="budget-item">
                <span class="budget-item-label">剩余：</span>
                <span class="budget-item-value budget-item-remaining">¥${Math.max(0, remaining).toFixed(2)}</span>
            </div>
        </div>
        
        <div class="progress-container">
            <div class="progress-label">
                <span>使用进度</span>
                <span>${percentageUsed.toFixed(1)}%</span>
            </div>
            <div class="progress-bar">
                <div class="progress-fill ${progressClass}" style="width: ${Math.min(100, percentageUsed)}%"></div>
            </div>
        </div>
        
        ${budget.remark ? `<div class="budget-card-remark">备注：${escapeHtml(budget.remark)}</div>` : ''}
    `;
    
    card.addEventListener('click', function() {
        showBudgetDetail(budget.id);
    });
    
    return card;
}

// 显示空状态
function showEmptyState() {
    const container = document.getElementById('budgetContainer');
    const emptyState = document.getElementById('emptyState');
    
    container.innerHTML = '';
    emptyState.style.display = 'block';
}

// 打开添加模态框
function openAddModal() {
    currentEditingId = null;
    document.getElementById('modalTitle').textContent = '添加预算';
    document.getElementById('budgetForm').reset();
    
    const today = new Date();
    const currentMonthStr = today.getFullYear() + '-' + String(today.getMonth() + 1).padStart(2, '0');
    document.getElementById('budgetMonth').value = currentMonthStr;
    
    document.getElementById('budgetModal').style.display = 'block';
}

// 关闭模态框
function closeModal() {
    document.getElementById('budgetModal').style.display = 'none';
    currentEditingId = null;
    document.getElementById('budgetForm').reset();
}

// 保存预算
function saveBudget(e) {
    e.preventDefault();
    
    const categoryId = document.getElementById('categoryId').value;
    const amount = document.getElementById('budgetAmount').value;
    const yearMonth = document.getElementById('budgetMonth').value;
    const remark = document.getElementById('remark').value;
    
    if (!categoryId || !amount || !yearMonth) {
        alert('请填写必填项');
        return;
    }
    
    const budgetData = {
        categoryId: parseInt(categoryId),
        amount: parseFloat(amount),
        yearMonth: yearMonth,
        remark: remark || null
    };
    
    const url = currentEditingId ? `/jizhang/api/budget/${currentEditingId}` : '/jizhang/api/budget';
    const method = currentEditingId ? 'PUT' : 'POST';
    
    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(budgetData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.code === 200) {
            alert(currentEditingId ? '修改成功' : '添加成功');
            closeModal();
            loadBudgets();
        } else {
            alert('操作失败：' + (data.message || '未知错误'));
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('操作失败：' + error.message);
    });
}

// 显示预算详情
function showBudgetDetail(budgetId) {
    fetch(`/jizhang/api/budget/${budgetId}`)
        .then(response => response.json())
        .then(data => {
            if (data.code === 200 && data.data) {
                const budget = data.data;
                const content = document.getElementById('detailContent');
                
                const spent = parseFloat(budget.spentAmount) || 0;
                const remaining = parseFloat(budget.remainingAmount) || 0;
                const budgetAmount = parseFloat(budget.budgetAmount) || 0;
                const percentageUsed = parseFloat(budget.percentageUsed) || 0;
                
                content.innerHTML = `
                    <div class="detail-item">
                        <span class="detail-item-label">分类：</span>
                        <span class="detail-item-value">${escapeHtml(budget.categoryName || '未分类')}</span>
                    </div>
                    
                    <div class="detail-item">
                        <span class="detail-item-label">月份：</span>
                        <span class="detail-item-value">${budget.yearMonth}</span>
                    </div>
                    
                    <div class="detail-item">
                        <span class="detail-item-label">预算金额：</span>
                        <span class="detail-item-value">¥${budgetAmount.toFixed(2)}</span>
                    </div>
                    
                    <div class="detail-item">
                        <span class="detail-item-label">已支出：</span>
                        <span class="detail-item-value">¥${spent.toFixed(2)}</span>
                    </div>
                    
                    <div class="detail-item">
                        <span class="detail-item-label">剩余：</span>
                        <span class="detail-item-value">¥${Math.max(0, remaining).toFixed(2)}</span>
                    </div>
                    
                    <div class="detail-item">
                        <span class="detail-item-label">使用比例：</span>
                        <span class="detail-item-value">${percentageUsed.toFixed(1)}%</span>
                    </div>
                    
                    ${budget.remark ? `
                    <div class="detail-item">
                        <span class="detail-item-label">备注：</span>
                        <span class="detail-item-value">${escapeHtml(budget.remark)}</span>
                    </div>
                    ` : ''}
                `;
                
                currentEditingId = budgetId;
                document.getElementById('detailModal').style.display = 'block';
            } else {
                alert('获取详情失败');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('获取详情失败');
        });
}

// 关闭详情模态框
function closeDetailModal() {
    document.getElementById('detailModal').style.display = 'none';
    currentEditingId = null;
}

// 编辑预算
function editBudget() {
    if (!currentEditingId) return;
    
    fetch(`/jizhang/api/budget/${currentEditingId}`)
        .then(response => response.json())
        .then(data => {
            if (data.success && data.data) {
                const budget = data.data;
                
                document.getElementById('modalTitle').textContent = '编辑预算';
                document.getElementById('categoryId').value = budget.categoryId;
                document.getElementById('budgetAmount').value = parseFloat(budget.budgetAmount);
                document.getElementById('budgetMonth').value = budget.yearMonth;
                document.getElementById('remark').value = budget.remark || '';
                
                closeDetailModal();
                document.getElementById('budgetModal').style.display = 'block';
            } else {
                alert('获取预算信息失败');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('获取预算信息失败');
        });
}

// 删除预算
function deleteBudget() {
    if (!currentEditingId) return;
    
    if (!confirm('确定要删除这个预算吗？')) {
        return;
    }
    
    fetch(`/jizhang/api/budget/${currentEditingId}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.code === 200) {
            alert('删除成功');
            closeDetailModal();
            loadBudgets();
        } else {
            alert('删除失败：' + (data.message || '未知错误'));
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('删除失败：' + error.message);
    });
}

// HTML转义函数
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
