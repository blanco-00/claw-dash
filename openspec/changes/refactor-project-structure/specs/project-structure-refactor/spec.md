## ADDED Requirements

### Requirement: Frontend code is in frontend/ directory
The frontend source code SHALL be located in the `frontend/` directory.

#### Scenario: Frontend src folder exists
- **WHEN** listing the project root
- **THEN** `frontend/src/` directory exists with all Vue components

#### Scenario: Root src folder does not exist
- **WHEN** after refactoring is complete
- **THEN** no `src/` folder exists in project root

### Requirement: Database migrations are in docker/mysql/migrations/
Database migration files SHALL be located in `docker/mysql/migrations/`.

#### Scenario: Migration files location
- **WHEN** checking `docker/mysql/migrations/`
- **THEN** SQL migration files exist there

#### Scenario: No migrations in backend folder
- **WHEN** after refactoring is complete
- **THEN** no migration files in `backend/src/main/resources/`

### Requirement: package.json is in frontend/
The frontend package.json SHALL be in `frontend/package.json`.

#### Scenario: Package.json location
- **WHEN** listing frontend directory
- **THEN** `frontend/package.json` exists with all frontend dependencies

### Requirement: Docker compose builds successfully
The docker-compose.yml SHALL successfully build and run all services after refactoring.

#### Scenario: Docker compose up
- **WHEN** running `docker-compose up --build`
- **THEN** all services start without errors

#### Scenario: Frontend builds
- **WHEN** running `npm run build` in frontend directory
- **THEN** build completes successfully

### Requirement: Development server works
The development server SHALL work from the new location.

#### Scenario: Dev server starts
- **WHEN** running `npm run dev` in frontend directory
- **THEN** development server starts on expected port
