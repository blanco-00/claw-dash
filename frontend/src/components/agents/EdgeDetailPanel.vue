<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Close, Check, View, Delete } from '@element-plus/icons-vue'
import { configGraphApi } from '@/lib/configGraphApi'
import type { EdgeRoutingType, DecisionMode, SyncPreviewResult } from '@/types/agentGraph'
import { EDGE_TYPE_OPTIONS, EDGE_VARIABLE_HINTS } from '@/types/agentGraph'

const props = defineProps<{
  visible: boolean
  graphId: number
  edge: any
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  'saved': []
  'deleted': []
  'preview': [result: SyncPreviewResult]
}>()

const loading = ref(false)
const saving = ref(false)
const previewing = ref(false)

const edgeTypes = ref<Array<{ value: string; defaultMessageTemplate?: string }>>([])
const edgeRoutingType = ref<EdgeRoutingType>('always')
const decisionMode = ref<DecisionMode>('always')
const messageTemplate = ref('')
const enabled = ref(true)
const isTemplateEdited = ref(false) // Track if user manually edited

async function fetchEdgeTypes() {
  try {
    const res = await configGraphApi.getEdgeTypes()
    edgeTypes.value = res.data || []
  } catch {
    edgeTypes.value = []
  }
}

function getDefaultTemplate(type: string): string {
  const found = edgeTypes.value.find(t => t.value === type)
  return found?.defaultMessageTemplate || ''
}

const drawerVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const variableHints = computed(() => EDGE_VARIABLE_HINTS[edgeRoutingType.value])

const charCount = computed(() => messageTemplate.value.length)
const charLimit = 1000

watch(() => props.visible, async (val) => {
  if (val && props.edge) {
    await fetchEdgeTypes()
    loadEdgeData()
  }
})

watch(() => props.edge, () => {
  if (props.visible && props.edge) {
    loadEdgeData()
  }
})

// Auto-fill default template when edge type changes (only if user hasn't edited)
watch(edgeRoutingType, (newType) => {
  if (!isTemplateEdited.value) {
    messageTemplate.value = getDefaultTemplate(newType)
  }
})

function loadEdgeData() {
  if (!props.edge?.data) return
  
  edgeRoutingType.value = props.edge.data.edgeRoutingType || 'always'
  decisionMode.value = props.edge.data.decisionMode || 'always'
  messageTemplate.value = props.edge.data.messageTemplate || getDefaultTemplate(props.edge.data.edgeRoutingType || 'always')
  enabled.value = props.edge.data.enabled !== false
  isTemplateEdited.value = !!props.edge.data.messageTemplate // If already has template, mark as edited
}

function onTemplateInput() {
  isTemplateEdited.value = true
}

async function handleSave() {
  if (charCount.value > charLimit) {
    ElMessage.warning(`消息模板超过 ${charLimit} 字符限制`)
    return
  }

  const edgeId = parseInt(props.edge?.id?.replace('e-', '') || '0')
  if (!edgeId) {
    ElMessage.error('无效的边 ID')
    return
  }

  saving.value = true
  try {
    await configGraphApi.updateEdge(props.graphId, edgeId, {
      edgeRoutingType: edgeRoutingType.value,
      decisionMode: decisionMode.value,
      messageTemplate: messageTemplate.value,
      enabled: enabled.value
    })
    
    if (props.edge?.data) {
      props.edge.data.edgeRoutingType = edgeRoutingType.value
      props.edge.data.decisionMode = decisionMode.value
      props.edge.data.messageTemplate = messageTemplate.value
      props.edge.data.enabled = enabled.value
    }
    
    ElMessage.success('保存成功')
    emit('saved')
  } catch (err: any) {
    ElMessage.error(err?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function handlePreview() {
  const edgeId = parseInt(props.edge?.id?.replace('e-', '') || '0')
  if (!edgeId) {
    ElMessage.error('无效的边 ID')
    return
  }

  previewing.value = true
  try {
    const result = await configGraphApi.syncPreview(props.graphId, edgeId)
    emit('preview', result.data)
  } catch (err: any) {
    ElMessage.error(err?.message || '获取预览失败')
  } finally {
    previewing.value = false
  }
}

async function handleDelete() {
  const edgeId = parseInt(props.edge?.id?.replace('e-', '') || '0')
  if (!edgeId) return

  try {
    await configGraphApi.removeEdge(props.graphId, edgeId)
    ElMessage.success('边已删除')
    emit('deleted')
    emit('update:visible', false)
  } catch (err: any) {
    ElMessage.error(err?.message || '删除失败')
  }
}

function insertVariable(variable: string) {
  messageTemplate.value += variable
}

function getPlaceholder(type: EdgeRoutingType): string {
  const placeholders: Record<EdgeRoutingType, string> = {
    always: '输入要发送的固定消息...',
    task: '请描述要委托给目标 Agent 的任务，例如: 帮我测试这个功能',
    reply: '输入回复内容，例如: 任务已完成，结果是...',
    error: '输入错误信息，例如: 发生错误: {error_message}'
  }
  return placeholders[type]
}
</script>

<template>
  <el-drawer
    v-model="drawerVisible"
    title="边配置"
    size="400px"
    direction="rtl"
  >
    <div v-if="edge" class="edge-panel">
      <div class="edge-info">
        <div class="info-row">
          <span class="label">源 Agent:</span>
          <span class="value">{{ edge.source }}</span>
        </div>
        <div class="info-row">
          <span class="label">目标 Agent:</span>
          <span class="value">{{ edge.target }}</span>
        </div>
      </div>

      <el-divider />

      <div class="form-section">
        <div class="section-title">边类型</div>
        <el-radio-group v-model="edgeRoutingType" class="radio-group">
          <el-radio-button 
            v-for="opt in EDGE_TYPE_OPTIONS" 
            :key="opt.value" 
            :value="opt.value"
          >
            {{ opt.labelCn }}
          </el-radio-button>
        </el-radio-group>
        <div class="radio-hint">
          {{ EDGE_TYPE_OPTIONS.find(o => o.value === edgeRoutingType)?.description }}
        </div>
      </div>

      <div class="form-section">
        <div class="section-title">决策模式</div>
        <el-radio-group v-model="decisionMode" class="radio-group">
          <el-radio-button value="always">直接发送</el-radio-button>
          <el-radio-button value="llm">AI 判断</el-radio-button>
        </el-radio-group>
        <div class="radio-hint">
          {{ decisionMode === 'always' ? '无条件执行此路由' : '由 AI 判断是否执行此路由' }}
        </div>
      </div>

      <div class="form-section">
        <div class="section-title">消息模板</div>
        <el-input
          v-model="messageTemplate"
          type="textarea"
          :rows="4"
          :maxlength="charLimit"
          show-word-limit
          :placeholder="getPlaceholder(edgeRoutingType)"
          @input="onTemplateInput"
        />
        
        <div v-if="variableHints.length > 0" class="variable-hints">
          <div class="hint-title">可用变量:</div>
          <div class="hint-list">
            <el-tag 
              v-for="v in variableHints" 
              :key="v" 
              size="small" 
              class="var-tag"
              @click="insertVariable(v)"
            >
              {{ v }}
            </el-tag>
          </div>
        </div>
      </div>

      <div class="form-section">
        <el-checkbox v-model="enabled">启用此路由</el-checkbox>
      </div>

      <el-divider />

      <div class="actions">
        <el-button type="primary" :loading="saving" @click="handleSave">
          <el-icon><Check /></el-icon>
          保存
        </el-button>
        <el-button type="info" :loading="previewing" @click="handlePreview">
          <el-icon><View /></el-icon>
          预览
        </el-button>
        <el-button type="danger" plain @click="handleDelete">
          <el-icon><Delete /></el-icon>
          删除
        </el-button>
      </div>
    </div>
  </el-drawer>
</template>

<style scoped>
.edge-panel {
  padding: 0 8px;
}

.edge-info {
  background: var(--bg-secondary);
  border-radius: 8px;
  padding: 12px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
}

.info-row .label {
  color: var(--text-secondary);
  font-size: 13px;
}

.info-row .value {
  font-weight: 500;
  font-size: 13px;
}

.form-section {
  margin-bottom: 20px;
}

.section-title {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 8px;
}

.radio-group {
  width: 100%;
}

.radio-group :deep(.el-radio-button__inner) {
  width: 100%;
}

.radio-hint {
  margin-top: 8px;
  font-size: 12px;
  color: var(--text-secondary);
}

.variable-hints {
  margin-top: 12px;
  padding: 10px;
  background: var(--bg-secondary);
  border-radius: 6px;
}

.hint-title {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.hint-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.var-tag {
  cursor: pointer;
}

.var-tag:hover {
  opacity: 0.8;
}

.actions {
  display: flex;
  gap: 8px;
}

.actions .el-button {
  flex: 1;
  min-width: 0;
  padding: 8px 12px;
  font-size: 13px;
}

.actions .el-button .el-icon {
  margin-right: 4px;
}
</style>
