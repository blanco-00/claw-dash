import { readdirSync, statSync, readFileSync, existsSync } from 'fs'
import { join } from 'path'
import type { AgentListItem, AgentInfo } from '@/types/agent'

const OPENCLAW_HOME = '/Users/hannah/.openclaw'
const AGENTS_DIR = join(OPENCLAW_HOME, 'agents')

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
 * 获取Agent列表
 */
export function getAgentList(): AgentListItem[] {
  try {
    if (!existsSync(AGENTS_DIR)) {
      return []
    }

    const agents = readdirSync(AGENTS_DIR).filter(name => {
      if (name.startsWith('.')) return false
      const stat = statSync(join(AGENTS_DIR, name))
      return stat.isDirectory()
    })

    return agents.map(id => {
      const config = AGENT_CONFIG[id] || { name: id, title: '未知', role: '未配置' }
      return {
        id,
        name: config.name,
        title: config.title,
        status: 'offline' as const
      }
    })
  } catch (error) {
    console.error('获取Agent列表失败:', error)
    return []
  }
}

/**
 * 获取Agent详情
 */
export function getAgentDetail(id: string): AgentInfo | null {
  try {
    const workspaceDir = join(AGENTS_DIR, id)
    
    if (!existsSync(workspaceDir)) {
      return null
    }

    const config = AGENT_CONFIG[id] || { name: id, title: '未知', role: '未配置' }
    
    const memory: AgentInfo['memory'] = {}
    
    // 读取SOUL.md大小
    const soulPath = join(workspaceDir, 'SOUL.md')
    if (existsSync(soulPath)) {
      memory.soul = statSync(soulPath).size
    }

    // 读取MEMORY.md大小
    const memoryPath = join(workspaceDir, 'MEMORY.md')
    if (existsSync(memoryPath)) {
      memory.memory = statSync(memoryPath).size
    }

    // 查找workspace路径
    const agentJsonPath = join(OPENCLAW_HOME, 'agents', id, 'openclaw.json')
    let workspace: string | undefined
    if (existsSync(agentJsonPath)) {
      try {
        const agentJson = JSON.parse(readFileSync(agentJsonPath, 'utf-8'))
        workspace = agentJson.workspace
      } catch {}
    }

    return {
      id,
      name: config.name,
      title: config.title,
      role: config.role,
      status: 'offline',
      workspace,
      memory
    }
  } catch (error) {
    console.error(`获取Agent详情失败 (${id}):`, error)
    return null
  }
}

/**
 * 获取所有Agent详情
 */
export function getAllAgentDetails(): AgentInfo[] {
  const list = getAgentList()
  return list.map(agent => getAgentDetail(agent.id)).filter(Boolean) as AgentInfo[]
}

export default {
  getAgentList,
  getAgentDetail,
  getAllAgentDetails
}
