<template>
  <el-dialog
    v-model="visible"
    :title="t('agentGraph.addAgentDialog.title')"
    width="480px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item :label="t('agentGraph.addAgentDialog.agentName')" prop="name">
        <el-input v-model="form.name" :placeholder="t('agentGraph.addAgentDialog.agentNamePlaceholder')" />
      </el-form-item>
      <el-form-item :label="t('agentGraph.addAgentDialog.workspace')" prop="workspace">
        <el-input v-model="form.workspace" :placeholder="t('agentGraph.addAgentDialog.workspacePlaceholder')" />
      </el-form-item>
      <el-form-item :label="t('agentGraph.addAgentDialog.description')" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          :placeholder="t('agentGraph.addAgentDialog.descriptionPlaceholder')"
        />
      </el-form-item>
      <el-form-item :label="t('agentGraph.addAgentDialog.model')" prop="model">
        <el-select v-model="form.model" :placeholder="t('agentGraph.addAgentDialog.selectModel')" style="width: 100%">
          <el-option :label="t('agentGraph.models.gpt4')" value="gpt-4" />
          <el-option :label="t('agentGraph.models.gpt4Turbo')" value="gpt-4-turbo" />
          <el-option :label="t('agentGraph.models.gpt35Turbo')" value="gpt-3.5-turbo" />
          <el-option :label="t('agentGraph.models.claude3Opus')" value="claude-3-opus" />
          <el-option :label="t('agentGraph.models.claude3Sonnet')" value="claude-3-sonnet" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('agentGraph.addAgentDialog.tags')" prop="tags">
        <el-select v-model="form.tags" multiple :placeholder="t('agentGraph.addAgentDialog.addTags')" style="width: 100%">
          <el-option :label="t('agentGraph.tagOptions.research')" value="research" />
          <el-option :label="t('agentGraph.tagOptions.development')" value="development" />
          <el-option :label="t('agentGraph.tagOptions.analysis')" value="analysis" />
          <el-option :label="t('agentGraph.tagOptions.support')" value="support" />
          <el-option :label="t('agentGraph.tagOptions.automation')" value="automation" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">{{ t('common.create') }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
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
const { t } = useI18n()

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
    { required: true, message: t('agentGraph.addAgentDialog.validation.nameRequired'), trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9-_]+$/,
      message: t('agentGraph.editAgent.validation.namePattern'),
      trigger: 'blur'
    }
  ],
  workspace: [{ required: true, message: t('agentGraph.addAgentDialog.validation.workspaceRequired'), trigger: 'blur' }]
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

      ElMessage.success(t('agentGraph.addAgentDialog.message.createSuccess'))
      emit('created', newNode)
      handleClose()
    } catch (error) {
      ElMessage.error(t('agentGraph.addAgentDialog.message.createFailed'))
    } finally {
      loading.value = false
    }
  })
}
</script>
