-- Agent Task Binding table for storing agent to task type bindings
CREATE TABLE IF NOT EXISTS agent_task_binding (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    agent_name VARCHAR(128) NOT NULL COMMENT 'Agent name from OpenClaw',
    task_type VARCHAR(64) NOT NULL COMMENT 'Task type name',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_agent_task (agent_name, task_type),
    INDEX idx_agent_name (agent_name),
    INDEX idx_task_type (task_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent to task type binding configuration';
