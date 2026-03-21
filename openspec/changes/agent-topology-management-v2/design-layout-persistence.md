# Config Graph 布局与位置持久化提案

## Status

Draft - 待用户确认

## Problem Statement

### 问题 1: 刷新后位置丢失

**现象：**
- 用户在 Config Graph 中拖动节点到理想位置
- 刷新页面后，所有节点位置重置
- 连线布局又变成乱糟糟的样子

**原因：**
- 节点位置坐标只存在前端内存
- 没有保存到后端数据库
- 刷新后从后端加载的是默认/空位置

### 问题 2: 自动布局不美观

**现象：**
- 点击 Layout 按钮后，节点排成一列
- main 在最上方，所有节点垂直排列
- 不符合用户的心理预期（main 应该居中，其他围绕）

**原因：**
- 当前使用 dagre 布局算法
- dagre 只支持 TB（从上到下）或 LR（从左到右）的层级布局
- 不支持放射状/环形布局

## Options

### Option A: 仅改进默认布局（不持久化位置）

**方案：**
- 用 dagre 的 LR 模式替代 TB，让 main 在左，关系向右展开
- 或用简单的网格布局
- 刷新后仍会重置，但"乱"得均匀一些

**Pros:**
- 实现简单，5 分钟搞定
- 不改数据库结构

**Cons:**
- 位置不持久化，用户体验差
- 布局仍然不直观

---

### Option B: 放射状布局（推荐）

**方案：**
- main 放在圆心
- 其他节点以 main 为中心，围绕排列成圆
- 按连线关系就近排列

**Pros:**
- 布局直观，main 作为核心一目了然
- dagre 本身不支持，但可以用 d3 或手动计算
- 不改数据库结构

**Cons:**
- 实现复杂度中等
- 位置仍然不持久化

---

### Option C: 位置持久化

**方案：**
- 后端增加 `node_position` 表或字段
- 前端每次拖动结束后自动保存位置到后端
- 刷新后从后端加载位置

**Pros:**
- 彻底解决问题
- 用户可以自定义完美的布局

**Cons:**
- 实现复杂，需要改数据库
- 每次拖动都调用 API，有性能开销
- 需要防抖处理

---

### Option D: 混合方案（推荐最终方案）

**方案分两步：**

#### Phase 1: 放射状布局
- 用 d3 的 radial layout 或手动计算
- main 在中心，其他节点围绕
- 刷新后仍会用放射状作为默认布局

#### Phase 2: 位置持久化（可选）
- 如果用户明确需要，再实现
- 可以先 MVP 版本不做

## Recommendation

**推荐 Option B (放射状布局)，理由：**
1. 解决最核心的布局美观问题
2. 实现成本低
3. 不需要改数据库结构
4. 符合 agent 拓扑的实际语义（main 是核心）

**后续可根据需要再实现位置持久化 (Option C)。**

## Implementation Notes

### 放射状布局算法

```
1. 找出 main 节点，作为圆心
2. 计算其他节点到 main 的距离（基于连线关系）
3. 第一层：直接连接 main 的节点，排列在 inner circle
4. 第二层：连接第一层节点的节点，排列在 outer circle
5. 同层节点尽量均匀分布
```

### 位置持久化数据模型（Phase 2 考虑）

```sql
-- 方案 A: 新建表
CREATE TABLE config_graph_node_positions (
  graph_id INT,
  node_id VARCHAR(255),
  position_x FLOAT,
  position_y FLOAT,
  PRIMARY KEY (graph_id, node_id)
);

-- 方案 B: 扩展现有 nodes 表
ALTER TABLE config_graph_nodes ADD COLUMN position_x FLOAT;
ALTER TABLE config_graph_nodes ADD COLUMN position_y FLOAT;
```

## Open Questions

1. 用户是否需要手动保存布局，还是自动保存？
2. 是否需要"重置为默认布局"按钮？
3. 连线类型（assigns/reports_to/communicates）是否影响布局？
