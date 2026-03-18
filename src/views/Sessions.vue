<script setup lang="ts">
const sessions = ref([
  { id: 'abc123', agent: '瑾儿', messages: 28, lastActive: '2026-03-18 14:25:00' }
])
</script>

<template>
  <div>
    <h2 class="text-2xl font-bold mb-6">💬 会话管理</h2>
    <el-table :data="sessions" stripe>
      <el-table-column prop="id" label="会话ID" width="150" />
      <el-table-column prop="agent" label="Agent" />
      <el-table-column prop="messages" label="消息数" />
      <el-table-column prop="lastActive" label="最后活跃" />
    </el-table>
  </div>
</template>

<script lang="ts">
import { ref } from 'vue'
export default { setup() { return { sessions: ref([]) } } }
</script>
