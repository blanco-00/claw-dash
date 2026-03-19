<template>
  <div class="agents-page">
    <div class="header">
      <h2>Agent 管理</h2>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>新建 Agent
      </el-button>
    </div>

    <el-tabs v-model="activeTab" class="agent-tabs">
      <el-tab-pane label="OpenClaw Agents" name="openclaw">
        <el-table :data="openclawAgents" style="width: 100%" v-loading="loading">
          <el-table-column prop="id" label="Agent ID" width="180" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.hasFiles ? 'success' : 'info'">
                {{ row.hasFiles ? '已配置' : '未配置' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="SOUL.md" width="100">
            <template #default="{ row }">
              <el-tag :type="row.soulSize > 0 ? 'success' : 'warning'" size="small">
                {{ formatSize(row.soulSize) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="MEMORY.md" width="100">
            <template #default="{ row }">
              <el-tag :type="row.memorySize > 0 ? 'success' : 'warning'" size="small">
                {{ formatSize(row.memorySize) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button size="small" @click="viewAgentDetails(row)">详情</el-button>
              <el-button size="small" type="danger" @click="deleteOpenclawAgent(row)"
                >删除</el-button
              >
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="元数据管理" name="metadata">
        <el-table :data="metadataAgents" style="width: 100%" v-loading="loading">
          <el-table-column prop="id" label="ID" width="150" />
          <el-table-column prop="name" label="名称" width="150" />
          <el-table-column prop="title" label="封号/头衔" width="150" />
          <el-table-column prop="role" label="角色" width="120" />
          <el-table-column prop="parent_id" label="上级" width="120">
            <template #default="{ row }">
              {{ row.parent_id || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" />
          <el-table-column label="操作" width="250">
            <template #default="{ row }">
              <el-button size="small" @click="editAgent(row)">编辑</el-button>
              <el-button size="small" @click="manageRelationships(row)">关系</el-button>
              <el-button size="small" type="danger" @click="deleteMetadataAgent(row)"
                >删除</el-button
              >
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog
      v-model="showAgentDialog"
      :title="isEditing ? '编辑 Agent' : '新建 Agent'"
      width="500px"
    >
      <el-form :model="agentForm" label-width="100px">
        <el-form-item label="名称">
          <el-input v-model="agentForm.name" placeholder="agent-name" />
        </el-form-item>
        <el-form-item label="封号/头衔">
          <el-input v-model="agentForm.title" placeholder="如：皇后、贵妃、嫔妃" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="agentForm.role" placeholder="选择角色" style="width: 100%">
            <el-option label="决策者" value="decision-maker" />
            <el-option label="协调者" value="coordinator" />
            <el-option label="执行者" value="executor" />
            <el-option label="辅助者" value="assistant" />
          </el-select>
        </el-form-item>
        <el-form-item label="上级">
          <el-select
            v-model="agentForm.parent_id"
            placeholder="选择上级 Agent"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="agent in metadataAgents"
              :key="agent.id"
              :label="agent.name"
              :value="agent.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="agentForm.description"
            type="textarea"
            :rows="3"
            placeholder="Agent 的职责描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAgentDialog = false">取消</el-button>
        <el-button type="primary" @click="saveAgent" :loading="saving">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showRelationshipDialog" title="管理 Agent 关系" width="600px">
      <div v-if="selectedAgent">
        <h4>{{ selectedAgent.name }} 的关系网络</h4>
        <el-divider />
        <el-form label-width="80px">
          <el-form-item label="上级">
            <el-select
              v-model="relationshipForm.parent_id"
              placeholder="选择上级"
              clearable
              style="width: 100%"
            >
              <el-option
                v-for="agent in availableParentAgents"
                :key="agent.id"
                :label="agent.name"
                :value="agent.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="下级">
            <el-select
              v-model="relationshipForm.children"
              multiple
              placeholder="选择下级"
              style="width: 100%"
            >
              <el-option
                v-for="agent in availableChildAgents"
                :key="agent.id"
                :label="agent.name"
                :value="agent.id"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="showRelationshipDialog = false">取消</el-button>
        <el-button type="primary" @click="saveRelationships" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showDetailDialog" title="Agent 详情" width="600px">
      <el-descriptions v-if="selectedAgent" :column="2" border>
        <el-descriptions-item label="ID">{{ selectedAgent.id }}</el-descriptions-item>
        <el-descriptions-item label="名称">{{ selectedAgent.name }}</el-descriptions-item>
        <el-descriptions-item label="封号">{{ selectedAgent.title }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ selectedAgent.role }}</el-descriptions-item>
        <el-descriptions-item label="上级">{{
          selectedAgent.parent_id || '-'
        }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{
          selectedAgent.description
        }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const activeTab = ref('openclaw')
const loading = ref(false)
const saving = ref(false)
const openclawAgents = ref<any[]>([])
const metadataAgents = ref<any[]>([])
const showAgentDialog = ref(false)
const showRelationshipDialog = ref(false)
const showDetailDialog = ref(false)
const isEditing = ref(false)
const selectedAgent = ref<any>(null)

const agentForm = ref({
  id: '',
  name: '',
  title: '',
  role: '',
  description: '',
  parent_id: ''
})

const relationshipForm = ref({
  parent_id: '',
  children: [] as string[]
})

const loadOpenclawAgents = async () => {
  loading.value = true
  try {
    const res = await request.get('/agents')
    openclawAgents.value = res.data || []
  } catch (error) {
    console.error('Failed to load OpenClaw agents:', error)
  } finally {
    loading.value = false
  }
}

const loadMetadataAgents = async () => {
  loading.value = true
  try {
    const res = await request.get('/agents-metadata')
    metadataAgents.value = res.data || []
  } catch (error) {
    console.error('Failed to load metadata agents:', error)
  } finally {
    loading.value = false
  }
}

const formatSize = (bytes: number) => {
  if (bytes === 0) return '无'
  if (bytes < 1024) return `${bytes}B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)}KB`
  return `${(bytes / 1024 / 1024).toFixed(1)}MB`
}

const openCreateDialog = () => {
  isEditing.value = false
  agentForm.value = { id: '', name: '', title: '', role: '', description: '', parent_id: '' }
  showAgentDialog.value = true
}

const editAgent = (row: any) => {
  isEditing.value = true
  agentForm.value = { ...row }
  showAgentDialog.value = true
}

const saveAgent = async () => {
  if (!agentForm.value.name) {
    ElMessage.warning('请输入名称')
    return
  }

  saving.value = true
  try {
    if (isEditing.value) {
      await request.put(`/api/agents/${agentForm.value.id}`, agentForm.value)
      ElMessage.success('更新成功')
    } else {
      await request.post('/api/agents', agentForm.value)
      ElMessage.success('创建成功')
    }
    showAgentDialog.value = false
    loadMetadataAgents()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    saving.value = false
  }
}

const deleteMetadataAgent = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除 Agent "${row.name}" 吗?`, '确认删除', {
      type: 'warning'
    })
    await request.delete(`/api/agents/${row.id}`)
    ElMessage.success('删除成功')
    loadMetadataAgents()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const deleteOpenclawAgent = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要删除 OpenClaw Agent "${row.id}" 吗?`, '确认删除', {
      type: 'warning'
    })
    ElMessage.info('删除 OpenClaw Agent 需要通过命令行操作')
  } catch (error) {}
}

const viewAgentDetails = (row: any) => {
  selectedAgent.value = row
  showDetailDialog.value = true
}

const manageRelationships = async (row: any) => {
  selectedAgent.value = row
  try {
    const res = await request.get(`/api/agents/${row.id}/relationships`)
    const data = res.data || {}
    relationshipForm.value = {
      parent_id: data.parents?.[0]?.id || '',
      children: data.children?.map((c: any) => c.id) || []
    }
  } catch (error) {
    relationshipForm.value = { parent_id: '', children: [] }
  }
  showRelationshipDialog.value = true
}

const availableParentAgents = computed(() => {
  return metadataAgents.value.filter(a => a.id !== selectedAgent.value?.id)
})

const availableChildAgents = computed(() => {
  return metadataAgents.value.filter(a => a.id !== selectedAgent.value?.id)
})

const saveRelationships = async () => {
  if (!selectedAgent.value) return

  saving.value = true
  try {
    if (relationshipForm.value.parent_id) {
      await request.post(`/api/agents/${relationshipForm.value.parent_id}/relationships`, {
        child_id: selectedAgent.value.id,
        relationship_type: 'direct'
      })
    }

    for (const childId of relationshipForm.value.children) {
      await request.post(`/api/agents/${selectedAgent.value.id}/relationships`, {
        child_id: childId,
        relationship_type: 'direct'
      })
    }

    ElMessage.success('关系保存成功')
    showRelationshipDialog.value = false
    loadMetadataAgents()
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadOpenclawAgents()
  loadMetadataAgents()
})
</script>

<style scoped>
.agents-page {
  padding: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.header h2 {
  margin: 0;
}
.agent-tabs {
  background: var(--el-bg-color);
  border-radius: 8px;
  padding: 10px;
}
</style>
