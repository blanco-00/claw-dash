<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import type { AgentInfo } from '@/types/agent'

const props = defineProps<{
  agents: AgentInfo[]
  loading: boolean
}>()

const emit = defineEmits<{
  refresh: []
}>()

const search = ref('')
const statusFilter = ref('')

const filteredAgents = computed(() => {
  return props.agents.filter(a => {
    const matchSearch = !search.value || 
      a.name?.toLowerCase().includes(search.value.toLowerCase()) ||
      a.role?.toLowerCase().includes(search.value.toLowerCase())
    const matchStatus = !statusFilter.value || a.status === statusFilter.value
    return matchSearch && matchStatus
  })
})
</script>

<template>
  <div class="agent-list-tab">
    <div class="filter-bar">
      <el-input v-model="search" placeholder="Search agents..." prefix-icon="Search" clearable />
      <el-select v-model="statusFilter" placeholder="Status" clearable>
        <el-option label="Active" value="ACTIVE" />
        <el-option label="Inactive" value="INACTIVE" />
      </el-select>
      <el-button :loading="loading" @click="emit('refresh')">
        <el-icon><Refresh /></el-icon>
        Refresh
      </el-button>
    </div>
    
    <el-table :data="filteredAgents" stripe style="width: 100%">
      <el-table-column prop="name" label="Name" min-width="150">
        <template #default="{ row }">
          <div class="agent-name-cell">
            <span class="status-dot" :class="row.status?.toLowerCase()"></span>
            <span>{{ row.name }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="role" label="Role" min-width="150" />
      <el-table-column prop="status" label="Status" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="workspace" label="Workspace" min-width="200" show-overflow-tooltip />
    </el-table>
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

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-dot.active {
  background: #10b981;
}

.status-dot.inactive {
  background: #6b7280;
}
</style>
