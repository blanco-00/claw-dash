<script setup lang="ts">
const tasks = ref([
  { id: '1', type: 'jishu-dev', title: '开发新功能', status: 'PENDING', priority: 'high' }
])
</script>

<template>
  <div>
    <h2 class="text-2xl font-bold mb-6">📋 任务队列</h2>
    <el-table :data="tasks" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="type" label="类型" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="priority" label="优先级">
        <template #default="{ row }">
          <el-tag :type="row.priority === 'high' ? 'danger' : 'info'">
            {{ row.priority }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag>{{ row.status }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script lang="ts">
import { ref } from 'vue'
export default { setup() { return { tasks: ref([]) } } }
</script>
