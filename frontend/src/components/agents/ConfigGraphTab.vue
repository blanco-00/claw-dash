<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { VueFlow, useVueFlow } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import { MiniMap } from '@vue-flow/minimap'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import '@vue-flow/controls/dist/style.css'
import '@vue-flow/minimap/dist/style.css'
import { ElMessage } from 'element-plus'
import { Plus, Link, Delete, Aim, Connection, Loading, Check } from '@element-plus/icons-vue'
import { configGraphApi } from '@/lib/configGraphApi'
import { getAllAgentDetails } from '@/api/agents'
import type { AgentInfo } from '@/types/agent'
import type { ConfigNode, ConfigEdge, EdgeType } from '@/types/agentGraph'

const { fitView } = useVueFlow()

const loading = ref(true)
const graphId = ref(1)
const nodes = ref<any[]>([])
const edges = ref<any[]>([])
const agents = ref<AgentInfo[]>([])
const agentPickerVisible = ref(false)
const agentSearch = ref('')
const linkMode = ref<EdgeType>('assigns')
const isConnecting = ref(false)
const connectSource = ref<string | null>(null)
const selectedEdge = ref<any>(null)
const edgeDialogVisible = ref(false)

const edgeColors: Record<string, string> = {
  assigns: '#10b981',
  reports_to: '#f59e0b',
  communicates: '#3b82f6'
}

const linkModeLabel = computed(() => {
  const labels: Record<string, string> = {
    assigns: 'Assigns',
    reports_to: 'Reports To',
    communicates: 'Communicates'
  }
  return labels[linkMode.value] || linkMode.value
})

const filteredAgents = computed(() => {
  if (!agentSearch.value) return agents.value
  const search = agentSearch.value.toLowerCase()
  return agents.value.filter(a => 
    a.name?.toLowerCase().includes(search) ||
    a.role?.toLowerCase().includes(search)
  )
})

function isAgentInGraph(agentId: string): boolean {
  return nodes.value.some(n => n.id === agentId)
}

async function loadData() {
  loading.value = true
  try {
    const [graphData, agentList] = await Promise.all([
      configGraphApi.get(graphId.value).catch(() => null),
      getAllAgentDetails().catch(() => [])
    ])
    
    agents.value = agentList || []
    
    if (graphData?.data) {
      const { graph, nodes: configNodes, edges: configEdges } = graphData.data
      nodes.value = configNodes.map((n: ConfigNode) => {
        const agent = agents.value.find(a => a.id === n.agentId)
        return {
          id: n.agentId,
          type: 'configAgent',
          position: { x: n.x || 0, y: n.y || 0 },
          data: { label: agent?.name || n.agentId }
        }
      })
      
      edges.value = configEdges.map((e: ConfigEdge) => ({
        id: `e-${e.id}`,
        source: e.sourceId,
        target: e.targetId,
        type: 'smoothstep',
        animated: e.edgeType === 'communicates',
        style: { stroke: edgeColors[e.edgeType] || '#6b7280' },
        data: { edgeType: e.edgeType, label: e.label, enabled: e.enabled }
      }))
    }
    
    if (nodes.value.length === 0 && agents.value.length > 0) {
      nodes.value = agents.value.slice(0, 5).map((a, i) => ({
        id: a.id,
        type: 'configAgent',
        position: { x: 200 + (i % 3) * 250, y: 150 + Math.floor(i / 3) * 200 },
        data: { label: a.name || a.id }
      }))
    }
  } catch (err) {
    console.error('Failed to load graph:', err)
  } finally {
    loading.value = false
  }
}

async function showAgentPicker() {
  await loadData()
  agentPickerVisible.value = true
}

async function addAgentToGraph(agent: AgentInfo) {
  if (isAgentInGraph(agent.id)) return
  
  try {
    const position = { 
      x: 100 + Math.random() * 400, 
      y: 100 + Math.random() * 300 
    }
    await configGraphApi.addNode(graphId.value, { 
      agentId: agent.id, 
      x: position.x, 
      y: position.y 
    })
    
    nodes.value = [...nodes.value, {
      id: agent.id,
      type: 'configAgent',
      position,
      data: { label: agent.name || agent.id }
    }]
    
    ElMessage.success(`Added ${agent.name} to graph`)
    agentPickerVisible.value = false
  } catch (err) {
    ElMessage.error('Failed to add agent')
  }
}

async function onConnect(params: any) {
  if (!params.source || !params.target) return
  if (params.source === params.target) return
  
  const exists = edges.value.some(
    e => e.source === params.source && e.target === params.target
  )
  if (exists) {
    ElMessage.warning('Edge already exists')
    return
  }
  
  try {
    const result = await configGraphApi.addEdge(graphId.value, {
      sourceId: params.source,
      targetId: params.target,
      edgeType: linkMode.value
    })
    
    edges.value = [...edges.value, {
      id: `e-${result.data?.id || Date.now()}`,
      source: params.source,
      target: params.target,
      type: 'smoothstep',
      animated: linkMode.value === 'communicates',
      style: { stroke: edgeColors[linkMode.value] },
      data: { edgeType: linkMode.value, enabled: true }
    }]
    
    ElMessage.success('Edge created')
  } catch (err) {
    ElMessage.error('Failed to create edge')
  }
}

async function onNodeDragStop(event: any) {
  const { node } = event
  try {
    await configGraphApi.updateNodePosition(graphId.value, node.id, {
      x: node.position.x,
      y: node.position.y
    })
  } catch (err) {
    console.error('Failed to save position:', err)
  }
}

function onNodeClick(event: any) {
  if (isConnecting.value && connectSource.value) {
    if (connectSource.value !== event.node.id) {
      onConnect({ source: connectSource.value, target: event.node.id })
    }
    isConnecting.value = false
    connectSource.value = null
  }
}

function onPaneClick() {
  isConnecting.value = false
  connectSource.value = null
}

function onEdgeClick(event: any) {
  selectedEdge.value = event.edge
  edgeDialogVisible.value = true
}

async function deleteSelected() {
  const selectedNodes = nodes.value.filter(n => n.selected)
  const selectedEdges = edges.value.filter(e => e.selected)
  
  for (const edge of selectedEdges) {
    const edgeId = edge.id.replace('e-', '')
    try {
      await configGraphApi.removeEdge(graphId.value, parseInt(edgeId))
    } catch (err) {
      console.error('Failed to delete edge:', err)
    }
  }
  
  for (const node of selectedNodes) {
    try {
      await configGraphApi.removeNode(graphId.value, node.id)
    } catch (err) {
      console.error('Failed to delete node:', err)
    }
  }
  
  edges.value = edges.value.filter(e => !e.selected)
  nodes.value = nodes.value.filter(n => !n.selected)
  ElMessage.success('Deleted selected items')
}

function fitViewGraph() {
  fitView({ padding: 0.2 })
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="config-graph-tab">
    <div class="toolbar glass-toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="showAgentPicker">
          <el-icon><Plus /></el-icon>
          Add Node
        </el-button>
        
        <el-select v-model="linkMode" placeholder="Link Mode" class="link-mode-select">
          <el-option label="Assigns (→)" value="assigns" />
          <el-option label="Reports To (↑)" value="reports_to" />
          <el-option label="Communicates (↔)" value="communicates" />
        </el-select>
      </div>
      
      <div class="toolbar-right">
        <el-button @click="deleteSelected">
          <el-icon><Delete /></el-icon>
          Delete
        </el-button>
        <el-button @click="fitViewGraph">
          <el-icon><Aim /></el-icon>
          Fit
        </el-button>
      </div>
    </div>
    
    <div class="canvas-container">
      <VueFlow
        v-model:nodes="nodes"
        v-model:edges="edges"
        :default-viewport="{ zoom: 1 }"
        @connect="onConnect"
        @node-drag-stop="onNodeDragStop"
        @pane-click="onPaneClick"
        @node-click="onNodeClick"
        @edge-click="onEdgeClick"
      >
        <Background pattern-color="#aaa" :gap="16" />
        <Controls />
        <MiniMap />
      </VueFlow>
      
      <div v-if="loading" class="loading-overlay">
        <el-icon class="is-loading" size="32"><Loading /></el-icon>
      </div>
    </div>
    
    <el-dialog v-model="agentPickerVisible" title="Add Agent" width="400px">
      <el-input v-model="agentSearch" placeholder="Search agents..." clearable />
      <div class="agent-list">
        <div
          v-for="agent in filteredAgents"
          :key="agent.id"
          class="agent-item"
          :class="{ disabled: isAgentInGraph(agent.id) }"
          @click="addAgentToGraph(agent)"
        >
          <span class="agent-name">{{ agent.name }}</span>
          <el-icon v-if="isAgentInGraph(agent.id)"><Check /></el-icon>
        </div>
      </div>
    </el-dialog>
    
    <el-dialog v-model="edgeDialogVisible" title="Edge Options" width="350px">
      <div v-if="selectedEdge" class="edge-info">
        <p>From: {{ selectedEdge.source }}</p>
        <p>To: {{ selectedEdge.target }}</p>
        <p>Type: {{ selectedEdge.data?.edgeType }}</p>
      </div>
      <template #footer>
        <el-button @click="edgeDialogVisible = false">Close</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.config-graph-tab {
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

.toolbar-left, .toolbar-right {
  display: flex;
  gap: 8px;
}

.link-mode-select {
  width: 160px;
}

.canvas-container {
  flex: 1;
  position: relative;
  background: var(--bg-secondary);
  border-radius: 8px;
  overflow: hidden;
}

.loading-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255,255,255,0.8);
}

.agent-list {
  margin-top: 12px;
  max-height: 300px;
  overflow-y: auto;
}

.agent-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 12px;
  cursor: pointer;
  border-radius: 4px;
}

.agent-item:hover {
  background: var(--bg-secondary);
}

.agent-item.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.edge-info p {
  margin: 4px 0;
}
</style>
