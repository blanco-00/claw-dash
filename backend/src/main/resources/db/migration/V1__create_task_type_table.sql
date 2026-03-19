-- Create task_type table for extensible task type configuration
CREATE TABLE IF NOT EXISTS task_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT 'Task type identifier',
    display_name VARCHAR(100) NOT NULL COMMENT 'Display name for UI',
    description VARCHAR(255) COMMENT 'Description of what this task type does',
    enabled BOOLEAN DEFAULT TRUE COMMENT 'Whether this task type is available for new tasks',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation timestamp',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',
    INDEX idx_enabled (enabled),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Task type configuration table';

-- Seed default task types
INSERT INTO task_type (name, display_name, description, enabled) VALUES
('agent-execute', 'Agent Execute', 'Execute an agent task', TRUE),
('data-sync', 'Data Sync', 'Synchronize data between systems', TRUE),
('notification', 'Notification', 'Send notification to users', TRUE),
('cleanup', 'Cleanup', 'Clean up old data or resources', TRUE);
