<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { ElDrawer, ElIcon, ElMessage } from 'element-plus'
import { Document, Folder, Close, Edit, Check, Refresh } from '@element-plus/icons-vue'
import axios from 'axios'

const FILE_DESCRIPTIONS: Record<string, { desc: string; tips: string }> = {
  'IDENTITY.md': {
    desc: 'Agent 身份设定 - 你叫什么名字、什么角色',
    tips: '用于定义 Agent 的基本身份，影响对外展示的名称和角色定位。例如：名称、封号/官职、你是什么类型的角色(销售/客服/助理等)、语言风格 Emoji'
  },
  'SOUL.md': {
    desc: 'Agent 灵魂/性格 - 怎么说话、怎么做事',
    tips: '定义 Agent 的沟通风格和行为准则。例如：说话语气(亲切/正式/幽默)、服务范围、禁止行为、特殊注意事项'
  },
  'MEMORY.md': {
    desc: 'Agent 记忆 - 记住什么、记多久',
    tips: '配置 Agent 的记忆管理方式。例如：长期记忆内容、短期记忆策略、记忆召回优先级、上下文窗口大小'
  },
  'TOOLS.md': {
    desc: 'Agent 工具 - 能调用什么能力',
    tips: '声明 Agent 可以使用的工具和接口。例如：搜索、计算、代码执行、API 调用等能力，以及每个工具的使用权限'
  },
  'AGENTS.md': {
    desc: 'Agent 关联 - 和其他 Agent 怎么协作',
    tips: '配置多 Agent 协作关系(A2A协议)。例如：指定上级 Agent、下属 Agent、同级协作 Agent，以及任务交接规则'
  },
  'BOOTSTRAP.md': {
    desc: '启动配置 - 初始化时做什么',
    tips: 'Agent 启动时的初始化流程。例如：欢迎语、自我介绍、初始状态设置、首次运行任务'
  },
  'HEARTBEAT.md': {
    desc: '心跳配置 - 怎么证明你还活着',
    tips: '健康检查和保活机制。例如：心跳间隔时间、超时判定、离线处理策略、自动恢复流程'
  },
  'USER.md': {
    desc: '用户信息 - 了解当前用户',
    tips: '记录用户信息用于个性化服务。例如：用户名、历史偏好、常用设置、上下文变量、session 数据'
  }
}

const props = defineProps<{
  visible: boolean
  agentName: string
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  'delete': [agentName: string]
}>()

const files = ref<Array<{name: string, path: string, size: number, modified: number}>>([])
const selectedFile = ref<string | null>(null)
const fileContent = ref('')
const editingContent = ref('')
const isEditing = ref(false)
const loading = ref(false)
const saving = ref(false)
const hasChanges = ref(false)

const drawerVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const currentFileInfo = computed(() => {
  if (!selectedFile.value) return null
  return FILE_DESCRIPTIONS[selectedFile.value] || { desc: '', tips: '' }
})

watch(() => props.visible, async (val) => {
  if (val && props.agentName) {
    isEditing.value = false
    hasChanges.value = false
    await loadFiles()
    if (files.value.length > 0 && !selectedFile.value) {
      selectFile(files.value[0].name)
    }
  }
})

watch(() => props.agentName, async (val) => {
  if (val && drawerVisible.value) {
    isEditing.value = false
    hasChanges.value = false
    await loadFiles()
    if (files.value.length > 0) {
      selectFile(files.value[0].name)
    }
  }
})

watch(fileContent, (val) => {
  if (isEditing.value) {
    hasChanges.value = val !== editingContent.value
  }
})

async function loadFiles() {
  loading.value = true
  selectedFile.value = null
  fileContent.value = ''
  editingContent.value = ''
  try {
    const res = await axios.get(`/api/openclaw/agents/${encodeURIComponent(props.agentName)}/files`)
    files.value = res.data.data || []
  } catch (err) {
    console.error('Failed to load files:', err)
    files.value = []
  } finally {
    loading.value = false
  }
}

async function selectFile(filename: string) {
  if (hasChanges.value && !confirm('有未保存的修改，确定要切换文件吗？')) {
    return
  }
  
  selectedFile.value = filename
  loading.value = true
  isEditing.value = false
  hasChanges.value = false
  try {
    const res = await axios.get(`/api/openclaw/agents/${encodeURIComponent(props.agentName)}/files/${encodeURIComponent(filename)}`)
    fileContent.value = res.data.data?.content || ''
    editingContent.value = fileContent.value
  } catch (err) {
    console.error('Failed to load file:', err)
    fileContent.value = ''
    editingContent.value = ''
  } finally {
    loading.value = false
  }
}

function startEdit() {
  editingContent.value = fileContent.value
  isEditing.value = true
  hasChanges.value = false
}

function cancelEdit() {
  editingContent.value = fileContent.value
  isEditing.value = false
  hasChanges.value = false
}

async function saveContent() {
  if (!selectedFile.value) return
  
  saving.value = true
  try {
    await axios.patch(
      `/api/openclaw/agents/${encodeURIComponent(props.agentName)}/files/${encodeURIComponent(selectedFile.value)}`,
      { content: editingContent.value }
    )
    fileContent.value = editingContent.value
    isEditing.value = false
    hasChanges.value = false
    ElMessage.success('保存成功')
  } catch (err: any) {
    ElMessage.error(err?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function getFileIcon(filename: string) {
  if (filename.endsWith('.md')) return Document
  return Folder
}

function formatSize(bytes: number) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

async function handleDelete() {
  if (agentName === 'main') {
    ElMessage.warning('main 是主 Agent，不能被删除')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除 Agent "${agentName}" 吗？\n\n删除后将无法恢复，包括工作区文件！`,
      '⚠️ 确认删除',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    emit('delete', agentName)
    emit('update:visible', false)
  } catch {
    ElMessage.info('已取消删除')
  }
}
</script>

<template>
  <el-drawer
    v-model="drawerVisible"
    :title="agentName"
    size="80%"
    direction="rtl"
    :before-close="() => emit('update:visible', false)"
  >
    <div class="detail-panel">
      <div class="file-sidebar">
        <div class="sidebar-header">
          <span>配置文件</span>
          <a :href="'http://localhost:18789'" target="_blank" class="open-btn">打开 →</a>
        </div>
        <div class="file-list">
          <div
            v-for="file in files"
            :key="file.name"
            class="file-item"
            :class="{ active: selectedFile === file.name }"
            @click="selectFile(file.name)"
          >
            <el-icon><component :is="getFileIcon(file.name)" /></el-icon>
            <div class="file-info">
              <span class="file-name">{{ file.name }}</span>
              <span v-if="FILE_DESCRIPTIONS[file.name]" class="file-desc">
                {{ FILE_DESCRIPTIONS[file.name].desc }}
              </span>
            </div>
          </div>
          <div v-if="files.length === 0 && !loading" class="empty">
            No files found
          </div>
        </div>
        
        <div class="file-sidebar-footer">
          <el-button 
            v-if="agentName !== 'main'" 
            type="danger" 
            size="small" 
            style="width: 100%"
            @click="handleDelete"
          >
            删除此 Agent
          </el-button>
          <el-tag v-else type="warning" size="small" style="width: 100%; text-align: center">主 Agent 不可删除</el-tag>
        </div>
      </div>
      
      <div class="file-content">
        <div v-if="selectedFile" class="content-header">
          <div class="content-title">
            <span class="content-filename">{{ selectedFile }}</span>
            <span v-if="currentFileInfo?.desc" class="content-desc">{{ currentFileInfo.desc }}</span>
          </div>
          <div class="content-actions">
            <template v-if="isEditing">
              <el-button type="primary" size="small" :loading="saving" @click="saveContent">
                <el-icon><Check /></el-icon>
                保存
              </el-button>
              <el-button size="small" @click="cancelEdit">取消</el-button>
            </template>
            <template v-else>
              <el-button size="small" @click="startEdit">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
            </template>
          </div>
        </div>
        
        <div v-if="currentFileInfo?.tips" class="content-tips">
          <strong>💡 提示：</strong>{{ currentFileInfo.tips }}
        </div>
        
        <div v-if="loading" class="loading">Loading...</div>
        <template v-else-if="selectedFile">
          <textarea
            v-if="isEditing"
            v-model="editingContent"
            class="content-editor"
            spellcheck="false"
          ></textarea>
          <pre v-else class="content-pre">{{ fileContent }}</pre>
        </template>
      </div>
    </div>
  </el-drawer>
</template>

<style scoped>
.detail-panel {
  display: flex;
  height: 100%;
  margin: -20px;
}

.file-sidebar {
  width: 280px;
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  background: var(--bg-secondary);
}

.sidebar-header {
  padding: 12px 16px;
  font-weight: 600;
  font-size: 13px;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-header .open-btn {
  font-size: 12px;
  font-weight: 500;
  color: var(--color-primary, #409eff);
  text-decoration: none;
}

.sidebar-header .open-btn:hover {
  text-decoration: underline;
}

.file-list {
  flex: 1;
  overflow-y: auto;
}

.file-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 16px;
  cursor: pointer;
  transition: background 0.2s;
}

.file-item:hover {
  background: var(--bg-hover);
}

.file-item.active {
  background: var(--bg-active, #e8f4ff);
  color: var(--color-primary, #409eff);
}

.file-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow: hidden;
}

.file-name {
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-desc {
  font-size: 11px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.content-header {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-secondary);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.content-title {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.content-filename {
  font-weight: 600;
  font-size: 14px;
}

.content-desc {
  font-size: 12px;
  color: var(--text-secondary);
}

.content-actions {
  display: flex;
  gap: 8px;
}

.content-tips {
  padding: 10px 16px;
  background: #fffbeb;
  border-bottom: 1px solid #fcd34d;
  font-size: 12px;
  color: #92400e;
}

.loading {
  padding: 20px;
  text-align: center;
  color: var(--text-secondary);
}

.content-editor {
  flex: 1;
  margin: 0;
  padding: 16px;
  border: none;
  resize: none;
  font-size: 13px;
  line-height: 1.6;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  background: var(--bg-card);
}

.content-editor:focus {
  outline: none;
}

.content-pre {
  flex: 1;
  margin: 0;
  padding: 16px;
  overflow: auto;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

.empty {
  padding: 20px;
  text-align: center;
  color: var(--text-secondary);
  font-size: 13px;
}

.file-sidebar-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--border-color);
  background: var(--bg-secondary);
}
</style>
