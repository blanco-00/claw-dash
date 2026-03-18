<template>
  <div class="tasks-page">
    <div class="header">
      <h2>任务列表</h2>
      <el-button type="primary" @click="showCreateDialog = true">创建任务</el-button>
    </div>

    <el-table :data="tasks" style="width: 100%">
      <el-table-column prop="taskId" label="任务ID" width="200" />
      <el-table-column prop="type" label="类型" width="120" />
      <el-table-column prop="priority" label="优先级" width="80" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="viewTask(row)">查看</el-button>
          <el-button size="small" type="danger" @click="deleteTask(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showCreateDialog" title="创建任务" width="500px">
      <el-form :model="taskForm" label-width="80px">
        <el-form-item label="类型">
          <el-input v-model="taskForm.type" />
        </el-form-item>
        <el-form-item label=" payload">
          <el-input v-model="taskForm.payload" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="taskForm.priority" :min="0" :max="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createTask">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const tasks = ref([])
const showCreateDialog = ref(false)
const taskForm = ref({ type: '', payload: '', priority: 0 })

const loadTasks = async () => {
  try {
    const res = await request.get('/tasks')
    tasks.value = res.data
  } catch (error) {
    console.error('Failed to load tasks:', error)
  }
}

const createTask = async () => {
  try {
    await request.post('/tasks', taskForm.value)
    ElMessage.success('创建成功')
    showCreateDialog.value = false
    loadTasks()
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

const getStatusType = (status: string) => {
  const map: Record<string, any> = {
    PENDING: 'info',
    RUNNING: 'warning',
    COMPLETED: 'success',
    FAILED: 'danger',
    DEAD: 'danger'
  }
  return map[status] || 'info'
}

const viewTask = (row: any) => {
  console.log('View task:', row)
}

const deleteTask = async (row: any) => {
  console.log('Delete task:', row)
}

onMounted(() => {
  loadTasks()
})
</script>

<style scoped>
.tasks-page {
  padding: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
</style>
