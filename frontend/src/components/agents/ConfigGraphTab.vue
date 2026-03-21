<script setup lang="ts">
import { ref, computed, onMounted, markRaw } from 'vue'
import { VueFlow, useVueFlow } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import { MiniMap } from '@vue-flow/minimap'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import '@vue-flow/controls/dist/style.css'
import '@vue-flow/minimap/dist/style.css'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Link, Delete, Aim, Connection, Loading, Check, Warning } from '@element-plus/icons-vue'
import { configGraphApi } from '@/lib/configGraphApi'
import { openclawAgentApi } from '@/lib/openclawAgentApi'
import { getAllAgentDetails } from '@/api/agents'
import AgentNode from './AgentNode.vue'
import AgentDetailPanel from './AgentDetailPanel.vue'
import type { AgentInfo } from '@/types/agent'
import type { ConfigNode, ConfigEdge, EdgeType } from '@/types/agentGraph'

const { fitView } = useVueFlow()

// Custom node types
const nodeTypes = {
  agent: markRaw(AgentNode)
}

const loading = ref(true)
const graphId = ref(1)
const nodes = ref<any[]>([])
const edges = ref<any[]>([])
const agents = ref<AgentInfo[]>([])
const agentPickerVisible = ref(false)
const agentSearch = ref('')
const newAgentName = ref('')
const creatingAgent = ref(false)
const linkMode = ref<EdgeType>('assigns')
const isConnecting = ref(false)
const connectSource = ref<string | null>(null)
const selectedEdge = ref<any>(null)
const edgeDialogVisible = ref(false)
const selectedAgentName = ref<string | null>(null)
const detailPanelVisible = ref(false)

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
    
    // OpenClaw is the source of truth - all agents become nodes
    nodes.value = agents.value.map((a, i) => ({
      id: a.id,
      type: 'agent',
      position: { x: 200 + (i % 4) * 200, y: 150 + Math.floor(i / 4) * 180 },
      data: { 
        label: a.name || a.id,
        orphaned: false
      }
    }))
    
    // Edges come from database (stored A2A relationships)
    if (graphData?.data) {
      edges.value = graphData.data.edges.map((e: ConfigEdge) => ({
        id: `e-${e.id}`,
        source: e.sourceId,
        target: e.targetId,
        type: 'smoothstep',
        animated: e.edgeType === 'communicates',
        style: { stroke: edgeColors[e.edgeType] || '#6b7280' },
        data: { edgeType: e.edgeType, label: e.label, enabled: e.enabled }
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

async function createAndAddAgent() {
  const name = newAgentName.value.trim()
  if (!name) {
    ElMessage.warning('请输入 agent 名称')
    return
  }
  
  creatingAgent.value = true
  try {
    const workspace = `workspace-${name}`
    await openclawAgentApi.add({ name, workspace })
    
    ElMessage.success(`Agent "${name}" 创建成功`)
    newAgentName.value = ''
    
    await loadData()
    
    const newAgent = agents.value.find(a => a.name === name || a.id === name)
    if (newAgent) {
      await addAgentToGraph(newAgent)
    }
    
    agentPickerVisible.value = false
  } catch (err: any) {
    ElMessage.error(err?.message || '创建 agent 失败')
  } finally {
    creatingAgent.value = false
  }
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
      type: 'default',
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
  } else {
    // Open agent detail panel
    selectedAgentName.value = event.node.id
    detailPanelVisible.value = true
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
  
  if (selectedNodes.length === 0 && selectedEdges.length === 0) {
    ElMessage.warning('No items selected')
    return
  }
  
  let deleteFromOpenClaw = false
  
  if (selectedNodes.length > 0) {
    const nodeNames = selectedNodes.map(n => n.id).join(', ')
    const isMainAgent = selectedNodes.some(n => n.id === 'main')
    
    let warningMsg = `确定要删除以下 Agent 吗？\n\n${nodeNames}\n\n删除后将无法恢复！`
    
    if (isMainAgent) {
      warningMsg = `⚠️ 警告：main 是系统主 Agent，不能被删除！\n\n其他 Agent 将被删除：\n\n${nodeNames.replace('main, ', '').replace(', main', '')}\n\n确定继续吗？`
    }
    
    try {
      const action = await ElMessageBox.confirm(
        warningMsg,
        '⚠️ 确认删除 Agent',
        {
          confirmButtonText: isMainAgent ? '取消' : '确认删除',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      deleteFromOpenClaw = action === 'confirm' && !isMainAgent
    } catch (e) {
      ElMessage.info('已取消删除')
      return
    }
  }
  
  for (const edge of selectedEdges) {
    const edgeId = edge.id.replace('e-', '')
    try {
      await configGraphApi.removeEdge(graphId.value, parseInt(edgeId))
    } catch (err) {
      console.error('Failed to delete edge:', err)
    }
  }
  
  for (const node of selectedNodes) {
    if (node.id === 'main') {
      ElMessage.warning('main Agent 不能被删除')
      continue
    }
    try {
      if (deleteFromOpenClaw) {
        await openclawAgentApi.delete(node.id)
        ElMessage.success(`Agent "${node.id}" 已从 OpenClaw 删除`)
      }
      await configGraphApi.removeNode(graphId.value, node.id)
    } catch (err) {
      console.error('Failed to delete node:', err)
      ElMessage.error(`删除 Agent "${node.id}" 失败`)
    }
  }
  
  edges.value = edges.value.filter(e => !e.selected)
  nodes.value = nodes.value.filter(n => !n.selected)
  
  if (selectedNodes.length > 0 && !deleteFromOpenClaw) {
    ElMessage.success('已从配置图中移除')
  }
}

async function handleAgentDelete(agentName: string) {
  try {
    await openclawAgentApi.delete(agentName)
    await configGraphApi.removeNode(graphId.value, agentName)
    nodes.value = nodes.value.filter(n => n.id !== agentName)
    ElMessage.success(`Agent "${agentName}" 已删除`)
  } catch (err) {
    console.error('Failed to delete agent:', err)
    ElMessage.error(`删除 Agent "${agentName}" 失败`)
  }
}

function autoLayout() {
  const centerX = 400
  const centerY = 300
  const levelRadius = 180
  
  const levels = new Map<string, number>()
  const visited = new Set<string>()
  
  if (!nodes.value.find(n => n.id === 'main')) {
    setTimeout(() => fitView({ padding: 0.2 }), 50)
    return
  }
  
  levels.set('main', 0)
  visited.add('main')
  
  const queue: Array<{ id: string, level: number }> = [{ id: 'main', level: 0 }]
  
  while (queue.length > 0) {
    const current = queue.shift()!
    
    edges.value.forEach(edge => {
      if (edge.source === current.id && !visited.has(edge.target)) {
        visited.add(edge.target)
        levels.set(edge.target, current.level + 1)
        queue.push({ id: edge.target, level: current.level + 1 })
      } else if (edge.target === current.id && !visited.has(edge.source)) {
        visited.add(edge.source)
        levels.set(edge.source, current.level + 1)
        queue.push({ id: edge.source, level: current.level + 1 })
      }
    })
  }
  
  const levelGroups = new Map<number, any[]>()
  
  nodes.value.forEach(node => {
    if (!levels.has(node.id)) {
      levels.set(node.id, 999)
    }
    const lvl = levels.get(node.id)!
    if (!levelGroups.has(lvl)) {
      levelGroups.set(lvl, [])
    }
    levelGroups.get(lvl)!.push(node)
  })
  
  const newPositions = new Map<string, { x: number, y: number }>()
  
  levelGroups.forEach((groupNodes, level) => {
    const radius = levelRadius * (level + 1)
    groupNodes.forEach((node, i) => {
      const angle = (2 * Math.PI * i) / groupNodes.length - Math.PI / 2
      newPositions.set(node.id, {
        x: centerX + radius * Math.cos(angle),
        y: centerY + radius * Math.sin(angle)
      })
    })
  })
  
  nodes.value = nodes.value.map(node => ({
    ...node,
    position: newPositions.get(node.id) || node.position
  }))
  
  setTimeout(() => fitView({ padding: 0.2 }), 50)
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
        <el-button @click="autoLayout">
          <el-icon><Aim /></el-icon>
          Layout
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
        :node-types="nodeTypes"
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
      <div class="create-agent-form">
        <el-input 
          v-model="newAgentName" 
          placeholder="输入新 agent 名称..." 
          clearable
          @keyup.enter="createAndAddAgent"
        />
        <el-button 
          type="primary" 
          :loading="creatingAgent"
          @click="createAndAddAgent"
          :disabled="!newAgentName.trim()"
        >
          创建并添加
        </el-button>
      </div>
      <el-divider content-position="center">或选择已有 Agent</el-divider>
      <el-input v-model="agentSearch" placeholder="搜索已有 agents..." clearable />
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

    <AgentDetailPanel
      v-model:visible="detailPanelVisible"
      :agent-name="selectedAgentName"
      @delete="handleAgentDelete"
    />
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

.create-agent-form {
  display: flex;
  gap: 8px;
}

.create-agent-form .el-input {
  flex: 1;
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
