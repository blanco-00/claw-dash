<template>
  <div class="finance-page">
    <div class="header">
      <h2>Token 消耗统计</h2>
      <div class="header-actions">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          @change="loadStats"
        />
        <el-button type="primary" @click="loadStats">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
    </div>

    <el-row :gutter="20" class="summary-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon input">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ formatNumber(summary.total_input_tokens) }}</div>
            <div class="stat-label">输入 Token</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon output">
            <el-icon><DocumentChecked /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ formatNumber(summary.total_output_tokens) }}</div>
            <div class="stat-label">输出 Token</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon total">
            <el-icon><Coin /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ formatNumber(summary.total_cost || 0) }}</div>
            <div class="stat-label">总消耗 ($)</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon requests">
            <el-icon><Connection /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ summary.total_requests || 0 }}</div>
            <div class="stat-label">请求次数</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>按 Agent 分布</span>
            </div>
          </template>
          <div ref="agentChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>按 Model 分布</span>
            </div>
          </template>
          <div ref="modelChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>Token 消耗趋势</span>
            </div>
          </template>
          <div ref="trendChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="table-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>按 Agent 统计</span>
            </div>
          </template>
          <el-table :data="byAgent" style="width: 100%">
            <el-table-column prop="agent_id" label="Agent" width="150" />
            <el-table-column prop="input_tokens" label="输入" width="120">
              <template #default="{ row }">
                {{ formatNumber(row.input_tokens || 0) }}
              </template>
            </el-table-column>
            <el-table-column prop="output_tokens" label="输出" width="120">
              <template #default="{ row }">
                {{ formatNumber(row.output_tokens || 0) }}
              </template>
            </el-table-column>
            <el-table-column prop="cost" label="费用 ($)">
              <template #default="{ row }">
                {{ (row.cost || 0).toFixed(4) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>按 Model 统计</span>
            </div>
          </template>
          <el-table :data="byModel" style="width: 100%">
            <el-table-column prop="model_name" label="Model" width="150" />
            <el-table-column prop="input_tokens" label="输入" width="120">
              <template #default="{ row }">
                {{ formatNumber(row.input_tokens || 0) }}
              </template>
            </el-table-column>
            <el-table-column prop="output_tokens" label="输出" width="120">
              <template #default="{ row }">
                {{ formatNumber(row.output_tokens || 0) }}
              </template>
            </el-table-column>
            <el-table-column prop="cost" label="费用 ($)">
              <template #default="{ row }">
                {{ (row.cost || 0).toFixed(4) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="table-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>详细记录</span>
            </div>
          </template>
          <el-table :data="usageRecords" style="width: 100%" max-height="400">
            <el-table-column prop="created_at" label="时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.created_at) }}
              </template>
            </el-table-column>
            <el-table-column prop="agent_id" label="Agent" width="150" />
            <el-table-column prop="task_id" label="任务ID" width="150" />
            <el-table-column prop="model_name" label="Model" width="150" />
            <el-table-column prop="input_tokens" label="输入" width="100">
              <template #default="{ row }">
                {{ formatNumber(row.input_tokens || 0) }}
              </template>
            </el-table-column>
            <el-table-column prop="output_tokens" label="输出" width="100">
              <template #default="{ row }">
                {{ formatNumber(row.output_tokens || 0) }}
              </template>
            </el-table-column>
            <el-table-column prop="cost" label="费用 ($)">
              <template #default="{ row }">
                {{ (row.cost || 0).toFixed(4) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="table-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>操作日志</span>
              <el-button size="small" @click="loadTransactions">刷新</el-button>
            </div>
          </template>
          <el-table :data="transactions" style="width: 100%" max-height="300">
            <el-table-column prop="timestamp" label="时间" width="180">
              <template #default="{ row }">
                {{ formatTime(row.timestamp) }}
              </template>
            </el-table-column>
            <el-table-column prop="operation" label="操作" width="150">
              <template #default="{ row }">
                <el-tag :type="getOperationType(row.operation)">{{
                  getOperationText(row.operation)
                }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="entity_type" label="类型" width="100" />
            <el-table-column prop="entity_id" label="实体ID" width="150" />
            <el-table-column prop="details" label="详情">
              <template #default="{ row }">
                <el-button v-if="row.details" size="small" @click="viewDetails(row)"
                  >查看</el-button
                >
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="showDetailsDialog" title="详情" width="500px">
      <pre class="details-pre">{{ selectedDetails }}</pre>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { Refresh, Document, DocumentChecked, Coin, Connection } from '@element-plus/icons-vue'
import request from '@/utils/request'

const dateRange = ref<[string, string]>([
  new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
  new Date().toISOString().split('T')[0]
])

const summary = ref<any>({})
const byAgent = ref<any[]>([])
const byModel = ref<any[]>([])
const usageRecords = ref<any[]>([])
const transactions = ref<any[]>([])
const showDetailsDialog = ref(false)
const selectedDetails = ref('')

const agentChartRef = ref<HTMLElement>()
const modelChartRef = ref<HTMLElement>()
const trendChartRef = ref<HTMLElement>()

let agentChart: echarts.ECharts | null = null
let modelChart: echarts.ECharts | null = null
let trendChart: echarts.ECharts | null = null

const formatNumber = (num: number) => {
  if (!num) return '0'
  return num.toLocaleString()
}

const formatTime = (timestamp: string) => {
  if (!timestamp) return '-'
  return new Date(timestamp).toLocaleString('zh-CN')
}

const getOperationType = (op: string) => {
  const map: Record<string, string> = {
    create: 'success',
    update: 'warning',
    delete: 'danger',
    task_start: 'success',
    task_stop: 'warning',
    task_retry: 'info',
    link: 'primary'
  }
  return map[op] || 'info'
}

const getOperationText = (op: string) => {
  const map: Record<string, string> = {
    create: '创建',
    update: '更新',
    delete: '删除',
    task_start: '任务启动',
    task_stop: '任务停止',
    task_retry: '任务重试',
    link: '关联'
  }
  return map[op] || op
}

const viewDetails = (row: any) => {
  try {
    selectedDetails.value = JSON.stringify(JSON.parse(row.details), null, 2)
  } catch {
    selectedDetails.value = row.details
  }
  showDetailsDialog.value = true
}

const loadStats = async () => {
  try {
    const params: any = {}
    if (dateRange.value) {
      params.start_date = dateRange.value[0]
      params.end_date = dateRange.value[1]
    }
    const res = await request.get('/api/tokens/stats', { params })
    const data = res.data || {}
    summary.value = data.summary || {}
    byAgent.value = data.by_agent || []
    byModel.value = data.by_model || []
    usageRecords.value = data.usage || []
    updateCharts()
  } catch (error) {
    console.error('Failed to load stats:', error)
  }
}

const loadTransactions = async () => {
  try {
    const res = await request.get('/api/transactions', { params: { limit: 100 } })
    transactions.value = res.data || []
  } catch (error) {
    console.error('Failed to load transactions:', error)
  }
}

const updateCharts = () => {
  nextTick(() => {
    if (agentChartRef.value) {
      if (!agentChart) {
        agentChart = echarts.init(agentChartRef.value)
      }
      agentChart.setOption({
        tooltip: { trigger: 'item' },
        series: [
          {
            type: 'pie',
            radius: '60%',
            data: byAgent.value.map((item: any) => ({
              name: item.agent_id || 'Unknown',
              value: item.cost || 0
            }))
          }
        ]
      })
    }

    if (modelChartRef.value) {
      if (!modelChart) {
        modelChart = echarts.init(modelChartRef.value)
      }
      modelChart.setOption({
        tooltip: { trigger: 'item' },
        series: [
          {
            type: 'pie',
            radius: '60%',
            data: byModel.value.map((item: any) => ({
              name: item.model_name || 'Unknown',
              value: item.cost || 0
            }))
          }
        ]
      })
    }

    if (trendChartRef.value) {
      if (!trendChart) {
        trendChart = echarts.init(trendChartRef.value)
      }
      const dateMap = new Map<string, { input: number; output: number; cost: number }>()
      usageRecords.value.forEach((r: any) => {
        const date = r.created_at?.split('T')[0] || 'Unknown'
        const existing = dateMap.get(date) || { input: 0, output: 0, cost: 0 }
        dateMap.set(date, {
          input: existing.input + (r.input_tokens || 0),
          output: existing.output + (r.output_tokens || 0),
          cost: existing.cost + (r.cost || 0)
        })
      })
      const sortedDates = Array.from(dateMap.keys()).sort()
      trendChart.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: sortedDates },
        yAxis: { type: 'value' },
        series: [
          { name: 'Input', type: 'line', data: sortedDates.map(d => dateMap.get(d)?.input || 0) },
          { name: 'Output', type: 'line', data: sortedDates.map(d => dateMap.get(d)?.output || 0) }
        ]
      })
    }
  })
}

onMounted(() => {
  loadStats()
  loadTransactions()
})
</script>

<style scoped>
.finance-page {
  padding: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 10px;
}
.header h2 {
  margin: 0;
}
.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}
.summary-row {
  margin-bottom: 20px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 15px;
}
.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}
.stat-icon.input {
  background: #e6f7ff;
  color: #1890ff;
}
.stat-icon.output {
  background: #f6ffed;
  color: #52c41a;
}
.stat-icon.total {
  background: #fff7e6;
  color: #fa8c16;
}
.stat-icon.requests {
  background: #f9f0ff;
  color: #722ed1;
}
.stat-content {
  flex: 1;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: var(--el-text-color-primary);
}
.stat-label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}
.chart-row,
.table-row {
  margin-bottom: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.details-pre {
  background: var(--el-fill-color-light);
  padding: 15px;
  border-radius: 4px;
  overflow-x: auto;
  max-height: 400px;
  margin: 0;
}
</style>
