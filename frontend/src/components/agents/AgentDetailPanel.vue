<template>
  <el-drawer
    v-model="visible"
    :title="agent ? `${agent.name} - Details` : 'Agent Details'"
    direction="rtl"
    size="550px"
    @close="handleClose"
  >
    <div v-if="agent" class="agent-detail-panel">
      <div class="detail-header">
        <div class="agent-avatar">
          <el-icon size="32"><User /></el-icon>
        </div>
        <div class="agent-info">
          <h3>{{ agent.name }}</h3>
          <el-tag :type="getStatusType(agent.status)" size="small">
            {{ agent.status || 'UNKNOWN' }}
          </el-tag>
        </div>
      </div>

      <el-tabs v-model="activeSection" class="detail-tabs">
        <el-tab-pane label="Info" name="info">
          <div class="detail-section">
            <h4>Basic Information</h4>
            <div class="info-grid">
              <div class="info-item">
                <span class="label">Agent ID</span>
                <span class="value mono">{{ agent.agentId }}</span>
              </div>
              <div v-if="agent.role" class="info-item">
                <span class="label">Role</span>
                <span class="value">{{ agent.role }}</span>
              </div>
              <div v-if="agent.description" class="info-item">
                <span class="label">Description</span>
                <span class="value">{{ agent.description }}</span>
              </div>
              <div v-if="agent.workspace" class="info-item">
                <span class="label">Workspace</span>
                <span class="value mono small">{{ agent.workspace }}</span>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <div class="section-header">
              <h4>OpenClaw Bindings</h4>
              <el-button size="small" type="primary" @click="showBindDialog">
                <el-icon><Plus /></el-icon>
                Bind
              </el-button>
            </div>
            
            <div v-if="loadingBindings" class="loading-state">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>Loading bindings...</span>
            </div>
            
            <div v-else-if="bindings.length === 0" class="empty-state">
              <el-icon><Connection /></el-icon>
              <span>No bindings</span>
            </div>
            
            <div v-else class="bindings-list">
              <div v-for="binding in bindings" :key="binding.channel" class="binding-item">
                <div class="binding-info">
                  <el-icon><Connection /></el-icon>
                  <span class="channel-name">{{ binding.channel }}</span>
                </div>
                <el-button 
                  size="small" 
                  type="danger" 
                  text
                  @click="unbindChannel(binding.channel)"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <div class="section-header">
              <h4>Config Graph Connections</h4>
            </div>
            
            <div v-if="loadingConnections" class="loading-state">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>Loading connections...</span>
            </div>
            
            <div v-else class="connections-summary">
              <div class="connection-stat">
                <span class="stat-value">{{ outboundEdges.length }}</span>
                <span class="stat-label">Outbound</span>
              </div>
              <div class="connection-stat">
                <span class="stat-value">{{ inboundEdges.length }}</span>
                <span class="stat-label">Inbound</span>
              </div>
            </div>

            <div v-if="outboundEdges.length > 0" class="connection-group">
              <div class="group-label">Outbound</div>
              <div v-for="edge in outboundEdges" :key="edge.id" class="connection-item">
                <el-icon><Right /></el-icon>
                <span class="target-name">{{ getAgentName(edge.targetId) }}</span>
                <el-tag :type="getEdgeTypeColor(edge.edgeType)" size="small">
                  {{ formatEdgeType(edge.edgeType) }}
                </el-tag>
                <el-tag :type="edge.enabled ? 'success' : 'info'" size="small">
                  {{ edge.enabled ? 'Enabled' : 'Disabled' }}
                </el-tag>
              </div>
            </div>

            <div v-if="inboundEdges.length > 0" class="connection-group">
              <div class="group-label">Inbound</div>
              <div v-for="edge in inboundEdges" :key="edge.id" class="connection-item">
                <el-icon><Back /></el-icon>
                <span class="target-name">{{ getAgentName(edge.sourceId) }}</span>
                <el-tag :type="getEdgeTypeColor(edge.edgeType)" size="small">
                  {{ formatEdgeType(edge.edgeType) }}
                </el-tag>
                <el-tag :type="edge.enabled ? 'success' : 'info'" size="small">
                  {{ edge.enabled ? 'Enabled' : 'Disabled' }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="Files" name="files">
          <div class="files-section">
            <div class="files-header">
              <h4>Agent Files</h4>
              <el-button size="small" @click="loadFiles">
                <el-icon><Refresh /></el-icon>
                Refresh
              </el-button>
            </div>
            
            <el-tabs v-model="activeFile" class="file-tabs">
              <el-tab-pane label="SOUL.md" name="soul">
                <div class="file-content">
                  <div v-if="loadingFiles" class="loading-state">
                    <el-icon class="is-loading"><Loading /></el-icon>
                    <span>Loading...</span>
                  </div>
                  <el-input
                    v-else
                    v-model="files.soul"
                    type="textarea"
                    :rows="12"
                    placeholder="SOUL.md defines the agent's core personality and purpose..."
                    @input="hasChanges.soul = files.soul !== originalContent.soul"
                  />
                </div>
              </el-tab-pane>
              
              <el-tab-pane label="AGENT.md" name="agent">
                <div class="file-content">
                  <div v-if="loadingFiles" class="loading-state">
                    <el-icon class="is-loading"><Loading /></el-icon>
                    <span>Loading...</span>
                  </div>
                  <el-input
                    v-else
                    v-model="files.agent"
                    type="textarea"
                    :rows="12"
                    placeholder="AGENT.md defines the agent's capabilities and behaviors..."
                    @input="hasChanges.agent = files.agent !== originalContent.agent"
                  />
                </div>
              </el-tab-pane>
              
              <el-tab-pane label="USER.md" name="user">
                <div class="file-content">
                  <div v-if="loadingFiles" class="loading-state">
                    <el-icon class="is-loading"><Loading /></el-icon>
                    <span>Loading...</span>
                  </div>
                  <el-input
                    v-else
                    v-model="files.user"
                    type="textarea"
                    :rows="12"
                    placeholder="USER.md defines user preferences and context..."
                    @input="hasChanges.user = files.user !== originalContent.user"
                  />
                </div>
              </el-tab-pane>
            </el-tabs>
            
            <div class="file-footer">
              <span v-if="currentFileHasChanges" class="unsaved-badge">Unsaved changes</span>
              <div class="file-actions">
                <el-button 
                  v-if="currentFileHasChanges" 
                  size="small"
                  @click="discardFileChanges"
                >
                  Discard
                </el-button>
                <el-button 
                  type="primary" 
                  size="small"
                  :loading="savingFile"
                  :disabled="!currentFileHasChanges"
                  @click="saveCurrentFile"
                >
                  Save
                </el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <template #footer>
      <div class="panel-footer">
        <el-button @click="handleClose">Close</el-button>
      </div>
    </template>

    <el-dialog v-model="bindDialogVisible" title="Bind to Channel" width="350px">
      <el-form label-width="100px">
        <el-form-item label="Channel">
          <el-input v-model="newChannel" placeholder="channel:name" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bindDialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="bindChannel" :loading="binding">
          Bind
        </el-button>
      </template>
    </el-dialog>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Plus, Delete, Loading, Connection, Right, Back, Refresh } from '@element-plus/icons-vue'
import { configGraphApi } from '@/lib/configGraphApi'

interface Agent {
  agentId: string
  name: string
  role?: string
  description?: string
  status?: string
  workspace?: string
}

interface ConfigEdge {
  id: number
  sourceId: string
  targetId: string
  edgeType: string
  enabled: boolean
  label?: string
}

interface GraphData {
  graph: { id: number }
  nodes: any[]
  edges: ConfigEdge[]
}

const props = defineProps<{
  modelValue: boolean
  agent: Agent | null
  graphId?: number
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const activeSection = ref('info')
const activeFile = ref('soul')
const loadingBindings = ref(false)
const loadingConnections = ref(false)
const loadingFiles = ref(false)
const savingFile = ref(false)
const bindings = ref<{ channel: string; status: string }[]>([])
const graphData = ref<GraphData | null>(null)
const bindDialogVisible = ref(false)
const newChannel = ref('')
const binding = ref(false)

const files = ref<Record<string, string>>({
  soul: '',
  agent: '',
  user: ''
})
const originalContent = ref<Record<string, string>>({
  soul: '',
  agent: '',
  user: ''
})
const hasChanges = ref<Record<string, boolean>>({
  soul: false,
  agent: false,
  user: false
})

const fileNames: Record<string, string> = {
  soul: 'SOUL.md',
  agent: 'AGENT.md',
  user: 'USER.md'
}

const graphId = computed(() => props.graphId || 1)

const outboundEdges = computed(() => {
  if (!props.agent || !graphData.value) return []
  return graphData.value.edges.filter(e => e.sourceId === props.agent!.agentId)
})

const inboundEdges = computed(() => {
  if (!props.agent || !graphData.value) return []
  return graphData.value.edges.filter(e => e.targetId === props.agent!.agentId)
})

const currentFileHasChanges = computed(() => hasChanges.value[activeFile.value])

async function loadBindings() {
  if (!props.agent) return
  
  loadingBindings.value = true
  try {
    const response = await fetch('/api/openclaw/agents/bindings')
    const result = await response.json()
    if (result.code === 200 && result.data) {
      const allBindings: any[] = result.data.bindings || []
      bindings.value = allBindings
        .filter((b: any) => b.agent === props.agent?.name)
        .flatMap((b: any) => {
          const channels = b.channels ? b.channels.split(',').map((c: string) => c.trim()) : []
          return channels.map((channel: string) => ({ channel, status: b.status || 'active' }))
        })
    }
  } catch (error) {
    console.error('Failed to load bindings:', error)
  } finally {
    loadingBindings.value = false
  }
}

async function loadConnections() {
  if (!props.agent) return
  
  loadingConnections.value = true
  try {
    const data = await configGraphApi.get(graphId.value) as GraphData
    graphData.value = data
  } catch (error) {
    console.error('Failed to load connections:', error)
  } finally {
    loadingConnections.value = false
  }
}

async function loadFiles() {
  if (!props.agent) return
  
  loadingFiles.value = true
  const fileKeys = ['soul', 'agent', 'user']
  
  try {
    for (const key of fileKeys) {
      const response = await fetch(`/api/agents/${props.agent.agentId}/files/${fileNames[key]}`)
      const data = await response.json()
      
      if (data.code === 200 && data.data) {
        files.value[key] = data.data.content || ''
        originalContent.value[key] = data.data.content || ''
      } else {
        files.value[key] = ''
        originalContent.value[key] = ''
      }
      hasChanges.value[key] = false
    }
  } catch (err) {
    console.error('Failed to load agent files:', err)
  } finally {
    loadingFiles.value = false
  }
}

async function saveCurrentFile() {
  if (!props.agent) return
  
  savingFile.value = true
  try {
    const response = await fetch(`/api/agents/${props.agent.agentId}/files/${fileNames[activeFile.value]}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ content: files.value[activeFile.value] })
    })
    
    const data = await response.json()
    if (data.code === 200) {
      originalContent.value[activeFile.value] = files.value[activeFile.value]
      hasChanges.value[activeFile.value] = false
      ElMessage.success('File saved successfully')
    } else {
      ElMessage.error(data.message || 'Failed to save file')
    }
  } catch (err) {
    console.error('Failed to save file:', err)
    ElMessage.error('Failed to save file')
  } finally {
    savingFile.value = false
  }
}

function discardFileChanges() {
  files.value[activeFile.value] = originalContent.value[activeFile.value]
  hasChanges.value[activeFile.value] = false
}

async function bindChannel() {
  if (!props.agent || !newChannel.value.trim()) return
  
  binding.value = true
  try {
    const response = await fetch('/api/openclaw/agents/bind', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name: props.agent.name, channel: newChannel.value.trim() })
    })
    const result = await response.json()
    if (result.code === 200) {
      ElMessage.success(`Bound to ${newChannel.value}`)
      bindDialogVisible.value = false
      newChannel.value = ''
      await loadBindings()
    } else {
      ElMessage.error(result.message || 'Failed to bind')
    }
  } catch (error) {
    console.error('Failed to bind:', error)
    ElMessage.error('Failed to bind channel')
  } finally {
    binding.value = false
  }
}

async function unbindChannel(channel: string) {
  if (!props.agent) return
  
  try {
    const response = await fetch('/api/openclaw/agents/unbind', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name: props.agent.name, channel })
    })
    const result = await response.json()
    if (result.code === 200) {
      ElMessage.success(`Unbound from ${channel}`)
      await loadBindings()
    } else {
      ElMessage.error(result.message || 'Failed to unbind')
    }
  } catch (error) {
    console.error('Failed to unbind:', error)
    ElMessage.error('Failed to unbind channel')
  }
}

function showBindDialog() {
  newChannel.value = 'channel:'
  bindDialogVisible.value = true
}

function getAgentName(agentId: string): string {
  return agentId
}

function getStatusType(status?: string): string {
  const map: Record<string, string> = {
    ACTIVE: 'success',
    INACTIVE: 'info',
    ONLINE: 'success',
    OFFLINE: 'info'
  }
  return map[status || ''] || 'info'
}

function getEdgeTypeColor(edgeType?: string): string {
  const map: Record<string, string> = {
    assigns: 'success',
    reports_to: 'warning',
    communicates: 'primary'
  }
  return map[edgeType || ''] || 'info'
}

function formatEdgeType(edgeType?: string): string {
  const map: Record<string, string> = {
    assigns: 'Assigns',
    reports_to: 'Reports To',
    communicates: 'Communicates'
  }
  return map[edgeType || ''] || 'Unknown'
}

function handleClose() {
  const changedFiles = Object.values(hasChanges.value).some(Boolean)
  if (changedFiles) {
    ElMessage.warning('Please save or discard changes before closing')
    return
  }
  visible.value = false
}

watch(() => props.agent, (newAgent) => {
  if (newAgent && visible.value) {
    loadBindings()
    loadConnections()
  }
}, { immediate: true })

watch(visible, (val) => {
  if (val && props.agent) {
    loadBindings()
    loadConnections()
  }
})

watch(activeFile, () => {
  hasChanges.value[activeFile.value] = files.value[activeFile.value] !== originalContent.value[activeFile.value]
})
</script>

<style scoped>
.agent-detail-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.agent-avatar {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  border-radius: 50%;
  color: white;
}

.agent-info h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: var(--text-primary);
}

.detail-tabs {
  flex: 1;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.section-header h4 {
  margin: 0;
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item .label {
  font-size: 12px;
  color: var(--text-secondary);
}

.info-item .value {
  font-size: 14px;
  color: var(--text-primary);
}

.info-item .value.mono {
  font-family: monospace;
  font-size: 13px;
}

.info-item .value.small {
  font-size: 12px;
  word-break: break-all;
}

.loading-state,
.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px;
  color: var(--text-secondary);
  background: var(--bg-secondary);
  border-radius: var(--radius);
}

.bindings-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.binding-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  background: var(--bg-secondary);
  border-radius: var(--radius);
  border: 1px solid var(--border-color);
}

.binding-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.channel-name {
  font-family: monospace;
  font-size: 13px;
}

.connections-summary {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.connection-stat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px;
  background: var(--bg-secondary);
  border-radius: var(--radius);
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--primary);
}

.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
  text-transform: uppercase;
}

.connection-group {
  margin-bottom: 12px;
}

.group-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 8px;
  text-transform: uppercase;
}

.connection-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: var(--bg-secondary);
  border-radius: var(--radius-sm);
  margin-bottom: 6px;
  font-size: 13px;
}

.target-name {
  flex: 1;
  font-weight: 500;
}

.panel-footer {
  display: flex;
  justify-content: flex-end;
}

.files-section {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.files-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.files-header h4 {
  margin: 0;
}

.file-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.file-tabs :deep(.el-tabs__content) {
  flex: 1;
  overflow: auto;
}

.file-content {
  height: 100%;
}

.file-content :deep(.el-textarea__inner) {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 1.6;
  resize: none;
}

.file-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
  margin-top: 16px;
}

.unsaved-badge {
  font-size: 12px;
  color: var(--el-color-warning);
  font-weight: 500;
}

.file-actions {
  display: flex;
  gap: 8px;
}
</style>
