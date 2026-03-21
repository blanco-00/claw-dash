-- V2__settings_table.sql
-- Key-Value Settings Table for flexible configuration storage
--
-- Supports: global, graph, user, or any other scope
-- Examples:
--   scope='graph', scope_id='1', key='autoSave', value='true'
--   scope='global', scope_id=NULL, key='theme', value='dark'

CREATE TABLE IF NOT EXISTS settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scope VARCHAR(64) NOT NULL COMMENT 'Scope: global, graph, user, etc.',
    scope_id VARCHAR(64) COMMENT 'ID within scope (NULL for global)',
    setting_key VARCHAR(128) NOT NULL COMMENT 'Configuration key name',
    setting_value TEXT COMMENT 'Configuration value (stored as string)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_scope_key (scope, scope_id, setting_key),
    INDEX idx_scope (scope),
    INDEX idx_scope_id (scope_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
