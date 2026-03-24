<template>
  <div class="docker">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic :title="t('docker.stats.runningContainers')" :value="stats.containersRunning">
            <template #prefix>
              <el-icon><Box /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic :title="t('docker.stats.totalContainers')" :value="stats.containersTotal" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic :title="t('docker.stats.images')" :value="stats.images" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic :title="t('docker.stats.volumes')" :value="stats.volumes" />
        </el-card>
      </el-col>
    </el-row>

    <el-card class="status-card" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>{{ t('docker.status.title') }}</span>
          <el-button type="primary" @click="refreshStatus">{{ t('docker.refresh') }}</el-button>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item :label="t('docker.status.connectionStatus')">
          <el-tag :type="status.connected ? 'success' : 'danger'">
            {{ status.connected ? t('docker.status.connected') : t('docker.status.disconnected') }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="t('docker.status.lastUpdated')">{{ status.timestamp }}</el-descriptions-item>
        <el-descriptions-item :label="t('docker.status.message')" :span="2">{{ status.message }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="containers-card" style="margin-top: 20px">
      <template #header>
        <span>{{ t('docker.containers.title') }}</span>
      </template>

      <el-table :data="containers" style="width: 100%">
        <el-table-column prop="name" :label="t('docker.containers.name')" />
        <el-table-column prop="image" :label="t('docker.containers.image')" />
        <el-table-column prop="ports" :label="t('docker.containers.ports')" />
        <el-table-column prop="status" :label="t('docker.containers.status')">
          <template #default="{ row }">
            <el-tag :type="row.status === 'running' ? 'success' : 'info'">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="created" :label="t('docker.containers.created')" />
      </el-table>
    </el-card>

    <el-card class="images-card" style="margin-top: 20px">
      <template #header>
        <span>{{ t('docker.images.title') }}</span>
      </template>

      <el-table :data="images" style="width: 100%">
        <el-table-column prop="id" :label="t('docker.images.id')" />
        <el-table-column prop="size" :label="t('docker.images.size')" />
        <el-table-column prop="created" :label="t('docker.images.created')" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { getDockerStatus, getContainers, getImages, getDockerStats } from '@/api/docker'

const { t } = useI18n()

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
    status.value = statusRes.data || statusRes
    containers.value = (containersRes.data || containersRes) || []
    images.value = (imagesRes.data || imagesRes) || []
    stats.value = statsRes.data || statsRes
  } catch (e) {
    ElMessage.error(t('docker.message.fetchFailed'))
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
