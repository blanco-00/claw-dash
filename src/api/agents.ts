const API_BASE = 'http://localhost:3001'

async function fetchAPI(url: string) {
  const response = await fetch(url)
  if (!response.ok) {
    throw new Error(`API Error: ${response.status}`)
  }
  return response.json()
}

// 女儿国Agent配置模板
const AGENT_TEMPLATES: Record<string, { name: string; title: string; role: string }> = {
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
  shangyaosi: { name: '允贤', title: '丫鬟', role: '尚药司' },
  yanfa: { name: '研发动态', title: '研发', role: '技术研发' }
}

function getAgentTemplate(id: string) {
  return (
    AGENT_TEMPLATES[id] || {
      name: id.charAt(0).toUpperCase() + id.slice(1),
      title: '待配置',
      role: '待配置'
    }
  )
}

/**
 * 获取Agent列表
 */
export async function getAgentList() {
  try {
    const agents = await fetchAPI(`${API_BASE}/api/agents`)
    return agents.map((a: any) => {
      const template = getAgentTemplate(a.id)
      return {
        id: a.id,
        name: template.name,
        title: template.title,
        role: template.role,
        status: a.hasFiles ? 'online' : 'offline'
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
export async function getAgentDetail(id: string) {
  const agents = await getAgentList()
  return agents.find((a: any) => a.id === id) || null
}

/**
 * 获取所有Agent详情
 */
export async function getAllAgentDetails() {
  const agents = await fetchAPI(`${API_BASE}/api/agents`)
  return agents.map((a: any) => {
    const template = getAgentTemplate(a.id)
    return {
      id: a.id,
      name: template.name,
      title: template.title,
      role: template.role,
      status: a.hasFiles ? 'online' : 'offline',
      memory: {
        soul: a.soulSize,
        memory: a.memorySize
      }
    }
  })
}

export default {
  getAgentList,
  getAgentDetail,
  getAllAgentDetails
}
