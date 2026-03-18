<script setup lang="ts">
const cronTasks = ref([
  { id: '1', name: '健康检查', schedule: '*/30 * * * *', status: 'ok', nextRun: '2026-03-18 14:30:00' }
])
</script>

<template>
  <div>
    <h2 class="text-2xl font-bold mb-6">⏰ Cron任务</h2>
    <el-table :data="cronTasks" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="任务名称" />
      <el-table-column prop="schedule" label="调度表达式" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ok' ? 'success' : 'danger'">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="nextRun" label="下次执行" />
    </el-table>
  </div>
</template>

<script lang="ts">
import { ref } from 'vue'
export default { setup() { return { cronTasks: ref([]) } } }
</script>
