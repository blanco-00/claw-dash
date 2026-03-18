<script setup lang="ts">
import { ref, onMounted, computed, watch, nextTick } from 'vue'
import { getAllAgentDetails } from '@/api/agents'
import * as d3 from 'd3'
import 'd3-org-chart'
import 'd3-flextree'

interface AgentNode {
  id: string
  name: string
  title: string
  role: string
  status: string
  _directSubordinates?: AgentNode[]
  [key: string]: any
}

const props = defineProps<{
  agents: AgentNode[]
}>()

const emit = defineEmits<{
  (e: 'nodeClick', agent: AgentNode): void
}>()

const chartContainer = ref<HTMLElement | null>(null)
const chartInstance = ref<any>(null)

const HIERARCHY_MAP: Record<string, string[]> = {
  main: ['menxiasheng', 'shangshusheng', 'jinyiwei', 'libu4', 'hubu', 'libu3', 'bingbu', 'xingbu'],
  shangshusheng: ['gongbu', 'jishu', 'shangshiju', 'shangyaosi']
}

const LEVEL_COLORS: Record<number, string> = {
  1: '#FFD700',
  2: '#FF69B4',
  3: '#9370DB',
  4: '#87CEEB',
  5: '#90EE90'
}

function getAgentLevel(id: string): number {
  if (id === 'main') return 1
  if (['menxiasheng', 'shangshusheng'].includes(id)) return 2
  if (['jinyiwei', 'libu4', 'hubu', 'libu3', 'bingbu', 'xingbu'].includes(id)) return 3
  if (['gongbu'].includes(id)) return 4
  return 5
}

function buildHierarchy(agents: AgentNode[]): AgentNode {
  const agentMap = new Map<string, AgentNode>()

  agents.forEach(a => {
    agentMap.set(a.id, { ...a, _directSubordinates: [] })
  })

  const root = agentMap.get('main') || {
    id: 'main',
    name: '瑾儿',
    title: '皇后',
    role: '中书省决策',
    status: 'online',
    _directSubordinates: []
  }

  Object.entries(HIERARCHY_MAP).forEach(([parentId, childIds]) => {
    const parent = agentMap.get(parentId)
    if (parent) {
      childIds.forEach(childId => {
        const child = agentMap.get(childId)
        if (child) {
          parent._directSubordinates!.push(child)
        }
      })
    }
  })

  agents.forEach(a => {
    if (!Object.keys(HIERARCHY_MAP).includes(a.id)) {
      const parentId = Object.entries(HIERARCHY_MAP).find(([_, children]) =>
        children.includes(a.id)
      )?.[0]
      if (parentId && agentMap.get(parentId)?._directSubordinates?.length === 0) {
        const parent = agentMap.get(parentId)
        if (parent) {
          parent._directSubordinates!.push(a)
        }
      }
    }
  })

  return root
}

function transformToChartData(root: AgentNode): any[] {
  const result: any[] = []

  function traverse(node: AgentNode, level: number, parentId?: string) {
    const agentLevel = getAgentLevel(node.id)
    result.push({
      id: node.id,
      name: node.name,
      parentId: parentId || '',
      nodeColor: LEVEL_COLORS[agentLevel] || LEVEL_COLORS[5],
      title: node.title,
      role: node.role,
      status: node.status,
      level: agentLevel
    })

    if (node._directSubordinates) {
      node._directSubordinates.forEach(child => {
        traverse(child, level + 1, node.id)
      })
    }
  }

  traverse(root, 1)
  return result
}

function initChart() {
  if (!chartContainer.value || !props.agents.length) return

  const hierarchy = buildHierarchy(
    props.agents[0]
      ? props.agents
      : [{ id: 'main', name: '瑾儿', title: '皇后', role: '中书省决策', status: 'online' }]
  )

  const chartData = transformToChartData(hierarchy)

  if (chartInstance.value) {
    chartInstance.value = null
  }

  try {
    chartInstance.value = new (window as any).OrgChart()
      .container(chartContainer.value)
      .data(chartData)
      .nodeWidth(() => 180)
      .nodeHeight(() => 80)
      .childrenMargin(() => 50)
      .compactMarginBetween(() => 35)
      .compactMarginPairing(() => 25)
      .neightbourMargin((a: any, b: any) => 20)
      .buttonContent(({ node, state }: any) => {
        return `<div style="padding:3px;font-size:10px;margin:auto auto;background-color:white;border:1px solid #717171;border-radius:10px;color:#717171"> <span style="font-size:9px">${node.data._directSubordinates && node.data._directSubordinates.length ? (state === 'expanded' ? '-' : '+') : ''}</span></div>`
      })
      .nodeContent((d: any) => {
        const color = d.data.nodeColor || LEVEL_COLORS[5]
        return `
          <div style="font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;background-color:white;border-radius:10px;border:2px solid ${color};padding:10px;height:80px;display:flex;flex-direction:column;justify-content:center;box-shadow: 0 2px 4px rgba(0,0,0,0.1)">
            <div style="font-size:14px;font-weight:bold;color:#333;text-align:center">${d.data.name}</div>
            <div style="font-size:12px;color:${color};text-align:center">${d.data.title}</div>
            <div style="font-size:10px;color:#999;text-align:center;margin-top:4px">${d.data.role}</div>
          </div>
        `
      })
      .onNodeClick((d: any) => {
        const agent = props.agents.find((a: AgentNode) => a.id === d.id) || {
          id: d.id,
          name: d.name,
          title: d.title,
          role: d.role,
          status: d.status
        }
        emit('nodeClick', agent)
      })
      .render()
  } catch (e) {
    console.error('Chart init error:', e)
  }
}

watch(
  () => props.agents,
  () => {
    nextTick(() => {
      initChart()
    })
  },
  { deep: true }
)

onMounted(() => {
  nextTick(() => {
    initChart()
  })
})
</script>

<template>
  <div class="org-chart-wrapper">
    <div ref="chartContainer" class="org-chart-container"></div>
  </div>
</template>

<style scoped>
.org-chart-wrapper {
  width: 100%;
  height: 600px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
  border-radius: 8px;
  overflow: hidden;
}

.org-chart-container {
  width: 100%;
  height: 100%;
}
</style>
