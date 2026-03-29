<template>
  <div class="page-container task-queue-page">
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon">📋</div>
        <div class="header-text">
          <h2 class="page-title">{{ t('taskQueue.title') }}</h2>
          <p class="page-subtitle">管理任务队列</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          {{ t('taskQueue.createTask') }}
        </el-button>
      </div>
    </div>

    <div class="stat-card-grid">
      <div class="stat-card warning">
        <div class="stat-value">{{ stats.pending }}</div>
        <div class="stat-label">{{ t('taskQueue.stats.pending') }}</div>
      </div>
      <div class="stat-card info">
        <div class="stat-value">{{ stats.running }}</div>
        <div class="stat-label">{{ t('taskQueue.stats.running') }}</div>
      </div>
      <div class="stat-card success">
        <div class="stat-value">{{ stats.completed }}</div>
        <div class="stat-label">{{ t('taskQueue.stats.completed') }}</div>
      </div>
      <div class="stat-card danger">
        <div class="stat-value">{{ stats.failed }}</div>
        <div class="stat-label">{{ t('taskQueue.stats.failed') }}</div>
      </div>
    </div>

    <div class="table-panel task-container">
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
        <el-form-item label="任务组" prop="taskGroupId">
          <el-select v-model="taskForm.taskGroupId" placeholder="选择任务组（可选）" clearable style="width: 100%">
            <el-option v-for="tg in taskGroups" :key="tg.id" :label="tg.name" :value="String(tg.id)" />
          </el-select>
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
import { ref, reactive, onMounted, watch } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useI18n } from 'vue-i18n'
import TaskList from '@/components/TaskQueue/TaskList.vue'
import TaskDetail from '@/components/TaskQueue/TaskDetail.vue'
import { createTask, listTasks } from '@/lib/openclaw/taskQueueApi'
import { getTaskTypes } from '@/lib/openclaw/taskTypeApi'
import { listTaskGroups } from '@/lib/openclaw/taskGroupApi'
import type { TaskQueueTask } from '@/types/agentGraph'
import type { TaskGroup } from '@/types/task'

const { t } = useI18n()

const taskListRef = ref()
const showDetailDrawer = ref(false)
const showCreateDialog = ref(false)
const selectedTask = ref<TaskQueueTask>()
const creating = ref(false)
const taskTypes = ref<{ name: string; displayName: string }[]>([])
const taskGroups = ref<TaskGroup[]>([])

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
  maxRetries: 3,
  taskGroupId: ''
})

const rules: FormRules = {
  type: [{ required: true, message: 'Task type is required', trigger: 'change' }]
}

const formRef = ref<FormInstance>()

onMounted(async () => {
  await Promise.all([fetchTaskTypes(), fetchTaskGroups(), fetchStats()])
})

watch(showCreateDialog, (val) => {
  if (val) {
    taskForm.type = ''
    taskForm.payload = ''
    taskForm.priority = 5
    taskForm.maxRetries = 3
    taskForm.taskGroupId = ''
  }
})

async function fetchTaskTypes() {
  try {
    const response = await getTaskTypes()
    taskTypes.value = response.data || []
  } catch {
    taskTypes.value = [
      { name: 'agent-execute', displayName: 'agent-execute' },
      { name: 'data-sync', displayName: 'data-sync' },
      { name: 'notification', displayName: 'notification' },
      { name: 'cleanup', displayName: 'cleanup' }
    ]
  }
}

async function fetchTaskGroups() {
  try {
    const response = await listTaskGroups(0, 100)
    taskGroups.value = response.data?.content || []
  } catch {
    taskGroups.value = []
  }
}

async function fetchStats() {
  try {
    const statuses = ['PENDING', 'RUNNING', 'COMPLETED', 'FAILED']
    const results = await Promise.all(statuses.map(status => listTasks(0, 1, status)))
    stats.pending = results[0]?.data?.totalElements || 0
    stats.running = results[1]?.data?.totalElements || 0
    stats.completed = results[2]?.data?.totalElements || 0
    stats.failed = results[3]?.data?.totalElements || 0
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
        maxRetries: taskForm.maxRetries,
        taskGroupId: taskForm.taskGroupId || undefined
      })

      ElMessage.success(t('taskQueue.message.createSuccess'))
      showCreateDialog.value = false
      taskListRef.value?.refresh()
      fetchStats()
    } catch {
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
}

.task-container {
  flex: 1;
  overflow: auto;
}

@media (max-width: 768px) {
  .task-queue-page {
    padding: 8px;
  }
}
</style>
