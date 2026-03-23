<template>
  <el-dialog
    v-model="visible"
    :title="isReadOnly ? t('agentGraph.editAgent.titleReadOnly') : t('agentGraph.editAgent.title')"
    width="520px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div v-if="agent" class="agent-detail">
      <div class="readonly-banner" v-if="isReadOnly">
        <el-icon><Warning /></el-icon>
        <span>{{ t('agentGraph.editAgent.readOnlyBanner') }}</span>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        :disabled="isReadOnly"
      >
        <el-form-item :label="t('agentGraph.editAgent.agentId')">
          <el-input :model-value="agent.id" disabled />
        </el-form-item>
        <el-form-item :label="t('agentGraph.editAgent.name')" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="t('agentGraph.editAgent.workspace')">
          <el-input :model-value="agent.workspace" disabled />
        </el-form-item>
        <el-form-item :label="t('agentGraph.editAgent.description')" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item :label="t('agentGraph.editAgent.model')" prop="model">
          <el-select v-model="form.model" style="width: 100%">
            <el-option :label="t('agentGraph.models.gpt4')" value="gpt-4" />
            <el-option :label="t('agentGraph.models.gpt4Turbo')" value="gpt-4-turbo" />
            <el-option :label="t('agentGraph.models.gpt35Turbo')" value="gpt-3.5-turbo" />
            <el-option :label="t('agentGraph.models.claude3Opus')" value="claude-3-opus" />
            <el-option :label="t('agentGraph.models.claude3Sonnet')" value="claude-3-sonnet" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('agentGraph.editAgent.tags')" prop="tags">
          <el-select v-model="form.tags" multiple style="width: 100%">
            <el-option :label="t('agentGraph.tagOptions.research')" value="research" />
            <el-option :label="t('agentGraph.tagOptions.development')" value="development" />
            <el-option :label="t('agentGraph.tagOptions.analysis')" value="analysis" />
            <el-option :label="t('agentGraph.tagOptions.support')" value="support" />
            <el-option :label="t('agentGraph.tagOptions.automation')" value="automation" />
          </el-select>
        </el-form-item>
      </el-form>

      <div class="agent-meta">
        <div class="meta-item">
          <span class="label">{{ t('agentGraph.editAgent.created') }}:</span>
          <span class="value">{{ formatDate(agent.createdAt) }}</span>
        </div>
        <div class="meta-item">
          <span class="label">{{ t('agentGraph.editAgent.updated') }}:</span>
          <span class="value">{{ formatDate(agent.updatedAt) }}</span>
        </div>
      </div>
    </div>

    <template #footer>
      <div v-if="!isReadOnly">
        <el-button type="danger" @click="handleDelete">{{ t('agentGraph.editAgent.delete') }}</el-button>
        <el-button @click="handleClose">{{ t('agentGraph.editAgent.cancel') }}</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">{{ t('agentGraph.editAgent.save') }}</el-button>
      </div>
      <div v-else>
        <el-button @click="handleClose">{{ t('agentGraph.editAgent.close') }}</el-button>
      </div>
    </template>
  </el-dialog>

  <el-dialog v-model="deleteConfirmVisible" :title="t('agentGraph.editAgent.confirmDelete')" width="400px">
    <p>{{ t('agentGraph.editAgent.confirmDeleteMessage', { name: agent?.name }) }}</p>
    <p class="warning-text">{{ t('agentGraph.editAgent.actionCannotUndo') }}</p>
    <template #footer>
      <el-button @click="deleteConfirmVisible = false">{{ t('common.cancel') }}</el-button>
      <el-button type="danger" :loading="deleting" @click="confirmDelete">{{ t('agentGraph.editAgent.delete') }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'
import { deleteOpenClawAgent } from '@/lib/openclaw/agentApi'
import type { AgentNode } from '@/types/agentGraph'

interface Props {
  modelValue: boolean
  agent?: AgentNode
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'updated', node: AgentNode): void
  (e: 'deleted', nodeId: string): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const { t } = useI18n()

const visible = ref(props.modelValue)
const formRef = ref<FormInstance>()
const loading = ref(false)
const deleting = ref(false)
const deleteConfirmVisible = ref(false)

const isReadOnly = computed(() => props.agent?.isMain ?? false)

const form = reactive({
  name: '',
  description: '',
  model: '',
  tags: [] as string[]
})

const rules: FormRules = {
  name: [
    {
      pattern: /^[a-zA-Z0-9-_]+$/,
      message: t('agentGraph.editAgent.validation.namePattern'),
      trigger: 'blur'
    }
  ]
}

watch(
  () => props.modelValue,
  val => {
    visible.value = val
    if (val && props.agent) {
      form.name = props.agent.name
      form.description = props.agent.description || ''
      form.model = props.agent.model || ''
      form.tags = props.agent.tags || []
    }
  }
)

watch(visible, val => {
  emit('update:modelValue', val)
})

function handleClose() {
  visible.value = false
  formRef.value?.resetFields()
}

function formatDate(dateStr: string): string {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}

async function handleSubmit() {
  if (!formRef.value || !props.agent) return

  await formRef.value.validate(async valid => {
    if (!valid) return

    loading.value = true
    try {
      const updatedNode: AgentNode = {
        ...props.agent,
        name: form.name,
        description: form.description,
        model: form.model,
        tags: form.tags,
        updatedAt: new Date().toISOString()
      }

      ElMessage.success(t('agentGraph.editAgent.message.updateSuccess'))
      emit('updated', updatedNode)
      handleClose()
    } catch (error) {
      ElMessage.error(t('agentGraph.editAgent.message.updateFailed'))
    } finally {
      loading.value = false
    }
  })
}

function handleDelete() {
  deleteConfirmVisible.value = true
}

async function confirmDelete() {
  if (!props.agent) return

  deleting.value = true
  try {
    await deleteOpenClawAgent(props.agent.id)
    ElMessage.success(t('agentGraph.editAgent.message.deleteSuccess'))
    emit('deleted', props.agent.id)
    deleteConfirmVisible.value = false
    handleClose()
  } catch (error) {
    ElMessage.error(t('agentGraph.editAgent.message.deleteFailed'))
  } finally {
    deleting.value = false
  }
}
</script>

<style scoped>
.agent-detail {
  padding: 0 8px;
}

.readonly-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: var(--warning-bg, #fdf6ec);
  border: 1px solid var(--warning-border, #f5dab1);
  border-radius: 4px;
  color: var(--warning-color);
  margin-bottom: 20px;
}

.agent-meta {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
}

.meta-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
}

.meta-item .label {
  color: var(--text-secondary);
}

.meta-item .value {
  color: var(--text-primary);
}

.warning-text {
  color: var(--danger-color);
  font-size: 13px;
  margin-top: 8px;
}
</style>
