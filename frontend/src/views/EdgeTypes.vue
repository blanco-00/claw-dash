<template>
  <div class="edge-type-page">
    <div class="page-header">
      <h2>边类型管理</h2>
      <el-button type="primary" @click="showDialog = true">
        添加类型
      </el-button>
    </div>

    <el-table :data="edgeTypes" v-loading="loading" style="width: 100%">
      <el-table-column prop="value" label="值" width="120">
        <template #default="{ row }">
          <el-tag type="info">{{ row.value }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" width="150" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="defaultMessageTemplate" label="默认模板" show-overflow-tooltip />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button text size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button text size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="showDialog" :title="editingId ? '编辑边类型' : '添加边类型'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="值" prop="value" v-if="!editingId">
          <el-input v-model="form.value" placeholder="如: custom" :disabled="!!editingId" />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="如: 自定义" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="描述此边类型的用途" />
        </el-form-item>
        <el-form-item label="默认模板" prop="defaultMessageTemplate">
          <el-input v-model="form.defaultMessageTemplate" type="textarea" :rows="3" placeholder="如: 请帮我完成以下任务:\n{original_message}" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { configGraphApi } from '@/lib/configGraphApi'

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
  value: [{ required: true, message: '值不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '名称不能为空', trigger: 'blur' }]
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
    ElMessage.error('获取边类型失败')
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
    await ElMessageBox.confirm('确定要删除此边类型吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await configGraphApi.deleteEdgeType(row.id)
    ElMessage.success('删除成功')
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
        ElMessage.success('更新成功')
      } else {
        await configGraphApi.createEdgeType({
          value: form.value,
          name: form.name,
          description: form.description,
          defaultMessageTemplate: form.defaultMessageTemplate
        })
        ElMessage.success('创建成功')
      }
      showDialog.value = false
      resetForm()
      fetchEdgeTypes()
    } catch (error) {
      ElMessage.error('操作失败')
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
