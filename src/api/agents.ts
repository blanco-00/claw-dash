import type { AgentListItem, AgentInfo } from '@/types/agent'

// 女儿国Agent配置
const AGENT_CONFIG: Record<string, { name: string; title: string; role: string }> = {
  main: { name: '瑾儿', title: '皇后', role: '中书省决策' },
  menxiasheng: { name: '卿酒', title: '皇贵妃', role: '门下省审核' },
  shangshusheng: { name: '红袖', title: '贵妃', role: '尚书省分发' },
  jinyiwei: { name: '灵鸢', title: '贵人', role: '锦衣卫督查' },
  libu4: { name: '珊瑚', title: '妃', role: '吏部人事' },
  hubu: { name: '琉璃', title: '妃', role: '户部财务' },
  libu3: { name: '书瑶', title: '妃', role: '礼部外交' },
  bingbu: { name: '魅羽', title: '妃', role: '兵部安全' },
  xingbu: { name: '如意', title: '嫔', role: '刑部法务' },
  gongbu: { name: '灵犀', title: '嫔', role: '工部技术' },
  jishu: { name: '青岚', title: '丫鬟', role: '工部研发' },
  shangshiju: { name: '婉儿', title: '丫鬟', role: '尚食局' },
  shangyaosi: { name: '允贤', title: '丫鬟', role: '尚药司' }
}

/**
 * 获取Agent列表（模拟数据）
 */
export function getAgentList(): AgentListItem[] {
  // 返回模拟数据
  return Object.entries(AGENT_CONFIG).map(([id, config]) => ({
    id,
    name: config.name,
    title: config.title,
    status: 'online' as const
  }))
}

/**
 * 获取Agent详情
 */
export function getAgentDetail(id: string): AgentInfo | null {
  const config = AGENT_CONFIG[id]
  if (!config) return null
  
  return {
    id,
    name: config.name,
    title: config.title,
    role: config.role,
    status: 'online',
    memory: {
      soul: Math.floor(Math.random() * 5000),
      memory: Math.floor(Math.random() * 10000)
    }
  }
}

/**
 * 获取所有Agent详情
 */
export function getAllAgentDetails(): AgentInfo[] {
  return getAgentList().map(agent => getAgentDetail(agent.id)!)
}

export default {
  getAgentList,
  getAgentDetail,
  getAllAgentDetails
}
