## ADDED Requirements

### Requirement: Overview Dashboard Grid Layout
The overview page SHALL display a modern dashboard with a 3-row card grid layout containing system status, KPI metrics, charts, and activity panels.

#### Scenario: Page loads with all components
- **WHEN** user navigates to `/overview`
- **THEN** page displays Gateway status banner, 4 KPI cards, 2 charts + system info row, and 2 list panels (recent tasks + active agents)

#### Scenario: Refresh button updates all data
- **WHEN** user clicks "刷新" button
- **THEN** all data components show loading state, then display updated values

#### Scenario: Responsive layout adapts to screen size
- **WHEN** page is viewed on a smaller screen (tablet/mobile)
- **THEN** cards reflow to stack vertically maintaining readability

### Requirement: Gateway Status Banner
The page SHALL display a prominent Gateway status indicator at the top of the page.

#### Scenario: Gateway is running
- **WHEN** Gateway status is "running"
- **THEN** banner shows green indicator with "运行中" text

#### Scenario: Gateway is stopped
- **WHEN** Gateway status is "stopped" or "error"
- **THEN** banner shows red indicator with "已停止" or "错误" text

### Requirement: Last Updated Timestamp
The dashboard SHALL display when the data was last refreshed.

#### Scenario: Data refresh completes
- **WHEN** data refresh finishes successfully
- **THEN** banner shows "最后更新: HH:mm:ss" timestamp
