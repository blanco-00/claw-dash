<template>
  <el-dialog
    v-model="visible"
    :title="isReadOnly ? 'Agent Details' : 'Edit Agent'"
    width="520px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div v-if="agent" class="agent-detail">
      <div class="readonly-banner" v-if="isReadOnly">
        <el-icon><Warning /></el-icon>
        <span>This is a main agent - read only</span>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        :disabled="isReadOnly"
      >
        <el-form-item label="Agent ID">
          <el-input :model-value="agent.id" disabled />
        </el-form-item>
        <el-form-item label="Name" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="Workspace">
          <el-input :model-value="agent.workspace" disabled />
        </el-form-item>
        <el-form-item label="Description" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="Model" prop="model">
          <el-select v-model="form.model" style="width: 100%">
            <el-option label="GPT-4" value="gpt-4" />
            <el-option label="GPT-4 Turbo" value="gpt-4-turbo" />
            <el-option label="GPT-3.5 Turbo" value="gpt-3.5-turbo" />
            <el-option label="Claude 3 Opus" value="claude-3-opus" />
            <el-option label="Claude 3 Sonnet" value="claude-3-sonnet" />
          </el-select>
        </el-form-item>
        <el-form-item label="Tags" prop="tags">
          <el-select v-model="form.tags" multiple style="width: 100%">
            <el-option label="Research" value="research" />
            <el-option label="Development" value="development" />
            <el-option label="Analysis" value="analysis" />
            <el-option label="Support" value="support" />
            <el-option label="Automation" value="automation" />
          </el-select>
        </el-form-item>
      </el-form>

      <div class="agent-meta">
        <div class="meta-item">
          <span class="label">Created:</span>
          <span class="value">{{ formatDate(agent.createdAt) }}</span>
        </div>
        <div class="meta-item">
          <span class="label">Updated:</span>
          <span class="value">{{ formatDate(agent.updatedAt) }}</span>
        </div>
      </div>
    </div>

    <template #footer>
      <div v-if="!isReadOnly">
        <el-button type="danger" @click="handleDelete">Delete</el-button>
        <el-button @click="handleClose">Cancel</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">Save</el-button>
      </div>
      <div v-else>
        <el-button @click="handleClose">Close</el-button>
      </div>
    </template>
  </el-dialog>

  <el-dialog v-model="deleteConfirmVisible" title="Confirm Delete" width="400px">
    <p>Are you sure you want to delete agent "{{ agent?.name }}"?</p>
    <p class="warning-text">This action cannot be undone.</p>
    <template #footer>
      <el-button @click="deleteConfirmVisible = false">Cancel</el-button>
      <el-button type="danger" :loading="deleting" @click="confirmDelete">Delete</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
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
      message: 'Only letters, numbers, hyphens and underscores',
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

      ElMessage.success('Agent updated successfully')
      emit('updated', updatedNode)
      handleClose()
    } catch (error) {
      ElMessage.error('Failed to update agent')
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
    ElMessage.success('Agent deleted successfully')
    emit('deleted', props.agent.id)
    deleteConfirmVisible.value = false
    handleClose()
  } catch (error) {
    ElMessage.error('Failed to delete agent')
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
  background: #fdf6ec;
  border: 1px solid #f5dab1;
  border-radius: 4px;
  color: #e6a23c;
  margin-bottom: 20px;
}

.agent-meta {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.meta-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
}

.meta-item .label {
  color: #909399;
}

.meta-item .value {
  color: #606266;
}

.warning-text {
  color: #f56c6c;
  font-size: 13px;
  margin-top: 8px;
}
</style>
