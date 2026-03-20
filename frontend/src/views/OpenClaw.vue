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
        <el-descriptions-item label="最后更新">{{ displayTime }}</el-descriptions-item>
        <el-descriptions-item label="错误信息">{{ status.error || '-' }}</el-descriptions-item>
      </el-descriptions>

      <div class="actions">
        <el-button type="success" @click="handleAutoConnect">一键对接</el-button>
        <el-button @click="showConfigDialog = true">配置</el-button>
      </div>

    <el-dialog v-model="showConfigDialog" title="OpenClaw 配置" width="400px">
      <el-form label-width="100px">
        <el-form-item label="配置路径">
          <el-input v-model="configPath" placeholder="~/.openclaw" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showConfigDialog = false">取消</el-button>
        <el-button type="primary" @click="saveConfigPath">保存</el-button>
      </template>
    </el-dialog>
    </el-card>

    <el-card class="plugins-card">
      <template #header>
        <span>插件管理</span>
      </template>

      <el-table :data="pluginList" style="width: 100%">
        <el-table-column prop="name" label="插件名称" />
        <el-table-column label="状态">
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

    <el-dialog v-model="detectDialogVisible" title="OpenClaw 自动检测结果" width="500px">
      <div v-if="detectResult">
        <el-alert :type="detectResult.running ? 'success' : 'warning'" :title="detectResult.running ? 'OpenClaw 运行中' : 'OpenClaw 未运行'" :closable="false" style="margin-bottom: 20px" />
        
        <el-descriptions :column="1" border>
          <el-descriptions-item label="API地址">{{ detectResult.apiUrl }}</el-descriptions-item>
          <el-descriptions-item v-if="detectResult.error" label="状态">{{ detectResult.error }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="detectResult.running && detectResult.plugins" style="margin-top: 20px">
          <h4>已启用插件</h4>
          <el-tag v-for="(info, name) in detectResult.plugins" :key="name" :type="info.enabled ? 'success' : 'info'" style="margin-right: 8px; margin-bottom: 8px">
            {{ name }} {{ info.enabled ? '✅' : '❌' }}
          </el-tag>
        </div>

        <div v-if="detectResult.running && detectResult.workspaces && detectResult.workspaces.length" style="margin-top: 20px">
          <h4>工作空间</h4>
          <el-tag v-for="ws in detectResult.workspaces" :key="ws" style="margin-right: 8px; margin-bottom: 8px">
            {{ ws }}
          </el-tag>
        </div>
      </div>
      <template #footer>
        <el-button @click="detectDialogVisible = false">取消</el-button>
        <el-button v-if="detectResult?.running" type="primary" @click="handleConfirmConnect">确认对接</el-button>
        <el-button v-else type="primary" @click="detectDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import {
  getOpenClawStatus,
  installOpenClaw,
  uninstallOpenClaw,
  getOpenClawPlugins,
  togglePlugin,
  autoDetectOpenClaw,
  confirmConnect,
  type OpenClawStatus,
  type AutoDetectResult
} from '@/api/openclaw'

const { locale } = useI18n()

const formatTime = (timestamp: string) => {
  if (!timestamp) return '-'
  return new Date(timestamp).toLocaleString(locale.value === 'zh' ? 'zh-CN' : 'en-US')
}

const displayTime = computed(() => formatTime(status.value.timestamp))

const status = ref<OpenClawStatus>({
  running: false,
  apiUrl: '',
  timestamp: '',
  error: ''
})

const pluginList = ref<{ name: string; enabled: boolean }[]>([])
const detectDialogVisible = ref(false)
const detectResult = ref<AutoDetectResult | null>(null)
const showConfigDialog = ref(false)
const configPath = ref(localStorage.getItem('openclawConfigPath') || '~/.openclaw')

const refreshStatus = async () => {
  try {
    const res = await getOpenClawStatus() as any
    if (res.code === 200 && res.data) {
      status.value.running = res.data.running
      status.value.apiUrl = res.data.apiUrl
      status.value.timestamp = res.data.timestamp
      status.value.error = res.data.error
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '获取状态失败')
  }
}

const loadPlugins = async () => {
  try {
    const res = await getOpenClawPlugins() as any
    if (res.code === 200 && res.data) {
      pluginList.value = (res.data.available || []).map((name: string) => ({
        name,
        enabled: (res.data.enabled || []).includes(name)
      }))
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '获取插件列表失败')
  }
}

const handleAutoConnect = async () => {
  try {
    const res = await autoDetectOpenClaw(configPath.value) as any
    if (res.code === 200 && res.data) {
      detectResult.value = res.data as AutoDetectResult
      detectDialogVisible.value = true
    } else {
      ElMessage.error(res.message || '检测失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '检测失败')
  }
}

const handleConfirmConnect = async () => {
  if (!detectResult.value) return
  try {
    const res = await confirmConnect(detectResult.value.apiUrl, detectResult.value.token || '', configPath.value) as any
    if (res.code === 200) {
      ElMessage.success('对接成功')
      detectDialogVisible.value = false
      refreshStatus()
    } else {
      ElMessage.error(res.message || '对接失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '对接失败')
  }
}

const saveConfigPath = () => {
  localStorage.setItem('openclawConfigPath', configPath.value)
  showConfigDialog.value = false
  ElMessage.success('配置已保存')
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
