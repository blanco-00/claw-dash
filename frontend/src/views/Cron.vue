<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCronTasks, getCronStats } from '@/api/cron'
import type { CronTask } from '@/types/cron'

const loading = ref(true)
const tasks = ref<CronTask[]>([])
const stats = ref({ total: 0, enabled: 0, disabled: 0, errors: 0 })

async function refresh() {
  loading.value = true
  try {
    tasks.value = await getCronTasks()
    stats.value = await getCronStats()
  } catch (error) {
    console.error('获取Cron任务失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  refresh()
})
</script>

<template>
  <div class="cron-page">
    <!-- 页面头部 -->
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold">⏰ Cron任务</h2>
      <el-button type="primary" :loading="loading" @click="refresh">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb-6">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="text-center">
            <div class="text-3xl font-bold text-blue-500">{{ stats.total }}</div>
            <div class="text-gray-500">总任务</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="text-center">
            <div class="text-3xl font-bold text-green-500">{{ stats.enabled }}</div>
            <div class="text-gray-500">运行中</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="text-center">
            <div class="text-3xl font-bold text-gray-500">{{ stats.disabled }}</div>
            <div class="text-gray-500">已禁用</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="text-center">
            <div class="text-3xl font-bold text-red-500">{{ stats.errors }}</div>
            <div class="text-gray-500">错误</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 任务列表 -->
    <el-card shadow="hover">
      <el-table :data="tasks" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="200">
          <template #default="{ row }">
            <span class="font-mono text-sm">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="任务名称" />
        <el-table-column prop="agent" label="Agent" width="120" />
        <el-table-column prop="schedule" label="调度" width="150">
          <template #default="{ row }">
            <el-tag size="small">{{ row.schedule }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'ok' ? 'success' : row.status === 'error' ? 'danger' : 'info'"
            >
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="nextRun" label="下次执行" width="180">
          <template #default="{ row }">
            <span v-if="row.nextRun" class="text-sm">
              {{ new Date(row.nextRun).toLocaleString('zh-CN') }}
            </span>
            <span v-else class="text-gray-400">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" size="small">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script lang="ts">
import { Refresh } from '@element-plus/icons-vue'
export default {
  components: { Refresh }
}
</script>

<style scoped>
.cron-page {
  padding: 20px;
}
</style>
