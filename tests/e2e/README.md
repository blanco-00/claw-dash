# ClawDash E2E Tests

## 运行测试

```bash
# 安装 Playwright
npx playwright install

# 运行所有测试
npx playwright test

# 运行特定测试
npx playwright test tests/e2e/home.spec.ts
```

## 测试用例

### 首页测试

```typescript
import { test, expect } from '@playwright/test'

test('homepage loads correctly', async ({ page }) => {
  await page.goto('http://localhost:5177')
  await expect(page).toHaveTitle(/ClawDash/)
})

test('navigation works', async ({ page }) => {
  await page.goto('http://localhost:5177')
  await page.click('text=任务队列')
  await expect(page).toHaveURL(/.*tasks/)
})
```

### 任务管理测试

```typescript
test('can create task', async ({ page }) => {
  await page.goto('http://localhost:5177/tasks')
  await page.click('text=新建任务')
  await page.fill('input[type="text"]', 'Test Task')
  await page.click('text=创建')
  await expect(page.locator('text=Test Task')).toBeVisible()
})
```

### Agent 管理测试

```typescript
test('agents page displays correctly', async ({ page }) => {
  await page.goto('http://localhost:5177/agents')
  await expect(page.locator('.agent-card')).toBeVisible()
})
```

## CI 集成

```yaml
# .github/workflows/e2e.yml
name: E2E Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: 18
      - run: npm ci
      - run: npx playwright install
      - run: npx playwright test
```
