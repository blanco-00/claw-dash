<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Check, Delete, Link } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { configGraphApi } from '@/lib/configGraphApi'
import type { AgentInfo } from '@/types/agent'

const { t } = useI18n()

const props = defineProps<{
  visible: boolean
  graphId: number
  edge: any
  agents: AgentInfo[]
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  'saved': []
  'deleted': []
}>()

const saving = ref(false)

// Task (source config)
const messageTemplate = ref('')
const enabled = ref(true)
const isTemplateEdited = ref(false)

// Reply config (target config)
const replyEnabled = ref(false)
const replyTarget = ref('')
const replyTemplate = ref('')

// Error config (target config)
const errorEnabled = ref(false)
const errorTarget = ref('')
const errorTemplate = ref('')

const charLimit = 2000

const drawerVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const charCount = computed(() => messageTemplate.value.length)
const replyCharCount = computed(() => replyTemplate.value.length)
const errorCharCount = computed(() => errorTemplate.value.length)

const variableHints = ['{original_message}', '{task_result}']
const errorVariableHints = ['{error_message}', '{original_message}']

watch(() => props.visible, (val) => {
  if (val && props.edge) {
    loadEdgeData()
  }
})

watch(() => props.edge, () => {
  if (props.visible && props.edge) {
    loadEdgeData()
  }
})

function loadEdgeData() {
  if (!props.edge?.data) return

  // Task config (source side)
  messageTemplate.value = props.edge.data.messageTemplate || ''
  enabled.value = props.edge.data.enabled !== false
  isTemplateEdited.value = !!props.edge.data.messageTemplate

  // Reply config (target side)
  replyEnabled.value = !!props.edge.data.replyTarget
  replyTarget.value = props.edge.data.replyTarget || ''
  replyTemplate.value = props.edge.data.replyTemplate || ''

  // Error config (target side)
  errorEnabled.value = !!props.edge.data.errorTarget
  errorTarget.value = props.edge.data.errorTarget || ''
  errorTemplate.value = props.edge.data.errorTemplate || ''
}

function onTemplateInput() {
  isTemplateEdited.value = true
}

async function handleSave() {
  if (messageTemplate.value.length > charLimit) {
    ElMessage.warning(t('agents.edge.taskTemplateLimit', { limit: charLimit }))
    return
  }
  if (replyEnabled.value && replyTemplate.value.length > charLimit) {
    ElMessage.warning(t('agents.edge.replyTemplateLimit', { limit: charLimit }))
    return
  }
  if (errorEnabled.value && errorTemplate.value.length > charLimit) {
    ElMessage.warning(t('agents.edge.errorTemplateLimit', { limit: charLimit }))
    return
  }

  const edgeId = parseInt(props.edge?.id?.replace('e-', '') || '0')
  if (!edgeId) {
    ElMessage.error(t('agents.edge.invalidEdgeId'))
    return
  }

  saving.value = true
  try {
    await configGraphApi.updateEdge(props.graphId, edgeId, {
      edgeRoutingType: 'task',
      decisionMode: 'llm',
      messageTemplate: messageTemplate.value,
      enabled: enabled.value,
      replyTarget: replyEnabled.value ? replyTarget.value : null,
      replyTemplate: replyEnabled.value ? replyTemplate.value : null,
      errorTarget: errorEnabled.value ? errorTarget.value : null,
      errorTemplate: errorEnabled.value ? errorTemplate.value : null
    })

    if (props.edge?.data) {
      props.edge.data.edgeRoutingType = 'task'
      props.edge.data.decisionMode = 'llm'
      props.edge.data.messageTemplate = messageTemplate.value
      props.edge.data.enabled = enabled.value
      props.edge.data.replyTarget = replyEnabled.value ? replyTarget.value : null
      props.edge.data.replyTemplate = replyEnabled.value ? replyTemplate.value : null
      props.edge.data.errorTarget = errorEnabled.value ? errorTarget.value : null
      props.edge.data.errorTemplate = errorEnabled.value ? errorTemplate.value : null
    }

    ElMessage.success(t('agentsConfig.message.saveSuccess'))
    emit('saved')
  } catch (err: any) {
    ElMessage.error(err?.message || t('agentsConfig.message.saveFailed'))
  } finally {
    saving.value = false
  }
}

async function handleDelete() {
  const edgeId = parseInt(props.edge?.id?.replace('e-', '') || '0')
  if (!edgeId) return

  try {
    await configGraphApi.removeEdge(props.graphId, edgeId)
    ElMessage.success(t('agents.edge.edgeDeleted'))
    emit('deleted')
    emit('update:visible', false)
  } catch (err: any) {
    ElMessage.error(err?.message || t('common.error'))
  }
}

function insertVariable(variable: string) {
  messageTemplate.value += variable
}

function insertReplyVariable(variable: string) {
  replyTemplate.value += variable
}

function insertErrorVariable(variable: string) {
  errorTemplate.value += variable
}
</script>

<template>
  <el-drawer
    v-model="drawerVisible"
    :title="t('agents.edge.title')"
    size="700px"
    direction="rtl"
  >
    <div v-if="edge" class="edge-panel">
      <!-- Edge Info -->
      <div class="edge-info">
        <div class="info-row">
          <span class="label">{{ t('agents.edge.sourceAgent') }}:</span>
          <span class="value">{{ edge.source }}</span>
        </div>
        <div class="info-row">
          <span class="label">{{ t('agents.edge.targetAgent') }}:</span>
          <span class="value">{{ edge.target }}</span>
        </div>
      </div>

      <el-divider />

      <!-- Two Column Layout -->
      <div class="config-grid">
        <!-- Left Column: Source Config (Task) -->
        <div class="config-column">
          <div class="column-header">
            <el-icon><Link /></el-icon>
            {{ t('agents.edge.sourceConfig') }} ({{ edge.source }})
          </div>

          <div class="form-section">
            <div class="section-title">{{ t('agents.edge.taskMessageTemplate') }}</div>
            <div class="section-hint">{{ t('agents.edge.taskMessageHint', { target: edge.target }) }}</div>
            <el-input
              v-model="messageTemplate"
              type="textarea"
              :rows="5"
              :maxlength="charLimit"
              show-word-limit
              :placeholder="t('agents.edge.taskMessagePlaceholder')"
              @input="onTemplateInput"
            />

            <div class="variable-hints">
              <div class="hint-title">{{ t('agents.edge.availableVariables') }}:</div>
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
            <el-checkbox v-model="enabled">{{ t('agents.edge.enableRoute') }}</el-checkbox>
          </div>
        </div>

        <!-- Right Column: Target Config (Reply/Error) -->
        <div class="config-column">
          <div class="column-header">
            <el-icon><Link /></el-icon>
            {{ t('agents.edge.targetConfig') }} ({{ edge.target }})
          </div>

          <!-- Reply Config -->
          <div class="target-section">
            <div class="target-section-header">
              <el-checkbox v-model="replyEnabled">{{ t('agents.edge.enableReply') }}</el-checkbox>
            </div>
            <div v-if="replyEnabled" class="target-section-body">
              <div class="form-section">
                <div class="section-title">{{ t('agents.edge.replyToAgent') }}</div>
                <el-select
                  v-model="replyTarget"
                  :placeholder="t('agents.edge.selectReplyTarget')"
                  filterable
                  clearable
                  style="width: 100%"
                >
                  <el-option
                    v-for="agent in agents"
                    :key="agent.id"
                    :label="agent.name || agent.id"
                    :value="agent.id"
                  />
                </el-select>
              </div>

              <div class="form-section">
                <div class="section-title">{{ t('agents.edge.replyTemplate') }}</div>
                <el-input
                  v-model="replyTemplate"
                  type="textarea"
                  :rows="3"
                  :maxlength="charLimit"
                  show-word-limit
                  :placeholder="t('agents.edge.replyTemplatePlaceholder')"
                />

                <div class="variable-hints">
                  <div class="hint-title">{{ t('agents.edge.availableVariables') }}:</div>
                  <div class="hint-list">
                    <el-tag
                      v-for="v in variableHints"
                      :key="v"
                      size="small"
                      class="var-tag"
                      @click="insertReplyVariable(v)"
                    >
                      {{ v }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Error Config -->
          <div class="target-section">
            <div class="target-section-header">
              <el-checkbox v-model="errorEnabled">{{ t('agents.edge.enableErrorHandling') }}</el-checkbox>
            </div>
            <div v-if="errorEnabled" class="target-section-body">
              <div class="form-section">
                <div class="section-title">{{ t('agents.edge.errorReportToAgent') }}</div>
                <el-select
                  v-model="errorTarget"
                  :placeholder="t('agents.edge.selectErrorTarget')"
                  filterable
                  clearable
                  style="width: 100%"
                >
                  <el-option
                    v-for="agent in agents"
                    :key="agent.id"
                    :label="agent.name || agent.id"
                    :value="agent.id"
                  />
                </el-select>
              </div>

              <div class="form-section">
                <div class="section-title">{{ t('agents.edge.errorTemplate') }}</div>
                <el-input
                  v-model="errorTemplate"
                  type="textarea"
                  :rows="3"
                  :maxlength="charLimit"
                  show-word-limit
                  :placeholder="t('agents.edge.errorTemplatePlaceholder')"
                />

                <div class="variable-hints">
                  <div class="hint-title">{{ t('agents.edge.availableVariables') }}:</div>
                  <div class="hint-list">
                    <el-tag
                      v-for="v in errorVariableHints"
                      :key="v"
                      size="small"
                      class="var-tag"
                      @click="insertErrorVariable(v)"
                    >
                      {{ v }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <el-divider />

      <div class="actions">
        <el-button type="primary" :loading="saving" @click="handleSave">
          <el-icon><Check /></el-icon>
          {{ t('common.save') }}
        </el-button>
        <el-button type="danger" plain @click="handleDelete">
          <el-icon><Delete /></el-icon>
          {{ t('common.delete') }}
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

.config-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.config-column {
  background: var(--bg-secondary);
  border-radius: 8px;
  padding: 12px;
}

.column-header {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.column-header .el-icon {
  font-size: 16px;
}

.form-section {
  margin-bottom: 16px;
}

.section-title {
  font-weight: 600;
  font-size: 13px;
  margin-bottom: 6px;
}

.section-hint {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.variable-hints {
  margin-top: 10px;
  padding: 8px;
  background: var(--bg-primary);
  border-radius: 6px;
}

.hint-title {
  font-size: 11px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.hint-list {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.var-tag {
  cursor: pointer;
}

.var-tag:hover {
  opacity: 0.8;
}

.target-section {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-color);
}

.target-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.target-section-header {
  margin-bottom: 8px;
}

.target-section-body {
  padding-left: 4px;
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
