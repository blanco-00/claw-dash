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

// ========== Database Initialization ==========
const dbPath = join(OPENCLAW_HOME, 'agentforge.db')
let db: Database.Database

function initDatabase() {
  try {
    db = new Database(dbPath)

    // Create agents metadata table
    db.exec(`
      CREATE TABLE IF NOT EXISTS agents (
        id TEXT PRIMARY KEY,
        name TEXT NOT NULL,
        title TEXT,
        role TEXT,
        description TEXT,
        parent_id TEXT,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (parent_id) REFERENCES agents(id)
      )
    `)

    // Create agent relationships table
    db.exec(`
      CREATE TABLE IF NOT EXISTS agent_relationships (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        parent_id TEXT NOT NULL,
        child_id TEXT NOT NULL,
        relationship_type TEXT DEFAULT 'direct',
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (parent_id) REFERENCES agents(id),
        FOREIGN KEY (child_id) REFERENCES agents(id),
        UNIQUE(parent_id, child_id)
      )
    `)

    // Create token_usage table
    db.exec(`
      CREATE TABLE IF NOT EXISTS token_usage (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        agent_id TEXT,
        task_id TEXT,
        model_name TEXT,
        input_tokens INTEGER DEFAULT 0,
        output_tokens INTEGER DEFAULT 0,
        cost REAL DEFAULT 0,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
      )
    `)

    // Create transaction_log table
    db.exec(`
      CREATE TABLE IF NOT EXISTS transaction_log (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
        operation TEXT NOT NULL,
        entity_type TEXT,
        entity_id TEXT,
        details TEXT,
        user_id TEXT
      )
    `)

    // Create model_pricing table
    db.exec(`
      CREATE TABLE IF NOT EXISTS model_pricing (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        model_name TEXT UNIQUE NOT NULL,
        price_per_1k_input REAL DEFAULT 0,
        price_per_1k_output REAL DEFAULT 0,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
      )
    `)

    // Insert default model pricing
    const defaultModels = [
      { model_name: 'gpt-4', price_per_1k_input: 0.03, price_per_1k_output: 0.06 },
      { model_name: 'gpt-4-turbo', price_per_1k_input: 0.01, price_per_1k_output: 0.03 },
      { model_name: 'gpt-3.5-turbo', price_per_1k_input: 0.001, price_per_1k_output: 0.002 },
      { model_name: 'claude-3-opus', price_per_1k_input: 0.015, price_per_1k_output: 0.075 },
      { model_name: 'claude-3-sonnet', price_per_1k_input: 0.003, price_per_1k_output: 0.015 }
    ]

    const insertModel = db.prepare(`
      INSERT OR IGNORE INTO model_pricing (model_name, price_per_1k_input, price_per_1k_output)
      VALUES (?, ?, ?)
    `)

    for (const model of defaultModels) {
      insertModel.run(model.model_name, model.price_per_1k_input, model.price_per_1k_output)
    }

    console.log('✅ Database initialized with agentforge tables')
  } catch (error: any) {
    console.error('❌ Database initialization failed:', error.message)
  }
}

initDatabase()

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

app.post('/api/agents', (req, res) => {
  try {
    const { name, title, role, description, parent_id } = req.body
    const id = name.toLowerCase().replace(/\s+/g, '-')

    db.prepare(
      `
      INSERT INTO agents (id, name, title, role, description, parent_id)
      VALUES (?, ?, ?, ?, ?, ?)
    `
    ).run(id, name, title || '', role || '', description || '', parent_id || null)

    db.prepare(
      `
      INSERT INTO transaction_log (operation, entity_type, entity_id, details)
      VALUES (?, ?, ?, ?)
    `
    ).run('create', 'agent', id, JSON.stringify({ name, title, role, description }))

    res.json({ success: true, id, message: 'Agent created successfully' })
  } catch (error: any) {
    res.json({ error: error.message })
  }
})

app.put('/api/agents/:id', (req, res) => {
  try {
    const { id } = req.params
    const { name, title, role, description, parent_id } = req.body

    db.prepare(
      `
      UPDATE agents SET name = ?, title = ?, role = ?, description = ?, parent_id = ?, updated_at = CURRENT_TIMESTAMP
      WHERE id = ?
    `
    ).run(name, title || '', role || '', description || '', parent_id || null, id)

    db.prepare(
      `
      INSERT INTO transaction_log (operation, entity_type, entity_id, details)
      VALUES (?, ?, ?, ?)
    `
    ).run('update', 'agent', id, JSON.stringify({ name, title, role, description }))

    res.json({ success: true, message: 'Agent updated successfully' })
  } catch (error: any) {
    res.json({ error: error.message })
  }
})

app.delete('/api/agents/:id', (req, res) => {
  try {
    const { id } = req.params

    db.prepare('DELETE FROM agent_relationships WHERE parent_id = ? OR child_id = ?').run(id, id)
    db.prepare('DELETE FROM agents WHERE id = ?').run(id)

    db.prepare(
      `
      INSERT INTO transaction_log (operation, entity_type, entity_id, details)
      VALUES (?, ?, ?, ?)
    `
    ).run('delete', 'agent', id, JSON.stringify({ deleted_at: new Date().toISOString() }))

    res.json({ success: true, message: 'Agent deleted successfully' })
  } catch (error: any) {
    res.json({ error: error.message })
  }
})

app.get('/api/agents/:id/relationships', (req, res) => {
  try {
    const { id } = req.params

    const parents = db
      .prepare(
        `
      SELECT a.* FROM agents a
      JOIN agent_relationships ar ON a.id = ar.parent_id
      WHERE ar.child_id = ?
    `
      )
      .all(id)

    const children = db
      .prepare(
        `
      SELECT a.* FROM agents a
      JOIN agent_relationships ar ON a.id = ar.child_id
      WHERE ar.parent_id = ?
    `
      )
      .all(id)

    const relationships = db
      .prepare(
        `
      SELECT * FROM agent_relationships WHERE parent_id = ? OR child_id = ?
    `
      )
      .all(id, id)

    res.json({ parents, children, relationships })
  } catch (error: any) {
    res.json({ error: error.message })
  }
})

app.post('/api/agents/:id/relationships', (req, res) => {
  try {
    const { id } = req.params
    const { child_id, relationship_type } = req.body

    db.prepare(
      `
      INSERT OR REPLACE INTO agent_relationships (parent_id, child_id, relationship_type)
      VALUES (?, ?, ?)
    `
    ).run(id, child_id, relationship_type || 'direct')

    db.prepare(
      `
      INSERT INTO transaction_log (operation, entity_type, entity_id, details)
      VALUES (?, ?, ?, ?)
    `
    ).run('link', 'relationship', id, JSON.stringify({ child_id, relationship_type }))

    res.json({ success: true, message: 'Relationship created successfully' })
  } catch (error: any) {
    res.json({ error: error.message })
  }
})

app.get('/api/agents-metadata', (_req, res) => {
  try {
    const agents = db.prepare('SELECT * FROM agents ORDER BY created_at DESC').all()
    res.json(agents)
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
      if (line.includes('Key') && line.includes('Kind') && line.includes('Age')) {
        inSessions = true
        continue
      }
      if (inSessions && (line.includes('┘') || line.includes('└'))) {
        break
      }
      if (inSessions && line.includes('│') && !line.includes('──') && !line.includes('─')) {
        const parts = line
          .split('│')
          .map(p => p.trim())
          .filter(Boolean)
        if (parts.length >= 4 && parts[0] && !parts[0].startsWith('=')) {
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
      return res.json({
        tasks: [],
        counts: { pending: 0, running: 0, completed: 0, failed: 0, total: 0 }
      })
    }

    const db = new Database(dbPath, { readonly: true })

    const counts = db
      .prepare(
        `
      SELECT status, COUNT(*) as count FROM tasks GROUP BY status
    `
      )
      .all() as { status: string; count: number }[]

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
    res.json({
      error: error.message,
      tasks: [],
      counts: { pending: 0, running: 0, completed: 0, failed: 0, total: 0 }
    })
  }
})

app.post('/api/tasks/:id/start', (req, res) => {
  try {
    const { id } = req.params
    execSync(`${OPENCLAW_BIN} task start ${id}`, { encoding: 'utf-8', timeout: 30000 })

    db.prepare(
      `
      INSERT INTO transaction_log (operation, entity_type, entity_id, details)
      VALUES (?, ?, ?, ?)
    `
    ).run('task_start', 'task', id, JSON.stringify({ started_at: new Date().toISOString() }))

    res.json({ success: true, message: 'Task started successfully' })
  } catch (error: any) {
    res.json({ error: error.message })
  }
})

app.post('/api/tasks/:id/stop', (req, res) => {
  try {
    const { id } = req.params
    execSync(`${OPENCLAW_BIN} task stop ${id}`, { encoding: 'utf-8', timeout: 30000 })

    db.prepare(
      `
      INSERT INTO transaction_log (operation, entity_type, entity_id, details)
      VALUES (?, ?, ?, ?)
    `
    ).run('task_stop', 'task', id, JSON.stringify({ stopped_at: new Date().toISOString() }))

    res.json({ success: true, message: 'Task stopped successfully' })
  } catch (error: any) {
    res.json({ error: error.message })
  }
})

app.post('/api/tasks/:id/retry', (req, res) => {
  try {
    const { id } = req.params
    execSync(`${OPENCLAW_BIN} task retry ${id}`, { encoding: 'utf-8', timeout: 30000 })

    db.prepare(
      `
      INSERT INTO transaction_log (operation, entity_type, entity_id, details)
      VALUES (?, ?, ?, ?)
    `
    ).run('task_retry', 'task', id, JSON.stringify({ retried_at: new Date().toISOString() }))

    res.json({ success: true, message: 'Task retried successfully' })
  } catch (error: any) {
    res.json({ error: error.message })
  }
})

app.post('/api/tasks/:id/pause', (req, res) => {
  try {
    const { id } = req.params
    execSync(`${OPENCLAW_BIN} task pause ${id}`, { encoding: 'utf-8', timeout: 30000 })

    db.prepare(
      `
      INSERT INTO transaction_log (operation, entity_type, entity_id, details)
      VALUES (?, ?, ?, ?)
    `
    ).run('task_pause', 'task', id, JSON.stringify({ paused_at: new Date().toISOString() }))

    res.json({ success: true, message: 'Task paused successfully' })
  } catch (error: any) {
    res.json({ error: error.message })
  }
})

app.post('/api/tasks/:id/resume', (req, res) => {
  try {
    const { id } = req.params
    execSync(`${OPENCLAW_BIN} task resume ${id}`, { encoding: 'utf-8', timeout: 30000 })

    db.prepare(
      `
      INSERT INTO transaction_log (operation, entity_type, entity_id, details)
      VALUES (?, ?, ?, ?)
    `
    ).run('task_resume', 'task', id, JSON.stringify({ resumed_at: new Date().toISOString() }))

    res.json({ success: true, message: 'Task resumed successfully' })
  } catch (error: any) {
    res.json({ error: error.message })
  }
})

// ========== Token Stats API ==========
app.get('/api/tokens/stats', (req, res) => {
  try {
    const { agent_id, task_id, start_date, end_date } = req.query

    let query = 'SELECT * FROM token_usage WHERE 1=1'
    const params: any[] = []

    if (agent_id) {
      query += ' AND agent_id = ?'
      params.push(agent_id)
    }
    if (task_id) {
      query += ' AND task_id = ?'
      params.push(task_id)
    }
    if (start_date) {
      query += ' AND created_at >= ?'
      params.push(start_date)
    }
    if (end_date) {
      query += ' AND created_at <= ?'
      params.push(end_date)
    }

    query += ' ORDER BY created_at DESC'

    const usage = db.prepare(query).all(...params)

    const totalInput = usage.reduce((sum: number, r: any) => sum + (r.input_tokens || 0), 0)
    const totalOutput = usage.reduce((sum: number, r: any) => sum + (r.output_tokens || 0), 0)
    const totalCost = usage.reduce((sum: number, r: any) => sum + (r.cost || 0), 0)

    const byAgent = db
      .prepare(
        `
      SELECT agent_id, SUM(input_tokens) as input_tokens, SUM(output_tokens) as output_tokens, SUM(cost) as cost
      FROM token_usage
      GROUP BY agent_id
    `
      )
      .all()

    const byModel = db
      .prepare(
        `
      SELECT model_name, SUM(input_tokens) as input_tokens, SUM(output_tokens) as output_tokens, SUM(cost) as cost
      FROM token_usage
      GROUP BY model_name
    `
      )
      .all()

    res.json({
      usage,
      summary: {
        total_input_tokens: totalInput,
        total_output_tokens: totalOutput,
        total_cost: totalCost,
        total_requests: usage.length
      },
      by_agent: byAgent,
      by_model: byModel
    })
  } catch (error: any) {
    res.json({ error: error.message })
  }
})

app.post('/api/tokens/record', (req, res) => {
  try {
    const { agent_id, task_id, model_name, input_tokens, output_tokens } = req.body

    const pricing = db
      .prepare('SELECT * FROM model_pricing WHERE model_name = ?')
      .get(model_name) as any
    let cost = 0
    if (pricing) {
      cost =
        (input_tokens / 1000) * pricing.price_per_1k_input +
        (output_tokens / 1000) * pricing.price_per_1k_output
    }

    db.prepare(
      `
      INSERT INTO token_usage (agent_id, task_id, model_name, input_tokens, output_tokens, cost)
      VALUES (?, ?, ?, ?, ?, ?)
    `
    ).run(agent_id, task_id, model_name, input_tokens, output_tokens, cost)

    res.json({ success: true, cost })
  } catch (error: any) {
    res.json({ error: error.message })
  }
})

// ========== Transaction Log API ==========
app.get('/api/transactions', (req, res) => {
  try {
    const { entity_type, entity_id, operation, limit = 100 } = req.query

    let query = 'SELECT * FROM transaction_log WHERE 1=1'
    const params: any[] = []

    if (entity_type) {
      query += ' AND entity_type = ?'
      params.push(entity_type)
    }
    if (entity_id) {
      query += ' AND entity_id = ?'
      params.push(entity_id)
    }
    if (operation) {
      query += ' AND operation = ?'
      params.push(operation)
    }

    query += ' ORDER BY timestamp DESC LIMIT ?'
    params.push(parseInt(limit as string) || 100)

    const transactions = db.prepare(query).all(...params)
    res.json(transactions)
  } catch (error: any) {
    res.json({ error: error.message })
  }
})

// ========== Model Pricing API ==========
app.get('/api/models/pricing', (_req, res) => {
  try {
    const models = db.prepare('SELECT * FROM model_pricing').all()
    res.json(models)
  } catch (error: any) {
    res.json({ error: error.message })
  }
})

app.put('/api/models/pricing/:model_name', (req, res) => {
  try {
    const { model_name } = req.params
    const { price_per_1k_input, price_per_1k_output } = req.body

    db.prepare(
      `
      UPDATE model_pricing SET price_per_1k_input = ?, price_per_1k_output = ?, updated_at = CURRENT_TIMESTAMP
      WHERE model_name = ?
    `
    ).run(price_per_1k_input, price_per_1k_output, model_name)

    res.json({ success: true, message: 'Pricing updated successfully' })
  } catch (error: any) {
    res.json({ error: error.message })
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
        status:
          completed === groupTasks.length
            ? 'completed'
            : groupTasks.some(t => t.status === 'RUNNING')
              ? 'running'
              : 'pending'
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
