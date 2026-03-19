## Why

The Task Queue page at `/tasks` has three significant usability issues that don't meet user expectations:
1. No internationalization (i18n) support - the UI mixes Chinese and English inconsistently
2. Unnecessary tagline "Async task management with Spring Boot + MySQL + Redis" clutters the UI
3. Task Types are hardcoded (agent-execute, data-sync, notification, cleanup) - users cannot add custom task types

These issues limit the application's usability for international users and prevent flexible task type configuration.

## What Changes

1. **Add i18n support** - Implement full Chinese/English internationalization for all Task Queue UI text
2. **Remove tagline** - Delete the "Async task management with Spring Boot + MySQL + Redis" subtitle from the page header
3. **Make Task Types extensible** - Replace hardcoded task types with database-driven configuration so users can add, modify, or remove task types through the UI

## Capabilities

### New Capabilities
- `task-i18n`: Full internationalization for Task Queue page (Chinese/English)
- `task-type-management`: Database-driven task type configuration with CRUD UI

### Modified Capabilities
- None - this is a new feature set for the Task Queue module

## Impact

- **Frontend**: TaskQueue.vue, new i18n locale files, new TaskType management components
- **Backend**: New task_type table, new TaskType API endpoints
- **Database**: Add task_type table for extensible task type configuration
- **Dependencies**: May need vue-i18n or similar library
