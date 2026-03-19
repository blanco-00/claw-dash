import type { TokenUsage, TokenStats, TokenTrend } from '@/types/token'

/**
 * 获取Token消耗统计（模拟数据）
 */
export function getTokenStats(): TokenStats {
  const trends: TokenTrend[] = []
  const now = new Date()

  for (let i = 29; i >= 0; i--) {
    const date = new Date(now.getTime() - i * 24 * 60 * 60 * 1000)
    const tokens = Math.floor(Math.random() * 100000 + 50000)
    trends.push({
      date: date.toISOString().split('T')[0],
      tokens,
      cost: tokens * 0.000002
    })
  }

  const byAgent: Record<string, TokenUsage> = {
    'main': { agentId: 'main', agentName: '瑾儿', totalTokens: 1500000, inputTokens: 600000, outputTokens: 900000, requestCount: 156, lastUsed: new Date().toISOString() },
    'jishu': { agentId: 'jishu', agentName: '青岚', totalTokens: 800000, inputTokens: 320000, outputTokens: 480000, requestCount: 89, lastUsed: new Date().toISOString() },
    'gongbu': { agentId: 'gongbu', agentName: '灵犀', totalTokens: 450000, inputTokens: 180000, outputTokens: 270000, requestCount: 45, lastUsed: new Date().toISOString() },
    'hubu': { agentId: 'hubu', agentName: '琉璃', totalTokens: 320000, inputTokens: 128000, outputTokens: 192000, requestCount: 32, lastUsed: new Date().toISOString() }
  }

  const totalTokens = Object.values(byAgent).reduce((sum, a) => sum + a.totalTokens, 0)

  return {
    totalTokens,
    totalCost: totalTokens * 0.000002,
    avgDailyTokens: Math.round(totalTokens / 30),
    byAgent,
    trends
  }
}

/**
 * 获取按Agent分类的Token使用
 */
export function getTokenByAgent(): TokenUsage[] {
  const stats = getTokenStats()
  return Object.values(stats.byAgent)
}

/**
 * 计算预估费用
 */
export function calculateCost(inputTokens: number, outputTokens: number): number {
  const TOKEN_PRICING = { input: 0.000001, output: 0.000003 }
  return inputTokens * TOKEN_PRICING.input + outputTokens * TOKEN_PRICING.output
}

export default {
  getTokenStats,
  getTokenByAgent,
  calculateCost
}
