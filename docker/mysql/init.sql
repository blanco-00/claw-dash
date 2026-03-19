-- AgentForge Database Initialization Script

CREATE DATABASE IF NOT EXISTS agentforge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE agentforge;

-- Agents metadata table (for Express backend)
CREATE TABLE IF NOT EXISTS agents (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    title VARCHAR(255),
    role VARCHAR(255),
    description TEXT,
    parent_id VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES agents(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Agent relationships table
CREATE TABLE IF NOT EXISTS agent_relationships (
    id INT AUTO_INCREMENT PRIMARY KEY,
    parent_id VARCHAR(255) NOT NULL,
    child_id VARCHAR(255) NOT NULL,
    relationship_type VARCHAR(50) DEFAULT 'direct',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES agents(id) ON DELETE CASCADE,
    FOREIGN KEY (child_id) REFERENCES agents(id) ON DELETE CASCADE,
    UNIQUE KEY unique_relationship (parent_id, child_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Token usage table
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
    INDEX idx_task_id (task_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Transaction log table
CREATE TABLE IF NOT EXISTS transaction_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    operation VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50),
    entity_id VARCHAR(255),
    details TEXT,
    user_id VARCHAR(255),
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Model pricing table
CREATE TABLE IF NOT EXISTS model_pricing (
    id INT AUTO_INCREMENT PRIMARY KEY,
    model_name VARCHAR(255) UNIQUE NOT NULL,
    price_per_1k_input DECIMAL(10, 6) DEFAULT 0,
    price_per_1k_output DECIMAL(10, 6) DEFAULT 0,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default model pricing
INSERT INTO model_pricing (model_name, price_per_1k_input, price_per_1k_output) VALUES
    ('gpt-4', 0.03, 0.06),
    ('gpt-4-turbo', 0.01, 0.03),
    ('gpt-3.5-turbo', 0.001, 0.002),
    ('claude-3-opus', 0.015, 0.075),
    ('claude-3-sonnet', 0.003, 0.015)
ON DUPLICATE KEY UPDATE model_name = model_name;
