<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getAllAgentDetails } from '@/api/agents'
import type { AgentInfo } from '@/types/agent'
import AgentListTab from '@/components/agents/AgentListTab.vue'
import ConfigGraphTab from '@/components/agents/ConfigGraphTab.vue'

const { t } = useI18n()

const loading = ref(true)
const agents = ref<AgentInfo[]>([])
const activeTab = ref('config')

async function refresh() {
  loading.value = true
  try {
    agents.value = await getAllAgentDetails()
  } catch (error) {
    console.error('Failed to load agents:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  refresh()
})
</script>

<template>
  <div class="agents-page">
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold">🕸️ {{ t('agents.title') }}</h2>
      <el-button type="primary" :loading="loading" @click="refresh">
        <el-icon><Refresh /></el-icon>
        {{ t('agents.refresh') }}
      </el-button>
    </div>

    <el-tabs v-model="activeTab" class="agent-tabs">
      <el-tab-pane :label="t('agents.tabs.configGraph')" name="config">
        <ConfigGraphTab :graph-id="1" />
      </el-tab-pane>
      <el-tab-pane :label="t('agents.tabs.agentList')" name="list">
        <AgentListTab :agents="agents" :loading="loading" @refresh="refresh" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script lang="ts">
import { Refresh } from '@element-plus/icons-vue'
export default {
  components: { Refresh }
}
</script>

<style scoped>
.agents-page {
  padding: 20px;
  height: 100%;
}

.agent-tabs {
  height: 100%;
}

.agent-tabs :deep(.el-tabs__content) {
  height: calc(100% - 55px);
  overflow: auto;
}

.agent-tabs :deep(.el-tab-pane) {
  height: 100%;
}
</style>
