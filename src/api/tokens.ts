import { query } from './database'
import type { TokenUsage, TokenStats, TokenTrend } from '@/types/token'

/**
 * 获取Token消耗统计
 */
export function getTokenStats(): TokenStats {
  // 从sessions表获取数据
  const sessions = query<{ agentId: string; tokens: string }>(
    `SELECT agentId, tokens FROM sessions ORDER BY createdAt DESC LIMIT 1000`
  )

  const byAgent: Record<string, TokenUsage> = {}
  let totalTokens = 0

  sessions.forEach(session => {
    const tokens = parseTokenString(session.tokens)
    if (!byAgent[session.agentId]) {
      byAgent[session.agentId] = {
        agentId: session.agentId,
        agentName: session.agentId,
        totalTokens: 0,
        inputTokens: 0,
        outputTokens: 0,
        requestCount: 0,
        lastUsed: ''
      }
    }
    byAgent[session.agentId].totalTokens += tokens.total
    byAgent[session.agentId].inputTokens += tokens.input
    byAgent[session.agentId].outputTokens += tokens.output
    byAgent[session.agentId].requestCount++
    totalTokens += tokens.total
  })

  // 转换 trends
  const trends: TokenTrend[] = generateMockTrends()

  return {
    totalTokens,
    totalCost: totalTokens * 0.000002, // 平均价格
    avgDailyTokens: Math.round(totalTokens / 30),
    byAgent,
    trends
  }
}

/**
 * 解析Token字符串
 */
function parseTokenString(tokenStr: string): { total: number; input: number; output: number } {
  // 格式: "42k/200k (21%)" 或类似
  const match = tokenStr.match(/(\d+)([kKmM]?)/)
  if (!match) return { total: 0, input: 0, output: 0 }

  let value = parseInt(match[1], 10)
  const unit = match[2]?.toLowerCase() || ''

  if (unit === 'k') value *= 1000
  if (unit === 'm') value *= 1000000

  return {
    total: value,
    input: Math.round(value * 0.4),
    output: Math.round(value * 0.6)
  }
}

/**
 * 生成模拟趋势数据
 */
function generateMockTrends(): TokenTrend[] {
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

  return trends
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
  const { TOKEN_PRICING } = require('@/types/token')
  return inputTokens * TOKEN_PRICING.input + outputTokens * TOKEN_PRICING.output
}

export default {
  getTokenStats,
  getTokenByAgent,
  calculateCost
}
