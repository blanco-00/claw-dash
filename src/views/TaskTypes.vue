<template>
  <div class="task-type-page">
    <div class="page-header">
      <h2>{{ t('taskType.title') }}</h2>
      <el-button type="primary" @click="showDialog = true">
        {{ t('taskType.create') }}
      </el-button>
    </div>

    <el-table :data="taskTypes" v-loading="loading" style="width: 100%">
      <el-table-column prop="name" :label="t('taskType.name')" width="150" />
      <el-table-column prop="displayName" :label="t('taskType.displayName')" width="150" />
      <el-table-column prop="description" :label="t('taskType.description')" />
      <el-table-column :label="t('taskType.enabled')" width="100">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
            {{ row.enabled ? '✓' : '✕' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('taskType.actions')" width="150">
        <template #default="{ row }">
          <el-button text size="small" @click="handleEdit(row)">{{ t('taskQueue.button.view') }}</el-button>
          <el-button text size="small" type="danger" @click="handleDelete(row)">✕</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="showDialog" :title="editingId ? t('taskType.edit') : t('taskType.create')" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item :label="t('taskType.form.name')" prop="name">
          <el-input v-model="form.name" :placeholder="t('taskType.form.namePlaceholder')" :disabled="!!editingId" />
        </el-form-item>
        <el-form-item :label="t('taskType.form.displayName')" prop="displayName">
          <el-input v-model="form.displayName" :placeholder="t('taskType.form.displayNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('taskType.form.description')" prop="description">
          <el-input v-model="form.description" type="textarea" :placeholder="t('taskType.form.descriptionPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('taskType.form.enabled')" prop="enabled">
          <el-switch v-model="form.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">{{ t('taskQueue.button.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">{{ t('taskQueue.button.create') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getTaskTypes, createTaskType, updateTaskType, deleteTaskType, type TaskType } from '@/lib/openclaw/taskTypeApi'

const { t } = useI18n()

const taskTypes = ref<TaskType[]>([])
const loading = ref(false)
const showDialog = ref(false)
const saving = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const form = reactive({
  name: '',
  displayName: '',
  description: '',
  enabled: true
})

const rules: FormRules = {
  name: [{ required: true, message: 'Name is required', trigger: 'blur' }],
  displayName: [{ required: true, message: 'Display name is required', trigger: 'blur' }]
}

onMounted(() => {
  fetchTaskTypes()
})

async function fetchTaskTypes() {
  loading.value = true
  try {
    const response = await getTaskTypes()
    taskTypes.value = (response.data as any).data || []
  } catch (error) {
    console.error('Failed to fetch task types:', error)
    ElMessage.error(t('taskQueue.message.fetchError'))
  } finally {
    loading.value = false
  }
}

function handleEdit(row: TaskType) {
  editingId.value = row.id || null
  form.name = row.name
  form.displayName = row.displayName
  form.description = row.description || ''
  form.enabled = row.enabled
  showDialog.value = true
}

async function handleDelete(row: TaskType) {
  try {
    await ElMessageBox.confirm(t('taskType.confirmDelete'), 'Warning', {
      confirmButtonText: 'OK',
      cancelButtonText: t('taskQueue.button.cancel'),
      type: 'warning'
    })
    
    await deleteTaskType(row.id!)
    ElMessage.success('Deleted successfully')
    fetchTaskTypes()
  } catch (error) {
    // User cancelled or error
  }
}

async function handleSave() {
  if (!formRef.value) return
  
  await formRef.value.validate(async valid => {
    if (!valid) return
    
    saving.value = true
    try {
      if (editingId.value) {
        await updateTaskType(editingId.value, form)
        ElMessage.success('Updated successfully')
      } else {
        await createTaskType(form)
        ElMessage.success('Created successfully')
      }
      showDialog.value = false
      resetForm()
      fetchTaskTypes()
    } catch (error) {
      ElMessage.error('Operation failed')
    } finally {
      saving.value = false
    }
  })
}

function resetForm() {
  editingId.value = null
  form.name = ''
  form.displayName = ''
  form.description = ''
  form.enabled = true
}

showDialog.value = false
</script>

<style scoped>
.task-type-page {
  padding: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}
</style>
