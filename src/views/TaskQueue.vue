<template>
  <div class="task-queue-page">
    <div class="page-header">
      <div class="header-left">
        <h2>Task Queue</h2>
        <span class="subtitle">Async task management with Spring Boot + MySQL + Redis</span>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          Create Task
        </el-button>
      </div>
    </div>

    <div class="stats-cards">
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value">{{ stats.pending }}</div>
            <div class="stat-label">Pending</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value">{{ stats.running }}</div>
            <div class="stat-label">Running</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value">{{ stats.completed }}</div>
            <div class="stat-label">Completed</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value">{{ stats.failed }}</div>
            <div class="stat-label">Failed</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="task-container">
      <TaskList ref="taskListRef" @view="handleViewTask" />
    </div>

    <TaskDetail v-model="showDetailDrawer" :task="selectedTask" @close="showDetailDrawer = false" />

    <el-dialog v-model="showCreateDialog" title="Create Task" width="500px">
      <el-form ref="formRef" :model="taskForm" :rules="rules" label-width="100px">
        <el-form-item label="Task Type" prop="type">
          <el-select v-model="taskForm.type" placeholder="Select task type" style="width: 100%">
            <el-option label="agent-execute" value="agent-execute" />
            <el-option label="data-sync" value="data-sync" />
            <el-option label="notification" value="notification" />
            <el-option label="cleanup" value="cleanup" />
          </el-select>
        </el-form-item>
        <el-form-item label="Payload" prop="payload">
          <el-input
            v-model="taskForm.payload"
            type="textarea"
            :rows="4"
            placeholder='{"key": "value"}'
          />
        </el-form-item>
        <el-form-item label="Priority" prop="priority">
          <el-slider
            v-model="taskForm.priority"
            :min="0"
            :max="10"
            :marks="{ 0: 'Low', 5: 'Medium', 10: 'High' }"
          />
        </el-form-item>
        <el-form-item label="Max Retries" prop="maxRetries">
          <el-input-number v-model="taskForm.maxRetries" :min="0" :max="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">Cancel</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreateTask">Create</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import TaskList from '@/components/TaskQueue/TaskList.vue'
import TaskDetail from '@/components/TaskQueue/TaskDetail.vue'
import { createTask, listTasks } from '@/lib/openclaw/taskQueueApi'
import type { TaskQueueTask } from '@/types/agentGraph'

const taskListRef = ref()
const showDetailDrawer = ref(false)
const showCreateDialog = ref(false)
const selectedTask = ref<TaskQueueTask>()
const creating = ref(false)

const stats = reactive({
  pending: 0,
  running: 0,
  completed: 0,
  failed: 0
})

const taskForm = reactive({
  type: '',
  payload: '',
  priority: 5,
  maxRetries: 3
})

const rules: FormRules = {
  type: [{ required: true, message: 'Task type is required', trigger: 'change' }]
}

const formRef = ref<FormInstance>()

onMounted(async () => {
  await fetchStats()
})

async function fetchStats() {
  try {
    const statuses = ['PENDING', 'RUNNING', 'COMPLETED', 'FAILED']
    const results = await Promise.all(statuses.map(status => listTasks(0, 1, status)))

    stats.pending = (results[0].data as any).totalElements || 0
    stats.running = (results[1].data as any).totalElements || 0
    stats.completed = (results[2].data as any).totalElements || 0
    stats.failed = (results[3].data as any).totalElements || 0
  } catch (error) {
    console.error('Failed to fetch stats:', error)
  }
}

function handleViewTask(task: TaskQueueTask) {
  selectedTask.value = task
  showDetailDrawer.value = true
}

async function handleCreateTask() {
  if (!formRef.value) return

  await formRef.value.validate(async valid => {
    if (!valid) return

    creating.value = true
    try {
      let payload = {}
      if (taskForm.payload) {
        try {
          payload = JSON.parse(taskForm.payload)
        } catch {
          payload = { raw: taskForm.payload }
        }
      }

      await createTask({
        type: taskForm.type,
        payload,
        priority: taskForm.priority,
        maxRetries: taskForm.maxRetries
      })

      ElMessage.success('Task created successfully')
      showCreateDialog.value = false
      taskListRef.value?.refresh()
      fetchStats()
    } catch (error) {
      ElMessage.error('Failed to create task')
    } finally {
      creating.value = false
    }
  })
}
</script>

<style scoped>
.task-queue-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 16px;
  background: #f5f7fa;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.subtitle {
  font-size: 13px;
  color: #909399;
}

.stats-cards {
  margin-bottom: 16px;
}

.stat-card {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: 600;
  color: #409eff;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.task-container {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  overflow: auto;
}

@media (max-width: 768px) {
  .task-queue-page {
    padding: 8px;
  }

  .page-header {
    flex-direction: column;
    gap: 12px;
    padding: 12px 16px;
  }

  .stats-cards :deep(.el-col) {
    margin-bottom: 12px;
  }

  .stat-card {
    padding: 12px;
  }

  .stat-value {
    font-size: 24px;
  }

  .stat-label {
    font-size: 12px;
  }
}
</style>
