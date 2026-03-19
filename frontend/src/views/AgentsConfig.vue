<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { API_BASE } from '@/api/config'

const loading = ref(true)
const agents = ref<any[]>([])
const selectedAgent = ref<any | null>(null)
const editing = ref(false)
const editForm = ref({
  id: '',
  name: '',
  title: '',
  role: '',
  description: '',
  parent_id: '',
  is_active: true
})

async function refresh() {
  loading.value = true
  try {
    const response = await fetch(`${API_BASE}/api/agent-templates`)
    agents.value = await response.json()
  } catch (error) {
    console.error('获取预设模板失败:', error)
  } finally {
    loading.value = false
  }
}

function openDetail(agent: any) {
  selectedAgent.value = { ...agent }
  editForm.value = {
    id: agent.id,
    name: agent.name,
    title: agent.title,
    role: agent.role,
    description: agent.description || '',
    parent_id: agent.parent_id || '',
    is_active: agent.is_active ?? true
  }
  editing.value = false
}

function closeDetail() {
  selectedAgent.value = null
}

function startEdit() {
  editing.value = true
}

function cancelEdit() {
  editing.value = false
  if (selectedAgent.value) {
    editForm.value = {
      id: selectedAgent.value.id,
      name: selectedAgent.value.name,
      title: selectedAgent.value.title,
      role: selectedAgent.value.role,
      description: selectedAgent.value.description || '',
      parent_id: selectedAgent.value.parent_id || '',
      is_active: selectedAgent.value.is_active ?? true
    }
  }
}

async function saveEdit() {
  if (!selectedAgent.value) return

  try {
    const response = await fetch(`${API_BASE}/api/agent-templates/${editForm.value.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(editForm.value)
    })

    if (response.ok) {
      ElMessage.success('保存成功')
      // 更新本地数据
      const index = agents.value.findIndex(a => a.id === selectedAgent.value!.id)
      if (index !== -1) {
        agents.value[index] = { ...editForm.value }
      }
      selectedAgent.value = { ...editForm.value }
      editing.value = false
    } else {
      ElMessage.error('保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  }
}

async function deleteAgent() {
  if (!selectedAgent.value) return

  try {
    const response = await fetch(`${API_BASE}/api/agent-templates/${selectedAgent.value.id}`, {
      method: 'DELETE'
    })

    if (response.ok) {
      ElMessage.success('删除成功')
      agents.value = agents.value.filter(a => a.id !== selectedAgent.value!.id)
      selectedAgent.value = null
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

function addNew() {
  const newId = 'agent_' + Date.now()
  selectedAgent.value = {
    id: newId,
    name: '',
    title: '',
    role: '',
    description: '',
    parent_id: '',
    is_active: true
  }
  editForm.value = {
    id: newId,
    name: '',
    title: '',
    role: '',
    description: '',
    parent_id: '',
    is_active: true
  }
  editing.value = true
}

async function createAgent() {
  try {
    const response = await fetch(`${API_BASE}/api/agent-templates`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(editForm.value)
    })

    if (response.ok) {
      ElMessage.success('创建成功')
      agents.value.push({ ...editForm.value })
      selectedAgent.value = null
    } else {
      ElMessage.error('创建失败')
    }
  } catch (error) {
    console.error('创建失败:', error)
    ElMessage.error('创建失败')
  }
}

onMounted(() => {
  refresh()
})
</script>

<template>
  <div class="agents-config-page">
    <!-- 页面头部 -->
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold">📋 预设模板</h2>
      <div class="flex items-center gap-2">
        <el-button type="primary" :loading="loading" @click="refresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="success" @click="addNew">
          <el-icon><Plus /></el-icon>
          添加Agent
        </el-button>
      </div>
    </div>

    <!-- Agent配置列表 -->
    <el-row :gutter="20">
      <el-col v-for="agent in agents" :key="agent.id" :span="6" class="mb-4">
        <el-card
          shadow="hover"
          class="agent-card cursor-pointer"
          :class="{ 'ring-2 ring-pink-500': selectedAgent?.id === agent.id }"
          @click="openDetail(agent)"
        >
          <div class="text-center">
            <div class="text-4xl mb-2">
              {{
                ['👸', '👩‍🎤', '👩‍💼', '🕵️‍♀️', '👩‍🔬', '💰', '🎭', '🛡️', '⚖️', '🔧', '💻', '🍜', '💊'][
                  agents.indexOf(agent) % 13
                ]
              }}
            </div>
            <div class="font-bold text-lg">{{ agent.name }}</div>
            <div class="text-pink-500">{{ agent.title }}</div>
            <div class="text-gray-500 text-sm mt-1">{{ agent.role }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Agent详情配置抽屉 -->
    <el-drawer
      v-model="selectedAgent"
      :title="selectedAgent?.name + ' - 配置'"
      direction="rtl"
      size="500px"
      @close="closeDetail"
    >
      <div v-if="selectedAgent" class="space-y-4">
        <!-- 头像 -->
        <div class="text-center py-4 border-b">
          <el-avatar :size="80" class="text-4xl">
            {{
              ['👸', '👩‍🎤', '👩‍💼', '🕵️‍♀️', '👩‍🔬', '💰', '🎭', '🛡️', '⚖️', '🔧', '💻', '🍜', '💊'][
                agents.indexOf(selectedAgent) % 13
              ]
            }}
          </el-avatar>
        </div>

        <!-- 配置表单 -->
        <el-form v-if="editing" :model="editForm" label-width="80px">
          <el-form-item label="ID">
            <el-input v-model="editForm.id" :disabled="agents.some(a => a.id === editForm.id)" />
          </el-form-item>
          <el-form-item label="名称">
            <el-input v-model="editForm.name" placeholder="如: 瑾儿" />
          </el-form-item>
          <el-form-item label="封号">
            <el-select v-model="editForm.title" placeholder="选择封号">
              <el-option label="皇后" value="皇后" />
              <el-option label="皇贵妃" value="皇贵妃" />
              <el-option label="贵妃" value="贵妃" />
              <el-option label="妃" value="妃" />
              <el-option label="贵人" value="贵人" />
              <el-option label="嫔" value="嫔" />
              <el-option label="丫鬟" value="丫鬟" />
              <el-option label="研发" value="研发" />
            </el-select>
          </el-form-item>
          <el-form-item label="职责">
            <el-input v-model="editForm.role" placeholder="如: 中书省决策" />
          </el-form-item>
          <el-form-item label="上级">
            <el-select v-model="editForm.parent_id" placeholder="选择上级" clearable>
              <el-option
                v-for="a in agents"
                :key="a.id"
                :label="a.name + ' (' + a.title + ')'"
                :value="a.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="editForm.description" type="textarea" rows="3" />
          </el-form-item>
          <el-form-item label="状态">
            <el-switch v-model="editForm.is_active" active-text="启用" inactive-text="禁用" />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              @click="agents.some(a => a.id === editForm.id) ? saveEdit() : createAgent()"
            >
              {{ agents.some(a => a.id === editForm.id) ? '保存' : '创建' }}
            </el-button>
            <el-button @click="cancelEdit">取消</el-button>
            <el-button
              v-if="agents.some(a => a.id === editForm.id)"
              type="danger"
              plain
              @click="deleteAgent"
              >删除</el-button
            >
          </el-form-item>
        </el-form>

        <!-- 详情展示 -->
        <div v-else class="space-y-4">
          <div class="flex justify-between items-center">
            <span class="text-gray-500">ID</span>
            <span class="font-mono">{{ selectedAgent.id }}</span>
          </div>
          <div class="flex justify-between items-center">
            <span class="text-gray-500">名称</span>
            <span>{{ selectedAgent.name }}</span>
          </div>
          <div class="flex justify-between items-center">
            <span class="text-gray-500">封号</span>
            <el-tag type="pink">{{ selectedAgent.title }}</el-tag>
          </div>
          <div class="flex justify-between items-center">
            <span class="text-gray-500">职责</span>
            <span>{{ selectedAgent.role }}</span>
          </div>
          <div class="flex justify-between items-center">
            <span class="text-gray-500">上级</span>
            <span>{{ selectedAgent.parent_id || '-' }}</span>
          </div>
          <div class="flex justify-between items-center">
            <span class="text-gray-500">描述</span>
            <span class="text-sm">{{ selectedAgent.description || '-' }}</span>
          </div>
          <div class="flex justify-between items-center">
            <span class="text-gray-500">状态</span>
            <el-tag :type="selectedAgent.is_active ? 'success' : 'info'">
              {{ selectedAgent.is_active ? '启用' : '禁用' }}
            </el-tag>
          </div>

          <!-- 操作按钮 -->
          <div class="pt-4 border-t flex gap-2">
            <el-button type="primary" @click="startEdit">编辑配置</el-button>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script lang="ts">
import { Refresh, Plus } from '@element-plus/icons-vue'
export default { components: { Refresh, Plus } }
</script>

<style scoped>
.agents-config-page {
  padding: 20px;
}
.agent-card {
  transition: transform 0.2s;
}
.agent-card:hover {
  transform: translateY(-2px);
}
.cursor-pointer {
  cursor: pointer;
}
</style>
