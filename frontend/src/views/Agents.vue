<template>
  <div class="agents-page">
    <div class="header">
      <h2>Agent 管理</h2>
    </div>

    <el-table :data="agents" style="width: 100%">
      <el-table-column prop="agentId" label="Agent ID" width="180" />
      <el-table-column prop="name" label="名称" width="150" />
      <el-table-column prop="role" label="角色" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastActiveAt" label="最后活跃" width="180" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button size="small" @click="viewAgent(row)">详情</el-button>
          <el-button size="small" type="danger" @click="deleteAgent(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const agents = ref([])

const loadAgents = async () => {
  try {
    const res = await request.get('/agents')
    agents.value = res.data
  } catch (error) {
    console.error('Failed to load agents:', error)
  }
}

const viewAgent = (row: any) => {
  console.log('View agent:', row)
}

const deleteAgent = async (row: any) => {
  console.log('Delete agent:', row)
}

onMounted(() => {
  loadAgents()
})
</script>

<style scoped>
.agents-page {
  padding: 20px;
}
.header {
  margin-bottom: 20px;
}
</style>
