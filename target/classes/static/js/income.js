let incomes = [];
let categories = [];
let editingIncomeId = null;

document.addEventListener('DOMContentLoaded', function() {
    loadIncomes();
    loadCategories();
    loadStatistics();
    
    document.getElementById('addIncomeBtn').addEventListener('click', openAddModal);
    document.getElementById('closeModal').addEventListener('click', closeModal);
    document.getElementById('incomeForm').addEventListener('submit', handleSubmit);
    document.getElementById('filterBtn').addEventListener('click', handleFilter);
    document.getElementById('resetFilterBtn').addEventListener('click', resetFilter);
    
    // 添加取消按钮事件
    const cancelBtn = document.getElementById('cancelBtn');
    if (cancelBtn) {
        cancelBtn.addEventListener('click', closeModal);
    }
});

async function loadIncomes() {
    try {
        const response = await fetch('/jizhang/api/income');
        const data = await response.json();
        if (data.code === 200) {
            incomes = data.data;
            renderIncomes(incomes);
        } else {
            alert('加载收入数据失败：' + data.message);
        }
    } catch (error) {
        console.error('加载收入数据失败：', error);
        alert('加载收入数据失败');
    }
}

async function loadCategories() {
    try {
        const response = await fetch('/jizhang/api/income-category');
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
        let url = '/jizhang/api/income/statistics';
        if (startDate && endDate) {
            url += `?startDate=${startDate}&endDate=${endDate}`;
        }
        const response = await fetch(url);
        const data = await response.json();
        if (data.code === 200) {
            const stats = data.data;
            const totalIncome = typeof stats.totalIncome === 'number' ? stats.totalIncome : parseFloat(stats.totalIncome);
            const avgIncome = typeof stats.avgIncome === 'number' ? stats.avgIncome : parseFloat(stats.avgIncome);
            const maxIncome = typeof stats.maxIncome === 'number' ? stats.maxIncome : parseFloat(stats.maxIncome);
            
            document.getElementById('totalIncome').textContent = '¥' + totalIncome.toFixed(2);
            document.getElementById('recordCount').textContent = stats.recordCount;
            document.getElementById('avgIncome').textContent = '¥' + avgIncome.toFixed(2);
            document.getElementById('maxIncome').textContent = '¥' + maxIncome.toFixed(2);
        }
    } catch (error) {
        console.error('加载统计数据失败：', error);
    }
}

function renderIncomes(incomeList) {
    const tbody = document.querySelector('.income-table tbody');
    tbody.innerHTML = '';
    
    if (incomeList.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="no-data">暂无收入记录</td></tr>';
        return;
    }
    
    incomeList.forEach(income => {
        const category = categories.find(c => c.id === income.categoryId);
        const categoryName = category ? category.name : '未分类';
        
        // 格式化时间
        const createTime = formatDateTime(income.createTime);
        
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${income.transactionDate}</td>
            <td>${categoryName}</td>
            <td>¥${parseFloat(income.amount).toFixed(2)}</td>
            <td>${income.description || '-'}</td>
            <td>${createTime}</td>
            <td class="actions">
                <button class="edit-btn" onclick="editIncome(${income.id})">编辑</button>
                <button class="delete-btn" onclick="deleteIncome(${income.id})">删除</button>
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
    editingIncomeId = null;
    document.getElementById('modalTitle').textContent = '添加收入';
    document.getElementById('incomeForm').reset();
    document.getElementById('incomeModal').classList.add('show');
}

function editIncome(id) {
    const income = incomes.find(i => i.id === id);
    if (!income) return;
    
    editingIncomeId = id;
    document.getElementById('modalTitle').textContent = '编辑收入';
    document.getElementById('amount').value = income.amount;
    document.getElementById('transactionDate').value = income.transactionDate;
    document.getElementById('categoryId').value = income.categoryId;
    document.getElementById('description').value = income.description || '';
    document.getElementById('incomeModal').classList.add('show');
}

function closeModal() {
    document.getElementById('incomeModal').classList.remove('show');
    editingIncomeId = null;
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
        if (editingIncomeId) {
            response = await fetch(`/jizhang/api/income/${editingIncomeId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });
        } else {
            response = await fetch('/jizhang/api/income', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });
        }
        
        const data = await response.json();
        if (data.code === 200) {
            alert(editingIncomeId ? '更新成功' : '添加成功');
            closeModal();
            loadIncomes();
            loadStatistics();
        } else {
            alert((editingIncomeId ? '更新失败：' : '添加失败：') + (data.message || '未知错误'));
        }
    } catch (error) {
        console.error('提交失败：', error);
        alert('提交失败，请检查网络连接');
    }
}

async function deleteIncome(id) {
    if (!confirm('确定要删除这条收入记录吗？')) return;
    
    try {
        const response = await fetch(`/jizhang/api/income/${id}`, {
            method: 'DELETE'
        });
        const data = await response.json();
        if (data.code === 200) {
            alert('删除成功');
            loadIncomes();
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
        let url = '/jizhang/api/income';
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
            incomes = data.data;
            renderIncomes(incomes);
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
    loadIncomes();
    loadStatistics();
}

// 导出收入数据为CSV
function exportIncomeData() {
    if (incomes.length === 0) {
        alert('没有数据可导出');
        return;
    }
    
    const headers = ['日期', '分类', '金额', '描述', '创建时间'];
    const rows = incomes.map(income => {
        const category = categories.find(c => c.id === income.categoryId);
        const categoryName = category ? category.name : '未分类';
        return [
            income.transactionDate,
            categoryName,
            income.amount,
            income.description || '',
            formatDateTime(income.createTime)
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
    link.setAttribute('download', `income_${new Date().getTime()}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

