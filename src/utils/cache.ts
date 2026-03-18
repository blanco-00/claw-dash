// 缓存工具

interface CacheItem<T> {
  value: T
  expires: number
}

class MemoryCache {
  private cache: Map<string, CacheItem<any>> = new Map()
  private defaultTTL = 30000 // 默认30秒

  /**
   * 设置缓存
   */
  set<T>(key: string, value: T, ttl?: number): void {
    const expires = Date.now() + (ttl || this.defaultTTL)
    this.cache.set(key, { value, expires })
  }

  /**
   * 获取缓存
   */
  get<T>(key: string): T | undefined {
    const item = this.cache.get(key)
    if (!item) return undefined
    
    if (Date.now() > item.expires) {
      this.cache.delete(key)
      return undefined
    }
    
    return item.value as T
  }

  /**
   * 检查缓存是否存在
   */
  has(key: string): boolean {
    const item = this.cache.get(key)
    if (!item) return false
    
    if (Date.now() > item.expires) {
      this.cache.delete(key)
      return false
    }
    
    return true
  }

  /**
   * 删除缓存
   */
  delete(key: string): void {
    this.cache.delete(key)
  }

  /**
   * 清空所有缓存
   */
  clear(): void {
    this.cache.clear()
  }

  /**
   * 清理过期缓存
   */
  cleanup(): void {
    const now = Date.now()
    for (const [key, item] of this.cache.entries()) {
      if (now > item.expires) {
        this.cache.delete(key)
      }
    }
  }
}

// 导出单例
export const cache = new MemoryCache()

/**
 * 带缓存的数据获取函数
 */
export async function withCache<T>(
  key: string,
  fetcher: () => Promise<T>,
  ttl?: number
): Promise<T> {
  const cached = cache.get<T>(key)
  if (cached !== undefined) {
    return cached
  }

  const value = await fetcher()
  cache.set(key, value, ttl)
  return value
}

export default cache
