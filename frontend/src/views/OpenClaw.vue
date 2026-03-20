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
        <el-descriptions-item label="Dashboard">
          <a v-if="status.apiUrl" :href="status.apiUrl" target="_blank" class="api-link">打开 →</a>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="Token">
          <span v-if="status.token" class="token">{{ status.token }}</span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="最后更新">{{ displayTime }}</el-descriptions-item>
        <el-descriptions-item label="错误信息">{{ status.error || '-' }}</el-descriptions-item>
      </el-descriptions>

      <div class="actions">
        <el-button 
          :type="detectStatus === 'success' ? 'success' : detectStatus === 'failed' ? 'danger' : 'default'"
          :loading="detectStatus === 'loading'"
          @click="handleDetect"
        >
          <el-icon v-if="detectStatus === 'success'" class="el-icon--left"><Check /></el-icon>
          {{ detectButtonText }}
        </el-button>
        <el-button @click="showConfigDialog = true">配置</el-button>
      </div>

    <el-dialog v-model="showConfigDialog" title="OpenClaw 配置" width="400px">
      <el-form label-width="100px">
        <el-form-item label="配置路径">
          <el-input v-model="configPath" placeholder="~/.openclaw" @change="handleConfigPathChange" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showConfigDialog = false">关闭</el-button>
      </template>
    </el-dialog>
    </el-card>

    <el-card class="mcp-card">
      <template #header>
        <div class="card-header">
          <span>ClawDash 接入方式</span>
          <el-button type="primary" size="small" @click="showIntegrationDialog = true">
            切换方式
          </el-button>
        </div>
      </template>

      <el-alert
        v-if="integrationType === 'skill'"
        type="success"
        :closable="false"
      >
        <template #title>
          <span>Skill + REST API（当前使用）</span>
        </template>
        OpenClaw 可通过 curl 调用 ClawDash REST API 管理任务。
      </el-alert>

      <el-alert
        v-else
        type="warning"
        :closable="false"
      >
        <template #title>
          <span>MCP Server（等待 OpenClaw 支持）</span>
        </template>
        请关注 <a href="https://github.com/openclaw/openclaw/issues/43509" target="_blank">Issue #43509</a>
      </el-alert>
    </el-card>

    <!-- 接入方式选择对话框 -->
    <el-dialog v-model="showIntegrationDialog" title="切换接入方式" width="700px">
      <el-alert type="info" :closable="false" style="margin-bottom: 20px">
        两种方式都能让 OpenClaw 访问 ClawDash 任务队列。切换时会自动清理上一种方式。
      </el-alert>

      <el-table :data="integrationOptions" style="width: 100%">
        <el-table-column prop="name" label="接入方式" width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.statusType" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button
              v-if="row.type === integrationType"
              type="success"
              size="small"
              disabled
            >
              当前使用
            </el-button>
            <el-button
              v-else
              type="primary"
              size="small"
              :disabled="row.status === '建设中'"
              @click="handleIntegrate(row.type)"
            >
              {{ row.status === '建设中' ? '等待' : '切换' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-divider />

      <h4>方式对比</h4>
      <el-table :data="comparisonData" style="width: 100%" size="small">
        <el-table-column prop="feature" label="特性" width="150" />
        <el-table-column prop="skill" label="Skill + REST API" />
        <el-table-column prop="mcp" label="MCP Server" />
      </el-table>

      <el-alert type="warning" :closable="false" style="margin-top: 20px">
        <template #title>
          <span>MCP Server 正在等待 OpenClaw 官方支持</span>
        </template>
        请关注 <a href="https://github.com/openclaw/openclaw/issues/43509" target="_blank">Issue #43509</a>
      </el-alert>

      <template #footer>
        <el-button @click="showIntegrationDialog = false">关闭</el-button>
      </template>
    </el-dialog>

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
import { ref, onMounted, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import {
  getOpenClawStatus,
  installOpenClaw,
  uninstallOpenClaw,
  getOpenClawPlugins,
  togglePlugin,
  autoDetectOpenClaw,
  confirmConnect,
  installClawdashSkill,
  uninstallClawdashSkill,
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
const showIntegrationDialog = ref(false)
const integrationType = ref<'skill' | 'mcp'>('skill')
const detectStatus = ref<'idle' | 'loading' | 'success' | 'failed'>('idle')

const detectButtonText = computed(() => {
  switch (detectStatus.value) {
    case 'loading': return '检测中...'
    case 'success': return '已连接'
    case 'failed': return '检测失败，点击重试'
    default: return '检测连接'
  }
})

const integrationOptions = [
  {
    type: 'skill',
    name: 'Skill + REST API',
    status: '可用',
    statusType: 'success',
    description: '创建 ClawDash Skill，OpenClaw 通过 curl 调用 REST API'
  },
  {
    type: 'mcp',
    name: 'MCP Server',
    status: '建设中',
    statusType: 'warning',
    description: '通过 MCP 协议连接，标准化但需要 OpenClaw 支持'
  }
]

const comparisonData = [
  { feature: '配置复杂度', skill: '简单', mcp: '需配置 mcpServers' },
  { feature: 'Token 消耗', skill: '较高 (每次 API 调用)', mcp: '较低 (工具发现一次)' },
  { feature: '标准化程度', skill: '非标准', mcp: 'MCP 协议标准' },
  { feature: '工具发现', skill: '无 (需看 Skill 文档)', mcp: '自动发现所有工具' },
  { feature: '稳定性', skill: '✅ 稳定', mcp: '⏳ 等待 OpenClaw 支持' }
]

const handleIntegrate = async (type: string) => {
  if (type === 'skill') {
    if (integrationType.value === 'mcp') {
      ElMessage.info('将切换到 Skill + REST API 方式')
    }
    await handleSkillIntegration()
  } else if (type === 'mcp') {
    ElMessage.info('MCP Server 接入方式正在等待 OpenClaw 更新支持')
  }
}

const handleSkillIntegration = async () => {
  try {
    const backendUrl = status.value.apiUrl 
      ? status.value.apiUrl.replace('3000', '5178')
      : 'http://localhost:5178'
    
    const res = await installClawdashSkill(`${backendUrl}`) as any
    if (res.code === 200) {
      ElMessage.success('Skill + REST API 配置成功！')
      showIntegrationDialog.value = false
      integrationType.value = 'skill'
    } else {
      ElMessage.error(res.message || '配置失败')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '配置失败')
  }
}

const refreshStatus = async () => {
  try {
    const res = await getOpenClawStatus() as any
    if (res.code === 200 && res.data) {
      status.value.running = res.data.running
      status.value.apiUrl = res.data.apiUrl
      status.value.token = res.data.token
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

const handleDetect = async () => {
  detectStatus.value = 'loading'
  try {
    const res = await autoDetectOpenClaw(configPath.value) as any
    if (res.code === 200 && res.data) {
      detectResult.value = res.data as AutoDetectResult
      
      if (res.data.running) {
        // Auto-save on success
        localStorage.setItem('openclawConfigPath', configPath.value)
        const confirmRes = await confirmConnect(res.data.apiUrl, res.data.token || '', configPath.value) as any
        if (confirmRes.code === 200) {
          detectStatus.value = 'success'
          refreshStatus()
        } else {
          detectStatus.value = 'failed'
        }
      } else {
        detectStatus.value = 'failed'
        ElMessage.warning('OpenClaw 未运行，请启动后再试')
      }
    } else {
      detectStatus.value = 'failed'
      ElMessage.error(res.message || '检测失败')
    }
  } catch (e: any) {
    detectStatus.value = 'failed'
    ElMessage.error(e.message || '检测失败')
  }
}

const handleConfigPathChange = () => {
  localStorage.setItem('openclawConfigPath', configPath.value)
  handleDetect()
}

const handleConfirmConnect = async () => {
  if (!detectResult.value) return
  try {
    const res = await confirmConnect(detectResult.value.apiUrl, detectResult.value.token || '', configPath.value) as any
    if (res.code === 200) {
      ElMessage.success('对接成功')
      detectDialogVisible.value = false
      detectStatus.value = 'success'
      refreshStatus()
    } else {
      ElMessage.error(res.message || '对接失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '对接失败')
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
  handleDetect()
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

.mcp-card {
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

.api-link {
  color: var(--color-primary, #409eff);
  text-decoration: none;
}

.api-link:hover {
  text-decoration: underline;
}

.token {
  font-family: monospace;
  font-size: 12px;
  color: var(--text-secondary);
  word-break: break-all;
}
</style>
