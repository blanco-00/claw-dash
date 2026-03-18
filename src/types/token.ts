// Token消耗类型定义
export interface TokenUsage {
  agentId: string
  agentName: string
  totalTokens: number
  inputTokens: number
  outputTokens: number
  requestCount: number
  lastUsed: string
}

export interface TokenTrend {
  date: string
  tokens: number
  cost: number
}

export interface TokenStats {
  totalTokens: number
  totalCost: number
  avgDailyTokens: number
  byAgent: Record<string, TokenUsage>
  trends: TokenTrend[]
}

// 费用配置（基于MiniMax M2.5定价）
export const TOKEN_PRICING = {
  input: 0.000001,  // $1/1M tokens
  output: 0.000003, // $3/1M tokens
}
