# ClawDash 增强版 - 技术设计

## Context

### 背景

当前有三个独立系统需要整合：

- **claw-dash**: Vue3 + Express + SQLite 监控系统
- **openclaw-task-queue**: TypeScript 任务队列库
- **privy-jiner**: Node.js Monorepo 个人助理系统

### 约束

- 前端必须基于 vue-pure-admin
- 后端必须使用 Spring Boot
- 数据库使用 MySQL + Redis
- 一键 Docker Compose 部署
- 保留现有 OpenClaw 集成能力

### 利益相关者

- OpenClaw 用户：需要一个统一的管理控制台
- 开发者：需要清晰的架构便于维护扩展

## Goals / Non-Goals

**Goals:**

1. 完成前后端技术栈重构（Vue3 + vue-pure-admin → Spring Boot）
2. 整合 task-queue 核心功能（任务/任务组/Cron）
3. 整合 jiner 财务模块 + 健康记录（收支记录、月度报表、预算管理、喝水记录、运动记录）
4. 实现一键接入 OpenClaw 功能
5. Docker Compose 一键部署

**Non-Goals:**

- 保留旧版 Express API（完全迁移到 Spring Boot）
- 兼容现有 SQLite 数据（新建 MySQL Schema）
- ~~实现 jiner 所有 AI 对话功能~~ → **保留核心 AI 对话功能**
- 多租户支持（仅支持单用户）

## Decisions

### 1. 前端架构：vue-pure-admin + TypeScript

**选择**：使用 vue-pure-admin 作为基础框架

**理由**：

- 开源免费，中文文档完善
- 内置多标签页、菜单、主题切换
- 支持动态路由、权限管理
- 与现有 Vue3 技术栈兼容

**替代方案**：

- vben-admin: 功能更全但更重
- Naive UI Admin: 组件库不同

### 2. 后端架构：Spring Boot 3 + MyBatis-Plus

**选择**：Spring Boot 3 + MyBatis-Plus

**理由**：

- 成熟的 Java 企业级框架
- MyBatis-Plus 简化 CRUD 开发
- 生态丰富，易于集成
- 与现有 Java 技能匹配

**替代方案**：

- Spring Cloud: 过度设计，单体足够
- Spring WebFlux: 响应式编程，增加复杂度

### 3. 日志设计：结构化日志 + 请求追踪

**选择**：Spring Boot 3.4 内置结构化日志 + MDC 追踪

**日志格式**：

```json
{
  "@timestamp": "2024-12-19T01:17:47.195Z",
  "log.level": "INFO",
  "trace.id": "abc123",
  "user.id": "user001",
  "action": "task.create",
  "message": "Task created successfully"
}
```

**设计要点**：

- **请求追踪**：每个 HTTP 请求分配唯一 traceId，贯穿前端→后端→日志
- **结构化输出**：JSON 格式，支持 Elasticsearch 直接解析
- **日志级别**：ERROR（异常）、WARN（警告）、INFO（业务）、DEBUG（调试）
- **敏感脱敏**：密码/Token 不记录日志

**替代方案**：

- 非结构化文本日志：难以搜索分析
- 手动traceId：容易遗漏

### 4. 代码设计：开闭原则 + 复用抽象

**核心原则**：

- **开闭原则 (OCP)**：模块对扩展开放，对修改关闭
- **DRY 原则**：Don't Repeat Yourself，避免重复代码

**复用抽象设计**：

```
┌─────────────────────────────────────────────────────┐
│                   公共层 (Common)                   │
├─────────────────────────────────────────────────────┤
│ • BaseEntity       - 基础实体（id, createdAt...）   │
│ • BaseService     - 通用CRUD服务                   │
│ • BaseController  - 通用REST接口                    │
│ • Result          - 统一响应包装                     │
│ • PageRequest    - 分页请求                         │
│ • PageResponse   - 分页响应                         │
└─────────────────────────────────────────────────────┘
                          ↓ 继承
┌─────────────────────────────────────────────────────┐
│                   业务模块                           │
├─────────────────────────────────────────────────────┤
│ TaskService extends BaseService<Task>               │
│ FinanceService extends BaseService<FinanceRecord>  │
│ AgentService extends BaseService<Agent>            │
└─────────────────────────────────────────────────────┘
```

**具体复用点**：

1. **通用 CRUD**：通过 BaseService 继承，80% 的增删改查无需重复代码
2. **分页查询**：PageRequest/PageResponse 统一处理
3. **统一响应**：Result 包装器，所有 API 返回格式一致
4. **日志追踪**：AOP 切面自动记录请求日志
5. **异常处理**：@GlobalExceptionHandler 统一异常捕获

### 5. Java 后端编码规范（基于阿里嵩山版）

#### 5.1 命名风格

```java
// ✅ 正例
private UserService userService;
private TaskDao taskDao;
private List<Task> taskList;

// ❌ 反例
private UserService u;
private TaskDao dao;
private List tasks;
```

#### 5.2 类注释

```java
/**
 * 任务服务
 * 负责任务的创建、查询、状态管理等业务逻辑
 *
 * @author ClawDash
 * @since 1.0.0
 */
@Service
public class TaskService {
    // ...
}
```

#### 5.3 方法注释

```java
/**
 * 创建新任务
 * 使用CAS保证任务不会被重复claim
 *
 * @param request 任务创建请求（包含type、payload、priority）
 * @return 创建成功的任务ID
 * @throws IllegalArgumentException 请求参数无效时抛出
 */
public String createTask(TaskCreateRequest request) {
    // 1. 参数校验
    validateRequest(request);

    // 2. 生成唯一ID
    String taskId = generateTaskId();

    // 3. 持久化存储
    taskMapper.insert(task);

    return taskId;
}
```

#### 5.4 常量定义

```java
// ✅ 使用枚举或常量类
public enum TaskStatus {
    PENDING("待执行"),
    RUNNING("执行中"),
    COMPLETED("已完成"),
    FAILED("执行失败"),
    DEAD("永久失败");

    private final String desc;
    TaskStatus(String desc) { this.desc = desc; }
}
```

### 6. 前端编码规范（Vue3 + TypeScript）

#### 6.1 ESLint + Prettier 配置

```json
// package.json
{
  "devDependencies": {
    "eslint": "^9.22.0",
    "prettier": "3.5.3",
    "@vue/eslint-config-prettier": "^10.2.0",
    "@vue/eslint-config-typescript": "^14.5.0",
    "eslint-plugin-vue": "~10.0.0"
  }
}
```

#### 6.2 组件规范

```typescript
// ✅ 组件文件：usePrefix.vue
<script setup lang="ts">
/**
 * 用户前缀管理组件
 * 用于管理用户的显示前缀（如：昵称、职称等）
 */
import { ref, computed } from 'vue'
import type { UserPrefix } from '@/types/user'

// 组件props
interface Props {
  modelValue: UserPrefix[]
  maxLength?: number
}

const props = withDefaults(defineProps<Props>(), {
  maxLength: 50
})

// emits
const emit = defineEmits<{
  'update:modelValue': [value: UserPrefix[]]
  'change': [value: UserPrefix]
}>()

// 响应式状态
const inputValue = ref('')

// 计算属性
const canAdd = computed(() =>
  inputValue.value.length > 0 &&
  props.modelValue.length < props.maxLength
)
</script>
```

#### 6.3 API 封装规范

```typescript
// ✅ 统一响应类型
interface ApiResponse<T> {
  code: number
  data: T
  message: string
}

// ✅ request 拦截器处理 token
import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器：自动携带token
request.interceptors.request.use(config => {
  const token = useAuthStore().token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：统一错误处理
request.interceptors.response.use(
  response => response.data,
  error => {
    const message = error.response?.data?.message || '请求失败'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)
```

### 7. 软件工程原则（适用于本项目）

#### 7.1 SOLID 原则

| 原则             | 含义                   | 应用                                            |
| ---------------- | ---------------------- | ----------------------------------------------- |
| **SRP** 单一职责 | 类/模块只负责一件事    | TaskService只处理任务，FinanceService只处理财务 |
| **OCP** 开闭     | 对扩展开放，对修改关闭 | 使用策略模式扩展任务类型                        |
| **LSP** 里氏替换 | 子类能替换父类         | 继承BaseService实现通用CRUD                     |
| **ISP** 接口隔离 | 客户端只依赖需要的接口 | 拆分大接口为小接口                              |
| **DIP** 依赖倒置 | 依赖抽象而非具体       | 面向接口编程，使用Repository                    |

#### 7.2 其他核心原则

| 原则           | 含义                    | 应用                       |
| -------------- | ----------------------- | -------------------------- |
| **DRY**        | Don't Repeat Yourself   | 提取BaseService、通用组件  |
| **KISS**       | Keep It Simple          | 避免过度设计，代码简洁易懂 |
| **YAGNI**      | You Ain't Gonna Need It | 只实现当前需要的功能       |
| **LoD** 迪米特 | 最少知道原则            | 类之间保持最少了解         |

#### 7.3 DDD 领域驱动设计（本项目简化版）

```
┌─────────────────────────────────────────────────────┐
│                   Interfaces (接口层)                │
│         Controllers / REST API / WebSocket          │
└──────────────────────────┬──────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────┐
│                   Application (应用层)               │
│              Services / DTOs / Mappers              │
└──────────────────────────┬──────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────┐
│                     Domain (领域层)                  │
│     Entities / Value Objects / Domain Services       │
└──────────────────────────┬──────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────┐
│                 Infrastructure (基础设施层)           │
│          Repositories / Redis / File IO             │
└─────────────────────────────────────────────────────┘
```

**领域划分**：

- **Task 领域**：任务、任务组、CronJob
- **Finance 领域**：收支记录、预算
- **Agent 领域**：Agent管理
- **System 领域**：用户、配置

### 8. 安全设计

#### 8.1 认证与授权

```java
// Spring Security 配置
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 密码加密器
     * 使用 BCrypt 算法，这是 Spring Security 推荐的方式
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * JWT 认证过滤器
     * 拦截请求，验证 JWT Token
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil);
    }
}
```

**核心安全组件**：
| 组件 | 用途 |
|------|------|
| BCryptPasswordEncoder | 密码加密存储 |
| JwtAuthenticationFilter | Token 验证 |
| JwtTokenProvider | Token 生成/解析 |

#### 8.2 接口安全

```java
// ✅ 限流配置
@RestController
@RequestMapping("/api")
public class TaskController {

    /**
     * 任务创建接口
     * 启用限流：每分钟最多 60 次请求
     */
    @PostMapping("/tasks")
    @RateLimiter(value = 60, timeUnit = TimeUnit.MINUTES)
    public Result<String> createTask(@RequestBody @Valid TaskCreateRequest request) {
        // 业务逻辑
    }
}
```

**安全措施**：
| 措施 | 说明 |
|------|------|
| 密码加密 | BCrypt 单向加密 |
| JWT Token | 无状态认证 |
| 接口限流 | 防止恶意请求 |
| CORS | 跨域控制 |
| HTTPS | 生产环境强制 |

#### 8.3 敏感数据处理

```java
// ❌ 禁止记录敏感信息
logger.info("用户密码: {}", user.getPassword());

// ✅ 正确做法：密码在传输/存储时已加密
logger.info("用户登录: {}", user.getUsername());
```

**敏感字段**：

- 密码：存储时 BCrypt 加密
- Token：仅返回一次，后续不显示
- 身份证/银行卡：脱敏存储

### 9. Docker Compose 一键部署

#### 9.1 服务架构

```yaml
# docker-compose.yml
version: '3.8'

services:
  # MySQL 数据库
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: clawdash
    volumes:
      - mysql_data:/var/lib/mysql
      - ./docker/mysql/init.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - '3306:3306'
    healthcheck:
      test: ['CMD', 'mysqladmin', 'ping', '-h', 'localhost']
      interval: 10s

  # Redis 缓存
  redis:
    image: redis:7-alpine
    ports:
      - '6379:6379'
    volumes:
      - redis_data:/data
    healthcheck:
      test: ['CMD', 'redis-cli', 'ping']

  # Spring Boot 后端
  backend:
    build: ./backend
    ports:
      - '8080:8080'
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/clawdash
      - SPRING_REDIS_HOST=redis
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
    restart: on-failure

  # Vue3 前端 (Nginx)
  frontend:
    build: ./frontend
    ports:
      - '80:80'
    depends_on:
      - backend

volumes:
  mysql_data:
  redis_data:
```

#### 9.2 一键启动

```bash
# .env 文件配置
MYSQL_ROOT_PASSWORD=your_secure_password
JWT_SECRET=your_jwt_secret

# 一键启动
docker-compose up -d

# 查看状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

**启动顺序**：MySQL → Redis → Backend → Frontend（自动依赖检测）

### 10. 编码规范

**Java 后端**：

1. **使用 import 替代全限定名** — 保持代码简洁

   ```java
   // ❌ 全限定名
   com.example.clawdash.entity.User user = new com.example.clawdash.entity.User();

   // ✅ import
   import com.example.clawdash.entity.User;
   User user = new User();
   ```

2. **关键注释必须添加** — 便于阅读理解

   ```java
   /**
    * 创建任务
    * 使用原子操作确保任务不会被重复claim
    *
    * @param request 任务创建请求
    * @return 创建成功的任务ID
    */
   public String createTask(TaskCreateRequest request) {
       // 1. 验证参数
       validateRequest(request);

       // 2. 生成唯一ID
       String taskId = generateTaskId();

       // 3. 插入数据库
       taskMapper.insert(task);

       return taskId;
   }
   ```

3. **注释原则**：
   - 类/接口：说明用途
   - public 方法：说明功能、参数、返回值
   - 复杂逻辑：步骤说明（1. 2. 3.）
   - 业务规则：为什么要这样做

**前端复用**：

- **通用组件**：表格(Table)、表单(Form)、弹窗(Modal)、筛选器(Filter)
- **API 封装**：request 拦截器统一处理 token、错误
- **类型定义**：共享 TypeScript 类型

### 4. 前端美学设计：Glassmorphism + 极简主义

**选择**：基于 vue-pure-admin 的现代化企业风格

**设计原则**：

1. **Glassmorphism 毛玻璃效果** - 半透明模糊背景，现代企业软件趋势
2. **极简主义** - 去除多余阴影/边框，专注内容
3. **视觉层次** - 重要信息(主要指标/操作按钮)更突出
4. **一致性** - 相似功能使用相似视觉语言
5. **空间分组** - 相关元素紧密相连，留白清晰

**配色方案**：

- 主色：科技蓝 (#3B82F6)
- 背景：深灰 (#1F2937) / 浅灰 (#F9FAFB)
- 强调：渐变紫蓝
- 语义色：成功绿 / 警告黄 / 错误红

**组件风格**：

- 卡片：圆角 12px，轻微阴影
- 按钮：扁平化，按压反馈
- 表格：斑马纹，细线分割

### 5. 数据库设计：MySQL 8 + Redis

**选择**：MySQL 8 主库 + Redis 缓存

**理由**：

- MySQL 8 支持 JSON、窗口函数
- Redis 用于会话缓存、任务队列分布式锁
- Docker Compose 易于部署

**Schema 设计**：

```
┌─────────────────────────────────────────────────────┐
│                    MySQL Schema                     │
├─────────────────────────────────────────────────────┤
│ users              - 用户认证                        │
│ tasks             - 任务队列                        │
│ task_groups       - 任务组/依赖                     │
│ task_logs         - 任务执行日志                    │
│ agents            - Agent管理                       │
│ cron_jobs         - Cron定时任务                   │
│ privy_finance     - 财务记录                        │
│ openclaw_config   - OpenClaw配置                    │
└─────────────────────────────────────────────────────┘
```

### 4. 目录结构

```
claw-dash/
├── frontend/                 # vue-pure-admin 前端
│   ├── src/
│   │   ├── views/           # 页面组件
│   │   │   ├── dashboard/   # 监控面板
│   │   │   ├── tasks/       # 任务管理
│   │   │   ├── agents/      # Agent管理
│   │   │   ├── jiner/       # 个人管家
│   │   │   └── settings/    # 系统设置
│   │   ├── api/             # API调用
│   │   ├── stores/           # Pinia状态
│   │   └── router/           # 路由配置
│   └── package.json
│
├── backend/                  # Spring Boot 后端
│   ├── src/main/java/
│   │   └── com/clawdash/
│   │       ├── controller/   # REST API
│   │       ├── service/      # 业务逻辑
│   │       ├── mapper/       # 数据访问
│   │       ├── entity/       # 实体类
│   │       └── config/       # 配置类
│   └── pom.xml
│
├── docker/                   # Docker 配置
│   ├── docker-compose.yml
│   ├── mysql/
│   │   └── init.sql
│   ├── redis/
│   └── nginx/
│
└── README.md
```

### 5. 前端菜单结构（基于 vue-pure-admin）

```
监控面板
├── 概览
├── 网关状态
├── 会话管理
└── Token管理

任务管理
├── 任务列表
├── 任务组
└── Cron定时任务

Agent管理
├── Agent列表
└── Agent配置

个人管家
├── 财务管理
├── 健康管理
├── 时尚管理
├── 知识库
└── 新闻聚合

系统设置
├── OpenClaw接入
└── Docker状态
```

### 6. OpenClaw 一键接入设计

**实现方式**：

1. 前端页面按钮「一键接入 OpenClaw」
2. 后端调用 OpenClaw API 完成插件安装
3. 自动创建默认 Agent 配置
4. 状态反馈到前端界面

## Risks / Trade-offs

### 风险

1. **[风险]** Spring Boot 迁移工作量大  
   **缓解**：分阶段迁移，先核心后模块

2. **[风险]** jiner 模块数据模型需重新设计  
   **缓解**：参考现有 MySQL Schema，保持兼容性

3. **[风险]** 前端 vue-pure-admin 学习曲线  
   **缓解**：使用标准配置，定制化部分逐步添加

4. **[风险]** Docker Compose 多服务调试困难  
   **缓解**：提供 docker-compose-dev.yml 支持热重载

### 权衡

- 选择单体架构而非微服务：降低部署复杂度，适合团队规模
- 选择 MySQL 而非 PostgreSQL：更熟悉的生态
- 选择 Redis 单一实例：简化部署，后续可扩展

## Migration Plan

### 阶段一：基础设施（Week 1）

1. 创建 Spring Boot 项目骨架
2. 配置 MySQL + Redis 连接
3. 搭建 vue-pure-admin 前端
4. 编写 docker-compose.yml

### 阶段二：核心功能（Week 2-3）

1. 用户认证模块（登录/注册/JWT）
2. 任务队列核心（从 task-queue 迁移）
3. 监控面板 API + 前端

### 阶段三：模块集成（Week 4-5）

1. 财务模块迁移（收支记录、月度报表、预算管理）
2. Agent 管理功能
3. OpenClaw 接入功能

### 阶段四：优化发布（Week 6）

1. 性能优化
2. 文档完善
3. 测试验收
4. 部署上线

## Open Questions

1. **Q**: jiner 的 AI 对话功能是否需要保留？
   **A**: 当前版本不包含，仅保留数据管理模块

2. **Q**: 是否需要支持多租户？
   **A**: 首个版本仅支持单用户，后续可扩展

3. **Q**: OpenClaw API 变更如何处理？
   **A**: 通过配置中心管理，保持版本兼容性
