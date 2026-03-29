# Autonomous Task Queue — Testing Proposal

## 测试范围

### 1. Backend 单元测试 (12.x)

#### 1.1 TaskCommandParser 测试
```java
@Test
void parseBasicTask() {
    ParsedTask result = parser.parse("#task 修一下登录bug");
    assertEquals("修一下登录bug", result.getTitle());
    assertEquals(Priority.MEDIUM, result.getPriority());
    assertFalse(result.isExplicitDecomposeRequested());
}

@Test
void parseTaskWithUrgentKeyword() {
    ParsedTask result = parser.parse("#task 支付接口挂了");
    assertEquals(Priority.URGENT, result.getPriority());
}

@Test
void parseTaskWithHighPriorityKeyword() {
    ParsedTask result = parser.parse("#task 用户登录一直报错");
    assertEquals(Priority.HIGH, result.getPriority());
}

@Test
void parseTaskWithDecomposeRequest() {
    ParsedTask result = parser.parse("#task 做一个登录注册流程 拆解一下");
    assertTrue(result.isExplicitDecomposeRequested());
}

@Test
void parseTaskWithWhitespace() {
    ParsedTask result = parser.parse("#task   修一下bug  ");
    assertEquals("修一下bug", result.getTitle());
}
```

#### 1.2 MenxiaShengDecompositionService 复杂度分析测试
```java
@Test
void detectSimpleTask() {
    // 单一目标、明确执行路径
    boolean complex = decompositionService.isComplex("修一下登录bug");
    assertFalse(complex);
}

@Test
void detectComplexTask_MultiGoal() {
    // 多目标
    boolean complex = decompositionService.isComplex(
        "做一个用户登录注册流程，包括前端和后端"
    );
    assertTrue(complex);
}

@Test
void detectComplexTask_ManyKeywords() {
    // 字数多、多个动词
    boolean complex = decompositionService.isComplex(
        "用户反馈App在iOS上偶发崩溃，需要排查是前端问题还是后端问题，然后修复并验证"
    );
    assertTrue(complex);
}
```

#### 1.3 SubtaskGenerator 测试
```java
@Test
void generateSubtasks_Atomicity() {
    List<Subtask> subtasks = decompositionService.decompose(
        "修复用户登录问题"
    );
    
    for (Subtask st : subtasks) {
        // 每个子任务应该是原子的
        assertNotNull(st.getTitle());
        assertNotNull(st.getAssignedAgent());
        assertNotNull(st.getContext().getTotalGoal());
    }
}

@Test
void generateSubtasks_Dependencies() {
    List<Subtask> subtasks = decompositionService.decompose(
        "修复用户登录问题"
    );
    
    // 验证依赖关系设置正确
    for (Subtask st : subtasks) {
        if (st.getDependsOn() != null) {
            for (String depId : st.getDependsOn()) {
                // 依赖的子任务必须存在
                assertTrue(subtasks.stream()
                    .anyMatch(s -> s.getId().equals(depId)));
            }
        }
    }
}

@Test
void generateSubtasks_Context() {
    List<Subtask> subtasks = decompositionService.decompose(
        "修复用户登录问题"
    );
    
    for (Subtask st : subtasks) {
        assertNotNull(st.getContext().getTotalGoal());
        assertNotNull(st.getContext().getOverallDesign());
        assertNotNull(st.getContext().getSubtaskDescription());
    }
}
```

#### 1.4 AgentSelector 测试
```java
@Test
void selectAgent_EngineeringTask() {
    String agent = selector.selectAgent("修复登录bug");
    assertEquals("gongbu", agent);
}

@Test
void selectAgent_FinanceTask() {
    String agent = selector.selectAgent("分析本月财务报表");
    assertEquals("hubu", agent);
}

@Test
void selectAgent_SecurityTask() {
    String agent = selector.selectAgent("检查系统安全漏洞");
    assertEquals("bingbu", agent);
}

@Test
void selectAgent_UnknownTask_Fallback() {
    String agent = selector.selectAgent("做点什么");
    assertEquals("gongbu", agent); // 默认 fallback
}
```

#### 1.5 指数退避计算测试
```java
@Test
void exponentialBackoff_FirstRetry() {
    long delay = retryService.calculateDelay(0, 5000); // base 5s
    assertEquals(5000, delay); // 5s * 2^0 = 5s
}

@Test
void exponentialBackoff_SecondRetry() {
    long delay = retryService.calculateDelay(1, 5000);
    assertEquals(10000, delay); // 5s * 2^1 = 10s
}

@Test
void exponentialBackoff_ThirdRetry() {
    long delay = retryService.calculateDelay(2, 5000);
    assertEquals(20000, delay); // 5s * 2^2 = 20s
}

@Test
void exponentialBackoff_FourthRetry() {
    long delay = retryService.calculateDelay(3, 5000);
    assertEquals(40000, delay); // 5s * 2^3 = 40s
}
```

#### 1.6 AggregationService 测试
```java
@Test
void aggregate_AllSuccess() {
    List<Task> subtasks = Arrays.asList(
        createTask("t1", "completed"),
        createTask("t2", "completed"),
        createTask("t3", "completed")
    );
    
    AggregatedReport report = aggregationService.aggregate("group1", subtasks);
    
    assertEquals("success", report.getStatus());
    assertEquals(3, report.getDetails().getCompleted().size());
    assertTrue(report.getDetails().getFailed().isEmpty());
}

@Test
void aggregate_PartialFailure() {
    List<Task> subtasks = Arrays.asList(
        createTask("t1", "completed"),
        createTask("t2", "failed"),
        createTask("t3", "completed")
    );
    
    AggregatedReport report = aggregationService.aggregate("group1", subtasks);
    
    assertEquals("partial", report.getStatus());
    assertEquals(2, report.getDetails().getCompleted().size());
    assertEquals(1, report.getDetails().getFailed().size());
}

@Test
void aggregate_AllFailed() {
    List<Task> subtasks = Arrays.asList(
        createTask("t1", "failed"),
        createTask("t2", "failed")
    );
    
    AggregatedReport report = aggregationService.aggregate("group1", subtasks);
    
    assertEquals("failed", report.getStatus());
    assertNotNull(report.getRecommendations());
}
```

#### 1.7 TaskGroup API 测试
```java
@Test
void createTaskGroup() {
    TaskGroup group = taskGroupService.create("测试任务", "描述");
    
    assertNotNull(group.getId());
    assertEquals("测试任务", group.getTitle());
    assertEquals("pending", group.getStatus());
}

@Test
void getProgress_SomeCompleted() {
    // 创建任务组 + 3个子任务
    TaskGroup group = taskGroupService.create("测试", "描述");
    taskService.createSubtask(group.getId(), "t1", "assigned");
    taskService.createSubtask(group.getId(), "t2", "completed");
    taskService.createSubtask(group.getId(), "t3", "completed");
    
    Progress progress = taskGroupService.getProgress(group.getId());
    
    assertEquals(3, progress.getTotal());
    assertEquals(2, progress.getCompleted());
    assertEquals(66.67, progress.getPercentage(), 0.01);
}

@Test
void reassignTask() {
    Task task = taskService.createTask("t1");
    taskService.reassignTask(task.getId(), "hubu");
    
    Task updated = taskService.getTask(task.getId());
    assertEquals("hubu", updated.getAssignedAgent());
}
```

---

### 2. Frontend 浏览器测试 (13.x) — chrome-devtools

#### 2.1 #task 命令测试
```
Test: #task 命令创建任务

Steps:
1. 打开 OpenClaw Chat 窗口
2. 输入 "#task 测试任务"
3. 发送消息

Expected:
- 任务被创建
- 返回任务 ID
- 任务出现在 TaskQueue UI
```

#### 2.2 任务组显示测试
```
Test: 任务组卡片显示

Steps:
1. 创建复杂任务 "#task 做一个登录注册流程"
2. 等待门下省拆解
3. 查看 TaskQueue UI

Expected:
- 看到任务组卡片
- 显示进度 "0/3"
- 状态 badge 为蓝色 (进行中)
```

#### 2.3 任务组详情抽屉测试
```
Test: 任务组详情抽屉

Steps:
1. 点击任务组卡片
2. 打开详情抽屉

Expected:
- 显示总目标 (totalGoal)
- 显示整体设计 (overallDesign)
- 显示子任务列表
- 每个子任务显示 assigned_agent 和 status
```

#### 2.4 异常任务高亮测试
```
Test: 需要介入任务高亮

Steps:
1. 创建一个会失败的任务
2. 等待重试超过 max_retries

Expected:
- 任务卡片显示红色边框/背景
- 显示 "需要介入" 标签
- 显示重试次数和最后错误
```

#### 2.5 重新指派功能测试
```
Test: 重新指派任务

Steps:
1. 找到需要介入的任务
2. 点击 "重新指派" 按钮
3. 选择新的 agent

Expected:
- 弹出 agent 选择对话框
- 选择后任务重新进入队列
- 状态更新
```

#### 2.6 放弃任务测试
```
Test: 放弃任务

Steps:
1. 找到需要介入的任务
2. 点击 "放弃" 按钮

Expected:
- 任务组状态变为 "failed"
- 任务从活跃队列移除
- UI 更新显示
```

#### 2.7 任务完成通知测试
```
Test: 门下省收到完成通知

Steps:
1. 创建复杂任务
2. 执行一个子任务并完成
3. 观察门下省收到通知

Expected:
- 门下省 (MenxiaSheng) 收到 A2A 消息
- 消息类型: SUBTASK_COMPLETED
```

---

### 3. 集成测试场景

#### 3.1 完整流程测试
```
Scenario: 复杂任务完整生命周期

Given: 系统正常运行，agents 可用
When: 用户发送 "#task 做一个登录注册流程"
Then:
  1. 门下省收到任务
  2. 门下省分析为复杂任务
  3. 门下省拆解为 3 个子任务
  4. 子任务分配给对应 agents
  5. agents 执行子任务
  6. 子任务完成通知门下省
  7. 门下省聚合结果
  8. 门下省汇报用户: "登录注册流程已完成"
```

#### 3.2 简单任务流程测试
```
Scenario: 简单任务直接执行

Given: 系统正常运行
When: 用户发送 "#task 修一下登录bug"
Then:
  1. 门下省收到任务
  2. 门下省分析为简单任务
  3. 门下省直接分配给 gongbu
  4. gongbu 执行并完成
  5. 门下省汇报用户
```

#### 3.3 异常恢复测试
```
Scenario: 任务失败后重试最终成功

Given: 任务分配给 agent
When: agent 执行失败，重试 3 次后成功
Then:
  1. 首次失败，delay = 5s
  2. 第二次失败，delay = 10s
  3. 第三次失败，delay = 20s
  4. 超过 max_retries，标记为需要介入
  5. 用户重新指派
  6. 新 agent 执行成功
```

---

### 4. 测试环境要求

```
Backend:
- MySQL 数据库 (测试实例)
- Redis (如果任务队列使用)
- JUnit 5 + Mockito
- H2 内存数据库用于单元测试

Frontend:
- chrome-devtools 或 Playwright
- 独立的测试环境 / staging

OpenClaw:
- OpenClaw Gateway 运行中
- 测试 agents 配置好
- A2A 消息通道可用
```

---

### 5. 测试优先级

| 优先级 | 测试项 | 理由 |
|--------|--------|------|
| P0 | TaskCommandParser 解析 | 入口基础 |
| P0 | 任务组 CRUD API | 核心数据操作 |
| P0 | #task 命令浏览器测试 | 用户主要操作 |
| P1 | 复杂度分析 | 拆解逻辑核心 |
| P1 | 指数退避计算 | 异常处理核心 |
| P1 | 任务组 UI 显示 | 主要用户界面 |
| P2 | Agent 选择逻辑 | 辅助功能 |
| P2 | 聚合汇报 | 汇报质量 |
| P2 | 重新指派/放弃 | 异常处理 UI |
| P3 | A2A 通知集成测试 | 需要完整环境 |

---

### 6. 建议的测试执行顺序

```
Phase 1: 基础单元测试
├── TaskCommandParser
├── TaskGroup CRUD
└── 指数退避

Phase 2: 核心逻辑测试
├── 复杂度分析
├── Agent 选择
└── 聚合服务

Phase 3: API 集成测试
├── #task 命令
├── 任务分配
└── 状态流转

Phase 4: 完整 E2E 测试
├── 简单任务流程
├── 复杂任务流程
└── 异常恢复流程
```
