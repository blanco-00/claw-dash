import express from 'express'
import cors from 'cors'
import { execSync } from 'child_process'
import { existsSync, readdirSync, readFileSync, statSync } from 'fs'
import { join } from 'path'
import Database from 'better-sqlite3'

const app = express()
const PORT = 3001

app.use(cors())
app.use(express.json())

const OPENCLAW_HOME = '/Users/hannah/.openclaw'
const OPENCLAW_BIN = '/Users/hannah/.npm-global/bin/openclaw'

// ========== Gateway API ==========
app.get('/api/gateway/status', (_req, res) => {
  try {
    const output = execSync(`${OPENCLAW_BIN} status`, { encoding: 'utf-8', timeout: 10000 })
    
    const running = output.includes('running')
    const pidMatch = output.match(/pid (\d+)/)
    const portMatch = output.match(/Dashboard.*?:(\d+)/)
    const versionMatch = output.match(/node (v[\d.]+)/)
    
    res.json({
      status: running ? 'running' : 'stopped',
      pid: pidMatch ? parseInt(pidMatch[1]) : null,
      port: portMatch ? parseInt(portMatch[1]) : 18789,
      version: versionMatch ? versionMatch[1] : 'unknown',
      uptime: extractUptime(output)
    })
  } catch (error: any) {
    res.json({ status: 'error', error: error.message })
  }
})

function extractUptime(output: string): string {
  const match = output.match(/loaded.*?\(([^)]+)\)/)
  return match ? match[1] : 'unknown'
}

// ========== Agents API ==========
app.get('/api/agents', (_req, res) => {
  try {
    const agentsDir = join(OPENCLAW_HOME, 'agents')
    if (!existsSync(agentsDir)) {
      return res.json([])
    }
    
    const agents = readdirSync(agentsDir).filter(name => {
      if (name.startsWith('.')) return false
      const stat = statSync(join(agentsDir, name))
      return stat.isDirectory()
    })
    
    const agentList = agents.map(id => {
      const soulPath = join(agentsDir, id, 'SOUL.md')
      const memoryPath = join(agentsDir, id, 'MEMORY.md')
      
      let soulSize = 0
      let memorySize = 0
      
      if (existsSync(soulPath)) {
        soulSize = statSync(soulPath).size
      }
      if (existsSync(memoryPath)) {
        memorySize = statSync(memoryPath).size
      }
      
      return {
        id,
        soulSize,
        memorySize,
        hasFiles: soulSize > 0 || memorySize > 0
      }
    })
    
    res.json(agentList)
  } catch (error: any) {
    res.json({ error: error.message })
  }
})

// ========== Cron API ==========
app.get('/api/cron', (_req, res) => {
  try {
    const cronDir = join(OPENCLAW_HOME, 'cron')
    if (!existsSync(cronDir)) {
      return res.json([])
    }
    
    const files = readdirSync(cronDir).filter(f => f.endsWith('.json'))
    const tasks = files.map(file => {
      const filePath = join(cronDir, file)
      const content = readFileSync(filePath, 'utf-8')
      const job = JSON.parse(content)
      
      return {
        id: file.replace('.json', ''),
        name: job.name || file.replace('.json', ''),
        agent: job.agentId || 'unknown',
        schedule: parseSchedule(job.schedule),
        enabled: job.enabled ?? true,
        status: job.enabled ? 'ok' : 'idle'
      }
    })
    
    res.json(tasks)
  } catch (error: any) {
    res.json({ error: error.message })
  }
})

function parseSchedule(schedule: any): string {
  if (!schedule) return 'unknown'
  if (schedule.kind === 'cron') return schedule.expr || '* * * * *'
  if (schedule.kind === 'every' && schedule.everyMs) {
    const mins = Math.floor(schedule.everyMs / 60000)
    return `*/${mins} * * * *`
  }
  return 'unknown'
}

// ========== Sessions API ==========
app.get('/api/sessions', (_req, res) => {
  try {
    const output = execSync(`${OPENCLAW_BIN} status`, { encoding: 'utf-8', timeout: 10000 })
    
    const sessions: any[] = []
    const lines = output.split('\n')
    let inSessions = false
    
    for (const line of lines) {
      if (line.includes('Sessions')) {
        inSessions = true
        continue
      }
      if (inSessions && line.includes('┘')) {
        break
      }
      if (inSessions && line.includes('│') && !line.includes('─')) {
        const parts = line.split('│').map(p => p.trim()).filter(Boolean)
        if (parts.length >= 3 && parts[0]) {
          sessions.push({
            key: parts[0],
            kind: parts[1] || 'direct',
            age: parts[2] || '',
            model: parts[3] || '',
            tokens: parts[4] || ''
          })
        }
      }
    }
    
    res.json(sessions)
  } catch (error: any) {
    res.json({ error: error.message })
  }
})

// ========== Tasks API ==========
app.get('/api/tasks', (req, res) => {
  try {
    const dbPath = join(OPENCLAW_HOME, 'task-queue.db')
    if (!existsSync(dbPath)) {
      return res.json({ tasks: [], counts: { pending: 0, running: 0, completed: 0, failed: 0, total: 0 } })
    }
    
    const db = new Database(dbPath, { readonly: true })
    
    const counts = db.prepare(`
      SELECT status, COUNT(*) as count FROM tasks GROUP BY status
    `).all() as { status: string; count: number }[]
    
    const countMap = { pending: 0, running: 0, completed: 0, failed: 0, dead: 0, total: 0 }
    counts.forEach(c => {
      const key = c.status.toLowerCase()
      if (key in countMap) {
        countMap[key as keyof typeof countMap] = c.count
      }
    })
    countMap.total = counts.reduce((sum, c) => sum + c.count, 0)
    
    const status = req.query.status as string
    let query = 'SELECT * FROM tasks ORDER BY created_at DESC LIMIT 50'
    const params: any[] = []
    
    if (status) {
      query = 'SELECT * FROM tasks WHERE status = ? ORDER BY created_at DESC LIMIT 50'
      params.push(status)
    }
    
    const tasks = db.prepare(query).all(...params)
    
    db.close()
    
    res.json({ tasks, counts: countMap })
  } catch (error: any) {
    res.json({ error: error.message, tasks: [], counts: { pending: 0, running: 0, completed: 0, failed: 0, total: 0 } })
  }
})

// ========== Task Groups API ==========
app.get('/api/task-groups', (_req, res) => {
  try {
    const dbPath = join(OPENCLAW_HOME, 'task-queue.db')
    if (!existsSync(dbPath)) {
      return res.json([])
    }
    
    const db = new Database(dbPath, { readonly: true })
    const tasks = db.prepare('SELECT * FROM tasks').all() as any[]
    db.close()
    
    const groups = new Map<string, any[]>()
    
    tasks.forEach(task => {
      if (task.groupId) {
        const group = groups.get(task.groupId) || []
        group.push(task)
        groups.set(task.groupId, group)
      }
    })
    
    const result = Array.from(groups.entries()).map(([id, groupTasks]) => {
      const completed = groupTasks.filter(t => t.status === 'COMPLETED').length
      return {
        id,
        name: `任务组 ${id.slice(0, 8)}`,
        totalTasks: groupTasks.length,
        completedTasks: completed,
        progress: Math.round((completed / groupTasks.length) * 100),
        status: completed === groupTasks.length ? 'completed' : groupTasks.some(t => t.status === 'RUNNING') ? 'running' : 'pending'
      }
    })
    
    res.json(result)
  } catch (error: any) {
    res.json({ error: error.message })
  }
})

app.listen(PORT, () => {
  console.log(`🚀 API Server running on http://localhost:${PORT}`)
})
