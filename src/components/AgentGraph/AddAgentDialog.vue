<template>
  <el-dialog
    v-model="visible"
    title="Add New Agent"
    width="480px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="Agent Name" prop="name">
        <el-input v-model="form.name" placeholder="e.g., research-assistant" />
      </el-form-item>
      <el-form-item label="Workspace" prop="workspace">
        <el-input v-model="form.workspace" placeholder="/path/to/workspace" />
      </el-form-item>
      <el-form-item label="Description" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="Business description..."
        />
      </el-form-item>
      <el-form-item label="Model" prop="model">
        <el-select v-model="form.model" placeholder="Select model" style="width: 100%">
          <el-option label="GPT-4" value="gpt-4" />
          <el-option label="GPT-4 Turbo" value="gpt-4-turbo" />
          <el-option label="GPT-3.5 Turbo" value="gpt-3.5-turbo" />
          <el-option label="Claude 3 Opus" value="claude-3-opus" />
          <el-option label="Claude 3 Sonnet" value="claude-3-sonnet" />
        </el-select>
      </el-form-item>
      <el-form-item label="Tags" prop="tags">
        <el-select v-model="form.tags" multiple placeholder="Add tags" style="width: 100%">
          <el-option label="Research" value="research" />
          <el-option label="Development" value="development" />
          <el-option label="Analysis" value="analysis" />
          <el-option label="Support" value="support" />
          <el-option label="Automation" value="automation" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">Cancel</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">Create</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted, onUnmounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { createOpenClawAgent } from '@/lib/openclaw/agentApi'
import type { AgentNode } from '@/types/agentGraph'

interface Props {
  modelValue: boolean
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'created', node: AgentNode): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const visible = ref(props.modelValue)
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  name: '',
  workspace: '',
  description: '',
  model: '',
  tags: [] as string[]
})

const rules: FormRules = {
  name: [
    { required: true, message: 'Agent name is required', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9-_]+$/,
      message: 'Only letters, numbers, hyphens and underscores',
      trigger: 'blur'
    }
  ],
  workspace: [{ required: true, message: 'Workspace path is required', trigger: 'blur' }]
}

watch(
  () => props.modelValue,
  val => {
    visible.value = val
  }
)

watch(visible, val => {
  emit('update:modelValue', val)
})

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape' && visible.value) {
    handleClose()
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})

function handleClose() {
  visible.value = false
  formRef.value?.resetFields()
}

async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async valid => {
    if (!valid) return

    loading.value = true
    try {
      await createOpenClawAgent({
        name: form.name,
        workspace: form.workspace
      })

      const newNode: AgentNode = {
        id: form.name,
        name: form.name,
        description: form.description,
        workspace: form.workspace,
        model: form.model,
        tags: form.tags,
        isMain: false,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      }

      ElMessage.success('Agent created successfully')
      emit('created', newNode)
      handleClose()
    } catch (error) {
      ElMessage.error('Failed to create agent')
    } finally {
      loading.value = false
    }
  })
}
</script>
