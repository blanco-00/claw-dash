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
  database: process.env.DB_NAME || 'agentforge',
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

    console.log('✅ Database initialized')
  } catch (error: any) {
    console.error('❌ Database initialization failed:', error.message)
  }
}

await initDatabase()
