let categories = [];
let editingCategoryId = null;

document.addEventListener('DOMContentLoaded', function() {
    loadCategories();
    
    document.getElementById('addCategoryBtn').addEventListener('click', openAddModal);
    document.getElementById('closeModal').addEventListener('click', closeModal);
    document.getElementById('cancelBtn').addEventListener('click', closeModal);
    document.getElementById('categoryForm').addEventListener('submit', handleSubmit);
});

async function loadCategories() {
    try {
        const response = await fetch('/jizhang/api/expense-category');
        const data = await response.json();
        if (data.code === 200) {
            categories = data.data;
            renderCategories();
        } else {
            alert('加载分类数据失败：' + data.message);
        }
    } catch (error) {
        console.error('加载分类数据失败：', error);
        alert('加载分类数据失败');
    }
}

function formatDateTime(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}`;
}

function renderCategories() {
    const tbody = document.getElementById('categoryTableBody');
    tbody.innerHTML = '';
    
    if (categories.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="no-data">暂无分类</td></tr>';
        return;
    }
    
    categories.forEach(category => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${category.name}</td>
            <td>${category.description || '-'}</td>
            <td>${formatDateTime(category.createTime)}</td>
            <td>${formatDateTime(category.updateTime)}</td>
            <td class="actions">
                <button class="edit-btn" onclick="editCategory(${category.id})">编辑</button>
                <button class="delete-btn" onclick="deleteCategory(${category.id})">删除</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function openAddModal() {
    editingCategoryId = null;
    document.getElementById('modalTitle').textContent = '添加分类';
    document.getElementById('categoryForm').reset();
    document.getElementById('categoryModal').classList.add('show');
}

function editCategory(id) {
    const category = categories.find(c => c.id === id);
    if (!category) return;
    
    editingCategoryId = id;
    document.getElementById('modalTitle').textContent = '编辑分类';
    document.getElementById('name').value = category.name;
    document.getElementById('description').value = category.description || '';
    document.getElementById('categoryModal').classList.add('show');
}

function closeModal() {
    document.getElementById('categoryModal').classList.remove('show');
    editingCategoryId = null;
}

async function handleSubmit(e) {
    e.preventDefault();
    
    const formData = {
        name: document.getElementById('name').value,
        description: document.getElementById('description').value
    };
    
    try {
        let response;
        if (editingCategoryId) {
            response = await fetch(`/jizhang/api/expense-category/${editingCategoryId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });
        } else {
            response = await fetch('/jizhang/api/expense-category', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });
        }
        
        const data = await response.json();
        if (data.code === 200) {
            alert(editingCategoryId ? '更新成功' : '添加成功');
            closeModal();
            loadCategories();
        } else {
            alert((editingCategoryId ? '更新失败：' : '添加失败：') + data.message);
        }
    } catch (error) {
        console.error('提交失败：', error);
        alert('提交失败');
    }
}

async function deleteCategory(id) {
    if (!confirm('确定要删除这个分类吗？删除后该分类下的支出记录将无法显示分类信息。')) return;
    
    try {
        const response = await fetch(`/jizhang/api/expense-category/${id}`, {
            method: 'DELETE'
        });
        const data = await response.json();
        if (data.code === 200) {
            alert('删除成功');
            loadCategories();
        } else {
            alert('删除失败：' + data.message);
        }
    } catch (error) {
        console.error('删除失败：', error);
        alert('删除失败');
    }
}
