## 1. Backup and Prepare

- [x] 1.1 Create git backup branch: `git checkout -b backup-pre-refactor`
- [x] 1.2 Commit current state: `git add -A && git commit -m "Before refactor"`

## 2. Move Frontend Source Code

- [x] 2.1 Move `src/` to `frontend/src/` using git mv: `git mv src frontend/`
- [x] 2.2 Move `vite.config.ts` to `frontend/`: `git mv vite.config.ts frontend/`
- [x] 2.3 Move root package.json to frontend/: `git mv package.json frontend/package.json`
- [x] 2.4 Move root tsconfig*.json to frontend/: `git mv tsconfig*.json frontend/`

## 3. Move Database Migrations

- [x] 3.1 Create `docker/mysql/migrations/` directory
- [x] 3.2 Move `backend/src/main/resources/db/migration/` to `docker/mysql/migrations/`
- [x] 3.3 Update docker-compose.yml volume mount for migrations

## 4. Clean Up Redundant Files

- [x] 4.1 Remove empty `frontend/src/` if exists
- [x] 4.2 Check `server/` folder - decide keep or remove
- [x] 4.3 Clean up any other empty/confusing folders

## 5. Update Configuration Files

- [x] 5.1 Update vite.config.ts paths if needed
- [x] 5.2 Update docker-compose.yml build contexts if needed
- [x] 5.3 Update .gitignore if needed

## 6. Test and Verify

- [x] 6.1 Test `npm run dev` in frontend/ works
- [x] 6.2 Test `npm run build` in frontend/ works
- [ ] 6.3 Test `docker-compose up --build` works
- [ ] 6.4 Verify all pages still work in browser

## 7. Commit Changes

- [x] 7.1 Add all changes: `git add -A`
- [x] 7.2 Commit refactor: `git commit -m "refactor: reorganize project structure"`
