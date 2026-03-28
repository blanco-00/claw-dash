-- V5__complete_task_group_schema.sql
-- Complete task group schema: needs_intervention index and view

-- Add index for needs intervention query (retry_count >= max_retries)
CREATE INDEX IF NOT EXISTS idx_needs_intervention ON task_queue_tasks(retry_count, max_retries);

-- Create view for task group with subtask details
CREATE OR REPLACE VIEW v_task_group_details AS
SELECT 
    tg.id AS task_group_id,
    tg.title AS group_title,
    tg.total_goal,
    tg.overall_design,
    tg.decomposer_agent_id,
    tg.status AS group_status,
    tg.created_at AS group_created_at,
    tg.completed_at AS group_completed_at,
    t.id AS task_id,
    t.taskId AS task_key,
    t.title AS task_title,
    t.type AS task_type,
    t.assigned_agent,
    t.status AS task_status,
    t.priority AS task_priority,
    t.retry_count,
    t.max_retries,
    t.result,
    t.error AS task_error,
    t.created_at AS task_created_at,
    t.started_at,
    t.completed_at AS task_completed_at,
    t.depends_on
FROM task_groups tg
LEFT JOIN task_queue_tasks t ON t.task_group_id = tg.id;
