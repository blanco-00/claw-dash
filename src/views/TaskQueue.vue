<template>
  <div class="task-queue-page">
    <div class="page-header">
      <div class="header-left">
        <h2>{{ t('taskQueue.title') }}</h2>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          {{ t('taskQueue.createTask') }}
        </el-button>
      </div>
    </div>

    <div class="stats-cards">
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value">{{ stats.pending }}</div>
            <div class="stat-label">{{ t('taskQueue.stats.pending') }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value">{{ stats.running }}</div>
            <div class="stat-label">{{ t('taskQueue.stats.running') }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value">{{ stats.completed }}</div>
            <div class="stat-label">{{ t('taskQueue.stats.completed') }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-value">{{ stats.failed }}</div>
            <div class="stat-label">{{ t('taskQueue.stats.failed') }}</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="task-container">
      <TaskList ref="taskListRef" @view="handleViewTask" />
    </div>

    <TaskDetail v-model="showDetailDrawer" :task="selectedTask" @close="showDetailDrawer = false" />

    <el-dialog v-model="showCreateDialog" :title="t('taskQueue.createTask')" width="500px">
      <el-form ref="formRef" :model="taskForm" :rules="rules" label-width="100px">
        <el-form-item :label="t('taskQueue.form.taskType')" prop="type">
          <el-select v-model="taskForm.type" :placeholder="t('taskQueue.form.selectTaskType')" style="width: 100%">
            <el-option v-for="tt in taskTypes" :key="tt.name" :label="tt.displayName" :value="tt.name" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('taskQueue.form.payload')" prop="payload">
          <el-input
            v-model="taskForm.payload"
            type="textarea"
            :rows="4"
            :placeholder="t('taskQueue.form.payloadPlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="t('taskQueue.form.priority')" prop="priority">
          <el-slider
            v-model="taskForm.priority"
            :min="0"
            :max="10"
            :marks="{ 0: t('taskQueue.form.priorityMarks.low'), 5: t('taskQueue.form.priorityMarks.medium'), 10: t('taskQueue.form.priorityMarks.high') }"
          />
        </el-form-item>
        <el-form-item :label="t('taskQueue.form.maxRetries')" prop="maxRetries">
          <el-input-number v-model="taskForm.maxRetries" :min="0" :max="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">{{ t('taskQueue.button.cancel') }}</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreateTask">{{ t('taskQueue.button.create') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useI18n } from 'vue-i18n'
import TaskList from '@/components/TaskQueue/TaskList.vue'
import TaskDetail from '@/components/TaskQueue/TaskDetail.vue'
import { createTask, listTasks } from '@/lib/openclaw/taskQueueApi'
import { getTaskTypes } from '@/lib/openclaw/taskTypeApi'
import type { TaskQueueTask } from '@/types/agentGraph'

const { t } = useI18n()

const taskListRef = ref()
const showDetailDrawer = ref(false)
const showCreateDialog = ref(false)
const selectedTask = ref<TaskQueueTask>()
const creating = ref(false)
const taskTypes = ref<{ name: string; displayName: string }[]>([])

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
  await fetchTaskTypes()
  await fetchStats()
})

async function fetchTaskTypes() {
  try {
    const response = await getTaskTypes()
    taskTypes.value = (response.data as any).data || []
  } catch (error) {
    console.error('Failed to fetch task types:', error)
    // Fallback to default types
    taskTypes.value = [
      { name: 'agent-execute', displayName: 'agent-execute' },
      { name: 'data-sync', displayName: 'data-sync' },
      { name: 'notification', displayName: 'notification' },
      { name: 'cleanup', displayName: 'cleanup' }
    ]
  }
}

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

      ElMessage.success(t('taskQueue.message.createSuccess'))
      showCreateDialog.value = false
      taskListRef.value?.refresh()
      fetchStats()
    } catch (error) {
      ElMessage.error(t('taskQueue.message.createError'))
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
