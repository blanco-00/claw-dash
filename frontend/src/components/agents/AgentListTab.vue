<script setup lang="ts">
import { ref, computed } from 'vue'
import { Search, Refresh, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import type { AgentInfo } from '@/types/agent'
import AgentDetailPanel from './AgentDetailPanel.vue'
import { getOrphanedAgents, cleanupOrphanedAgents } from '@/api/agents'

const { t } = useI18n()

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
const currentPage = ref(1)
const pageSize = ref(20)

const filteredAgents = computed(() => {
  return props.agents.filter(a => {
    const matchSearch = !search.value || 
      a.name?.toLowerCase().includes(search.value.toLowerCase()) ||
      a.title?.toLowerCase().includes(search.value.toLowerCase())
    return matchSearch
  })
})

const paginatedAgents = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredAgents.value.slice(start, end)
})

const totalCount = computed(() => filteredAgents.value.length)

function onPageChange(page: number) {
  currentPage.value = page
}

function onRowClick(row: AgentInfo) {
  selectedAgentName.value = row.name || row.id
  detailPanelVisible.value = true
}

async function handleCleanup() {
  const orphaned = await getOrphanedAgents()
  if (orphaned.length === 0) {
    ElMessage.info(t('agents.list.noOrphanedNodes'))
    return
  }
  
  try {
    await ElMessageBox.confirm(
      t('agents.list.confirmCleanup', { count: orphaned.length, nodes: orphaned.join(', ') }),
      t('agents.list.cleanupConfirm'),
      {
        confirmButtonText: t('agents.list.cleanup'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    
    cleaning.value = true
    const res = await cleanupOrphanedAgents()
    if (res.code === 200) {
      ElMessage.success(t('agents.list.cleanupSuccess', { count: res.data.deleted?.length || 0 }))
      emit('refresh')
    } else {
      ElMessage.error(res.message || t('agents.list.cleanupFailed'))
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
      <el-input v-model="search" :placeholder="t('agents.list.searchPlaceholder')" prefix-icon="Search" clearable />
      <el-button :loading="loading" @click="emit('refresh')">
        <el-icon><Refresh /></el-icon>
        {{ t('common.refresh') }}
      </el-button>
      <el-button type="warning" :loading="cleaning" @click="handleCleanup">
        <el-icon><Delete /></el-icon>
        {{ t('agents.list.orphanCleanup') }}
      </el-button>
    </div>
    
    <el-table 
      :data="paginatedAgents" 
      stripe 
      style="width: 100%"
      @row-click="onRowClick"
      class="clickable-table"
    >
      <el-table-column prop="name" :label="t('agents.list.name')" min-width="150">
        <template #default="{ row }">
          <div class="agent-name-cell">
            <span>{{ row.name || row.id }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="workspace" :label="t('agents.list.workspace')" min-width="250" show-overflow-tooltip>
        <template #default="{ row }">
          <span class="workspace-path">{{ row.workspace || '-' }}</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="totalCount"
        layout="total, prev, pager, next"
        @current-change="onPageChange"
      />
    </div>

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

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
  padding: 12px 0;
}
</style>
