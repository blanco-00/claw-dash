<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { listTasks } from '@/lib/openclaw/taskQueueApi'
import { API_BASE } from '@/api/config'
import type { AgentBinding } from '@/types/agentGraph'

const { t } = useI18n()

const loading = ref(true)
const agents = ref<any[]>([])
const taskTypes = ref<any[]>([])
const taskDistributor = ref('')
const bindings = ref<AgentBinding[]>([])
const showAddDialog = ref(false)
const addForm = reactive({
  agentId: '',
  agentName: '',
  taskTypes: [] as string[]
})

const distributorOptions = computed(() => {
  return agents.value.map(a => ({
    value: a.name,
    label: a.name
  }))
})

const taskTypeOptions = computed(() => {
  return taskTypes.value.map(t => ({
    value: t.name,
    label: t.displayName || t.name
  }))
})

async function refresh() {
  loading.value = true
  try {
    const [agentsRes, taskTypesRes] = await Promise.all([
      fetch(`${API_BASE}/api/openclaw/agents/names`),
      fetch(`${API_BASE}/api/task-types`)
    ])
    const agentsResult = await agentsRes.json()
    const taskTypesResult = await taskTypesRes.json()
    agents.value = agentsResult.data || []
    taskTypes.value = taskTypesResult.data || []
    await Promise.all([fetchBindings(), fetchDistributor()])
  } catch (error) {
    console.error('Failed to load data:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

async function fetchBindings() {
  try {
    const res = await fetch(`${API_BASE}/api/openclaw/agents/task-bindings`)
    const result = await res.json()
    if (result.code === 200 && result.data) {
      const bindingsMap = new Map<string, any>()
      for (const binding of result.data) {
        const key = binding.agentName
        if (!bindingsMap.has(key)) {
          bindingsMap.set(key, {
            agentId: binding.agentName,
            agentName: binding.agentName,
            taskTypes: [],
            pending: 0,
            running: 0,
            completed: 0
          })
        }
        bindingsMap.get(key).taskTypes.push(binding.taskType)
      }
      bindings.value = Array.from(bindingsMap.values())
    }
  } catch (error) {
    console.error('Failed to fetch bindings:', error)
  }
}

function openAddDialog() {
  addForm.agentId = ''
  addForm.agentName = ''
  addForm.taskTypes = []
  showAddDialog.value = true
}

async function handleAddBinding() {
  if (!addForm.agentId || addForm.taskTypes.length === 0) {
    ElMessage.warning('请选择代理和任务类型')
    return
  }
  
  try {
    for (const taskType of addForm.taskTypes) {
      const res = await fetch(`${API_BASE}/api/openclaw/agents/task-bindings`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ agentName: addForm.agentId, taskType })
      })
      const result = await res.json()
      if (result.code !== 200) {
        throw new Error(result.message || 'Failed to add binding')
      }
    }
    ElMessage.success('绑定添加成功')
    showAddDialog.value = false
    await fetchBindings()
  } catch (error) {
    ElMessage.error('添加绑定失败')
  }
}

async function removeBinding(agentName: string, taskType: string) {
  try {
    const res = await fetch(`${API_BASE}/api/openclaw/agents/task-bindings`, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ agentName, taskType })
    })
    const result = await res.json()
    if (result.code === 200) {
      ElMessage.success('绑定已移除')
      await fetchBindings()
    } else {
      ElMessage.error(result.message || '移除绑定失败')
    }
  } catch (error) {
    ElMessage.error('移除绑定失败')
  }
}

async function removeAgentBindings(agentName: string, taskTypes: string[]) {
  try {
    for (const taskType of taskTypes) {
      const res = await fetch(`${API_BASE}/api/openclaw/agents/task-bindings`, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ agentName, taskType })
      })
      const result = await res.json()
      if (result.code !== 200) {
        ElMessage.error(result.message || `移除 ${taskType} 绑定失败`)
        return
      }
    }
    ElMessage.success('绑定已移除')
    await fetchBindings()
  } catch (error) {
    ElMessage.error('移除绑定失败')
  }
}

async function saveDistributor() {
  if (!taskDistributor.value) {
    ElMessage.warning('请选择 TaskDistributor 代理')
    return
  }
  try {
    await fetch(`${API_BASE}/api/openclaw/agents/distributor`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ agentName: taskDistributor.value })
    })
    ElMessage.success('TaskDistributor 配置已保存')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

async function fetchDistributor() {
  try {
    const res = await fetch(`${API_BASE}/api/openclaw/agents/distributor`)
    const result = await res.json()
    if (result.code === 200 && result.data) {
      taskDistributor.value = result.data.agentName || ''
    }
  } catch (error) {
    console.error('Failed to fetch distributor:', error)
  }
}

onMounted(() => {
  refresh()
})
</script>

<template>
  <div class="page-container agent-task-config-page">
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon">🤖</div>
        <div class="header-text">
          <h2 class="page-title">代理任务配置</h2>
          <p class="page-subtitle">配置代理与任务类型的绑定关系</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button type="default" :loading="loading" @click="refresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <div class="config-section">
      <div class="section-header">
        <h3 class="section-title">TaskDistributor 配置</h3>
        <p class="section-desc">选择负责分解任务组的主代理</p>
      </div>
      <div class="distributor-row">
        <el-select 
          v-model="taskDistributor" 
          placeholder="选择 TaskDistributor 代理" 
          style="width: 300px"
          clearable
        >
          <el-option 
            v-for="opt in distributorOptions" 
            :key="opt.value" 
            :label="opt.label" 
            :value="opt.value" 
          />
        </el-select>
        <el-button type="primary" @click="saveDistributor" :disabled="!taskDistributor">
          保存配置
        </el-button>
      </div>
    </div>

    <div class="config-section">
      <div class="section-header">
        <div>
          <h3 class="section-title">代理绑定</h3>
          <p class="section-desc">配置每个代理负责的任务类型</p>
        </div>
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>
          添加绑定
        </el-button>
      </div>

      <div class="table-panel">
        <el-table :data="bindings" v-loading="loading" stripe>
          <el-table-column prop="agentName" label="代理" min-width="120">
            <template #default="{ row }">
              <div class="agent-cell">
                <span class="agent-icon">🤖</span>
                <span class="agent-name">{{ row.agentName }}</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="taskTypes" label="任务类型" min-width="200">
            <template #default="{ row }">
              <div class="task-types-cell">
                <el-tag 
                  v-for="type in row.taskTypes" 
                  :key="type" 
                  size="small" 
                  class="type-tag"
                >
                  {{ type }}
                </el-tag>
                <span v-if="!row.taskTypes?.length" class="empty-text">未配置</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="待处理" width="80" align="center">
            <template #default="{ row }">
              <span class="stat-num warning">{{ row.pending }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="进行中" width="80" align="center">
            <template #default="{ row }">
              <span class="stat-num primary">{{ row.running }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="已完成" width="80" align="center">
            <template #default="{ row }">
              <span class="stat-num success">{{ row.completed }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="100" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="danger" size="small" text @click="removeAgentBindings(row.agentId, row.taskTypes)">
                移除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog v-model="showAddDialog" title="添加代理绑定" width="500px">
      <el-form :model="addForm" label-width="100px">
        <el-form-item label="选择代理" required>
          <el-select v-model="addForm.agentId" placeholder="选择代理" style="width: 100%">
            <el-option 
              v-for="agent in agents" 
              :key="agent.name" 
              :label="agent.name" 
              :value="agent.name" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="任务类型" required>
          <el-select 
            v-model="addForm.taskTypes" 
            multiple 
            placeholder="选择任务类型" 
            style="width: 100%"
          >
            <el-option 
              v-for="type in taskTypeOptions" 
              :key="type.value" 
              :label="type.label" 
              :value="type.value" 
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddBinding">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { Refresh, Plus } from '@element-plus/icons-vue'
export default { components: { Refresh, Plus } }
</script>

<style scoped>
.agent-task-config-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.config-section {
  background: var(--card);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid var(--border);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: var(--text);
}

.section-desc {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
}

.distributor-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.agent-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.agent-icon {
  font-size: 18px;
}

.agent-name {
  font-weight: 500;
}

.task-types-cell {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.type-tag {
  margin: 0;
}

.empty-text {
  color: var(--text-tertiary);
  font-size: 13px;
}

.stat-num {
  font-weight: 600;
  font-size: 16px;
}

.stat-num.success {
  color: #52c41a;
}

.stat-num.warning {
  color: #faad14;
}

.stat-num.primary {
  color: #722ed1;
}

.stat-num.danger {
  color: #ff4d4f;
}

.table-panel {
  flex: 1;
  overflow: auto;
}
</style>
