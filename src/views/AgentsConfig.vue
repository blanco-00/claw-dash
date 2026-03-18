<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getAgentList, getAgentDetail } from '@/api/agents'
import type { AgentInfo } from '@/types/agent'

// 女儿国Agent配置模板
const AGENT_TEMPLATES = [
  {
    id: 'main',
    name: '瑾儿',
    title: '皇后',
    role: '中书省决策',
    description: '女儿国主Agent，负责统筹决策'
  },
  {
    id: 'menxiasheng',
    name: '卿酒',
    title: '皇贵妃',
    role: '门下省审核',
    description: '负责审核和把控内容质量'
  },
  {
    id: 'shangshusheng',
    name: '红袖',
    title: '贵妃',
    role: '尚书省分发',
    description: '负责任务分发和调度'
  },
  {
    id: 'jinyiwei',
    name: '灵鸢',
    title: '贵人',
    role: '锦衣卫督查',
    description: '负责监督和督查工作'
  },
  { id: 'libu4', name: '珊瑚', title: '妃', role: '吏部人事', description: '负责人事管理和调配' },
  { id: 'hubu', name: '琉璃', title: '妃', role: '户部财务', description: '负责财务和预算管理' },
  { id: 'libu3', name: '书瑶', title: '妃', role: '礼部外交', description: '负责对外交流和外交' },
  { id: 'bingbu', name: '魅羽', title: '妃', role: '兵部安全', description: '负责安全保障工作' },
  { id: 'xingbu', name: '如意', title: '嫔', role: '刑部法务', description: '负责法务和合规' },
  { id: 'gongbu', name: '灵犀', title: '嫔', role: '工部技术', description: '负责技术研发管理' },
  { id: 'jishu', name: '青岚', title: '丫鬟', role: '工部研发', description: '负责技术研发实现' },
  {
    id: 'shangshiju',
    name: '婉儿',
    title: '丫鬟',
    role: '尚食局',
    description: '负责饮食起居安排'
  },
  { id: 'shangyaosi', name: '允贤', title: '丫鬟', role: '尚药司', description: '负责健康医疗管理' }
]

const loading = ref(true)
const agents = ref<AgentInfo[]>([])
const selectedAgent = ref<AgentInfo | null>(null)
const editing = ref(false)
const editForm = ref({ name: '', title: '', role: '' })

async function refresh() {
  loading.value = true
  try {
    const list = await getAgentList()
    const details = await Promise.all(list.map((a: any) => getAgentDetail(a.id)))
    agents.value = details.filter(Boolean) as AgentInfo[]
  } catch (error) {
    console.error('获取Agent列表失败:', error)
  } finally {
    loading.value = false
  }
}

function openDetail(agent: AgentInfo) {
  selectedAgent.value = agent
  editForm.value = { name: agent.name, title: agent.title, role: agent.role }
  editing.value = false
}

function closeDetail() {
  selectedAgent.value = null
}

function startEdit() {
  editing.value = true
}

function cancelEdit() {
  editing.value = false
  if (selectedAgent.value) {
    editForm.value = {
      name: selectedAgent.value.name,
      title: selectedAgent.value.title,
      role: selectedAgent.value.role
    }
  }
}

function saveEdit() {
  if (selectedAgent.value) {
    selectedAgent.value.name = editForm.value.name
    selectedAgent.value.title = editForm.value.title
    selectedAgent.value.role = editForm.value.role
  }
  editing.value = false
}

function getTemplate(id: string) {
  return AGENT_TEMPLATES.find(t => t.id === id)
}

onMounted(() => {
  refresh()
})
</script>

<template>
  <div class="agents-config-page">
    <!-- 页面头部 -->
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold">🏯 女儿国集成</h2>
      <el-button type="primary" :loading="loading" @click="refresh">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- Agent配置列表 -->
    <el-row :gutter="20">
      <el-col v-for="agent in agents" :key="agent.id" :span="6" class="mb-4">
        <el-card
          shadow="hover"
          class="agent-card cursor-pointer"
          :class="{ 'ring-2 ring-pink-500': selectedAgent?.id === agent.id }"
          @click="openDetail(agent)"
        >
          <div class="text-center">
            <div class="text-4xl mb-2">
              {{
                ['👸', '👩‍🎤', '👩‍💼', '🕵️‍♀️', '👩‍🔬', '💰', '🎭', '🛡️', '⚖️', '🔧', '💻', '🍜', '💊'][
                  agents.indexOf(agent) % 13
                ]
              }}
            </div>
            <div class="font-bold text-lg">{{ agent.name }}</div>
            <div class="text-pink-500">{{ agent.title }}</div>
            <div class="text-gray-500 text-sm mt-1">{{ agent.role }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Agent详情配置抽屉 -->
    <el-drawer
      v-model="selectedAgent"
      :title="selectedAgent?.name + ' - 配置'"
      direction="rtl"
      size="500px"
      @close="closeDetail"
    >
      <div v-if="selectedAgent" class="space-y-4">
        <!-- 头像 -->
        <div class="text-center py-4 border-b">
          <el-avatar :size="80" class="text-4xl">
            {{
              ['👸', '👩‍🎤', '👩‍💼', '🕵️‍♀️', '👩‍🔬', '💰', '🎭', '🛡️', '⚖️', '🔧', '💻', '🍜', '💊'][
                agents.indexOf(selectedAgent) % 13
              ]
            }}
          </el-avatar>
        </div>

        <!-- 配置表单 -->
        <el-form v-if="editing" :model="editForm" label-width="80px">
          <el-form-item label="名称">
            <el-input v-model="editForm.name" />
          </el-form-item>
          <el-form-item label="封号">
            <el-input v-model="editForm.title" />
          </el-form-item>
          <el-form-item label="职责">
            <el-input v-model="editForm.role" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveEdit">保存</el-button>
            <el-button @click="cancelEdit">取消</el-button>
          </el-form-item>
        </el-form>

        <!-- 详情展示 -->
        <div v-else class="space-y-4">
          <div class="flex justify-between items-center">
            <span class="text-gray-500">ID</span>
            <span class="font-mono">{{ selectedAgent.id }}</span>
          </div>
          <div class="flex justify-between items-center">
            <span class="text-gray-500">名称</span>
            <span>{{ selectedAgent.name }}</span>
          </div>
          <div class="flex justify-between items-center">
            <span class="text-gray-500">封号</span>
            <el-tag type="pink">{{ selectedAgent.title }}</el-tag>
          </div>
          <div class="flex justify-between items-center">
            <span class="text-gray-500">职责</span>
            <span>{{ selectedAgent.role }}</span>
          </div>
          <div class="flex justify-between items-center">
            <span class="text-gray-500">状态</span>
            <el-tag :type="selectedAgent.status === 'online' ? 'success' : 'info'">
              {{ selectedAgent.status }}
            </el-tag>
          </div>
          <div v-if="selectedAgent.workspace" class="flex justify-between items-center">
            <span class="text-gray-500">工作区</span>
            <span class="text-sm font-mono truncate" style="max-width: 200px">{{
              selectedAgent.workspace
            }}</span>
          </div>

          <!-- 文件大小 -->
          <div v-if="selectedAgent.memory" class="pt-4 border-t">
            <div class="font-bold mb-2">文件大小</div>
            <div class="space-y-1 text-sm">
              <div class="flex justify-between">
                <span class="text-gray-500">SOUL.md</span>
                <span>{{
                  selectedAgent.memory.soul
                    ? (selectedAgent.memory.soul / 1024).toFixed(1) + ' KB'
                    : '-'
                }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-500">MEMORY.md</span>
                <span>{{
                  selectedAgent.memory.memory
                    ? (selectedAgent.memory.memory / 1024).toFixed(1) + ' KB'
                    : '-'
                }}</span>
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="pt-4 border-t flex gap-2">
            <el-button type="primary" @click="startEdit">编辑配置</el-button>
            <el-button type="success" plain>查看SOUL</el-button>
            <el-button type="info" plain>查看MEMORY</el-button>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script lang="ts">
import { Refresh } from '@element-plus/icons-vue'
export default { components: { Refresh } }
</script>

<style scoped>
.agents-config-page {
  padding: 20px;
}
.agent-card {
  transition: transform 0.2s;
}
.agent-card:hover {
  transform: translateY(-2px);
}
.cursor-pointer {
  cursor: pointer;
}
</style>
