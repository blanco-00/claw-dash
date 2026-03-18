<script setup lang="ts">
import { ref } from 'vue'

const agents = ref([
  { id: 'main', name: '瑾儿', title: '皇后', status: 'online' },
  { id: 'jishu', name: '青岚', title: '丫鬟', status: 'online' }
])
</script>

<template>
  <div>
    <h2 class="text-2xl font-bold mb-6">👩‍💼 Agent管理</h2>
    
    <el-table :data="agents" stripe>
      <el-table-column prop="id" label="ID" width="150" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="title" label="封号" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 'online' ? 'success' : 'info'">
            {{ row.status === 'online' ? '在线' : '离线' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <el-button type="primary" size="small">详情</el-button>
      </el-table-column>
    </el-table>
  </div>
</template>
