-- V3__add_missing_columns_to_tasks.sql
-- Add missing columns to tasks table

ALTER TABLE tasks ADD COLUMN priority INT DEFAULT 0 AFTER payload;
ALTER TABLE tasks ADD COLUMN deleted INT DEFAULT 0 AFTER updated_at;

CREATE INDEX IF NOT EXISTS idx_priority ON tasks(priority);
CREATE INDEX IF NOT EXISTS idx_deleted ON tasks(deleted);
