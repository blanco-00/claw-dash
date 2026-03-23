<template>
  <div class="agent-node" :class="{ 'is-main': data.isMain, 'is-selected': selected }">
    <div class="agent-node-header">
      <el-icon v-if="data.isMain" class="main-badge"><Star /></el-icon>
      <span class="agent-name">{{ data.name }}</span>
    </div>
    <div class="agent-node-body">
      <div class="agent-workspace" :title="data.workspace">{{ truncatePath(data.workspace) }}</div>
      <div v-if="data.tags?.length" class="agent-tags">
        <el-tag v-for="tag in data.tags.slice(0, 2)" :key="tag" size="small" type="info">
          {{ tag }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Star } from '@element-plus/icons-vue'
import type { AgentNode } from '@/types/agentGraph'

interface Props {
  data: AgentNode
  selected?: boolean
}

defineProps<Props>()

function truncatePath(path: string): string {
  if (!path) return ''
  if (path.length <= 20) return path
  return '...' + path.slice(-17)
}
</script>

<style scoped>
.agent-node {
  padding: 12px 16px;
  background: var(--card);
  border: 2px solid var(--border);
  border-radius: 8px;
  min-width: 160px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.2s ease;
}

.agent-node:hover {
  border-color: var(--primary);
  box-shadow: 0 4px 8px rgba(114, 46, 209, 0.2);
}

.agent-node.is-selected {
  border-color: var(--primary);
  box-shadow: 0 0 0 2px rgba(114, 46, 209, 0.2);
}

.agent-node.is-main {
  background: var(--primary-light);
  border-color: var(--primary);
}

.agent-node-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}

.main-badge {
  color: var(--warning-color);
}

.agent-name {
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);
}

.agent-node-body {
  font-size: 12px;
  color: var(--text-secondary);
}

.agent-workspace {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
}

.agent-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}
</style>
