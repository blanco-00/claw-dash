<template>
  <div class="docker">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="运行中容器" :value="stats.containersRunning">
            <template #prefix>
              <el-icon><Box /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="总容器数" :value="stats.containersTotal" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="镜像数" :value="stats.images" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="卷数" :value="stats.volumes" />
        </el-card>
      </el-col>
    </el-row>

    <el-card class="status-card" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>Docker 状态</span>
          <el-button type="primary" @click="refreshStatus">刷新</el-button>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="连接状态">
          <el-tag :type="status.connected ? 'success' : 'danger'">
            {{ status.connected ? '已连接' : '未连接' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="最后更新">{{ status.timestamp }}</el-descriptions-item>
        <el-descriptions-item label="消息" :span="2">{{ status.message }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="containers-card" style="margin-top: 20px">
      <template #header>
        <span>容器列表</span>
      </template>

      <el-table :data="containers" style="width: 100%">
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="image" label="镜像" />
        <el-table-column prop="ports" label="端口" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 'running' ? 'success' : 'info'">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="created" label="创建时间" />
      </el-table>
    </el-card>

    <el-card class="images-card" style="margin-top: 20px">
      <template #header>
        <span>镜像列表</span>
      </template>

      <el-table :data="images" style="width: 100%">
        <el-table-column prop="id" label="镜像ID" />
        <el-table-column prop="size" label="大小" />
        <el-table-column prop="created" label="创建时间" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getDockerStatus, getContainers, getImages, getDockerStats } from '@/api/docker'

const status = ref({ connected: false, message: '', timestamp: '' })
const containers = ref<any[]>([])
const images = ref<any[]>([])
const stats = ref({ containersRunning: 0, containersTotal: 0, images: 0, volumes: 0, networks: 0 })

const refreshStatus = async () => {
  try {
    const [statusRes, containersRes, imagesRes, statsRes] = await Promise.all([
      getDockerStatus(),
      getContainers(),
      getImages(),
      getDockerStats()
    ])
    status.value = statusRes.data
    containers.value = containersRes.data
    images.value = imagesRes.data
    stats.value = statsRes.data
  } catch (e) {
    ElMessage.error('获取Docker状态失败')
  }
}

onMounted(() => {
  refreshStatus()
})
</script>

<style scoped>
.docker {
  padding: 20px;
}

.stat-card {
  text-align: center;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
