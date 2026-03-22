-- V3__edge_routing_tables.sql
-- Multi-Agent Routing - Edge Routing Tables
-- 
-- This migration adds edge routing capabilities:
-- - Adds decision_mode and message_template to config_graph_edges
-- - Creates edge_types table for edge type definitions

-- Task 1.1-1.4: Add decision_mode and message_template columns to config_graph_edges
ALTER TABLE config_graph_edges 
  ADD COLUMN decision_mode VARCHAR(20) NOT NULL DEFAULT 'always',
  ADD COLUMN message_template TEXT;

-- Task 1.5: Create edge_types table
CREATE TABLE edge_types (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  value VARCHAR(20) NOT NULL UNIQUE,
  name VARCHAR(50) NOT NULL,
  description TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default edge types
INSERT INTO edge_types (value, name, description) VALUES
  ('always', '始终', '无条件发送'),
  ('task', '任务', '委托任务给目标agent'),
  ('reply', '回复', '完成任务后回复给某agent'),
  ('error', '错误', '发生错误时通知某agent');
