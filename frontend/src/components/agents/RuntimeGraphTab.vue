<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Connection, Loading } from '@element-plus/icons-vue'
import { VueFlow, useVueFlow } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import { MiniMap } from '@vue-flow/minimap'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import '@vue-flow/controls/dist/style.css'
import '@vue-flow/minimap/dist/style.css'
import { useI18n } from 'vue-i18n'
import { getCurrentRuntimeGraph } from '@/lib/runtimeGraphApi'

const { t } = useI18n()
const { fitView } = useVueFlow()

const loading = ref(false)
const lastRefresh = ref<string | null>(null)
const nodes = ref<any[]>([])
const edges = ref<any[]>([])
const error = ref<string | null>(null)

const nodeColors: Record<string, string> = {
  active: '#10b981',
  inactive: '#6b7280',
  unknown: '#f59e0b'
}

async function loadRuntimeGraph() {
  loading.value = true
  error.value = null
  
  try {
    const data = await getCurrentRuntimeGraph()
    
    if (data.error) {
      error.value = data.error
      ElMessage.warning(t('agents.runtime.fetchError') + ': ' + data.error)
      return
    }
    
    nodes.value = (data.nodes || []).map((n: any, i: number) => ({
      id: n.id,
      type: 'runtimeAgent',
      position: { x: 200 + (i % 4) * 200, y: 150 + Math.floor(i / 4) * 150 },
      data: { 
        label: n.name || n.id, 
        status: n.status || 'unknown' 
      },
      style: { 
        background: nodeColors[n.status] || nodeColors.unknown,
        color: '#fff',
        border: 'none',
        borderRadius: '8px',
        padding: '10px 20px'
      }
    }))
    
    edges.value = (data.edges || []).map((e: any) => ({
      id: e.id,
      source: e.source,
      target: e.target,
      type: 'smoothstep',
      animated: true,
      style: { stroke: '#3b82f6' }
    }))
    
    lastRefresh.value = new Date().toLocaleTimeString()
  } catch (err) {
    console.error('Failed to load runtime graph:', err)
    error.value = (err as Error).message
  } finally {
    loading.value = false
  }
}

function fitViewGraph() {
  fitView({ padding: 0.2 })
}

onMounted(() => {
  loadRuntimeGraph()
})
</script>

<template>
  <div class="runtime-graph-tab">
    <div class="toolbar glass-toolbar">
      <div class="toolbar-left">
        <el-icon><Connection /></el-icon>
        <span>{{ t('agents.runtime.runtimeGraph') }}</span>
        <span v-if="nodes.length" class="node-count">({{ nodes.length }} {{ t('agents.runtime.agentsCount') }})</span>
      </div>
      <div class="toolbar-right">
        <span v-if="lastRefresh" class="last-refresh">{{ t('agents.runtime.lastRefresh') }}: {{ lastRefresh }}</span>
        <el-button :loading="loading" @click="loadRuntimeGraph">
          <el-icon><Refresh /></el-icon>
          {{ t('common.refresh') }}
        </el-button>
        <el-button @click="fitViewGraph">
          <el-icon><Connection /></el-icon>
          {{ t('agents.graph.fit') }}
        </el-button>
      </div>
    </div>
    
    <div class="canvas-container">
      <VueFlow
        v-model:nodes="nodes"
        v-model:edges="edges"
        :default-viewport="{ zoom: 1 }"
      >
        <Background pattern-color="#aaa" :gap="16" />
        <Controls />
        <MiniMap />
      </VueFlow>
      
      <div v-if="loading" class="loading-overlay">
        <el-icon class="is-loading" size="32"><Loading /></el-icon>
      </div>
      
      <div v-if="!loading && nodes.length === 0 && !error" class="placeholder-overlay">
        <el-icon size="64" class="placeholder-icon"><Connection /></el-icon>
        <h3>{{ t('agents.runtime.noData') }}</h3>
        <p>{{ t('agents.runtime.noActiveBindings') }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.runtime-graph-tab {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  padding: 12px 16px;
  margin-bottom: 12px;
  border-radius: 8px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.node-count {
  font-weight: 400;
  color: var(--text-secondary);
  font-size: 13px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.last-refresh {
  font-size: 13px;
  color: var(--text-secondary);
}

.canvas-container {
  flex: 1;
  position: relative;
  background: var(--bg-secondary);
  border-radius: 8px;
  overflow: hidden;
}

.loading-overlay,
.placeholder-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(var(--bg-primary-rgb, 255,255,255), 0.9);
}

.placeholder-overlay {
  color: var(--text-secondary);
}

.placeholder-icon {
  color: var(--text-tertiary);
  margin-bottom: 16px;
}

.placeholder-overlay h3 {
  margin: 0 0 8px;
  color: var(--text-primary);
}

.placeholder-overlay p {
  margin: 4px 0;
  font-size: 14px;
}
</style>
