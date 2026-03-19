## 1. i18n Setup

- [x] 1.1 Install vue-i18n dependency in frontend
- [x] 1.2 Create frontend/src/locales/en.json with all Task Queue English strings
- [x] 1.3 Create frontend/src/locales/zh.json with all Task Queue Chinese strings
- [x] 1.4 Configure vue-i18n in frontend main.ts or App.vue

## 2. Update TaskQueue.vue for i18n

- [x] 2.1 Remove the tagline "Async task management with Spring Boot + MySQL + Redis" from TaskQueue.vue
- [x] 2.2 Replace hardcoded "Task Queue" with i18n key
- [x] 2.3 Replace hardcoded "Create Task" button text with i18n
- [x] 2.4 Replace stats card labels (Pending, Running, Completed, Failed) with i18n
- [x] 2.5 Replace table headers (Task ID, Type, Priority, Status, etc.) with i18n
- [x] 2.6 Update Create Task dialog labels and placeholders with i18n

## 3. Backend Task Type Database

- [x] 3.1 Create database migration for task_type table
- [x] 3.2 Create TaskType entity/model in backend
- [x] 3.3 Seed default task types on application startup (agent-execute, data-sync, notification, cleanup)

## 4. Task Type API

- [x] 4.1 Create GET /api/task-types endpoint
- [x] 4.2 Create POST /api/task-types endpoint
- [x] 4.3 Create PUT /api/task-types/{id} endpoint
- [x] 4.4 Create DELETE /api/task-types/{id} endpoint

## 5. Frontend Task Type API Integration

- [x] 5.1 Add taskTypeApi.ts in frontend/src/lib/ for API calls
- [x] 5.2 Update TaskQueue.vue to fetch task types from API instead of hardcoded
- [x] 5.3 Update Create Task form to use dynamic task types

## 6. Task Type Management UI

- [x] 6.1 Create TaskTypeManagement.vue component
- [x] 6.2 Add navigation entry for Task Type management (in settings or separate page)
- [x] 6.3 Implement list view for task types
- [x] 6.4 Implement create/edit form for task types
- [x] 6.5 Implement delete confirmation and action
