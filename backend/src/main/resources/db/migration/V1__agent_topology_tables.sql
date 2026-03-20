-- V1__agent_topology_tables.sql
-- Agent Topology Management - Initial Table Setup
-- 
-- This migration creates the core tables for agent topology management:
-- - config_graphs: Stores different configuration graph versions
-- - config_graph_nodes: Agent nodes within a graph with positions
-- - config_graph_edges: Connections between agents with edge types
-- - a2a_messages: Agent-to-agent message history

-- Create config_graphs table
CREATE TABLE IF NOT EXISTS config_graphs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    version INT DEFAULT 1,
    last_synced_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create config_graph_nodes table
CREATE TABLE IF NOT EXISTS config_graph_nodes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    graph_id BIGINT NOT NULL,
    agent_id VARCHAR(255) NOT NULL,
    x DOUBLE DEFAULT 0,
    y DOUBLE DEFAULT 0,
    collapsed BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_graph_id (graph_id),
    INDEX idx_agent_id (agent_id),
    CONSTRAINT fk_node_graph FOREIGN KEY (graph_id) REFERENCES config_graphs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create config_graph_edges table
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
    INDEX idx_graph_id (graph_id),
    INDEX idx_source_id (source_id),
    INDEX idx_target_id (target_id),
    INDEX idx_edge_type (edge_type),
    CONSTRAINT fk_edge_graph FOREIGN KEY (graph_id) REFERENCES config_graphs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create a2a_messages table
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

-- Insert default graph
INSERT INTO config_graphs (name, description, version) 
VALUES ('Default Graph', 'Initial configuration graph', 1);
