## Why

The current project structure is confusing and non-standard:
- Frontend source code is in root `src/` but there's also a `frontend/` folder
- Database migrations are in `backend/src/main/resources/db/migration/` instead of with other Docker configs
- Multiple redundant/confusing files and folders (`server/`, root `package.json`, etc.)

This makes the project hard to maintain and understand for new developers.

## What Changes

1. **Move frontend source code** - Move `src/` to `frontend/src/`
2. **Move package.json** - Use `frontend/package.json` as the main frontend package.json
3. **Move database migrations** - Move `backend/src/main/resources/db/migration/` to `docker/mysql/migrations/`
4. **Clean up redundant files** - Remove unused folders and consolidate Docker configs
5. **Update docker-compose.yml** - Adjust build contexts if needed

## Capabilities

### New Capabilities
- `project-structure-refactor`: Standardize the project folder structure

### Modified Capabilities
- None - this is purely a structural refactor

## Impact

- **File moves**: `src/` → `frontend/src/`, migration files → `docker/mysql/migrations/`
- **Config updates**: vite.config.ts, package.json paths may need updates
- **Docker updates**: docker-compose.yml may need build context changes
- **Breaking**: Developers will need to update their local workflow (new paths)
