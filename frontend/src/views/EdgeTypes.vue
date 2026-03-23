<template>
  <div class="edge-type-page">
    <div class="page-header">
      <h2>{{ t('edgeTypes.title') }}</h2>
      <el-button type="primary" @click="showDialog = true">
        {{ t('edgeTypes.addType') }}
      </el-button>
    </div>

    <el-table :data="edgeTypes" v-loading="loading" style="width: 100%">
      <el-table-column prop="value" :label="t('edgeTypes.table.value')" width="120">
        <template #default="{ row }">
          <el-tag type="info">{{ row.value }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="name" :label="t('edgeTypes.table.name')" width="150" />
      <el-table-column prop="description" :label="t('edgeTypes.table.description')" />
      <el-table-column prop="defaultMessageTemplate" :label="t('edgeTypes.table.defaultTemplate')" show-overflow-tooltip />
      <el-table-column :label="t('common.actions')" width="150">
        <template #default="{ row }">
          <el-button text size="small" @click="handleEdit(row)">{{ t('common.edit') }}</el-button>
          <el-button text size="small" type="danger" @click="handleDelete(row)">{{ t('common.delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="showDialog" :title="editingId ? t('common.edit') : t('edgeTypes.addType')" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="t('edgeTypes.form.value')" prop="value" v-if="!editingId">
          <el-input v-model="form.value" :placeholder="t('edgeTypes.form.valuePlaceholder')" :disabled="!!editingId" />
        </el-form-item>
        <el-form-item :label="t('edgeTypes.form.name')" prop="name">
          <el-input v-model="form.name" :placeholder="t('edgeTypes.form.namePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('edgeTypes.form.description')" prop="description">
          <el-input v-model="form.description" type="textarea" :placeholder="t('edgeTypes.form.descriptionPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('edgeTypes.form.defaultTemplate')" prop="defaultMessageTemplate">
          <el-input v-model="form.defaultMessageTemplate" type="textarea" :rows="3" :placeholder="t('edgeTypes.form.defaultTemplatePlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { configGraphApi } from '@/lib/configGraphApi'

const { t } = useI18n()

interface EdgeType {
  id: number
  value: string
  name: string
  description?: string
  defaultMessageTemplate?: string
}

const edgeTypes = ref<EdgeType[]>([])
const loading = ref(false)
const showDialog = ref(false)
const saving = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const form = reactive({
  value: '',
  name: '',
  description: '',
  defaultMessageTemplate: ''
})

const rules: FormRules = {
  value: [{ required: true, message: t('edgeTypes.message.valueRequired'), trigger: 'blur' }],
  name: [{ required: true, message: t('edgeTypes.message.nameRequired'), trigger: 'blur' }]
}

onMounted(() => {
  fetchEdgeTypes()
})

async function fetchEdgeTypes() {
  loading.value = true
  try {
    const response = await configGraphApi.getEdgeTypes()
    edgeTypes.value = response.data || []
  } catch (error) {
    console.error('Failed to fetch edge types:', error)
    ElMessage.error(t('edgeTypes.message.fetchError'))
  } finally {
    loading.value = false
  }
}

function handleEdit(row: EdgeType) {
  editingId.value = row.id
  form.value = row.value
  form.name = row.name
  form.description = row.description || ''
  form.defaultMessageTemplate = row.defaultMessageTemplate || ''
  showDialog.value = true
}

async function handleDelete(row: EdgeType) {
  try {
    await ElMessageBox.confirm(t('edgeTypes.message.confirmDelete') || t('common.confirmDelete'), t('common.warning'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    
    await configGraphApi.deleteEdgeType(row.id)
    ElMessage.success(t('edgeTypes.message.deleteSuccess'))
    fetchEdgeTypes()
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
        await configGraphApi.updateEdgeType(editingId.value, {
          name: form.name,
          description: form.description,
          defaultMessageTemplate: form.defaultMessageTemplate
        })
        ElMessage.success(t('edgeTypes.message.updateSuccess'))
      } else {
        await configGraphApi.createEdgeType({
          value: form.value,
          name: form.name,
          description: form.description,
          defaultMessageTemplate: form.defaultMessageTemplate
        })
        ElMessage.success(t('edgeTypes.message.createSuccess'))
      }
      showDialog.value = false
      resetForm()
      fetchEdgeTypes()
    } catch (error) {
      ElMessage.error(t('edgeTypes.message.createError'))
    } finally {
      saving.value = false
    }
  })
}

function resetForm() {
  editingId.value = null
  form.value = ''
  form.name = ''
  form.description = ''
  form.defaultMessageTemplate = ''
}
</script>

<style scoped>
.edge-type-page {
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
