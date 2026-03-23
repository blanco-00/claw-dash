## 1. Component Enhancement

- [ ] 1.1 Enhance StatCard.vue - add color variants (pink/blue/green/orange/purple)
- [ ] 1.2 Enhance StatCard.vue - add optional trend indicator (up/down arrow)
- [ ] 1.3 Fix ResourceChart.vue - ensure CPU/Memory mock data displays correctly
- [ ] 1.4 Update GatewayStatusCard.vue - improve header display consistency

## 2. New Components

- [ ] 2.1 Create TaskDistributionChart.vue - ring chart with CSS/SVG
- [ ] 2.2 Create RecentTasksList.vue - list panel with 5 recent tasks
- [ ] 2.3 Create ActiveAgentsPanel.vue - agent list with online/offline status
- [ ] 2.4 Add loading and empty states to all new components

## 3. API Integration

- [ ] 3.1 Verify /api/dashboard/overview returns correct data structure
- [ ] 3.2 Verify /api/tasks/stats returns status counts
- [ ] 3.3 Update API calls in overview page to fetch all required data
- [ ] 3.4 Add loading states during data fetch

## 4. Main Page Redesign

- [ ] 4.1 Create new dashboard grid layout in overview/index.vue
- [ ] 4.2 Add Gateway status banner with last updated timestamp
- [ ] 4.3 Implement 4 KPI stat cards row (tasks total, agents, running, success rate)
- [ ] 4.4 Implement charts + system info row (resources, task distribution, uptime)
- [ ] 4.5 Implement recent tasks + active agents row
- [ ] 4.6 Add refresh button functionality with loading states
- [ ] 4.7 Verify responsive layout on different screen sizes

## 5. Testing & Polish

- [ ] 5.1 Verify dark mode compatibility
- [ ] 5.2 Test empty states display correctly
- [ ] 5.3 Test loading states during data fetch
- [ ] 5.4 Verify all colors match existing purple theme
- [ ] 5.5 Run linter and fix any issues
