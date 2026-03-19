<template>
  <div class="agent-graph-page">
    <div class="page-header">
      <div class="header-left">
        <h2>Agent Graph</h2>
        <span class="subtitle">Manage OpenClaw agents visually</span>
      </div>
      <div class="header-actions">
        <SyncButton @sync="handleSync" :loading="syncing" />
        <el-button type="primary" @click="showAddDialog = true">
          <el-icon><Plus /></el-icon>
          Add Agent
        </el-button>
      </div>
    </div>

    <div class="graph-container">
      <AgentGraphCanvas
        ref="graphRef"
        v-model="graphData"
        @node-click="handleNodeClick"
        @edge-click="handleEdgeClick"
      />
    </div>

    <AgentDetailPanel
      v-model="showDetailPanel"
      :agent="selectedNode"
      :edges="graphData.edges"
      :all-nodes="graphData.nodes"
      @close="showDetailPanel = false"
      @edit="handleEditAgent"
    />

    <AddAgentDialog v-model="showAddDialog" @created="handleAgentCreated" />

    <EditAgentDialog
      v-model="showEditDialog"
      :agent="editingAgent"
      @updated="handleAgentUpdated"
      @deleted="handleAgentDeleted"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import AgentGraphCanvas from '@/components/AgentGraph/AgentGraphCanvas.vue'
import AgentDetailPanel from '@/components/AgentGraph/AgentDetailPanel.vue'
import AddAgentDialog from '@/components/AgentGraph/AddAgentDialog.vue'
import EditAgentDialog from '@/components/AgentGraph/EditAgentDialog.vue'
import SyncButton from '@/components/AgentGraph/SyncButton.vue'
import { loadGraph, saveGraph, listOpenClawAgents, getMainAgent } from '@/lib/openclaw/agentApi'
import type { AgentNode, AgentEdge, AgentGraph } from '@/types/agentGraph'

const graphRef = ref()
const graphData = reactive<AgentGraph>({
  version: '1.0',
  lastSync: '',
  nodes: [],
  edges: []
})

const showAddDialog = ref(false)
const showEditDialog = ref(false)
const showDetailPanel = ref(false)
const selectedNode = ref<AgentNode>()
const editingAgent = ref<AgentNode>()
const syncing = ref(false)

onMounted(async () => {
  const stored = loadGraph()
  Object.assign(graphData, stored)

  try {
    const [agentsRes, mainRes] = await Promise.all([listOpenClawAgents(), getMainAgent()])

    const agents = agentsRes.data || []
    const mainAgentId = mainRes.data?.mainAgentId || 'main'

    for (const name of agents) {
      const exists = graphData.nodes.find(n => n.id === name)
      if (!exists) {
        graphData.nodes.push({
          id: name,
          name: name,
          workspace: '',
          isMain: name === mainAgentId,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        })
      }
    }

    graphData.lastSync = new Date().toISOString()
    saveGraph(graphData)
  } catch (error) {
    console.error('Failed to load agents from OpenClaw:', error)
  }
})

function handleNodeClick(node: AgentNode) {
  selectedNode.value = node
  showDetailPanel.value = true
}

function handleEdgeClick(edge: AgentEdge) {
  console.log('Edge clicked:', edge)
}

function handleEditAgent(agent: AgentNode) {
  editingAgent.value = agent
  showEditDialog.value = true
  showDetailPanel.value = false
}

function handleAgentCreated(node: AgentNode) {
  graphData.nodes.push(node)
  saveGraph(graphData)
}

function handleAgentUpdated(node: AgentNode) {
  const index = graphData.nodes.findIndex(n => n.id === node.id)
  if (index !== -1) {
    graphData.nodes[index] = node
    saveGraph(graphData)
  }
}

function handleAgentDeleted(nodeId: string) {
  graphData.nodes = graphData.nodes.filter(n => n.id !== nodeId)
  graphData.edges = graphData.edges.filter(e => e.source !== nodeId && e.target !== nodeId)
  saveGraph(graphData)
  showDetailPanel.value = false
}

async function handleSync() {
  syncing.value = true
  try {
    const res = await listOpenClawAgents()
    const agents = res.data || []

    for (const name of agents) {
      const exists = graphData.nodes.find(n => n.id === name)
      if (!exists) {
        graphData.nodes.push({
          id: name,
          name: name,
          workspace: '',
          isMain: false,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        })
      }
    }

    graphData.lastSync = new Date().toISOString()
    saveGraph(graphData)
    ElMessage.success('Sync completed')
  } catch (error) {
    ElMessage.error('Sync failed')
  } finally {
    syncing.value = false
  }
}
</script>

<style scoped>
.agent-graph-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 16px;
  background: #f5f7fa;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.subtitle {
  font-size: 13px;
  color: #909399;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.graph-container {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

@media (max-width: 768px) {
  .agent-graph-page {
    padding: 8px;
  }

  .page-header {
    flex-direction: column;
    gap: 12px;
    padding: 12px 16px;
  }

  .header-left h2 {
    font-size: 18px;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
}

@media (max-width: 480px) {
  .page-header {
    padding: 10px 12px;
  }

  .header-actions {
    flex-wrap: wrap;
  }
}
</style>
