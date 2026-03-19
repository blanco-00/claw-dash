<template>
  <el-button :loading="loading" @click="handleSync">
    <el-icon v-if="!loading"><Refresh /></el-icon>
    Sync
  </el-button>
</template>

<script setup lang="ts">
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { listOpenClawAgents, getMainAgent, loadGraph, saveGraph } from '@/lib/openclaw/agentApi'
import type { AgentNode, AgentGraph } from '@/types/agentGraph'

interface Props {
  loading?: boolean
}

interface Emits {
  (e: 'sync'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

async function handleSync() {
  emit('sync')
}
</script>
