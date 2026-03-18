import Database from 'better-sqlite3'
import { join } from 'path'

const OPENCLAW_HOME = '/Users/hannah/.openclaw'
const DB_PATH = join(OPENCLAW_HOME, 'task-queue.db')

let db: Database.Database | null = null

/**
 * 获取数据库连接
 */
export function getDatabase(): Database.Database {
  if (!db) {
    db = new Database(DB_PATH, { readonly: true })
  }
  return db
}

/**
 * 关闭数据库连接
 */
export function closeDatabase(): void {
  if (db) {
    db.close()
    db = null
  }
}

/**
 * 执行查询
 */
export function query<T>(sql: string, params: any[] = []): T[] {
  const database = getDatabase()
  const stmt = database.prepare(sql)
  return stmt.all(...params) as T[]
}

/**
 * 执行单条查询
 */
export function queryOne<T>(sql: string, params: any[] = []): T | undefined {
  const database = getDatabase()
  const stmt = database.prepare(sql)
  return stmt.get(...params) as T | undefined
}

export default {
  getDatabase,
  closeDatabase,
  query,
  queryOne
}
