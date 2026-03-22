<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Check, Close } from '@element-plus/icons-vue'
import { configGraphApi } from '@/lib/configGraphApi'
import type { SyncPreviewResult, SyncResult, SyncAgentPreview } from '@/types/agentGraph'

const props = defineProps<{
  visible: boolean
  graphId: number
  previewData?: SyncPreviewResult
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  'synced': [result: SyncResult]
}>()

const loading = ref(false)
const syncing = ref(false)
const activeTab = ref(0)
const syncResult = ref<SyncResult | null>(null)
const a2aPreview = ref<{ enabled: boolean; allow: string[] } | null>(null)

async function fetchA2APreview() {
  try {
    const res = await configGraphApi.getA2AConfig()
    a2aPreview.value = { enabled: res.data.enabled, allow: res.data.allow }
  } catch {
    a2aPreview.value = null
  }
}

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const agents = computed(() => props.previewData?.agents || [])

const hasChanges = computed(() => {
  return agents.value.some(a => 
    a.blocksAdded.length > 0 || 
    a.blocksModified.length > 0 || 
    a.blocksRemoved.length > 0
  )
})

watch(() => props.visible, async (val) => {
  if (val) {
    activeTab.value = 0
    syncResult.value = null
    await fetchA2APreview()
  }
})

interface BlockDiff {
  blockId: string
  type: 'added' | 'modified' | 'removed'
  oldContent: string[]
  newContent: string[]
}

function getAgentChangeSummary(agent: SyncAgentPreview): string {
  const parts: string[] = []
  if (agent.blocksAdded.length > 0) parts.push(`+${agent.blocksAdded.length}`)
  if (agent.blocksModified.length > 0) parts.push(`~${agent.blocksModified.length}`)
  if (agent.blocksRemoved.length > 0) parts.push(`-${agent.blocksRemoved.length}`)
  return parts.join(' ') || '无变化'
}

function parseBlockDiffs(diff: string | undefined): BlockDiff[] {
  if (!diff) return []
  const blocks: BlockDiff[] = []
  const lines = diff.split('\n')
  let currentBlock: BlockDiff | null = null
  let section: 'old' | 'new' | 'meta' = 'meta'

  for (const line of lines) {
    if (line.startsWith('--- Old:')) {
      const blockId = line.replace('--- Old:', '').trim()
      currentBlock = { blockId, type: 'modified', oldContent: [], newContent: [] }
      section = 'old'
    } else if (line.startsWith('+++ New:')) {
      section = 'new'
    } else if (line.startsWith('+ ') && !line.startsWith('+++')) {
      blocks.push({ blockId: line.slice(2), type: 'added', oldContent: [], newContent: [] })
    } else if (line.startsWith('- ') && !line.startsWith('---')) {
      blocks.push({ blockId: line.slice(2), type: 'removed', oldContent: [], newContent: [] })
    } else if (currentBlock) {
      if (section === 'old') {
        currentBlock.oldContent.push(line)
      } else if (section === 'new') {
        currentBlock.newContent.push(line)
      }
    }
  }

  if (currentBlock) blocks.push(currentBlock)
  return blocks
}

async function handleSync() {
  syncing.value = true
  try {
    const result = await configGraphApi.sync(props.graphId)
    syncResult.value = result.data
    
    await configGraphApi.syncA2AConfig(props.graphId)
    
    ElMessage.success(`同步完成: ${result.data.agentsUpdated.length} 个 Agent 已更新，A2A 配置已同步`)
    emit('synced', result.data)
  } catch (err: any) {
    ElMessage.error(err?.message || '同步失败')
  } finally {
    syncing.value = false
  }
}

function closeDialog() {
  dialogVisible.value = false
}
</script>

<template>
  <el-dialog
    v-model="dialogVisible"
    title="同步到 OpenClaw 预览"
    width="700px"
    :close-on-click-modal="false"
  >
    <div class="sync-preview">
      <div v-if="loading" class="loading">
        <el-icon class="is-loading"><Refresh /></el-icon>
        <span>加载中...</span>
      </div>

      <template v-else-if="previewData">
        <div class="summary">
          <el-tag type="info">共 {{ previewData.totalEdgesSynced }} 条边</el-tag>
          <el-tag v-if="hasChanges" type="warning">有变更</el-tag>
          <el-tag v-else type="success">无变更</el-tag>
        </div>

        <div v-if="a2aPreview" class="a2a-preview">
          <div class="section-label">A2A 配置同步:</div>
          <div class="a2a-info">
            <el-tag :type="a2aPreview.enabled ? 'success' : 'danger'" size="small">
              {{ a2aPreview.enabled ? '已启用' : '未启用' }}
            </el-tag>
            <span class="a2a-agents">
              允许通信的 Agent:
              <el-tag 
                v-for="agent in a2aPreview.allow" 
                :key="agent" 
                size="small" 
                type="info"
                class="agent-tag"
              >
                {{ agent }}
              </el-tag>
              <span v-if="a2aPreview.allow.length === 0" class="no-agents">无</span>
            </span>
          </div>
        </div>

        <el-tabs v-model="activeTab" class="agent-tabs">
          <el-tab-pane 
            v-for="(agent, idx) in agents" 
            :key="agent.agentId" 
            :name="idx"
            :label="`${agent.agentId} (${getAgentChangeSummary(agent)})`"
          >
            <div class="agent-preview">
              <div class="changes">
                <el-tag v-if="agent.blocksAdded.length" type="success" size="small">
                  + 新增 {{ agent.blocksAdded.length }} 个块
                </el-tag>
                <el-tag v-if="agent.blocksModified.length" type="warning" size="small">
                  ~ 修改 {{ agent.blocksModified.length }} 个块
                </el-tag>
                <el-tag v-if="agent.blocksRemoved.length" type="danger" size="small">
                  - 删除 {{ agent.blocksRemoved.length }} 个块
                </el-tag>
              </div>

              <div class="diff-container">
                <template v-for="block in parseBlockDiffs(agent.newContent)" :key="block.blockId">
                  <!-- Added block -->
                  <div v-if="block.type === 'added'" class="diff-block diff-block--added">
                    <div class="diff-block-header">
                      <el-tag type="success" size="small">+ 新增</el-tag>
                      <span class="block-id">{{ block.blockId }}</span>
                    </div>
                    <div class="diff-side diff-side--new">
                      <div class="diff-side-header">New</div>
                      <pre class="diff-code">{{ block.newContent.join('\n') || '(empty)' }}</pre>
                    </div>
                  </div>

                  <!-- Removed block -->
                  <div v-else-if="block.type === 'removed'" class="diff-block diff-block--removed">
                    <div class="diff-block-header">
                      <el-tag type="danger" size="small">- 删除</el-tag>
                      <span class="block-id">{{ block.blockId }}</span>
                    </div>
                    <div class="diff-side diff-side--old">
                      <div class="diff-side-header">Old</div>
                      <pre class="diff-code">{{ block.oldContent.join('\n') || '(empty)' }}</pre>
                    </div>
                  </div>

                  <!-- Modified block -->
                  <div v-else-if="block.type === 'modified'" class="diff-block diff-block--modified">
                    <div class="diff-block-header">
                      <el-tag type="warning" size="small">~ 修改</el-tag>
                      <span class="block-id">{{ block.blockId }}</span>
                    </div>
                    <div class="diff-sides">
                      <div class="diff-side diff-side--old">
                        <div class="diff-side-header">Old</div>
                        <pre class="diff-code">{{ block.oldContent.join('\n') || '(empty)' }}</pre>
                      </div>
                      <div class="diff-side diff-side--new">
                        <div class="diff-side-header">New</div>
                        <pre class="diff-code">{{ block.newContent.join('\n') || '(empty)' }}</pre>
                      </div>
                    </div>
                  </div>
                </template>

                <div v-if="!parseBlockDiffs(agent.newContent).length" class="diff-empty">
                  无内容变更
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>

        <div v-if="syncResult" class="sync-result">
          <el-alert 
            :title="`同步完成`" 
            type="success"
            :closable="false"
          >
            <template #default>
              <div class="result-stats">
                <span>更新 Agent: {{ syncResult.agentsUpdated.join(', ') }}</span>
                <span>同步边: {{ syncResult.edgesSynced }}</span>
                <span>新增块: {{ syncResult.blocksAdded }}</span>
                <span>更新块: {{ syncResult.blocksUpdated }}</span>
                <span>删除块: {{ syncResult.blocksRemoved }}</span>
              </div>
            </template>
          </el-alert>
        </div>
      </template>

      <div v-else class="empty">
        <el-empty description="无预览数据" />
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="closeDialog">关闭</el-button>
        <el-button 
          type="primary" 
          :loading="syncing" 
          :disabled="!hasChanges || syncResult !== null"
          @click="handleSync"
        >
          <el-icon><Check /></el-icon>
          确认同步
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.sync-preview {
  min-height: 300px;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px;
  color: var(--text-secondary);
  min-height: 200px;
}

.summary {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.a2a-preview {
  background: var(--bg-secondary);
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 16px;
}

.a2a-preview .section-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.a2a-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.a2a-agents {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  font-size: 13px;
}

.agent-tag {
  margin-right: 0;
}

.no-agents {
  color: var(--text-secondary);
  font-style: italic;
}

.agent-tabs {
  margin-bottom: 16px;
}

.agent-preview {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.changes {
  display: flex;
  gap: 8px;
}

.diff-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
}

.diff-block {
  border: 1px solid var(--border-color);
  border-radius: 8px;
  overflow: hidden;
}

.diff-block--added {
  border-color: #22c55e;
}

.diff-block--removed {
  border-color: #ef4444;
}

.diff-block--modified {
  border-color: #f59e0b;
}

.diff-block-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
}

.block-id {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  color: var(--text-secondary);
}

.diff-sides {
  display: grid;
  grid-template-columns: 1fr 1fr;
}

.diff-side {
  padding: 0;
  overflow: hidden;
}

.diff-side + .diff-side {
  border-left: 1px solid var(--border-color);
}

.diff-side--old {
  background: rgba(239, 68, 68, 0.03);
}

.diff-side--new {
  background: rgba(34, 197, 94, 0.03);
}

.diff-side-header {
  padding: 6px 12px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border-bottom: 1px solid var(--border-color);
}

.diff-side--old .diff-side-header {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.05);
}

.diff-side--new .diff-side-header {
  color: #22c55e;
  background: rgba(34, 197, 94, 0.05);
}

.diff-code {
  margin: 0;
  padding: 10px 12px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
  color: var(--text-primary);
  max-height: 200px;
  overflow-y: auto;
}

.diff-empty {
  padding: 24px;
  text-align: center;
  color: var(--text-secondary);
  font-size: 13px;
}

.sync-result {
  margin-top: 16px;
}

.result-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12px;
}

.empty {
  padding: 40px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
