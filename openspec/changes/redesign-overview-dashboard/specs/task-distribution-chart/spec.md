## ADDED Requirements

### Requirement: Task Distribution Ring Chart
The dashboard SHALL display a ring chart showing task distribution across different statuses.

#### Scenario: Tasks exist in all statuses
- **WHEN** tasks exist with statuses: pending, running, completed, failed
- **THEN** ring chart displays 4 segments with correct proportions and percentages

#### Scenario: No tasks exist
- **WHEN** there are no tasks in the system
- **THEN** chart shows empty state with "暂无任务数据" message

#### Scenario: All tasks in single status
- **WHEN** all tasks have the same status (e.g., all completed)
- **THEN** chart shows 100% for that status with appropriate visual feedback

#### Scenario: Hover shows tooltip
- **WHEN** user hovers over a chart segment
- **THEN** tooltip shows "状态名称: 数量 (百分比%)"

### Requirement: Chart Legend
The chart SHALL display a legend showing all task statuses with their colors.

#### Scenario: Legend display
- **WHEN** chart renders
- **THEN** legend shows: 待处理 (orange), 运行中 (purple), 已完成 (green), 失败 (red)

### Requirement: Click to Filter
Clicking a chart segment SHALL filter the task list to that status.

#### Scenario: Click pending segment
- **WHEN** user clicks the "待处理" segment
- **THEN** recent tasks list filters to show only pending tasks
