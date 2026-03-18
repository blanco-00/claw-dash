<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { getTokenStats } from '@/api/tokens'
import type { TokenStats, TokenUsage } from '@/types/token'

use([CanvasRenderer, LineChart, PieChart, GridComponent, TooltipComponent, LegendComponent])

const loading = ref(true)
const stats = ref<TokenStats>()
const dateRange = ref<[Date, Date]>([new Date(Date.now() - 30 * 24 * 60 * 60 * 1000), new Date()])

function refresh() {
  loading.value = true
  try {
    stats.value = getTokenStats()
  } catch (error) {
    console.error('获取Token统计失败:', error)
  } finally {
    loading.value = false
  }
}

// 趋势图配置
const trendChartOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    formatter: (params: any) => {
      const p = params[0]
      return `${p.name}<br/>Token: ${(p.value / 1000).toFixed(1)}k<br/>费用: $${(p.value * 0.000002).toFixed(4)}`
    }
  },
  grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: stats.value?.trends.map(t => t.date.slice(5)) || []
  },
  yAxis: {
    type: 'value',
    axisLabel: { formatter: (v: number) => (v / 1000).toFixed(0) + 'k' }
  },
  series: [
    {
      name: 'Token消耗',
      type: 'line',
      smooth: true,
      data: stats.value?.trends.map(t => t.tokens) || [],
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(236, 72, 153, 0.3)' },
            { offset: 1, color: 'rgba(236, 72, 153, 0.05)' }
          ]
        }
      },
      itemStyle: { color: '#ec4899' }
    }
  ]
}))

// 饼图配置
const pieChartOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { orient: 'vertical', right: 10, top: 'center' },
  series: [
    {
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: Object.values(stats.value?.byAgent || {}).map((a, i) => ({
        name: a.agentName,
        value: a.totalTokens,
        itemStyle: { color: ['#ec4899', '#8b5cf6', '#3b82f6', '#10b981', '#f59e0b'][i % 5] }
      }))
    }
  ]
}))

// 格式化数字
function formatNumber(num: number): string {
  if (num >= 1000000) return (num / 1000000).toFixed(1) + 'M'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'K'
  return num.toString()
}

function formatCost(cost: number): string {
  if (cost >= 1) return '$' + cost.toFixed(2)
  return '$' + cost.toFixed(4)
}

onMounted(() => {
  refresh()
})
</script>

<template>
  <div class="tokens-page">
    <!-- 页面头部 -->
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-2xl font-bold">💰 Tokens监控</h2>
      <div class="flex items-center gap-2">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        />
        <el-button type="primary" :loading="loading" @click="refresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        <el-button type="success" plain>导出报表</el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb-6">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="text-center py-2">
            <div class="text-gray-500 text-sm">总消耗</div>
            <div class="text-3xl font-bold text-pink-500">
              {{ formatNumber(stats?.totalTokens || 0) }}
            </div>
            <div class="text-sm text-gray-400">tokens</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="text-center py-2">
            <div class="text-gray-500 text-sm">预估费用</div>
            <div class="text-3xl font-bold text-green-500">
              {{ formatCost(stats?.totalCost || 0) }}
            </div>
            <div class="text-sm text-gray-400">USD</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="text-center py-2">
            <div class="text-gray-500 text-sm">日均消耗</div>
            <div class="text-3xl font-bold text-blue-500">
              {{ formatNumber(stats?.avgDailyTokens || 0) }}
            </div>
            <div class="text-sm text-gray-400">tokens/天</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="text-center py-2">
            <div class="text-gray-500 text-sm">活跃Agent</div>
            <div class="text-3xl font-bold text-purple-500">
              {{ Object.keys(stats?.byAgent || {}).length }}
            </div>
            <div class="text-sm text-gray-400">个</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表 -->
    <el-row :gutter="20" class="mb-6">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header><span class="font-bold">Token消耗趋势</span></template>
          <v-chart v-if="!loading" class="h-72" :option="trendChartOption" autoresize />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span class="font-bold">Agent分布</span></template>
          <v-chart v-if="!loading" class="h-72" :option="pieChartOption" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <!-- 按Agent统计 -->
    <el-card shadow="hover">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-bold">按Agent统计</span>
        </div>
      </template>
      <el-table :data="Object.values(stats?.byAgent || {})" v-loading="loading" stripe>
        <el-table-column prop="agentName" label="Agent" />
        <el-table-column prop="requestCount" label="请求数" width="100" />
        <el-table-column label="总Token" width="150">
          <template #default="{ row }">
            <span class="font-mono">{{ formatNumber(row.totalTokens) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="输入" width="120">
          <template #default="{ row }">
            <span class="text-gray-500">{{ formatNumber(row.inputTokens) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="输出" width="120">
          <template #default="{ row }">
            <span class="text-gray-500">{{ formatNumber(row.outputTokens) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="预估费用" width="120">
          <template #default="{ row }">
            <span class="text-green-500">${{ (row.totalTokens * 0.000002).toFixed(4) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script lang="ts">
import { Refresh } from '@element-plus/icons-vue'
export default { components: { Refresh } }
</script>

<style scoped>
.tokens-page {
  padding: 20px;
}
</style>
