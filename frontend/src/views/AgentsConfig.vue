<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { API_BASE } from '@/api/config'

const { t } = useI18n()

const loading = ref(true)
const agents = ref<any[]>([])
const selectedAgent = ref<any | null>(null)
const editing = ref(false)
const editForm = ref({
  id: '',
  name: '',
  title: '',
  role: '',
  description: '',
  parent_id: '',
  is_active: true
})

async function refresh() {
  loading.value = true
  try {
    const response = await fetch(`${API_BASE}/api/agent-templates`)
    agents.value = await response.json()
  } catch (error) {
    console.error('Failed to load templates:', error)
    ElMessage.error(t('agentsConfig.message.fetchError'))
  } finally {
    loading.value = false
  }
}

function openDetail(agent: any) {
  selectedAgent.value = { ...agent }
  editForm.value = {
    id: agent.id,
    name: agent.name,
    title: agent.title,
    role: agent.role,
    description: agent.description || '',
    parent_id: agent.parent_id || '',
    is_active: agent.is_active ?? true
  }
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
      id: selectedAgent.value.id,
      name: selectedAgent.value.name,
      title: selectedAgent.value.title,
      role: selectedAgent.value.role,
      description: selectedAgent.value.description || '',
      parent_id: selectedAgent.value.parent_id || '',
      is_active: selectedAgent.value.is_active ?? true
    }
  }
}

async function saveEdit() {
  if (!selectedAgent.value) return

  try {
    const response = await fetch(`${API_BASE}/api/agent-templates/${editForm.value.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(editForm.value)
    })

    if (response.ok) {
      ElMessage.success(t('agentsConfig.message.saveSuccess'))
      // 更新本地数据
      const index = agents.value.findIndex(a => a.id === selectedAgent.value!.id)
      if (index !== -1) {
        agents.value[index] = { ...editForm.value }
      }
      selectedAgent.value = { ...editForm.value }
      editing.value = false
    } else {
      ElMessage.error(t('agentsConfig.message.saveFailed'))
    }
  } catch (error) {
    console.error('Save failed:', error)
    ElMessage.error(t('agentsConfig.message.saveFailed'))
  }
}

async function deleteAgent() {
  if (!selectedAgent.value) return

  try {
    const response = await fetch(`${API_BASE}/api/agent-templates/${selectedAgent.value.id}`, {
      method: 'DELETE'
    })

    if (response.ok) {
      ElMessage.success(t('agentsConfig.message.deleteSuccess'))
      agents.value = agents.value.filter(a => a.id !== selectedAgent.value!.id)
      selectedAgent.value = null
    } else {
      ElMessage.error(t('agentsConfig.message.deleteFailed'))
    }
  } catch (error) {
    console.error('Delete failed:', error)
    ElMessage.error(t('agentsConfig.message.deleteFailed'))
  }
}

function addNew() {
  const newId = 'agent_' + Date.now()
  selectedAgent.value = {
    id: newId,
    name: '',
    title: '',
    role: '',
    description: '',
    parent_id: '',
    is_active: true
  }
  editForm.value = {
    id: newId,
    name: '',
    title: '',
    role: '',
    description: '',
    parent_id: '',
    is_active: true
  }
  editing.value = true
}

async function createAgent() {
  try {
    const response = await fetch(`${API_BASE}/api/agent-templates`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(editForm.value)
    })

    if (response.ok) {
      ElMessage.success(t('agentsConfig.message.createSuccess'))
      agents.value.push({ ...editForm.value })
      selectedAgent.value = null
    } else {
      ElMessage.error(t('agentsConfig.message.createFailed'))
    }
  } catch (error) {
    console.error('Create failed:', error)
    ElMessage.error(t('agentsConfig.message.createFailed'))
  }
}

onMounted(() => {
  refresh()
})
</script>

<template>
  <div class="agents-config-page">
    <!-- Page Header -->
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold">📋 {{ t('agentsConfig.title') }}</h2>
      <div class="flex items-center gap-2">
        <el-button type="primary" :loading="loading" @click="refresh">
          <el-icon><Refresh /></el-icon>
          {{ t('common.refresh') }}
        </el-button>
        <el-button type="success" @click="addNew">
          <el-icon><Plus /></el-icon>
          {{ t('agentsConfig.addAgent') }}
        </el-button>
      </div>
    </div>

    <!-- Agent Config List -->
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

    <!-- Agent Detail Drawer -->
    <el-drawer
      v-model="selectedAgent"
      :title="selectedAgent?.name + ' - ' + t('agentsConfig.templateCard.config')"
      direction="rtl"
      size="500px"
      @close="closeDetail"
    >
      <div v-if="selectedAgent" class="space-y-4">
        <!-- Avatar -->
        <div class="text-center py-4 border-b">
          <el-avatar :size="80" class="text-4xl">
            {{
              ['👸', '👩‍🎤', '👩‍💼', '🕵️‍♀️', '👩‍🔬', '💰', '🎭', '🛡️', '⚖️', '🔧', '💻', '🍜', '💊'][
                agents.indexOf(selectedAgent) % 13
              ]
            }}
          </el-avatar>
        </div>

        <!-- Config Form -->
        <el-form v-if="editing" :model="editForm" label-width="80px">
          <el-form-item :label="t('agentsConfig.form.id')">
            <el-input v-model="editForm.id" :disabled="agents.some(a => a.id === editForm.id)" />
          </el-form-item>
          <el-form-item :label="t('agentsConfig.form.name')">
            <el-input v-model="editForm.name" :placeholder="t('agentsConfig.form.namePlaceholder')" />
          </el-form-item>
          <el-form-item :label="t('agentsConfig.form.title')">
            <el-select v-model="editForm.title" :placeholder="t('agentsConfig.form.title')">
              <el-option :label="t('agentsConfig.form.titleOptions.queen')" value="皇后" />
              <el-option :label="t('agentsConfig.form.titleOptions.imperialConcubine')" value="皇贵妃" />
              <el-option :label="t('agentsConfig.form.titleOptions.concubine')" value="贵妃" />
              <el-option :label="t('agentsConfig.form.titleOptions.fei')" value="妃" />
              <el-option :label="t('agentsConfig.form.titleOptions.guiren')" value="贵人" />
              <el-option :label="t('agentsConfig.form.titleOptions.pin')" value="嫔" />
              <el-option :label="t('agentsConfig.form.titleOptions.ya环')" value="丫鬟" />
              <el-option :label="t('agentsConfig.form.titleOptions.developer')" value="研发" />
            </el-select>
          </el-form-item>
          <el-form-item :label="t('agentsConfig.form.role')">
            <el-input v-model="editForm.role" :placeholder="t('agentsConfig.form.rolePlaceholder')" />
          </el-form-item>
          <el-form-item :label="t('agentsConfig.form.parent')">
            <el-select v-model="editForm.parent_id" :placeholder="t('agentsConfig.form.parentPlaceholder')" clearable>
              <el-option
                v-for="a in agents"
                :key="a.id"
                :label="a.name + ' (' + a.title + ')'"
                :value="a.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item :label="t('agentsConfig.form.description')">
            <el-input v-model="editForm.description" type="textarea" rows="3" />
          </el-form-item>
          <el-form-item :label="t('agentsConfig.form.status')">
            <el-switch v-model="editForm.is_active" :active-text="t('agentsConfig.form.statusActive')" :inactive-text="t('agentsConfig.form.statusInactive')" />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              @click="agents.some(a => a.id === editForm.id) ? saveEdit() : createAgent()"
            >
              {{ agents.some(a => a.id === editForm.id) ? t('agentsConfig.button.save') : t('agentsConfig.button.create') }}
            </el-button>
            <el-button @click="cancelEdit">{{ t('agentsConfig.button.cancel') }}</el-button>
            <el-button
              v-if="agents.some(a => a.id === editForm.id)"
              type="danger"
              plain
              @click="deleteAgent"
              >{{ t('agentsConfig.button.delete') }}</el-button
            >
          </el-form-item>
        </el-form>

        <!-- Detail View -->
        <div v-else class="space-y-4">
          <div class="flex justify-between items-center">
            <span class="text-gray-500">{{ t('agentsConfig.detail.id') }}</span>
            <span class="font-mono">{{ selectedAgent.id }}</span>
          </div>
          <div class="flex justify-between items-center">
            <span class="text-gray-500">{{ t('agentsConfig.detail.name') }}</span>
            <span>{{ selectedAgent.name }}</span>
          </div>
          <div class="flex justify-between items-center">
            <span class="text-gray-500">{{ t('agentsConfig.detail.title') }}</span>
            <el-tag type="pink">{{ selectedAgent.title }}</el-tag>
          </div>
          <div class="flex justify-between items-center">
            <span class="text-gray-500">{{ t('agentsConfig.detail.role') }}</span>
            <span>{{ selectedAgent.role }}</span>
          </div>
          <div class="flex justify-between items-center">
            <span class="text-gray-500">{{ t('agentsConfig.detail.parent') }}</span>
            <span>{{ selectedAgent.parent_id || '-' }}</span>
          </div>
          <div class="flex justify-between items-center">
            <span class="text-gray-500">{{ t('agentsConfig.detail.description') }}</span>
            <span class="text-sm">{{ selectedAgent.description || '-' }}</span>
          </div>
          <div class="flex justify-between items-center">
            <span class="text-gray-500">{{ t('agentsConfig.detail.status') }}</span>
            <el-tag :type="selectedAgent.is_active ? 'success' : 'info'">
              {{ selectedAgent.is_active ? t('agentsConfig.form.statusActive') : t('agentsConfig.form.statusInactive') }}
            </el-tag>
          </div>

          <!-- Action Buttons -->
          <div class="pt-4 border-t flex gap-2">
            <el-button type="primary" @click="startEdit">{{ t('agentsConfig.button.editConfig') }}</el-button>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script lang="ts">
import { Refresh, Plus } from '@element-plus/icons-vue'
export default { components: { Refresh, Plus } }
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
