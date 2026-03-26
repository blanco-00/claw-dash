-- V1__baseline_schema_with_seed_data.sql
-- ClawDash Baseline Migration
-- Complete schema + Sample Agent Topology Data
--
-- This migration creates all tables and inserts sample data for demonstration.

-- ============================================================================
-- Table: config_graphs
-- ============================================================================
CREATE TABLE IF NOT EXISTS config_graphs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    version INT DEFAULT 1,
    last_synced_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Table: config_graph_nodes
-- ============================================================================
CREATE TABLE IF NOT EXISTS config_graph_nodes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    graph_id BIGINT NOT NULL,
    agent_id VARCHAR(255) NOT NULL,
    x DOUBLE DEFAULT 0,
    y DOUBLE DEFAULT 0,
    collapsed BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_graph_id (graph_id),
    INDEX idx_agent_id (agent_id),
    CONSTRAINT fk_node_graph FOREIGN KEY (graph_id) REFERENCES config_graphs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Table: config_graph_edges
-- ============================================================================
CREATE TABLE IF NOT EXISTS config_graph_edges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    graph_id BIGINT NOT NULL,
    source_id VARCHAR(255) NOT NULL,
    target_id VARCHAR(255) NOT NULL,
    edge_type VARCHAR(50) NOT NULL,
    label VARCHAR(255),
    enabled BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    decision_mode VARCHAR(20) NOT NULL DEFAULT 'always',
    message_template TEXT,
    source_handle VARCHAR(50) DEFAULT NULL,
    target_handle VARCHAR(50) DEFAULT NULL,
    reply_target VARCHAR(100) DEFAULT NULL,
    reply_template TEXT,
    error_target VARCHAR(100) DEFAULT NULL,
    error_template TEXT,
    INDEX idx_graph_id (graph_id),
    INDEX idx_source_id (source_id),
    INDEX idx_target_id (target_id),
    INDEX idx_edge_type (edge_type),
    CONSTRAINT fk_edge_graph FOREIGN KEY (graph_id) REFERENCES config_graphs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Table: edge_types
-- ============================================================================
CREATE TABLE IF NOT EXISTS edge_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    value VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    default_message_template TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Table: settings
-- ============================================================================
CREATE TABLE IF NOT EXISTS settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scope VARCHAR(64) NOT NULL,
    scope_id VARCHAR(64),
    setting_key VARCHAR(128) NOT NULL,
    setting_value TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_scope_key (scope, scope_id, setting_key),
    INDEX idx_scope (scope),
    INDEX idx_scope_id (scope_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Table: a2a_messages
-- ============================================================================
CREATE TABLE IF NOT EXISTS a2a_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    from_agent_id VARCHAR(255) NOT NULL,
    to_agent_id VARCHAR(255) NOT NULL,
    content TEXT,
    message_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    sent_at DATETIME,
    delivered_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_from_agent (from_agent_id),
    INDEX idx_to_agent (to_agent_id),
    INDEX idx_sent_at (sent_at),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- Seed Data: Default Graph
-- ============================================================================
INSERT INTO config_graphs (name, description, version) 
VALUES ('Default Graph', 'Sample agent topology - Chinese Imperial Court System', 1);

-- ============================================================================
-- Seed Data: Agent Nodes (12 Agents - Imperial Court Structure)
-- ============================================================================
INSERT INTO config_graph_nodes (graph_id, agent_id, x, y, collapsed) VALUES
(1, 'main', 368, 162, 0),
(1, 'menxiasheng', 364, 275, 0),
(1, 'shangshusheng', 357, 395, 0),
(1, 'shangshiju', -107, 508, 0),
(1, 'jinyiwei', 82, 509, 0),
(1, 'shangyaosi', -270, 507, 0),
(1, 'gongbu', 209, 633, 0),
(1, 'hubu', 700, 635, 0),
(1, 'bingbu', 1048, 635, 0),
(1, 'libu3', 369, 634, 0),
(1, 'libu4', 529, 636, 0),
(1, 'xingbu', 873, 636, 0);

-- ============================================================================
-- Seed Data: Edge Types
-- ============================================================================
INSERT INTO edge_types (value, name, description, default_message_template) VALUES
('always', '始终', '无条件发送', ''),
('task', '任务', '委托任务给目标agent', '请帮我完成以下任务:\n{original_message}'),
('reply', '回复', '完成任务后回复给某agent', '任务已完成:\n{task_result}'),
('error', '错误', '发生错误时通知某agent', '发生错误:\n{error_message}');

-- ============================================================================
-- Seed Data: Agent Connections
-- ============================================================================
INSERT INTO config_graph_edges (id, graph_id, source_id, target_id, edge_type, enabled, decision_mode, message_template, source_handle, target_handle) VALUES
(74, 1, 'main', 'menxiasheng', 'task', 1, 'llm', '触发条件：需要审核把关、需要决策建议、调研分析、复杂任务评估\n【门下省】\n瑾儿皇后有请：\n{original_message}\n烦请卿酒姐姐审核把关，给出决策建议～', 'bottom', 'top'),
(75, 1, 'main', 'shangshiju', 'task', 1, 'llm', '触发条件：饮食安排、起居照料、服饰搭配、生活琐事\n【尚食局】\n主人吩咐：\n{original_message}\n照料好主人的日常起居哦～', 'left', 'top'),
(76, 1, 'menxiasheng', 'shangshusheng', 'task', 1, 'llm', '触发条件：审核通过、需要分发给六部执行、任务已决策\n【门下省】\n卿酒已审核完毕，请红袖姐姐分发执行：\n{original_message}\n执行要点：\n- 分配给对应部门\n- 监督执行', 'bottom', 'top'),
(77, 1, 'shangshusheng', 'gongbu', 'task', 1, 'llm', '触发条件：技术任务、工程开发、工具制作、系统维护\n【工部】\n红袖姐姐分发任务：\n{original_message}\n执行后回报结果～', 'bottom', 'top'),
(79, 1, 'shangshusheng', 'libu4', 'task', 1, 'llm', '触发条件：人事调配、人员考核、人才培养、人才发掘\n【吏部】\n红袖姐姐分发任务：\n{original_message}\n珊瑚姐姐安排一下哦～', 'bottom', 'top'),
(80, 1, 'shangshusheng', 'hubu', 'task', 1, 'llm', '触发条件：财务预算、支出审核、资产配置、费用审批\n【户部】\n红袖姐姐分发任务：\n{original_message}\n琉璃姐姐帮忙把关一下财务哦～', 'bottom', 'top'),
(81, 1, 'shangshusheng', 'xingbu', 'task', 1, 'llm', '触发条件：法务审核、规则制定、合同审查、违规处理\n【刑部】\n红袖姐姐分发任务：\n{original_message}\n如意姐姐帮忙把关一下哦～', 'bottom', 'top'),
(82, 1, 'shangshusheng', 'bingbu', 'task', 1, 'llm', '触发条件：安全保障、风险预警、安全审查、威胁评估\n【兵部】\n红袖姐姐分发任务：\n{original_message}\n魅羽姐姐帮忙评估一下安全风险哦～', 'bottom', 'top'),
(83, 1, 'shangshusheng', 'libu3', 'task', 1, 'llm', '触发条件：外交事务、文化推广、新闻推送、知识分享\n【礼部】\n红袖姐姐分发任务：\n{original_message}\n书瑶姐姐安排一下哦～', 'bottom', 'top'),
(84, 1, 'main', 'jinyiwei', 'task', 1, 'llm', '触发条件：简单查询，信息收集 / 跑腿、传话 / 一次性执行任务 / 用户明确说"快一点"\n【急件】锦衣卫亲启：\n{original_message}\n执行后直接回报结果。', 'left', 'top'),
(85, 1, 'main', 'shangyaosi', 'task', 1, 'llm', '触发条件：健康咨询、医疗保健、养生调理、身体状况\n【尚药司】\n主人吩咐：\n{original_message}\n允贤会细心照料主人的健康哦～', 'left', 'top');

-- ============================================================================
-- Seed Data: Settings
-- ============================================================================
INSERT INTO settings (scope, scope_id, setting_key, setting_value) VALUES
('global', NULL, 'autoSave', 'true'),
('global', NULL, 'graphAutoSave', 'false');
