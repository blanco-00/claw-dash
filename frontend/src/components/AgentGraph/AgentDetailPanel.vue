<template>
  <div class="detail-panel" :class="{ visible: !!agent }">
    <div v-if="agent" class="panel-content">
      <div class="panel-header">
        <div class="header-title">
          <el-icon v-if="agent.isMain" class="main-icon"><Star /></el-icon>
          <h3>{{ agent.name }}</h3>
        </div>
        <el-button text @click="$emit('close')">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>

      <div class="panel-body">
        <div class="info-section">
          <div class="info-item">
            <span class="label">ID:</span>
            <span class="value">{{ agent.id }}</span>
          </div>
          <div class="info-item">
            <span class="label">Workspace:</span>
            <span class="value path" :title="agent.workspace">{{
              truncatePath(agent.workspace)
            }}</span>
          </div>
          <div v-if="agent.model" class="info-item">
            <span class="label">Model:</span>
            <span class="value">{{ agent.model }}</span>
          </div>
          <div v-if="agent.description" class="info-item full">
            <span class="label">Description:</span>
            <p class="value">{{ agent.description }}</p>
          </div>
          <div v-if="agent.tags?.length" class="info-item full">
            <span class="label">Tags:</span>
            <div class="tags">
              <el-tag v-for="tag in agent.tags" :key="tag" size="small">{{ tag }}</el-tag>
            </div>
          </div>
        </div>

        <div class="relationships-section">
          <h4>Relationships</h4>
          <div v-if="incomingEdges.length" class="rel-group">
            <span class="rel-label">Assigned by:</span>
            <div class="rel-items">
              <span v-for="edge in incomingEdges" :key="edge.id" class="rel-item">
                {{ getNodeName(edge.source) }}
              </span>
            </div>
          </div>
          <div v-if="outgoingEdges.length" class="rel-group">
            <span class="rel-label">Assigns to:</span>
            <div class="rel-items">
              <span v-for="edge in outgoingEdges" :key="edge.id" class="rel-item">
                {{ getNodeName(edge.target) }}
              </span>
            </div>
          </div>
          <div v-if="!incomingEdges.length && !outgoingEdges.length" class="no-rels">
            No relationships defined
          </div>
        </div>

        <div class="meta-section">
          <div class="meta-item">
            <span class="label">Created:</span>
            <span class="value">{{ formatDate(agent.createdAt) }}</span>
          </div>
          <div class="meta-item">
            <span class="label">Updated:</span>
            <span class="value">{{ formatDate(agent.updatedAt) }}</span>
          </div>
        </div>
      </div>

      <div class="panel-footer">
        <el-button v-if="!agent.isMain" type="primary" @click="$emit('edit', agent)">
          Edit
        </el-button>
        <el-button v-else type="info" disabled> Main Agent (Read Only) </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Star, Close } from '@element-plus/icons-vue'
import type { AgentNode, AgentEdge } from '@/types/agentGraph'

interface Props {
  agent?: AgentNode
  edges: AgentEdge[]
  allNodes: AgentNode[]
}

interface Emits {
  (e: 'close'): void
  (e: 'edit', agent: AgentNode): void
}

const props = defineProps<Props>()
const defineEmits = useEmits<Emits>()

const incomingEdges = computed(() => props.edges.filter(e => e.target === props.agent?.id))

const outgoingEdges = computed(() => props.edges.filter(e => e.source === props.agent?.id))

function getNodeName(nodeId: string): string {
  const node = props.allNodes.find(n => n.id === nodeId)
  return node?.name || nodeId
}

function truncatePath(path: string): string {
  if (!path) return ''
  if (path.length <= 25) return path
  return '...' + path.slice(-22)
}

function formatDate(dateStr: string): string {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}
</script>

<script lang="ts">
function useEmits<Emits extends Record<string, any>>() {
  return defineEmits<Emits>()
}
</script>

<style scoped>
.detail-panel {
  position: fixed;
  right: -360px;
  top: 0;
  bottom: 0;
  width: 360px;
  background: var(--card);
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.1);
  transition: right 0.3s ease;
  z-index: 100;
  display: flex;
  flex-direction: column;
}

.detail-panel.visible {
  right: 0;
}

.panel-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-title h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.main-icon {
  color: var(--warning-color);
}

.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.info-section {
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 13px;
}

.info-item.full {
  flex-direction: column;
}

.info-item .label {
  color: var(--text-secondary);
  min-width: 70px;
}

.info-item .value {
  color: var(--text-primary);
  word-break: break-all;
}

.info-item .value.path {
  font-family: monospace;
  font-size: 12px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 4px;
}

.relationships-section {
  margin-bottom: 24px;
}

.relationships-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.rel-group {
  margin-bottom: 8px;
}

.rel-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.rel-items {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 4px;
}

.rel-item {
  display: inline-block;
  padding: 2px 8px;
  background: var(--bg-secondary);
  border-radius: 4px;
  font-size: 12px;
  color: var(--text-secondary);
}

.no-rels {
  color: var(--text-secondary);
  font-size: 13px;
  font-style: italic;
}

.meta-section {
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
}

.meta-item {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  margin-bottom: 8px;
}

.meta-item .label {
  color: var(--text-secondary);
}

.meta-item .value {
  color: var(--text-secondary);
}

.panel-footer {
  padding: 16px 20px;
  border-top: 1px solid var(--border-color);
}
</style>
