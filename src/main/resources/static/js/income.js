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
});

async function loadIncomes() {
    try {
        const response = await fetch('/jizhang/api/income');
        const data = await response.json();
        if (data.success) {
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
        if (data.success) {
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
        if (data.success) {
            const stats = data.data;
            document.getElementById('totalIncome').textContent = '¥' + stats.totalIncome.toFixed(2);
            document.getElementById('recordCount').textContent = stats.recordCount;
            document.getElementById('avgIncome').textContent = '¥' + stats.avgIncome.toFixed(2);
            document.getElementById('maxIncome').textContent = '¥' + stats.maxIncome.toFixed(2);
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
        
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${income.transactionDate}</td>
            <td>${categoryName}</td>
            <td>¥${income.amount.toFixed(2)}</td>
            <td>${income.description || '-'}</td>
            <td>${income.createTime}</td>
            <td class="actions">
                <button class="edit-btn" onclick="editIncome(${income.id})">编辑</button>
                <button class="delete-btn" onclick="deleteIncome(${income.id})">删除</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
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
    
    const formData = {
        amount: parseFloat(document.getElementById('amount').value),
        transactionDate: document.getElementById('transactionDate').value,
        categoryId: parseInt(document.getElementById('categoryId').value),
        description: document.getElementById('description').value
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
        if (data.success) {
            alert(editingIncomeId ? '更新成功' : '添加成功');
            closeModal();
            loadIncomes();
            loadStatistics();
        } else {
            alert((editingIncomeId ? '更新失败：' : '添加失败：') + data.message);
        }
    } catch (error) {
        console.error('提交失败：', error);
        alert('提交失败');
    }
}

async function deleteIncome(id) {
    if (!confirm('确定要删除这条收入记录吗？')) return;
    
    try {
        const response = await fetch(`/jizhang/api/income/${id}`, {
            method: 'DELETE'
        });
        const data = await response.json();
        if (data.success) {
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
        if (data.success) {
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
