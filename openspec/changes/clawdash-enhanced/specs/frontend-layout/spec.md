# Frontend Layout Specification

## ADDED Requirements

### Requirement: Sidebar navigation

The system SHALL provide collapsible sidebar navigation with menu items.

#### Scenario: Display sidebar menu

- **WHEN** user loads the application
- **THEN** system displays sidebar with all menu categories and icons

#### Scenario: Collapse sidebar

- **WHEN** user clicks collapse button
- **THEN** sidebar collapses to icon-only mode

#### Scenario: Expand sidebar

- **WHEN** user clicks expand button or hovers collapsed sidebar
- **THEN** sidebar expands to show full menu

### Requirement: Multi-tab page support

The system SHALL support multiple open pages as tabs.

#### Scenario: Open new page as tab

- **WHEN** user navigates to a new page
- **THEN** system opens page as new tab

#### Scenario: Switch between tabs

- **WHEN** user clicks on different tab
- **THEN** system switches to selected tab content

#### Scenario: Close tab

- **WHEN** user clicks close button on tab
- **THEN** system closes tab and switches to adjacent tab

### Requirement: Header with user menu

The system SHALL display header with user info and actions.

#### Scenario: Display user name

- **WHEN** user is logged in
- **THEN** header shows user's display name

#### Scenario: Open user menu

- **WHEN** user clicks user avatar/name
- **THEN** system displays dropdown with settings, logout options

### Requirement: Theme switching

The system SHALL support light/dark theme switching.

#### Scenario: Switch to dark theme

- **WHEN** user selects dark theme
- **THEN** system applies dark color scheme

#### Scenario: Switch to light theme

- **WHEN** user selects light theme
- **THEN** system applies light color scheme

### Requirement: Responsive layout

The system SHALL adapt to different screen sizes.

#### Scenario: Mobile view

- **WHEN** user accesses on small screen (<768px)
- **THEN** sidebar becomes drawer-based navigation

#### Scenario: Desktop view

- **WHEN** user accesses on large screen (>=768px)
- **THEN** sidebar displays as fixed side panel

### Requirement: Menu structure

The system SHALL organize menu items by functional categories.

#### Menu Structure:

```
监控面板
├── 概览 (/dashboard/overview)
├── 网关状态 (/dashboard/gateway)
├── 会话管理 (/dashboard/sessions)
└── Token管理 (/dashboard/tokens)

任务管理
├── 任务列表 (/tasks/list)
├── 任务组 (/tasks/groups)
└── Cron定时任务 (/tasks/cron)

Agent管理
├── Agent列表 (/agents/list)
└── Agent配置 (/agents/config)

个人管家
├── 财务管理 (/jiner/finance)
├── 健康管理 (/jiner/health)
├── 时尚管理 (/jiner/fashion)
├── 知识库 (/jiner/knowledge)
└── 新闻聚合 (/jiner/news)

系统设置
├── OpenClaw接入 (/settings/openclaw)
└── Docker状态 (/settings/docker)
```

### Requirement: Page layout components

The system SHALL provide consistent page layout with header, content area.

#### Scenario: Standard page layout

- **WHEN** user opens any page
- **THEN** system displays consistent layout with page title, breadcrumbs, content

#### Scenario: Loading state

- **WHEN** page is fetching data
- **THEN** system displays skeleton loading animation
