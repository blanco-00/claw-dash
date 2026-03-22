-- V4__edge_type_message_template.sql
-- Add default_message_template to edge_types table

ALTER TABLE edge_types
  ADD COLUMN default_message_template TEXT;

UPDATE edge_types SET default_message_template = '' WHERE value = 'always';
UPDATE edge_types SET default_message_template = '请帮我完成以下任务:\n{original_message}' WHERE value = 'task';
UPDATE edge_types SET default_message_template = '任务已完成:\n{task_result}' WHERE value = 'reply';
UPDATE edge_types SET default_message_template = '发生错误:\n{error_message}' WHERE value = 'error';
