<script setup lang="ts">
import { ref, computed } from 'vue'
import { Search, Refresh, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { AgentInfo } from '@/types/agent'
import AgentDetailPanel from './AgentDetailPanel.vue'
import { getOrphanedAgents, cleanupOrphanedAgents } from '@/api/agents'

const props = defineProps<{
  agents: AgentInfo[]
  loading: boolean
}>()

const emit = defineEmits<{
  refresh: []
}>()

const search = ref('')
const selectedAgentName = ref<string | null>(null)
const detailPanelVisible = ref(false)
const cleaning = ref(false)

const filteredAgents = computed(() => {
  return props.agents.filter(a => {
    const matchSearch = !search.value || 
      a.name?.toLowerCase().includes(search.value.toLowerCase()) ||
      a.title?.toLowerCase().includes(search.value.toLowerCase())
    return matchSearch
  })
})

function onRowClick(row: AgentInfo) {
  selectedAgentName.value = row.name || row.id
  detailPanelVisible.value = true
}

async function handleCleanup() {
  const orphaned = await getOrphanedAgents()
  if (orphaned.length === 0) {
    ElMessage.info('没有发现脏数据')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `发现 ${orphaned.length} 个孤立节点: ${orphaned.join(', ')}。是否清理？`,
      '清理确认',
      {
        confirmButtonText: '清理',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    cleaning.value = true
    const res = await cleanupOrphanedAgents()
    if (res.code === 200) {
      ElMessage.success(`已清理 ${res.data.deleted?.length || 0} 个孤立节点`)
      emit('refresh')
    } else {
      ElMessage.error(res.message || '清理失败')
    }
  } catch {
    // User cancelled
  } finally {
    cleaning.value = false
  }
}
</script>

<template>
  <div class="agent-list-tab">
    <div class="filter-bar">
      <el-input v-model="search" placeholder="Search agents..." prefix-icon="Search" clearable />
      <el-button :loading="loading" @click="emit('refresh')">
        <el-icon><Refresh /></el-icon>
        Refresh
      </el-button>
      <el-button type="warning" :loading="cleaning" @click="handleCleanup">
        <el-icon><Delete /></el-icon>
        清理孤立节点
      </el-button>
    </div>
    
    <el-table 
      :data="filteredAgents" 
      stripe 
      style="width: 100%"
      @row-click="onRowClick"
      class="clickable-table"
    >
      <el-table-column prop="name" label="名称" min-width="150">
        <template #default="{ row }">
          <div class="agent-name-cell">
            <span>{{ row.name || row.id }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="workspace" label="工作区" min-width="250" show-overflow-tooltip>
        <template #default="{ row }">
          <span class="workspace-path">{{ row.workspace || '-' }}</span>
        </template>
      </el-table-column>
    </el-table>

    <AgentDetailPanel
      v-model:visible="detailPanelVisible"
      :agent-name="selectedAgentName"
    />
  </div>
</template>

<style scoped>
.agent-list-tab {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.filter-bar .el-input {
  width: 250px;
}

.agent-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.workspace-path {
  font-size: 12px;
  color: var(--text-secondary);
  font-family: monospace;
}

.clickable-table :deep(tr.el-table__row) {
  cursor: pointer;
}
</style>
