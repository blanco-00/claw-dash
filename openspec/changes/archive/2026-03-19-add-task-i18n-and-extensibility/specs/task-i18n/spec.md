## ADDED Requirements

### Requirement: Task Queue displays in selected language
The system SHALL display all Task Queue UI text in the user's selected language (Chinese or English).

#### Scenario: User views Task Queue in English
- **WHEN** user visits `/tasks` with locale set to English
- **THEN** all UI text displays in English (Task Queue, Pending, Running, Completed, Failed, Create Task, etc.)

#### Scenario: User views Task Queue in Chinese
- **WHEN** user visits `/tasks` with locale set to Chinese
- **THEN** all UI text displays in Chinese (任务队列, 待处理, 运行中, 已完成, 失败, 创建任务, etc.)

### Requirement: Language switcher works correctly
The system SHALL allow users to switch between Chinese and English languages.

#### Scenario: User switches language
- **WHEN** user clicks language switcher and selects a different language
- **THEN** all Task Queue text updates to the selected language immediately

### Requirement: All Task Queue strings are internationalized
The system SHALL have all visible strings in the Task Queue module use i18n keys, not hardcoded text.

#### Scenario: Page header uses i18n
- **WHEN** Task Queue page renders
- **THEN** the page title uses i18n key (e.g., `taskQueue.title`)

#### Scenario: Statistics cards use i18n
- **WHEN** Task Queue statistics cards render
- **THEN** each label uses i18n key (e.g., `taskQueue.stats.pending`, `taskQueue.stats.running`)

#### Scenario: Create Task dialog uses i18n
- **WHEN** Create Task dialog opens
- **THEN** all labels, buttons, and placeholders use i18n keys

### Requirement: Tagline is removed from header
The system SHALL NOT display the "Async task management with Spring Boot + MySQL + Redis" tagline.

#### Scenario: Header renders without tagline
- **WHEN** Task Queue page renders
- **THEN** no tagline subtitle appears in the page header
