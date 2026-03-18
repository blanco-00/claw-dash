-- ClawDash Database Initialization Script

CREATE DATABASE IF NOT EXISTS clawdash DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE clawdash;

-- Users Table
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT 'Username',
    `password` VARCHAR(255) NOT NULL COMMENT 'Encrypted password',
    `email` VARCHAR(100) COMMENT 'Email address',
    `nickname` VARCHAR(50) COMMENT 'Display name',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT 'User role',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 1=active, 0=inactive',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Soft delete flag',
    PRIMARY KEY (`id`),
    INDEX `idx_username` (`username`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User table';

-- Tasks Table
CREATE TABLE IF NOT EXISTS `tasks` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `task_id` VARCHAR(64) NOT NULL UNIQUE COMMENT 'Unique task ID',
    `type` VARCHAR(50) NOT NULL COMMENT 'Task type',
    `payload` TEXT NOT NULL COMMENT 'Task payload JSON',
    `priority` INT NOT NULL DEFAULT 0 COMMENT 'Task priority',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'Task status',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT 'Current retry count',
    `max_retries` INT NOT NULL DEFAULT 3 COMMENT 'Max retry attempts',
    `claimed_at` DATETIME COMMENT 'Claim timestamp',
    `claimed_by` VARCHAR(64) COMMENT 'Worker ID that claimed',
    `started_at` DATETIME COMMENT 'Execution start time',
    `completed_at` DATETIME COMMENT 'Completion time',
    `scheduled_at` DATETIME COMMENT 'Scheduled execution time',
    `result` TEXT COMMENT 'Execution result JSON',
    `error` TEXT COMMENT 'Error message',
    `depends_on` VARCHAR(500) COMMENT 'JSON array of dependent task IDs',
    `order_index` INT DEFAULT 0 COMMENT 'Execution order',
    `source_channel` VARCHAR(50) COMMENT 'Task source',
    `source_conversation` VARCHAR(100) COMMENT 'Conversation ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_task_id` (`task_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_priority` (`priority`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Task queue table';

-- Task Groups Table
CREATE TABLE IF NOT EXISTS `task_groups` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `group_id` VARCHAR(64) NOT NULL UNIQUE,
    `name` VARCHAR(100) NOT NULL,
    `description` VARCHAR(500),
    `task_ids` TEXT COMMENT 'JSON array of task IDs',
    `status` VARCHAR(20) DEFAULT 'PENDING',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Task group table';

-- Cron Jobs Table
CREATE TABLE IF NOT EXISTS `cron_jobs` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `job_id` VARCHAR(64) NOT NULL UNIQUE,
    `name` VARCHAR(100) NOT NULL,
    `cron_expression` VARCHAR(50) NOT NULL COMMENT 'Cron expression',
    `task_template` TEXT NOT NULL COMMENT 'Task template JSON',
    `enabled` TINYINT NOT NULL DEFAULT 1,
    `last_run_at` DATETIME,
    `next_run_at` DATETIME,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_enabled` (`enabled`),
    INDEX `idx_next_run` (`next_run_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Cron job table';

-- Agents Table
CREATE TABLE IF NOT EXISTS `agents` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `agent_id` VARCHAR(64) NOT NULL UNIQUE,
    `name` VARCHAR(100) NOT NULL,
    `role` VARCHAR(50) NOT NULL COMMENT 'Agent role',
    `description` VARCHAR(500),
    `config` TEXT COMMENT 'Agent configuration JSON',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    `last_active_at` DATETIME,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_agent_id` (`agent_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent table';

-- Finance Records Table
CREATE TABLE IF NOT EXISTS `privy_finance` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `record_id` VARCHAR(64) NOT NULL UNIQUE,
    `type` VARCHAR(20) NOT NULL COMMENT 'INCOME or EXPENSE',
    `amount` DECIMAL(12,2) NOT NULL,
    `category` VARCHAR(50) NOT NULL COMMENT 'Expense category',
    `description` VARCHAR(500),
    `record_date` DATE NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_type` (`type`),
    INDEX `idx_category` (`category`),
    INDEX `idx_record_date` (`record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Finance record table';

-- Finance Budget Table
CREATE TABLE IF NOT EXISTS `privy_finance_budget` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `category` VARCHAR(50) NOT NULL,
    `budget_amount` DECIMAL(12,2) NOT NULL,
    `month` VARCHAR(7) NOT NULL COMMENT 'YYYY-MM',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_category_month` (`category`, `month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Budget table';

-- OpenClaw Config Table
CREATE TABLE IF NOT EXISTS `openclaw_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `config_key` VARCHAR(100) NOT NULL UNIQUE,
    `config_value` TEXT,
    `description` VARCHAR(500),
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OpenClaw configuration table';

-- API Tokens Table
CREATE TABLE IF NOT EXISTS `api_tokens` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `token` VARCHAR(100) NOT NULL UNIQUE,
    `name` VARCHAR(100) NOT NULL,
    `description` VARCHAR(500),
    `permissions` VARCHAR(500),
    `expires_at` DATETIME,
    `last_used_at` DATETIME,
    `enabled` TINYINT NOT NULL DEFAULT 1,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_token` (`token`),
    INDEX `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API token table';
