-- V4__add_task_group_fields.sql
-- Add task group and subtask support columns to task_queue_tasks

ALTER TABLE task_queue_tasks 
    ADD COLUMN IF NOT EXISTS task_group_id VARCHAR(36),
    ADD COLUMN IF NOT EXISTS parent_task_id VARCHAR(36),
    ADD COLUMN IF NOT EXISTS assigned_agent VARCHAR(100),
    ADD COLUMN IF NOT EXISTS report_to_agent VARCHAR(100),
    ADD COLUMN IF NOT EXISTS context TEXT,
    ADD COLUMN IF NOT EXISTS title VARCHAR(255),
    ADD COLUMN IF NOT EXISTS last_error TEXT;

-- Add indexes for task group queries
CREATE INDEX IF NOT EXISTS idx_task_group ON task_queue_tasks(task_group_id);
CREATE INDEX IF NOT EXISTS idx_parent ON task_queue_tasks(parent_task_id);
CREATE INDEX IF NOT EXISTS idx_assigned ON task_queue_tasks(assigned_agent);

-- Extend task_groups table with new fields
ALTER TABLE task_groups 
    ADD COLUMN IF NOT EXISTS total_goal TEXT,
    ADD COLUMN IF NOT EXISTS overall_design TEXT,
    ADD COLUMN IF NOT EXISTS decomposer_agent_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'pending',
    ADD COLUMN IF NOT EXISTS completed_at DATETIME;

-- Add index for decomposer agent
CREATE INDEX IF NOT EXISTS idx_decomposer ON task_groups(decomposer_agent_id);
CREATE INDEX IF NOT EXISTS idx_group_status ON task_groups(status);
