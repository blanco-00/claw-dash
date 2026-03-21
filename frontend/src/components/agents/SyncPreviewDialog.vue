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

watch(() => props.visible, (val) => {
  if (val) {
    activeTab.value = 0
    syncResult.value = null
  }
})

function getAgentChangeSummary(agent: SyncAgentPreview): string {
  const parts: string[] = []
  if (agent.blocksAdded.length > 0) parts.push(`+${agent.blocksAdded.length}`)
  if (agent.blocksModified.length > 0) parts.push(`~${agent.blocksModified.length}`)
  if (agent.blocksRemoved.length > 0) parts.push(`-${agent.blocksRemoved.length}`)
  return parts.join(' ') || '无变化'
}

function getDiffLines(diff: string): Array<{ type: string; content: string }> {
  return diff.split('\n').map(line => ({
    type: line.startsWith('+') ? 'add' : line.startsWith('-') ? 'remove' : line.startsWith('~') ? 'modify' : 'context',
    content: line
  }))
}

async function handleSync() {
  syncing.value = true
  try {
    const result = await configGraphApi.sync(props.graphId)
    syncResult.value = result.data
    ElMessage.success(`同步完成: ${result.data.agentsUpdated.length} 个 Agent 已更新`)
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
    title="Sync 预览"
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

        <el-tabs v-model="activeTab" class="agent-tabs">
          <el-tab-pane 
            v-for="agent in agents" 
            :key="agent.agentId" 
            :label="`${agent.agentId} (${getAgentChangeSummary(agent)})`"
          >
            <div class="agent-preview">
              <div class="changes">
                <el-tag v-if="agent.blocksAdded.length" type="success" size="small">
                  + 新增 {{ agent.blocksAdded.length }}
                </el-tag>
                <el-tag v-if="agent.blocksModified.length" type="warning" size="small">
                  ~ 修改 {{ agent.blocksModified.length }}
                </el-tag>
                <el-tag v-if="agent.blocksRemoved.length" type="danger" size="small">
                  - 删除 {{ agent.blocksRemoved.length }}
                </el-tag>
              </div>

              <div class="diff-container">
                <pre class="diff-content"><code><template v-for="(line, idx) in getDiffLines(agent.diff)" :key="idx"><span 
                  :class="['diff-line', line.type]"
                >{{ line.content }}</span>
</template></code></pre>
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
          执行 Sync
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
}

.summary {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
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
  background: var(--bg-secondary);
  border-radius: 8px;
  overflow: auto;
  max-height: 300px;
}

.diff-content {
  margin: 0;
  padding: 12px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
}

.diff-line {
  display: block;
}

.diff-line.add {
  color: #22c55e;
  background: rgba(34, 197, 94, 0.1);
}

.diff-line.remove {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
}

.diff-line.modify {
  color: #f59e0b;
  background: rgba(245, 158, 11, 0.1);
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
