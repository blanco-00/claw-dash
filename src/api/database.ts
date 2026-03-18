// 数据库模拟 - 开发环境使用
// 生产环境需要后端API

/**
 * 查询（模拟）
 */
export function query<T>(_sql: string, _params: any[] = []): T[] {
  return []
}

/**
 * 单条查询（模拟）
 */
export function queryOne<T>(_sql: string, _params: any[] = []): T | undefined {
  return undefined
}

export default {
  query,
  queryOne
}
