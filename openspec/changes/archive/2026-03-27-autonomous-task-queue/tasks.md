## 1. Database Schema

- [x] 1.1 Create task_groups table with fields: id, title, description, total_goal, overall_design, decomposer_agent_id, status, created_at, completed_at
- [x] 1.2 Add columns to tasks table: task_group_id, parent_task_id, assigned_agent, retry_count, max_retries, report_to_agent, context (JSON)
- [x] 1.3 Add foreign key constraints: task_group_id REFERENCES task_groups(id), parent_task_id REFERENCES tasks(id)
- [x] 1.4 Add indexes: idx_task_group, idx_parent, idx_assigned, idx_needs_intervention
- [x] 1.5 Create v_task_group_details view for querying group with subtasks

## 2. Backend API - Task Groups

- [x] 2.1 Create TaskGroup entity with JPA annotations
- [x] 2.2 Create TaskGroupRepository with JPA interface
- [x] 2.3 Create TaskGroupService with CRUD operations
- [x] 2.4 Create TaskGroupController with endpoints: POST /api/task-groups, GET /api/task-groups/{id}, PATCH /api/task-groups/{id}/status
- [x] 2.5 Create /api/task-groups/{id}/progress endpoint returning completion stats
- [x] 2.6 Extend Task entity with task_group_id, parent_task_id relationships

## 3. #task Command Parsing

- [x] 3.1 Create TaskCommandParser service with #task prefix detection
- [x] 3.2 Implement priority detection from keywords (挂/崩/一直/紧急 → high/urgent)
- [x] 3.3 Implement explicit decompose request detection (拆解/分解 → requiresDecompose=true)
- [x] 3.4 Create ParsedTask DTO with fields: rawInput, title, description, priority, explicitDecomposeRequested
- [x] 3.5 Integrate parser into message handling flow (where #task commands arrive)

## 4. MenxiaSheng (Decomposer) Agent Logic

- [x] 4.1 Create MenxiaShengDecompositionService for task analysis
- [x] 4.2 Implement complexity analysis (single goal vs multi-goal detection)
- [x] 4.3 Implement SubtaskGenerator: break complex tasks into atomic subtasks
- [x] 4.4 Implement DependencyResolver: set dependsOn for sequential subtasks
- [x] 4.5 Implement ContextGenerator: attach totalGoal, overallDesign, subtaskDescription
- [x] 4.6 Create DecomposedTask DTO with subtasks array

## 5. Agent Capability Matching

- [x] 5.1 Query agent graph for agent capabilities (from existing config_graph_nodes data)
- [x] 5.2 Create AgentSelector service with matching logic based on task domain
- [x] 5.3 Map task types to agent specialties (gongbu→engineering, hubu→finance, etc.)
- [x] 5.4 Implement fallback to default agent when no clear match

## 6. Task Assignment Flow

- [x] 6.1 Implement MenxiaShengAssignmentService: simple task → direct assignment
- [x] 6.2 Implement MenxiaShengAssignmentService: complex task → decomposition + assignment
- [x] 6.3 Set assigned_agent on each task after assignment
- [x] 6.4 Create subtasks in database with proper task_group_id and parent_task_id
- [x] 6.5 Integrate with A2A message system for notification (sessions_send)

## 7. A2A Notification on Task Completion

- [x] 7.1 Modify task_complete MCP tool to send A2A message to decomposer
- [x] 7.2 Create A2A message with type "SUBTASK_COMPLETED", taskId, taskGroupId, summary
- [x] 7.3 Implement MenxiaSheng listener for SUBTASK_COMPLETED messages
- [x] 7.4 Query full subtask details from database upon notification

## 8. Result Aggregation

- [x] 8.1 Create AggregationService for collecting subtask results
- [x] 8.2 Query all subtasks in task_group_id upon each completion notification
- [x] 8.3 Implement completion detection: count completed vs total
- [x] 8.4 Implement failure detection: check for failed/dead subtasks
- [x] 8.5 Create AggregatedReport DTO with summary, details, recommendations
- [x] 8.6 Generate meaningful user-facing report when all subtasks complete

## 9. Task Exception Handling

- [x] 9.1 Implement exponential backoff in retry logic (base_delay * 2^retry_count)
- [x] 9.2 Add "needs_intervention" status detection when retry_count >= max_retries
- [x] 9.3 Create GET /api/tasks/needs-intervention endpoint
- [x] 9.4 Implement task reassignment endpoint: PATCH /api/tasks/{id}/reassign
- [x] 9.5 Implement task abandonment: update task_group status to "failed"

## 10. Frontend - Task Group View

10: 
11. - [x] 10.1 Extend TaskQueue.vue to fetch and display task groups
- [x] 10.2 Create TaskGroupCard component with progress indicator
- [x] 10.3 Add "需要介入" (needs intervention) tab/section with red highlighting
- [x] 10.4 Create TaskGroupDetailDrawer with subtask list
- [x] 10.5 Display context (totalGoal, overallDesign) in drawer
- [x] 10.6 Add intervention action buttons: 重新指派, 查看详情, 放弃
- [x] 10.7 Style task groups with status-colored badges (blue/green/red)

## 11. MenxiaSheng Agent Prompt

- [ ] 11.1 Update MenxiaSheng agent prompt with decomposition instructions
- [ ] 11.2 Add task complexity analysis guidelines to prompt
- [ ] 11.3 Add subtask creation guidelines (atomic, dependencies, context)
- [ ] 11.4 Add result aggregation and reporting guidelines
- [ ] 11.5 Add exception handling expectations

## 12. Backend Unit Testing

- [x] 12.1 Write unit tests for TaskCommandParser (#task prefix detection, priority keywords, decompose flag) ✅ 10 tests passed
- [x] 12.2 Write unit tests for complexity analysis (simple vs complex detection) ✅ 5 tests passed
- [x] 12.3 Write unit tests for exponential backoff delay calculation ✅ 5 tests passed
- [ ] 12.4 Write unit tests for aggregation logic (success/partial/failure detection) ⚠️ Skipped - complex service dependencies (should be integration test)
- [x] 12.5 Write unit tests for agent capability matching ✅ 9 tests passed

## 13. Frontend Browser Testing (chrome-devtools)

- [ ] 13.1 Write chrome-devtools test: #task command creates task in queue
- [ ] 13.2 Write chrome-devtools test: Task group card displays with correct progress
- [ ] 13.3 Write chrome-devtools test: Task group detail drawer shows all subtasks
- [ ] 13.4 Write chrome-devtools test: Exception tasks highlighted with intervention buttons
- [ ] 13.5 Write chrome-devtools test: Reassign button opens agent selection dialog
- [ ] 13.6 Write chrome-devtools test: Abandon task marks group as failed
- [ ] 13.7 Run chrome-devtools in browser and verify all tests pass
