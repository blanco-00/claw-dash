## Context

Current problematic structure:
```
claw-dash/
├── src/                    # Actual frontend source (used)
├── frontend/
│   ├── src/               # Empty/confusing duplicate
│   ├── package.json       # Frontend dependencies
│   └── ...
├── backend/               # Spring Boot
├── server/                # Unknown purpose (Node.js Express?)
├── docker/
│   └── mysql/init.sql    # DB init only, no migrations
├── package.json           # Mixed frontend deps
└── ...
```

## Goals / Non-Goals

**Goals:**
- Create clear, standard project structure
- All frontend code in `frontend/` folder
- All database migrations in `docker/mysql/migrations/`
- Remove confusing duplicate/redundant folders
- Keep docker-compose.yml working

**Non-Goals:**
- Not merging frontend/backend into one (stay separate)
- Not changing any application logic
- Not fixing bugs or adding features

## Decisions

### Structure Decision: Separate frontend/backend

**Chosen:**
```
claw-dash/
├── frontend/              # Vue 3 frontend
│   ├── src/              # ← moved from root src/
│   ├── package.json      # ← moved from frontend/package.json
│   ├── vite.config.ts
│   ├── Dockerfile
│   └── nginx.conf
├── backend/              # Spring Boot (unchanged)
├── docker/
│   ├── mysql/
│   │   ├── init.sql      # Existing
│   │   └── migrations/   # ← moved from backend
│   └── redis/
└── docker-compose.yml
```

**Alternative considered**: Use root `src/` and keep `frontend/` as Docker build context only
**Reason for choice**: More standard, easier to understand, IDE works better

### Migration File Location

**Chosen**: `docker/mysql/migrations/`
**Rationale**: Keeps all Docker-related configs together, matches docker-compose.yml volume mount pattern

## Risks / Trade-offs

- **Risk**: Breaking existing dev workflow → **Mitigation**: Document new paths clearly
- **Risk**: docker-compose.yml build failing → **Mitigation**: Test after changes
- **Risk**: Losing git history on moved files → **Mitigation**: Use `git mv` to preserve history

## Migration Plan

1. Move `src/` → `frontend/src/` using git mv
2. Move `frontend/package.json` → merge into root or keep in frontend/
3. Move database migrations to `docker/mysql/migrations/`
4. Update vite.config.ts if needed (path aliases)
5. Update docker-compose.yml build contexts
6. Test `npm run dev` works
7. Test `docker-compose up` works
8. Remove empty/confusing folders

## Open Questions

- Should `server/` folder be kept or removed? (appears unused)
- Should root `package.json` be deleted or kept as workspace config?
