<template>
  <div class="family-management-container">
    <!-- 标签页切换 -->
    <div class="tabs">
      <button 
        v-if="familyGroup"
        class="tab-button" 
        :class="{ active: activeTab === 'info' }"
        @click="activeTab = 'info'">
        家庭组信息
      </button>
      <button 
        class="tab-button" 
        :class="{ active: activeTab === 'join' }"
        @click="activeTab = 'join'">
        {{ familyGroup ? '其他家庭' : '加入或创建家庭' }}
      </button>
      <button 
        v-if="familyGroup"
        class="tab-button" 
        :class="{ active: activeTab === 'members' }"
        @click="activeTab = 'members'">
        成员管理
      </button>
    </div>

    <!-- 标签页内容 -->
    <div class="tab-content">
      <!-- 家庭组信息 Tab -->
      <div v-show="activeTab === 'info'" class="tab-pane">
        <div v-if="familyGroup" class="family-info">
          <h3>{{ familyGroup.name }}</h3>
          
          <div class="info-section">
            <label>家庭组编号：</label>
            <div class="code-display">
              <span class="code-value">{{ familyGroup.code }}</span>
              <button class="copy-btn" @click="copyCode">复制</button>
            </div>
            <p class="hint">分享此编号给家庭成员，他们可以用编号加入家庭组</p>
          </div>

          <div class="info-section">
            <label>家庭组描述：</label>
            <p>{{ familyGroup.description || '暂无描述' }}</p>
          </div>

          <div class="info-section">
            <label>成员数量：</label>
            <p>{{ memberCount }} 人</p>
          </div>

          <div class="info-section">
            <label>创建时间：</label>
            <p>{{ formatDate(familyGroup.createTime) }}</p>
          </div>

          <div v-if="isCreator" class="info-section">
            <button class="edit-btn" @click="showEditModal = true">编辑家庭组</button>
          </div>
        </div>

        <div v-else class="empty-state">
          <p>您还没有加入任何家庭组</p>
          <p>请先加入家庭组或联系家庭组管理员邀请您</p>
        </div>
      </div>

      <!-- 加入或创建家庭 Tab -->
      <div v-show="activeTab === 'join'" class="tab-pane">
        <!-- 未加入家庭的情况 - 显示加入/创建选项 -->
        <div v-if="!familyGroup" class="join-section">
          <div class="no-family-container">
            <h2>👋 欢迎使用记账应用</h2>
            <p class="subtitle">您还没有加入任何家庭组，请选择以下操作之一</p>
            
            <div class="actions-grid">
              <!-- 创建新家庭选项 -->
              <div class="action-card">
                <div class="card-icon">🏠</div>
                <h3>创建新家庭</h3>
                <p>创建一个新的家庭组，邀请家人加入</p>
                <div class="form-group" v-if="showCreateForm">
                  <label for="family-name">家庭名称：</label>
                  <input 
                    id="family-name"
                    v-model="newFamilyName" 
                    type="text" 
                    placeholder="例如：王家"
                    @keyup.enter="createFamily">
                  <div class="action-buttons">
                    <button class="btn-primary" @click="createFamily" :disabled="createLoading">
                      {{ createLoading ? '创建中...' : '创建' }}
                    </button>
                    <button class="btn-cancel" @click="showCreateForm = false">取消</button>
                  </div>
                  <div v-if="createError" class="error-message">{{ createError }}</div>
                </div>
                <button v-else class="btn-primary" @click="showCreateForm = true">
                  开始创建
                </button>
              </div>
              
              <!-- 加入现有家庭选项 -->
              <div class="action-card">
                <div class="card-icon">👥</div>
                <h3>加入现有家庭</h3>
                <p>输入编号加入已创建的家庭组</p>
                <div class="form-group">
                  <label for="code-input">家庭组编号：</label>
                  <input 
                    id="code-input"
                    v-model="joinCode" 
                    type="text" 
                    placeholder="输入6位编号"
                    @keyup.enter="joinFamily"
                    maxlength="6">
                  <p class="hint">编号由字母和数字组成</p>
                  <button class="btn-primary" @click="joinFamily" :disabled="joinLoading">
                    {{ joinLoading ? '加入中...' : '加入家庭' }}
                  </button>
                  <div v-if="joinError" class="error-message">{{ joinError }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 已加入家庭的情况 - 显示可加入其他家庭的选项 -->
        <div v-else class="join-section">
          <h3>加入其他家庭</h3>
          <p>您已经是 <strong>{{ familyGroup.name }}</strong> 的成员</p>
          <p class="hint">如需加入其他家庭组，请先从现有家庭中退出</p>
          
          <div class="form-group">
            <label for="code-input-other">家庭组编号：</label>
            <input 
              id="code-input-other"
              v-model="joinCode" 
              type="text" 
              placeholder="输入6位编号"
              @keyup.enter="joinFamily"
              maxlength="6"
              disabled>
            <p class="hint">您需要先退出当前家庭才能加入其他家庭</p>
          </div>
        </div>
      </div>

      <!-- 成员管理 Tab -->
      <div v-show="activeTab === 'members'" class="tab-pane">
        <div v-if="familyGroup" class="members-section">
          <h3>家庭组成员 ({{ members.length }})</h3>
          
          <div class="members-list">
            <div v-for="member in members" :key="member.id" class="member-item">
              <div class="member-info">
                <span class="member-role" :class="`role-${member.role.toLowerCase()}`">
                  {{ member.role === 'ADMIN' ? '管理员' : '成员' }}
                </span>
                <div class="member-details">
                  <span class="member-nickname">{{ member.nickname || member.username }}</span>
                  <span class="member-username">@{{ member.username }}</span>
                </div>
              </div>

              <div v-if="isAdmin && member.userId !== currentUserId" class="member-actions">
                <button 
                  class="btn-small"
                  @click="openPermissionModal(member)"
                  title="管理此成员的权限">
                  权限
                </button>
                <button 
                  class="btn-small danger"
                  @click="removeMember(member.id)"
                  title="从家庭组中移除此成员">
                  移除
                </button>
              </div>

              <div v-else-if="member.userId === currentUserId" class="member-actions">
                <span class="badge" title="这是你的账户">您</span>
                <button 
                  class="btn-small"
                  @click="openPermissionModal(member)"
                  title="查看您的权限"
                  style="margin-left: 8px;">
                  📋 权限
                </button>
                <button 
                  class="btn-small danger"
                  @click="removeMember(member.id)"
                  title="退出此家庭组"
                  style="margin-left: 4px;">
                  退出
                </button>
              </div>
            </div>
          </div>

          <div v-if="members.length === 0" class="empty-state">
            <p>暂无其他成员</p>
          </div>
        </div>

        <div v-else class="empty-state">
          <p>您还没有加入任何家庭组</p>
        </div>
      </div>
    </div>

    <!-- 编辑家庭组模态框 -->
    <div v-if="showEditModal" class="modal-overlay" @click.self="showEditModal = false">
      <div class="modal">
        <h3>编辑家庭组</h3>
        
        <div class="form-group">
          <label for="edit-name">家庭组名称：</label>
          <input 
            id="edit-name"
            v-model="editForm.name" 
            type="text"
            placeholder="输入家庭组名称">
        </div>

        <div class="form-group">
          <label for="edit-description">描述：</label>
          <textarea 
            id="edit-description"
            v-model="editForm.description" 
            placeholder="输入家庭组描述（可选）"
            rows="3"></textarea>
        </div>

        <div class="modal-actions">
          <button class="btn-cancel" @click="showEditModal = false">取消</button>
          <button 
            class="btn-primary" 
            @click="updateFamilyGroup"
            :disabled="editLoading">
            {{ editLoading ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 权限管理模态框 -->
    <div v-if="isPermissionModalOpen" class="modal-overlay" @click.self="isPermissionModalOpen = false">
      <div class="modal permission-modal">
        <h3>{{ selectedMember?.userId === currentUserId ? '我的权限' : '成员权限' }}</h3>
        
        <div class="member-card">
          <span class="member-role" :class="`role-${selectedMember?.role?.toLowerCase()}`">
            {{ selectedMember?.role === 'ADMIN' ? '管理员' : '成员' }}
          </span>
          <div>
            <p class="member-name">{{ selectedMember?.nickname || selectedMember?.username }}</p>
            <p class="member-username">@{{ selectedMember?.username }}</p>
          </div>
        </div>
        
        <!-- 权限详情展示 -->
        <div v-if="permissionDetails" class="permission-details">
          <h4 v-if="isAdmin">📋 点击权限进行切换</h4>
          <h4 v-else>📋 您的权限（只读）</h4>
          
          <!-- 成员管理权限提示 -->
          <div v-if="isAdmin" class="permission-notice">
            <p>⚠️ 成员管理权限由管理员直接控制，不需要授权</p>
          </div>
          
          <div class="permissions-grid">
            <div 
              v-for="(value, key) in getFilteredPermissions()" 
              :key="key" 
              class="permission-item"
              :class="{ toggleable: isAdmin && !isAdminUser(selectedMember) }"
              @click="isAdmin && !isAdminUser(selectedMember) && togglePermission(key)">
              <span class="permission-label">{{ formatPermissionLabel(key) }}</span>
              <span class="permission-status" :class="{ granted: value }">
                {{ value ? '✓ 允许' : '✗ 禁止' }}
              </span>
            </div>
          </div>
        </div>

        <div class="modal-actions">
          <button class="btn-cancel" @click="closePermissionModal">{{ isAdmin ? '取消' : '关闭' }}</button>
          <button 
            v-if="isAdmin && !isAdminUser(selectedMember)"
            class="btn-primary" 
            @click="updatePermissions"
            :disabled="permissionLoading">
            {{ permissionLoading ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 加入家庭时选择是否带入数据的对话框 -->
    <div v-if="showJoinDialog" class="modal-overlay" @click="cancelJoin">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>加入家庭组</h3>
          <button class="close-btn" @click="cancelJoin">×</button>
        </div>
        <div class="modal-body">
          <p>您即将加入家庭组 <strong>{{ pendingJoinCode }}</strong></p>
          <p class="question">是否需要将现有的记录数据带入此家庭组？</p>
          <div class="option-group">
            <label class="option-label">
              <input 
                type="radio" 
                v-model="joinBringData" 
                :value="true"
                name="bring-data">
              <span>是的，带入现有数据</span>
              <span class="description">您现有的收入、支出、分类等数据将被共享到家庭组中</span>
            </label>
            <label class="option-label">
              <input 
                type="radio" 
                v-model="joinBringData" 
                :value="false"
                name="bring-data">
              <span>不带入，保留个人数据</span>
              <span class="description">您现有的数据将保留为个人数据，不会被共享</span>
            </label>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="cancelJoin">取消</button>
          <button class="btn-primary" @click="confirmJoin" :disabled="joinLoading">
            {{ joinLoading ? '加入中...' : '确认加入' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 退出家庭时选择是否保留数据的对话框 -->
    <div v-if="showExitDialog" class="modal-overlay" @click="cancelExit">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ isExitingSelf ? '退出家庭组' : '移除成员' }}</h3>
          <button class="close-btn" @click="cancelExit">×</button>
        </div>
        <div class="modal-body">
          <p v-if="isExitingSelf">您即将退出家庭组 <strong>{{ familyGroup?.name }}</strong></p>
          <p v-else>您即将移除成员 <strong>{{ exitMemberName }}</strong></p>
          <p class="question">是否保留此账户的数据？</p>
          <div class="option-group">
            <label class="option-label">
              <input 
                type="radio" 
                v-model="exitKeepData" 
                :value="false"
                name="keep-data">
              <span>删除数据</span>
              <span class="description">该账户的所有记录将被删除（无法恢复）</span>
            </label>
            <label class="option-label">
              <input 
                type="radio" 
                v-model="exitKeepData" 
                :value="true"
                name="keep-data">
              <span>保留数据</span>
              <span class="description">数据将被转为个人账户数据，不再与家庭共享</span>
            </label>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-cancel" @click="cancelExit">取消</button>
          <button class="btn-danger" @click="confirmExit" :disabled="joinLoading">
            {{ joinLoading ? '处理中...' : (isExitingSelf ? '确认退出' : '移除成员') }}
          </button>
        </div>
      </div>
    </div>

    <!-- 成功/错误提示 -->
    <div v-if="message" class="message" :class="messageType">
      {{ message }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { apiCall } from '../api/index.js'

const activeTab = ref('info')
const familyGroup = ref(null)
const members = ref([])
const memberCount = ref(0)
const currentUserId = ref(null)
const joinCode = ref('')
const joinLoading = ref(false)
const joinError = ref('')
const newFamilyName = ref('')
const createLoading = ref(false)
const createError = ref('')
const showCreateForm = ref(false)
const editLoading = ref(false)
const showEditModal = ref(false)
const editForm = ref({
  name: '',
  description: ''
})
const isPermissionModalOpen = ref(false)
const selectedMember = ref(null)
const permissionLoading = ref(false)
const permissionDetails = ref(null)
const message = ref('')
const messageType = ref('success')

// 加入家庭对话框相关
const showJoinDialog = ref(false)
const joinBringData = ref(false)
const pendingJoinCode = ref('')

// 退出家庭对话框相关
const showExitDialog = ref(false)
const exitKeepData = ref(true)
const pendingExitMemberId = ref(null)
const isExitingSelf = ref(false)
const exitMemberName = ref('')

const isCreator = computed(() => {
  return familyGroup.value && familyGroup.value.creatorId === currentUserId.value
})

const isAdmin = computed(() => {
  if (!currentUserId.value) {
    console.log('【DEBUG】isAdmin: currentUserId 未设置')
    return false
  }
  
  const member = members.value.find(m => m.userId === currentUserId.value)
  const isAdminUser = member && member.role === 'ADMIN'
  console.log('【DEBUG】isAdmin 计算: currentUserId=', currentUserId.value, ', 找到成员=', !!member, ', 角色=', member?.role, ', 结果=', isAdminUser)
  return isAdminUser
})

// 获取当前用户信息
const getCurrentUser = async () => {
  try {
    const response = await apiCall('users/current', 'GET')
    console.log('【DEBUG】获取当前用户响应:', response)
    if (response && response.code === 200 && response.data && response.data.id) {
      currentUserId.value = response.data.id
      console.log('【DEBUG】当前用户 ID:', currentUserId.value)
    } else if (response && response.data && response.data.id) {
      currentUserId.value = response.data.id
      console.log('【DEBUG】当前用户 ID (备用方案):', currentUserId.value)
    }
  } catch (error) {
    console.error('【DEBUG】获取用户信息失败:', error)
    // 备用方案：从会话存储或本地存储中获取
    const storedUserId = sessionStorage.getItem('userId') || localStorage.getItem('userId')
    if (storedUserId) {
      currentUserId.value = parseInt(storedUserId)
      console.log('【DEBUG】从本地存储获取用户 ID:', currentUserId.value)
    }
  }
}

// 获取家庭组信息
const loadFamilyGroup = async () => {
  try {
    const response = await apiCall('family-groups/current', 'GET')
    console.log('【DEBUG】获取家庭组响应:', response)
    if (response && response.code === 200) {
      familyGroup.value = response.data
      memberCount.value = response.memberCount
      editForm.value.name = response.data.name
      editForm.value.description = response.data.description
      console.log('【DEBUG】家庭组加载成功:', familyGroup.value)
      // 如果成功获取家庭组，设置默认标签为 info
      activeTab.value = 'info'
    } else if (response && response.code === 404) {
      console.log('【DEBUG】用户不在任何家庭组中，切换到加入/创建界面')
      familyGroup.value = null
      members.value = []
      // 用户不在家庭组中，自动切换到加入/创建标签
      activeTab.value = 'join'
    } else {
      console.warn('【DEBUG】获取家庭组失败，响应码:', response?.code, '响应信息:', response?.message)
      familyGroup.value = null
      activeTab.value = 'join'
    }
  } catch (error) {
    console.error('【DEBUG】获取家庭组异常:', error.message, error)
    familyGroup.value = null
    // 异常时也切换到加入/创建界面
    activeTab.value = 'join'
  }
}

// 获取家庭组成员
const loadMembers = async () => {
  try {
    // 使用新的 API 端点获取成员列表（包含昵称和当前用户 ID）
    const response = await apiCall('family-members/list', 'GET')
    if (response && response.code === 200 && Array.isArray(response.data)) {
      members.value = response.data
      
      // 后端直接返回当前用户 ID，无需推断
      if (response.currentUserId) {
        currentUserId.value = response.currentUserId
        console.log('【DEBUG】成员列表已加载，当前用户 ID:', currentUserId.value)
      } else {
        console.warn('【DEBUG】后端未返回 currentUserId，可能是后端版本不匹配')
      }
      
      // 验证当前用户是否在成员列表中
      const currentMember = members.value.find(m => m.userId === currentUserId.value)
      if (currentMember) {
        console.log('【DEBUG】已验证当前用户在成员列表中:', currentMember)
      } else {
        console.warn('【DEBUG】当前用户未在成员列表中，这可能表示有权限问题')
      }
    }
  } catch (error) {
    console.error('【DEBUG】获取成员列表失败:', error)
    // 备用方案：尝试旧的 API
    try {
      const response = await apiCall('family-groups/members', 'GET')
      if (response && response.code === 200 && Array.isArray(response.data)) {
        members.value = response.data
        console.log('【DEBUG】使用备用 API 加载成员:', members.value)
      }
    } catch (fallbackError) {
      console.error('【DEBUG】备用 API 也失败了:', fallbackError)
    }
  }
}

// 加入家庭组
const joinFamily = async () => {
  if (!joinCode.value.trim()) {
    joinError.value = '请输入家庭组编号'
    return
  }

  // 显示对话框，让用户选择是否带入数据
  pendingJoinCode.value = joinCode.value.toUpperCase()
  joinBringData.value = false // 默认不带入数据
  showJoinDialog.value = true
  joinError.value = ''
}

const cancelJoin = () => {
  showJoinDialog.value = false
  pendingJoinCode.value = ''
}

const confirmJoin = async () => {
  joinLoading.value = true
  joinError.value = ''

  try {
    const response = await apiCall('family-members/join', 'POST', {
      code: pendingJoinCode.value,
      bringExistingData: joinBringData.value
    })

    if (response.code === 200) {
      const msg = `加入家庭组成功！${joinBringData.value ? '现有数据已带入。' : '数据保留为个人。'}`
      showMessage(msg, 'success')
      await loadFamilyGroup()
      await loadMembers()
      joinCode.value = ''
      activeTab.value = 'info'
      showJoinDialog.value = false
    } else {
      joinError.value = response.message || '加入家庭组失败'
    }
  } catch (error) {
    joinError.value = error.message || '加入家庭组失败，请检查编号是否正确'
  } finally {
    joinLoading.value = false
  }
}

// 创建新家庭组
const createFamily = async () => {
  if (!newFamilyName.value.trim()) {
    createError.value = '请输入家庭组名称'
    return
  }

  createLoading.value = true
  createError.value = ''

  try {
    const response = await apiCall('family-groups/create', 'POST', {
      name: newFamilyName.value.trim(),
      description: ''
    })

    if (response && response.code === 200) {
      showMessage('家庭组创建成功！', 'success')
      newFamilyName.value = ''
      showCreateForm.value = false
      await loadFamilyGroup()
      await loadMembers()
      activeTab.value = 'info'
    } else {
      createError.value = response?.message || '创建家庭组失败'
    }
  } catch (error) {
    createError.value = error.message || '创建家庭组失败'
    console.error('【DEBUG】创建家庭组异常:', error)
  } finally {
    createLoading.value = false
  }
}

// 复制编号
const copyCode = () => {
  if (familyGroup.value && familyGroup.value.code) {
    navigator.clipboard.writeText(familyGroup.value.code)
    showMessage('编号已复制到剪贴板', 'success')
  }
}

// 更新家庭组信息
const updateFamilyGroup = async () => {
  if (!editForm.value.name.trim()) {
    showMessage('家庭组名称不能为空', 'error')
    return
  }

  editLoading.value = true

  try {
    const response = await apiCall(
      `family-groups/${familyGroup.value.id}`,
      'PUT',
      editForm.value
    )

    if (response.code === 200) {
      familyGroup.value = response.data
      showEditModal.value = false
      showMessage('家庭组已更新', 'success')
    }
  } catch (error) {
    showMessage(error.message || '更新失败', 'error')
  } finally {
    editLoading.value = false
  }
}

// 显示权限管理模态框
const openPermissionModal = async (member) => {
  selectedMember.value = member
  permissionDetails.value = null
  
  isPermissionModalOpen.value = true
  
  // 加载成员的权限详情
  await loadPermissionDetails(member.id)
}

// 加载权限详情
const loadPermissionDetails = async (memberId) => {
  try {
    console.log('【DEBUG】正在加载成员权限详情，memberId:', memberId)
    console.log('【DEBUG】selectedMember 对象:', {
      id: selectedMember.value?.id,
      userId: selectedMember.value?.userId,
      role: selectedMember.value?.role,
      permissions: selectedMember.value?.permissions
    })
    
    // 首先尝试从成员对象中获取权限信息
    if (selectedMember.value && selectedMember.value.permissions) {
      console.log('【DEBUG】成员对象中有 permissions 字段，尝试解析...')
      let permissions = selectedMember.value.permissions
      
      // 如果是 JSON 字符串，则解析它
      if (typeof permissions === 'string' && permissions.startsWith('{')) {
        try {
          permissions = JSON.parse(permissions)
          console.log('【DEBUG】成功解析权限 JSON 字符串:', permissions)
          // 平展嵌套权限结构
          const flatPerm = flattenPermissions(permissions)
          permissionDetails.value = flatPerm
          console.log('【DEBUG】权限已平展:', flatPerm)
          return
        } catch (e) {
          console.warn('【DEBUG】权限 JSON 字符串解析失败:', e, '原始值:', permissions)
        }
      } else if (typeof permissions === 'object' && permissions !== null) {
        console.log('【DEBUG】权限已是对象格式:', permissions)
        // 平展嵌套权限结构
        const flatPerm = flattenPermissions(permissions)
        permissionDetails.value = flatPerm
        console.log('【DEBUG】权限已平展:', flatPerm)
        return
      }
    }
    
    // 如果成员对象中没有权限信息，则调用 API
    console.log('【DEBUG】从 API 加载权限详情...')
    const response = await apiCall(`family-members/${memberId}/permission-details`, 'GET')
    console.log('【DEBUG】权限详情 API 完整响应:', JSON.stringify(response, null, 2))
    
    // 处理各种可能的响应格式
    let permData = null
    
    // 格式1: {code: 200, data: {memberId: ..., permissions: {...或字符串}}}
    if (response && response.code === 200 && response.data) {
      console.log('【DEBUG】标准响应格式 (code=200)')
      const data = response.data
      
      // 如果 data 中有 permissions 字段
      if (data.permissions !== undefined) {
        let perm = data.permissions
        console.log('【DEBUG】从 data.permissions 提取:', perm)
        
        // 如果是字符串，尝试解析
        if (typeof perm === 'string' && perm.startsWith('{')) {
          try {
            perm = JSON.parse(perm)
            console.log('【DEBUG】解析 permissions JSON 字符串成功:', perm)
          } catch (e) {
            console.warn('【DEBUG】解析 permissions JSON 字符串失败:', e)
          }
        }
        permData = perm
      } else {
        // 否则整个 data 就可能是权限对象
        permData = data
        console.log('【DEBUG】使用整个 data 作为权限')
      }
    }
    // 格式2: 直接返回权限对象
    else if (response && typeof response === 'object' && response.income_view !== undefined) {
      console.log('【DEBUG】直接返回权限对象格式')
      permData = response
    }
    // 格式3: {data: {...}}
    else if (response && response.data && typeof response.data === 'object') {
      console.log('【DEBUG】有 data 字段')
      if (response.data.permissions) {
        permData = response.data.permissions
      } else {
        permData = response.data
      }
    }
    
    if (permData) {
      console.log('【DEBUG】准备使用的权限数据:', permData)
      
      // 最后一次检查：确保数据是对象格式且包含权限字段
      if (typeof permData === 'string') {
        try {
          permData = JSON.parse(permData)
          console.log('【DEBUG】最后一次解析成功:', permData)
        } catch (e) {
          console.warn('【DEBUG】最后一次解析失败:', e)
        }
      }
      
      if (typeof permData === 'object' && permData !== null) {
        // 转换嵌套权限结构为平展结构
        const flatPerm = flattenPermissions(permData)
        permissionDetails.value = flatPerm
        console.log('【DEBUG】权限详情已成功加载!', flatPerm)
        return
      }
    }
    
    console.warn('【DEBUG】无法从响应中提取有效的权限数据，使用默认值')
    throw new Error('无法解析权限数据')
  } catch (error) {
    console.warn('【DEBUG】获取权限详情异常:', error.message)
    // 使用默认权限详情（简化的6个权限）
    permissionDetails.value = {
      income_view: true,
      income_edit: false,
      expense_view: true,
      expense_edit: false,
      budget_view: true,
      budget_edit: false
    }
  }
}

// 将嵌套权限结构转换为平展结构
const flattenPermissions = (nestedPerms) => {
  const flattened = {}
  
  if (typeof nestedPerms === 'string') {
    try {
      nestedPerms = JSON.parse(nestedPerms)
    } catch (e) {
      console.warn('【DEBUG】权限 JSON 解析失败:', e)
      return flattened
    }
  }
  
  if (!nestedPerms || typeof nestedPerms !== 'object') {
    return flattened
  }
  
  // 如果已经是平展结构（有 income_view 等字段），直接返回
  if (nestedPerms.income_view !== undefined) {
    return nestedPerms
  }
  
  // 否则转换嵌套结构
  for (const module in nestedPerms) {
    const perms = nestedPerms[module]
    if (typeof perms === 'object') {
      for (const action in perms) {
        flattened[`${module}_${action}`] = perms[action]
      }
    }
  }
  
  console.log('【DEBUG】权限结构转换:', { nested: nestedPerms, flattened: flattened })
  return flattened
}

// 关闭权限模态框
const closePermissionModal = () => {
  isPermissionModalOpen.value = false
  selectedMember.value = null
  permissionDetails.value = null
}

// 格式化权限标签（简化的6个权限）
const formatPermissionLabel = (key) => {
  const labels = {
    'income_view': '收入 - 查看',
    'income_edit': '收入 - 编辑（含创建/删除）',
    'expense_view': '支出 - 查看',
    'expense_edit': '支出 - 编辑（含创建/删除）',
    'budget_view': '预算 - 查看',
    'budget_edit': '预算 - 编辑（含创建/删除）'
  }
  return labels[key] || key
}

// 过滤权限 - 只显示简化的6个权限
const getFilteredPermissions = () => {
  if (!permissionDetails.value) return {}
  
  // 只保留6个权限
  const allowedKeys = ['income_view', 'income_edit', 'expense_view', 'expense_edit', 'budget_view', 'budget_edit']
  const filtered = {}
  for (const key of allowedKeys) {
    if (permissionDetails.value.hasOwnProperty(key)) {
      filtered[key] = permissionDetails.value[key]
    } else {
      filtered[key] = false
    }
  }
  return filtered
}

// 检查成员是否是管理员
const isAdminUser = (member) => {
  return member && member.role === 'ADMIN'
}

// 切换权限
const togglePermission = (key) => {
  if (permissionDetails.value && permissionDetails.value.hasOwnProperty(key)) {
    permissionDetails.value[key] = !permissionDetails.value[key]
    console.log('【DEBUG】权限已切换:', key, '=', permissionDetails.value[key])
  }
}

// 更新权限
const updatePermissions = async () => {
  if (!permissionDetails.value) {
    showMessage('权限信息未加载', 'error')
    return
  }

  permissionLoading.value = true

  try {
    console.log('【DEBUG】正在更新权限，成员ID:', selectedMember.value.id, '权限:', permissionDetails.value)
    
    // 将 Vue Proxy 对象转换为普通 JavaScript 对象
    const permissionsPlain = JSON.parse(JSON.stringify(permissionDetails.value))
    console.log('【DEBUG】转换后的权限对象:', permissionsPlain)
    
    const response = await apiCall('family-members/permissions', 'PUT', {
      memberId: selectedMember.value.id,
      permissions: permissionsPlain
    })
    
    console.log('【DEBUG】权限更新响应:', response)

    closePermissionModal()
    showMessage('权限已更新', 'success')
    await loadMembers()
  } catch (error) {
    console.error('【DEBUG】更新权限异常:', error)
    showMessage(error.message || '更新失败', 'error')
  } finally {
    permissionLoading.value = false
  }
}

// 移除成员
const removeMember = async (memberId) => {
  const member = members.value.find(m => m.id === memberId)
  const isSelf = member && member.userId === currentUserId.value
  
  // 显示对话框，让用户选择是否保留数据
  pendingExitMemberId.value = memberId
  isExitingSelf.value = isSelf
  exitMemberName.value = member?.nickname || member?.username || '该成员'
  exitKeepData.value = true // 默认保留数据
  showExitDialog.value = true
}

const cancelExit = () => {
  showExitDialog.value = false
  pendingExitMemberId.value = null
  isExitingSelf.value = false
}

const confirmExit = async () => {
  const memberId = pendingExitMemberId.value
  const keepData = exitKeepData.value
  const isSelf = isExitingSelf.value
  
  joinLoading.value = true

  try {
    const response = await apiCall(`family-members/${memberId}/exit`, 'POST', {
      deleteData: !keepData // deleteData: true 表示删除，false 表示保留
    })
    
    if (response.code === 200) {
      const msg = isSelf 
        ? `您已成功退出家庭组。${keepData ? '您的数据已转为个人数据。' : '您的数据已被删除。'}`
        : `成员已${keepData ? '移除（数据已转为个人）' : '移除（数据已删除）'}` 
      showMessage(msg, 'success')
      
      if (isSelf) {
        // 延迟后重新加载家庭组信息
        setTimeout(async () => {
          await loadFamilyGroup()
          await loadMembers()
        }, 500)
      } else {
        await loadMembers()
      }
      showExitDialog.value = false
    } else {
      showMessage(response.message || '操作失败', 'error')
    }
  } catch (error) {
    showMessage(error.message || '操作失败', 'error')
  } finally {
    joinLoading.value = false
  }
}

// 获取权限模板列表

// 显示消息
const showMessage = (msg, type = 'success') => {
  message.value = msg
  messageType.value = type
  setTimeout(() => {
    message.value = ''
  }, 3000)
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 生命周期
onMounted(async () => {
  console.log('【DEBUG】FamilyManagement 组件已挂载')
  try {
    // 第一步：获取当前用户信息
    console.log('【DEBUG】第一步：获取当前用户信息')
    await getCurrentUser()
    
    // 第二步：加载家庭组信息
    console.log('【DEBUG】第二步：加载家庭组信息')
    await loadFamilyGroup()
    
    // 第三步：加载成员列表（这会帮助我们确定当前用户 ID）
    console.log('【DEBUG】第三步：加载成员列表')
    await loadMembers()
    
    // 加载完成
    console.log('【DEBUG】加载完成。currentUserId:', currentUserId.value, ', isAdmin:', isAdmin.value, ', members:', members.value.length)
  } catch (error) {
    console.error('【DEBUG】组件加载时出错:', error)
  }
})
</script>

<style scoped>
.family-management-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 8px;
}

/* 标签页样式 */
.tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  border-bottom: 2px solid #e0e0e0;
}

.tab-button {
  padding: 10px 20px;
  background: none;
  border: none;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  transition: all 0.3s ease;
}

.tab-button:hover {
  color: #333;
}

.tab-button.active {
  color: #1890ff;
  border-bottom-color: #1890ff;
}

.tab-content {
  background: white;
  border-radius: 8px;
  padding: 20px;
}

.tab-pane {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 家庭组信息 */
.family-info h3 {
  font-size: 20px;
  margin-bottom: 20px;
  color: #333;
}

.info-section {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.info-section label {
  display: block;
  font-weight: 600;
  color: #666;
  margin-bottom: 8px;
}

.info-section p {
  margin: 0;
  color: #333;
  line-height: 1.6;
}

.code-display {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
}

.code-value {
  font-family: monospace;
  font-size: 18px;
  font-weight: bold;
  color: #1890ff;
}

.copy-btn {
  padding: 6px 12px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: background 0.3s;
}

.copy-btn:hover {
  background: #40a9ff;
}

.hint {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
  margin: 8px 0 0 0;
}

.edit-btn {
  padding: 8px 16px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.3s;
}

.edit-btn:hover {
  background: #40a9ff;
}

/* 加入家庭部分 */
.join-section h3 {
  font-size: 18px;
  margin-bottom: 15px;
  color: #333;
}

.join-section p {
  color: #666;
  margin-bottom: 15px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  font-weight: 600;
  color: #666;
  margin-bottom: 8px;
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 10px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  font-family: inherit;
  box-sizing: border-box;
}

.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

.no-family-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
  text-align: center;
}

.no-family-container h2 {
  font-size: 28px;
  color: #333;
  margin-bottom: 10px;
}

.no-family-container .subtitle {
  font-size: 16px;
  color: #666;
  margin-bottom: 40px;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 24px;
  margin: 0 auto;
}

.action-card {
  background: white;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
  text-align: center;
}

.action-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.action-card .card-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.action-card h3 {
  font-size: 18px;
  color: #333;
  margin-bottom: 8px;
}

.action-card p {
  font-size: 14px;
  color: #666;
  margin-bottom: 16px;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.action-buttons .btn-cancel {
  padding: 8px 16px;
  background: #f0f0f0;
  color: #333;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
}

.action-buttons .btn-cancel:hover {
  background: #e6e6e6;
  border-color: #b3b3b3;
}

.join-btn {
  width: 100%;
  padding: 10px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.3s;
}

.join-btn:hover:not(:disabled) {
  background: #40a9ff;
}

.join-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-message {
  margin-top: 10px;
  padding: 10px;
  background: #fff2f0;
  color: #ff4d4f;
  border-radius: 4px;
  font-size: 12px;
}

/* 成员管理 */
.members-section h3 {
  font-size: 18px;
  margin-bottom: 20px;
  color: #333;
}

.members-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.member-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 4px;
  border-left: 4px solid #1890ff;
}

.member-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.member-role {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.role-admin {
  background: #f6ffed;
  color: #52c41a;
}

.role-member {
  background: #e6f7ff;
  color: #1890ff;
}

.member-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.member-nickname {
  color: #333;
  font-weight: 500;
  font-size: 14px;
}

.member-username {
  color: #999;
  font-size: 12px;
}

.member-actions {
  display: flex;
  gap: 8px;
}

.btn-small {
  padding: 6px 12px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-small:hover {
  background: #40a9ff;
}

.btn-small.danger {
  background: #ff4d4f;
}

.btn-small.danger:hover {
  background: #ff7875;
}

.badge {
  display: inline-block;
  padding: 4px 12px;
  background: #1890ff;
  color: white;
  border-radius: 4px;
  font-size: 12px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.empty-state p {
  margin: 10px 0;
}

/* 模态框 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 8px;
  padding: 30px;
  max-width: 500px;
  width: 90%;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.modal h3 {
  font-size: 18px;
  margin-bottom: 20px;
  color: #333;
}

.member-info-text {
  color: #666;
  margin-bottom: 15px;
  font-size: 14px;
}

.member-username-in-modal {
  color: #999;
  font-size: 12px;
  margin-left: 8px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 25px;
}

.btn-cancel {
  padding: 8px 20px;
  background: #f0f0f0;
  color: #333;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-cancel:hover {
  background: #e0e0e0;
}

.btn-primary {
  padding: 8px 20px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-primary:hover:not(:disabled) {
  background: #40a9ff;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 消息提示 */
.message {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 12px 20px;
  border-radius: 4px;
  color: white;
  font-size: 14px;
  z-index: 2000;
  animation: slideIn 0.3s ease;
}

.message.success {
  background: #52c41a;
}

.message.error {
  background: #ff4d4f;
}

@keyframes slideIn {
  from {
    transform: translateX(400px);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* 响应式设计 */
@media (max-width: 600px) {
  .family-management-container {
    padding: 10px;
  }

  .tabs {
    flex-wrap: wrap;
  }

  .tab-button {
    flex: 1;
    min-width: 80px;
    padding: 8px 10px;
  }

  .member-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .member-actions {
    width: 100%;
    margin-top: 10px;
  }

  .btn-small {
    flex: 1;
  }

  .modal {
    padding: 20px;
  }

  .permission-modal {
    max-width: 90vw;
  }
}

/* 权限管理模态框样式 */
.permission-modal {
  max-width: 600px;
  max-height: 85vh;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.member-card {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 6px;
  margin-bottom: 20px;
}

.member-card .member-role {
  flex-shrink: 0;
}

.member-card .member-name {
  color: #333;
  font-weight: 500;
  margin: 0 0 4px 0;
}

.member-card .member-username {
  color: #999;
  font-size: 12px;
  margin: 0;
}

.permission-details {
  background: #fafafa;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  padding: 15px;
  margin-bottom: 20px;
}

.permission-details h4 {
  margin: 0 0 15px 0;
  font-size: 14px;
  color: #333;
  font-weight: 600;
}

.permissions-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.permission-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 8px;
  background: white;
  border-radius: 4px;
  border: 1px solid #f0f0f0;
}

.permission-item.toggleable {
  cursor: pointer;
  transition: all 0.3s ease;
  user-select: none;
}

.permission-item.toggleable:hover {
  background: #f5f9ff;
  border-color: #1890ff;
  box-shadow: 0 2px 6px rgba(24, 144, 255, 0.1);
}

.permission-label {
  font-size: 12px;
  color: #666;
  font-weight: 500;
}

.permission-status {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 3px;
  font-size: 11px;
  font-weight: 600;
}

.permission-status.granted {
  background: #f6ffed;
  color: #52c41a;
}

.permission-status:not(.granted) {
  background: #fff1f0;
  color: #ff4d4f;
}

.form-section {
  margin-bottom: 20px;
}

.form-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #333;
  font-weight: 600;
}

/* 对话框样式 */
.modal-content {
  background: white;
  border-radius: 8px;
  max-width: 500px;
  width: 90%;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
}

.modal-body > p:first-child {
  margin: 0 0 10px 0;
  color: #666;
  font-size: 14px;
}

.modal-body .question {
  margin: 15px 0 20px 0;
  font-weight: 600;
  color: #333;
  font-size: 15px;
}

.option-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-label {
  display: flex;
  align-items: flex-start;
  padding: 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.option-label:hover {
  border-color: #40a9ff;
  background: #f5f5f5;
}

.option-label input[type="radio"] {
  margin-right: 10px;
  margin-top: 2px;
  cursor: pointer;
  flex-shrink: 0;
}

.option-label > span:first-of-type {
  font-weight: 600;
  color: #333;
}

.option-label .description {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #999;
  margin-left: 0;
}

.modal-footer {
  padding: 15px 20px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn-primary, .btn-danger {
  padding: 8px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.btn-primary {
  background: #1890ff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #40a9ff;
}

.btn-primary:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
  color: #999;
}

.btn-danger {
  background: #ff4d4f;
  color: white;
}

.btn-danger:hover:not(:disabled) {
  background: #ff7875;
}

.btn-danger:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
  color: #999;
}

</style>
