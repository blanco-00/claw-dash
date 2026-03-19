import express from 'express'
import cors from 'cors'
import { execSync } from 'child_process'
import { existsSync, readdirSync, readFileSync, statSync } from 'fs'
import { join } from 'path'
import mysql from 'mysql2/promise'

const app = express()
const PORT = process.env.PORT ? parseInt(process.env.PORT) : 3001

app.use(cors())
app.use(express.json())

const OPENCLAW_HOME = process.env.OPENCLAW_HOME || '/Users/hannah/.openclaw'
const OPENCLAW_BIN = process.env.OPENCLAW_BIN || '/Users/hannah/.npm-global/bin/openclaw'

const dbConfig = {
  host: process.env.DB_HOST || 'localhost',
  port: parseInt(process.env.DB_PORT || '3306'),
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD || '',
  database: process.env.DB_NAME || 'clawdash',
  waitForConnections: true,
  connectionLimit: 10
}

let pool: mysql.Pool

async function initDatabase() {
  try {
    pool = mysql.createPool(dbConfig)
    console.log('✅ Connected to MySQL database')

    await pool.execute(`
      CREATE TABLE IF NOT EXISTS agents (
        id VARCHAR(255) PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        title VARCHAR(255),
        role VARCHAR(255),
        description TEXT,
        parent_id VARCHAR(255),
        is_template BOOLEAN DEFAULT FALSE,
        is_active BOOLEAN DEFAULT TRUE,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
      )
    `)

    await pool.execute(`
      CREATE TABLE IF NOT EXISTS agent_relationships (
        id INT AUTO_INCREMENT PRIMARY KEY,
        parent_id VARCHAR(255) NOT NULL,
        child_id VARCHAR(255) NOT NULL,
        relationship_type VARCHAR(50) DEFAULT 'direct',
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        UNIQUE KEY unique_relationship (parent_id, child_id)
      )
    `)

    await pool.execute(`
      CREATE TABLE IF NOT EXISTS token_usage (
        id INT AUTO_INCREMENT PRIMARY KEY,
        agent_id VARCHAR(255),
        task_id VARCHAR(255),
        model_name VARCHAR(255),
        input_tokens INT DEFAULT 0,
        output_tokens INT DEFAULT 0,
        cost DECIMAL(10, 6) DEFAULT 0,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        INDEX idx_agent_id (agent_id),
        INDEX idx_task_id (task_id)
      )
    `)

    await pool.execute(`
      CREATE TABLE IF NOT EXISTS transaction_log (
        id INT AUTO_INCREMENT PRIMARY KEY,
        timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
        operation VARCHAR(50) NOT NULL,
        entity_type VARCHAR(50),
        entity_id VARCHAR(255),
        details TEXT,
        user_id VARCHAR(255),
        INDEX idx_entity (entity_type, entity_id)
      )
    `)

    await pool.execute(`
      CREATE TABLE IF NOT EXISTS model_pricing (
        id INT AUTO_INCREMENT PRIMARY KEY,
        model_name VARCHAR(255) UNIQUE NOT NULL,
        price_per_1k_input DECIMAL(10, 6) DEFAULT 0,
        price_per_1k_output DECIMAL(10, 6) DEFAULT 0,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
      )
    `)

    const defaultModels = [
      { model_name: 'gpt-4', price_per_1k_input: 0.03, price_per_1k_output: 0.06 },
      { model_name: 'gpt-4-turbo', price_per_1k_input: 0.01, price_per_1k_output: 0.03 },
      { model_name: 'gpt-3.5-turbo', price_per_1k_input: 0.001, price_per_1k_output: 0.002 },
      { model_name: 'claude-3-opus', price_per_1k_input: 0.015, price_per_1k_output: 0.075 },
      { model_name: 'claude-3-sonnet', price_per_1k_input: 0.003, price_per_1k_output: 0.015 }
    ]

    for (const model of defaultModels) {
      await pool.execute(
        'INSERT IGNORE INTO model_pricing (model_name, price_per_1k_input, price_per_1k_output) VALUES (?, ?, ?)',
        [model.model_name, model.price_per_1k_input, model.price_per_1k_output]
      )
    }

    const defaultTemplates = [
      {
        id: 'main',
        name: '瑾儿',
        title: '皇后',
        role: '中书省决策',
        description: '女儿国主Agent，负责统筹决策',
        parent_id: null,
        is_template: true
      },
      {
        id: 'menxiasheng',
        name: '卿酒',
        title: '皇贵妃',
        role: '门下省审核',
        description: '负责审核和把控内容质量',
        parent_id: 'main',
        is_template: true
      },
      {
        id: 'shangshusheng',
        name: '红袖',
        title: '贵妃',
        role: '尚书省分发',
        description: '负责任务分发和调度',
        parent_id: 'main',
        is_template: true
      },
      {
        id: 'jinyiwei',
        name: '灵鸢',
        title: '贵人',
        role: '锦衣卫督查',
        description: '负责监督和督查工作',
        parent_id: 'main',
        is_template: true
      },
      {
        id: 'libu4',
        name: '珊瑚',
        title: '妃',
        role: '吏部人事',
        description: '负责人事管理和调配',
        parent_id: 'main',
        is_template: true
      },
      {
        id: 'hubu',
        name: '琉璃',
        title: '妃',
        role: '户部财务',
        description: '负责财务和预算管理',
        parent_id: 'main',
        is_template: true
      },
      {
        id: 'libu3',
        name: '书瑶',
        title: '妃',
        role: '礼部外交',
        description: '负责对外交流和外交',
        parent_id: 'main',
        is_template: true
      },
      {
        id: 'bingbu',
        name: '魅羽',
        title: '妃',
        role: '兵部安全',
        description: '负责安全保障工作',
        parent_id: 'main',
        is_template: true
      },
      {
        id: 'xingbu',
        name: '如意',
        title: '嫔',
        role: '刑部法务',
        description: '负责法务和合规',
        parent_id: 'main',
        is_template: true
      },
      {
        id: 'gongbu',
        name: '灵犀',
        title: '嫔',
        role: '工部技术',
        description: '负责技术研发管理',
        parent_id: 'shangshusheng',
        is_template: true
      },
      {
        id: 'jishu',
        name: '青岚',
        title: '丫鬟',
        role: '工部研发',
        description: '负责技术研发实现',
        parent_id: 'gongbu',
        is_template: true
      },
      {
        id: 'shangshiju',
        name: '婉儿',
        title: '丫鬟',
        role: '尚食局',
        description: '负责饮食起居安排',
        parent_id: 'shangshusheng',
        is_template: true
      },
      {
        id: 'shangyaosi',
        name: '允贤',
        title: '丫鬟',
        role: '尚药司',
        description: '负责健康医疗管理',
        parent_id: 'shangshusheng',
        is_template: true
      },
      {
        id: 'yanfa',
        name: '研发动态',
        title: '研发',
        role: '技术研发',
        description: '负责技术研发',
        parent_id: 'main',
        is_template: true
      }
    ]

    for (const tpl of defaultTemplates) {
      await pool.execute(
        'INSERT IGNORE INTO agents (id, name, title, role, description, parent_id, is_template) VALUES (?, ?, ?, ?, ?, ?, ?)',
        [tpl.id, tpl.name, tpl.title, tpl.role, tpl.description, tpl.parent_id, tpl.is_template]
      )
    }

    console.log('✅ Database initialized')
  } catch (error: any) {
    console.error('❌ Database initialization failed:', error.message)
  }
}

await initDatabase()

// API Routes
app.get('/api/gateway/status', (req, res) => {
  try {
    const gatewayPath = join(OPENCLAW_HOME, 'gateway.pid')
    const pid = existsSync(gatewayPath) ? readFileSync(gatewayPath, 'utf-8').trim() : null
    res.json({ running: !!pid, pid: pid || '-' })
  } catch {
    res.json({ running: false, pid: '-' })
  }
})

app.get('/api/agents', async (req, res) => {
  try {
    const [rows] = await pool.query('SELECT * FROM agents ORDER BY created_at DESC')
    res.json(rows)
  } catch (error: any) {
    res.status(500).json({ error: error.message })
  }
})

app.get('/api/cron', async (req, res) => {
  try {
    res.json([])
  } catch (error: any) {
    res.status(500).json({ error: error.message })
  }
})

app.get('/api/sessions', async (req, res) => {
  try {
    res.json([])
  } catch (error: any) {
    res.status(500).json({ error: error.message })
  }
})

app.get('/api/tasks', async (req, res) => {
  try {
    res.json({ pending: 0, running: 0, completed: 0 })
  } catch (error: any) {
    res.status(500).json({ error: error.message })
  }
})

app.get('/api/tokens/stats', async (req, res) => {
  try {
    res.json({ total: 0, cost: 0 })
  } catch (error: any) {
    res.status(500).json({ error: error.message })
  }
})

app.get('/api/health', (req, res) => {
  res.json({ status: 'ok' })
})

app.get('/api/agent-templates', async (req, res) => {
  try {
    const [rows] = await pool.query('SELECT * FROM agents WHERE is_template = TRUE ORDER BY id ASC')
    res.json(rows)
  } catch (error: any) {
    res.status(500).json({ error: error.message })
  }
})

app.post('/api/agent-templates', async (req, res) => {
  try {
    const { id, name, title, role, description, parent_id, is_active } = req.body
    await pool.execute(
      'INSERT INTO agents (id, name, title, role, description, parent_id, is_template, is_active) VALUES (?, ?, ?, ?, ?, ?, TRUE, ?)',
      [id, name, title, role, description, parent_id, is_active ?? true]
    )
    res.json({ success: true })
  } catch (error: any) {
    res.status(500).json({ error: error.message })
  }
})

app.put('/api/agent-templates/:id', async (req, res) => {
  try {
    const { id } = req.params
    const { name, title, role, description, parent_id, is_active } = req.body
    await pool.execute(
      'UPDATE agents SET name=?, title=?, role=?, description=?, parent_id=?, is_active=? WHERE id=? AND is_template=TRUE',
      [name, title, role, description, parent_id, is_active, id]
    )
    res.json({ success: true })
  } catch (error: any) {
    res.status(500).json({ error: error.message })
  }
})

app.delete('/api/agent-templates/:id', async (req, res) => {
  try {
    const { id } = req.params
    await pool.execute('DELETE FROM agents WHERE id = ? AND is_template = TRUE', [id])
    res.json({ success: true })
  } catch (error: any) {
    res.status(500).json({ error: error.message })
  }
})

app.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 Server running on port ${PORT}`)
})
