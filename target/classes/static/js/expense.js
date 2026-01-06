let expenses = [];
let categories = [];
let editingExpenseId = null;

document.addEventListener('DOMContentLoaded', function() {
    loadExpenses();
    loadCategories();
    loadStatistics();
    
    document.getElementById('addExpenseBtn').addEventListener('click', openAddModal);
    document.getElementById('closeModal').addEventListener('click', closeModal);
    document.getElementById('expenseForm').addEventListener('submit', handleSubmit);
    document.getElementById('filterBtn').addEventListener('click', handleFilter);
    document.getElementById('resetFilterBtn').addEventListener('click', resetFilter);
    
    // 添加取消按钮事件
    const cancelBtn = document.getElementById('cancelBtn');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', closeModal);
    }
});

async function loadExpenses() {
    try {
        const response = await fetch('/jizhang/api/expense');
        const data = await response.json();
        if (data.code === 200) {
            expenses = data.data;
            renderExpenses(expenses);
        } else {
            alert('加载支出数据失败：' + data.message);
        }
    } catch (error) {
        console.error('加载支出数据失败：', error);
        alert('加载支出数据失败');
    }
}

async function loadCategories() {
    try {
        const response = await fetch('/jizhang/api/expense-category');
        const data = await response.json();
        if (data.code === 200) {
            categories = data.data;
            renderCategoryOptions();
        } else {
            alert('加载分类数据失败：' + data.message);
        }
    } catch (error) {
        console.error('加载分类数据失败：', error);
        alert('加载分类数据失败');
    }
}

async function loadStatistics(startDate = null, endDate = null) {
    try {
        let url = '/jizhang/api/expense/statistics';
        if (startDate && endDate) {
            url += `?startDate=${startDate}&endDate=${endDate}`;
        }
        const response = await fetch(url);
        const data = await response.json();
        if (data.code === 200) {
            const stats = data.data;
            const totalExpense = typeof stats.totalExpense === 'number' ? stats.totalExpense : parseFloat(stats.totalExpense);
            const avgExpense = typeof stats.avgExpense === 'number' ? stats.avgExpense : parseFloat(stats.avgExpense);
            const maxExpense = typeof stats.maxExpense === 'number' ? stats.maxExpense : parseFloat(stats.maxExpense);
            
            document.getElementById('totalExpense').textContent = '¥' + totalExpense.toFixed(2);
            document.getElementById('recordCount').textContent = stats.recordCount;
            document.getElementById('avgExpense').textContent = '¥' + avgExpense.toFixed(2);
            document.getElementById('maxExpense').textContent = '¥' + maxExpense.toFixed(2);
        }
    } catch (error) {
        console.error('加载统计数据失败：', error);
    }
}

function renderExpenses(expenseList) {
    const tbody = document.querySelector('.expense-table tbody');
    tbody.innerHTML = '';
    
    if (expenseList.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="no-data">暂无支出记录</td></tr>';
        return;
    }
    
    expenseList.forEach(expense => {
        const category = categories.find(c => c.id === expense.categoryId);
        const categoryName = category ? category.name : '未分类';
        
        // 格式化时间
        const createTime = formatDateTime(expense.createTime);
        
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${expense.transactionDate}</td>
            <td>${categoryName}</td>
            <td>¥${parseFloat(expense.amount).toFixed(2)}</td>
            <td>${expense.description || '-'}</td>
            <td>${createTime}</td>
            <td class="actions">
                <button class="edit-btn" onclick="editExpense(${expense.id})">编辑</button>
                <button class="delete-btn" onclick="deleteExpense(${expense.id})">删除</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function formatDateTime(dateTimeString) {
    if (!dateTimeString) return '-';
    // 处理多种时间格式
    const date = new Date(dateTimeString);
    if (isNaN(date.getTime())) return dateTimeString;
    
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    
    return `${year}-${month}-${day} ${hours}:${minutes}`;
}

function renderCategoryOptions() {
    const select = document.getElementById('categoryId');
    select.innerHTML = '<option value="">请选择分类</option>';
    
    const filterSelect = document.getElementById('filterCategoryId');
    filterSelect.innerHTML = '<option value="">全部分类</option>';
    
    categories.forEach(category => {
        const option = document.createElement('option');
        option.value = category.id;
        option.textContent = category.name;
        select.appendChild(option);
        
        const filterOption = document.createElement('option');
        filterOption.value = category.id;
        filterOption.textContent = category.name;
        filterSelect.appendChild(filterOption);
    });
}

function openAddModal() {
    editingExpenseId = null;
    document.getElementById('modalTitle').textContent = '添加支出';
    document.getElementById('expenseForm').reset();
    document.getElementById('expenseModal').classList.add('show');
}

function editExpense(id) {
    const expense = expenses.find(e => e.id === id);
    if (!expense) return;
    
    editingExpenseId = id;
    document.getElementById('modalTitle').textContent = '编辑支出';
    document.getElementById('amount').value = expense.amount;
    document.getElementById('transactionDate').value = expense.transactionDate;
    document.getElementById('categoryId').value = expense.categoryId;
    document.getElementById('description').value = expense.description || '';
    document.getElementById('expenseModal').classList.add('show');
}

function closeModal() {
    document.getElementById('expenseModal').classList.remove('show');
    editingExpenseId = null;
}

async function handleSubmit(e) {
    e.preventDefault();
    
    const categoryId = document.getElementById('categoryId').value;
    const amount = document.getElementById('amount').value;
    const transactionDate = document.getElementById('transactionDate').value;
    
    // 前端验证
    if (!categoryId) {
        alert('请选择分类');
        return;
    }
    if (!amount || parseFloat(amount) <= 0) {
        alert('请输入有效的金额');
        return;
    }
    if (!transactionDate) {
        alert('请选择交易日期');
        return;
    }
    
    const formData = {
        amount: parseFloat(amount),
        transactionDate: transactionDate,
        categoryId: parseInt(categoryId),
        description: document.getElementById('description').value || null
    };
    
    try {
        let response;
        if (editingExpenseId) {
            response = await fetch(`/jizhang/api/expense/${editingExpenseId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });
        } else {
            response = await fetch('/jizhang/api/expense', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });
        }
        
        const data = await response.json();
        if (data.code === 200) {
            alert(editingExpenseId ? '更新成功' : '添加成功');
            closeModal();
            loadExpenses();
            loadStatistics();
        } else {
            alert((editingExpenseId ? '更新失败：' : '添加失败：') + (data.message || '未知错误'));
        }
    } catch (error) {
        console.error('提交失败：', error);
        alert('提交失败，请检查网络连接');
    }
}

async function deleteExpense(id) {
    if (!confirm('确定要删除这条支出记录吗？')) return;
    
    try {
        const response = await fetch(`/jizhang/api/expense/${id}`, {
            method: 'DELETE'
        });
        const data = await response.json();
        if (data.code === 200) {
            alert('删除成功');
            loadExpenses();
            loadStatistics();
        } else {
            alert('删除失败：' + data.message);
        }
    } catch (error) {
        console.error('删除失败：', error);
        alert('删除失败');
    }
}

async function handleFilter() {
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;
    const categoryId = document.getElementById('filterCategoryId').value;
    
    try {
        let url = '/jizhang/api/expense';
        const params = new URLSearchParams();
        
        if (startDate) params.append('startDate', startDate);
        if (endDate) params.append('endDate', endDate);
        if (categoryId) params.append('categoryId', categoryId);
        
        if (params.toString()) {
            url += '?' + params.toString();
        }
        
        const response = await fetch(url);
        const data = await response.json();
        if (data.code === 200) {
            expenses = data.data;
            renderExpenses(expenses);
            loadStatistics(startDate, endDate);
        } else {
            alert('筛选失败：' + data.message);
        }
    } catch (error) {
        console.error('筛选失败：', error);
        alert('筛选失败');
    }
}

function resetFilter() {
    document.getElementById('startDate').value = '';
    document.getElementById('endDate').value = '';
    document.getElementById('filterCategoryId').value = '';
    loadExpenses();
    loadStatistics();
}

// 导出支出数据为CSV
function exportExpenseData() {
    if (expenses.length === 0) {
        alert('没有数据可导出');
        return;
    }
    
    const headers = ['日期', '分类', '金额', '描述', '创建时间'];
    const rows = expenses.map(expense => {
        const category = categories.find(c => c.id === expense.categoryId);
        const categoryName = category ? category.name : '未分类';
        return [
            expense.transactionDate,
            categoryName,
            expense.amount,
            expense.description || '',
            formatDateTime(expense.createTime)
        ];
    });
    
    let csvContent = '\ufeff'; // BOM for Excel UTF-8
    csvContent += headers.join(',') + '\n';
    rows.forEach(row => {
        csvContent += row.map(cell => {
            // 对包含逗号的单元格进行引用
            const cellStr = String(cell).replace(/"/g, '""');
            return cellStr.includes(',') ? `"${cellStr}"` : cellStr;
        }).join(',') + '\n';
    });
    
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', `expense_${new Date().getTime()}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}
