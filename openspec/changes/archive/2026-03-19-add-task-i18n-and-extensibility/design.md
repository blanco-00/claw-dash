## Context

The Task Queue page (`/tasks`) currently has:
- Hardcoded English text mixed with some Chinese navigation
- Fixed tagline in the header
- Hardcoded Task Types: agent-execute, data-sync, notification, cleanup

User requirements:
1. Full i18n support (Chinese/English)
2. Remove the tagline
3. Allow users to add custom Task Types dynamically

## Goals / Non-Goals

**Goals:**
- Add vue-i18n for internationalization
- Create Chinese and English locale files for all Task Queue UI text
- Remove the static tagline from TaskQueue.vue
- Create database table for Task Types
- Build Task Type management UI (CRUD)

**Non-Goals:**
- Not adding other languages beyond Chinese/English (extensible for future)
- Not changing task execution logic - only configuration
- Not adding task type-specific processing handlers

## Decisions

### 1. i18n Library Selection
**Decision:** Use vue-i18n (existing Element Plus ecosystem compatibility)

**Alternatives Considered:**
- vue-i18n: Best Element Plus integration, widely used
- vue-localize: Simpler but less ecosystem support
- Custom solution: Too much boilerplate

### 2. Task Type Storage
**Decision:** Database-driven with API endpoints

**Schema:**
```sql
CREATE TABLE task_type (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL UNIQUE,
  display_name VARCHAR(100) NOT NULL,
  description VARCHAR(255),
  enabled BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**Alternatives Considered:**
- Enum in code: Rejected - not extensible
- Config file: Rejected - requires restart, no UI
- Database: Best flexibility with management UI

### 3. Frontend Architecture
**Decision:** 
- Add locale files under `frontend/src/locales/`
- Create TaskTypeManagement.vue component for CRUD
- Fetch task types from API, not hardcoded

**API Endpoints Needed:**
- `GET /api/task-types` - List all task types
- `POST /api/task-types` - Create new task type
- `PUT /api/task-types/{id}` - Update task type
- `DELETE /api/task-types/{id}` - Delete task type

## Risks / Trade-offs

- **Risk**: Existing tasks with deleted task types → **Mitigation**: Keep task type reference even if deleted (soft delete or preserve in task records)
- **Risk**: i18n coverage gaps → **Mitigation**: Audit all strings after implementation
- **Risk**: Backend API changes → **Mitigation**: Add new endpoints, don't break existing

## Migration Plan

1. **Phase 1**: Add i18n library and locale files
2. **Phase 2**: Update TaskQueue.vue to use i18n, remove tagline
3. **Phase 3**: Create backend task_type table and API
4. **Phase 4**: Update frontend to fetch task types from API
5. **Phase 5**: Add Task Type management UI

## Open Questions

- Should default task types be seeded on first run? (Recommended: Yes, seed agent-execute, data-sync, notification, cleanup)
- Should task types be organization-scoped or global? (Recommended: Global for simplicity)
