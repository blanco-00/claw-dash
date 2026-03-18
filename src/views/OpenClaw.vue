<template>
  <div class="openclaw">
    <el-card class="status-card">
      <template #header>
        <div class="card-header">
          <span>OpenClaw 状态</span>
          <el-button type="primary" @click="refreshStatus">刷新</el-button>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="运行状态">
          <el-tag :type="status.running ? 'success' : 'danger'">
            {{ status.running ? '运行中' : '未运行' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="API地址">{{ status.apiUrl }}</el-descriptions-item>
        <el-descriptions-item label="最后更新">{{ status.timestamp }}</el-descriptions-item>
        <el-descriptions-item label="错误信息">{{ status.error || '-' }}</el-descriptions-item>
      </el-descriptions>

      <div class="actions">
        <el-button type="success" @click="handleInstall">一键安装</el-button>
        <el-button type="danger" @click="handleUninstall">卸载</el-button>
      </div>
    </el-card>

    <el-card class="plugins-card">
      <template #header>
        <span>插件管理</span>
      </template>

      <el-table :data="pluginList" style="width: 100%">
        <el-table-column prop="name" label="插件名称" />
        <el-table-column prop="enabled" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">
              {{ row.enabled ? '已启用' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button size="small" @click="handleTogglePlugin(row.name)">
              {{ row.enabled ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getOpenClawStatus,
  installOpenClaw,
  uninstallOpenClaw,
  getOpenClawPlugins,
  togglePlugin
} from '@/api/openclaw'

const status = ref({
  running: false,
  apiUrl: '',
  timestamp: ''
})

const pluginList = ref<{ name: string; enabled: boolean }[]>([])

const refreshStatus = async () => {
  try {
    const res = await getOpenClawStatus()
    status.value = res.data
  } catch (e) {
    ElMessage.error('获取状态失败')
  }
}

const loadPlugins = async () => {
  try {
    const res = await getOpenClawPlugins()
    pluginList.value = res.data.available.map((name: string) => ({
      name,
      enabled: res.data.enabled.includes(name)
    }))
  } catch (e) {
    ElMessage.error('获取插件列表失败')
  }
}

const handleInstall = async () => {
  try {
    await installOpenClaw()
    ElMessage.success('安装成功')
    refreshStatus()
  } catch (e) {
    ElMessage.error('安装失败')
  }
}

const handleUninstall = async () => {
  try {
    await uninstallOpenClaw()
    ElMessage.success('卸载成功')
    refreshStatus()
  } catch (e) {
    ElMessage.error('卸载失败')
  }
}

const handleTogglePlugin = async (name: string) => {
  try {
    await togglePlugin(name)
    ElMessage.success('操作成功')
    loadPlugins()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  refreshStatus()
  loadPlugins()
})
</script>

<style scoped>
.openclaw {
  padding: 20px;
}

.status-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}
</style>
