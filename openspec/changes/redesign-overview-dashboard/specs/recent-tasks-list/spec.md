## ADDED Requirements

### Requirement: Recent Tasks List Panel
The dashboard SHALL display a panel showing the 5 most recent tasks with their status.

#### Scenario: Tasks exist
- **WHEN** there are recent tasks in the system
- **THEN** panel displays up to 5 tasks with: task ID, type, status tag, and creation time

#### Scenario: No tasks exist
- **WHEN** there are no tasks in the system
- **THEN** panel shows empty state with "暂无任务" message

#### Scenario: Status colors are displayed correctly
- **WHEN** task status is pending
- **THEN** status tag shows orange "待处理"
- **WHEN** task status is running
- **THEN** status tag shows purple "运行中"
- **WHEN** task status is completed
- **THEN** status tag shows green "已完成"
- **WHEN** task status is failed
- **THEN** status tag shows red "失败"

### Requirement: Task Click Opens Detail
Clicking a task in the list SHALL navigate to the task detail view.

#### Scenario: Click task row
- **WHEN** user clicks on a task row
- **THEN** system opens task detail panel or navigates to task detail page

### Requirement: Refresh Task List
The task list SHALL update when the dashboard refresh button is clicked.

#### Scenario: Refresh button clicked
- **WHEN** user clicks the dashboard refresh button
- **THEN** recent tasks list reloads and displays updated data
