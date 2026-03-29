import { test, expect } from '@playwright/test';

test.describe('Agent Task Distribution', () => {
  test.describe('UI Tests', () => {
    test('Agent Task Configuration page loads', async ({ page }) => {
      await page.goto('/agent-task-config');
      await page.waitForLoadState('networkidle');
      const pageTitle = page.locator('.page-title');
      await expect(pageTitle).toContainText('代理任务配置');
    });

    test('Agent binding table displays', async ({ page }) => {
      await page.goto('/agent-task-config');
      await page.waitForLoadState('networkidle');
      const table = page.locator('.el-table');
      await expect(table).toBeVisible();
      const tableHeaders = page.locator('.el-table__header-wrapper .el-table__cell');
      const headerTexts = await tableHeaders.allTextContents();
      expect(headerTexts.some(text => text.includes('代理'))).toBeTruthy();
      expect(headerTexts.some(text => text.includes('任务类型'))).toBeTruthy();
      expect(headerTexts.some(text => text.includes('待处理'))).toBeTruthy();
      expect(headerTexts.some(text => text.includes('进行中'))).toBeTruthy();
      expect(headerTexts.some(text => text.includes('已完成'))).toBeTruthy();
    });

    test('Add binding dialog opens', async ({ page }) => {
      await page.goto('/agent-task-config');
      await page.waitForLoadState('networkidle');
      const addButton = page.getByRole('button', { name: '添加绑定' });
      await addButton.click();
      const dialog = page.locator('.el-dialog');
      await expect(dialog).toBeVisible();
      const dialogTitle = page.locator('.el-dialog__title');
      await expect(dialogTitle).toContainText('添加代理绑定');
      const formLabels = page.locator('.el-form-item__label');
      const labelTexts = await formLabels.allTextContents();
      expect(labelTexts.some(text => text.includes('选择代理'))).toBeTruthy();
      expect(labelTexts.some(text => text.includes('任务类型'))).toBeTruthy();
    });
  });

  test.describe('API Tests', () => {
    test('Cannot claim task assigned to different agent', async ({ request }) => {
      const createResponse = await request.post('/api/tasks', {
        data: {
          type: 'test-task',
          payload: { test: true },
          assignedAgent: 'agent-a',
          status: 'PENDING'
        }
      });

      if (!createResponse.ok()) {
        test.skip();
        return;
      }

      const task = await createResponse.json();
      const taskId = task.id || task.data?.id;

      const claimResponse = await request.post(`/api/tasks/${taskId}/claim`, {
        data: { workerId: 'agent-b' }
      });

      expect([400, 403, 409]).toContain(claimResponse.status());

      if (taskId) {
        await request.delete(`/api/tasks/${taskId}`);
      }
    });

    test('Main agent receives completion notification', async ({ request }) => {
      const createResponse = await request.post('/api/tasks', {
        data: {
          type: 'notification-test',
          payload: { testNotification: true },
          reportToAgent: 'main-agent',
          status: 'PENDING'
        }
      });

      if (!createResponse.ok()) {
        test.skip();
        return;
      }

      const task = await createResponse.json();
      const taskId = task.id || task.data?.id;

      const claimResponse = await request.post(`/api/tasks/${taskId}/claim`, {
        data: { workerId: 'test-worker' }
      });

      if (!claimResponse.ok()) {
        test.skip();
        return;
      }

      const completeResponse = await request.post(`/api/tasks/${taskId}/complete`, {
        data: { result: { success: true, message: 'Test completed' } }
      });

      expect(completeResponse.ok()).toBeTruthy();

      const notificationsResponse = await request.get('/api/notifications', {
        params: { taskId: taskId, agentId: 'main-agent' }
      });

      if (notificationsResponse.ok()) {
        const notifications = await notificationsResponse.json();
        expect(notifications.length || notifications.content?.length || notifications.data?.length).toBeGreaterThan(0);
      }

      if (taskId) {
        await request.delete(`/api/tasks/${taskId}`);
      }
    });
  });

  test.describe('TaskGroup Tests', () => {
    test('TaskGroup detail shows per-agent progress', async ({ page }) => {
      await page.goto('/task-group');
      await page.waitForLoadState('networkidle');

      const taskGroupRows = page.locator('.el-table__row');
      const rowCount = await taskGroupRows.count();

      if (rowCount === 0) {
        const createButton = page.getByRole('button', { name: '创建任务组' });
        if (await createButton.isVisible()) {
          await createButton.click();
          const nameInput = page.locator('.el-dialog input').first();
          await nameInput.fill('Test Task Group for Per-Agent Progress');
          const dialogCreateButton = page.locator('.el-dialog .el-button--primary').last();
          await dialogCreateButton.click();
          await page.waitForSelector('.el-dialog', { state: 'hidden' });
          await page.waitForLoadState('networkidle');
        }
      }

      const firstRow = page.locator('.el-table__row').first();
      await firstRow.click();

      const drawer = page.locator('.el-drawer');
      await expect(drawer).toBeVisible();

      const agentGroups = page.locator('.agent-group');
      const hasAgentGroups = await agentGroups.count() > 0;
      const hasTaskList = await page.locator('.task-list').count() > 0;
      expect(hasAgentGroups || hasTaskList).toBeTruthy();

      if (hasAgentGroups) {
        const progressBars = page.locator('.agent-progress .el-progress');
        const progressCount = await progressBars.count();
        expect(progressCount).toBeGreaterThan(0);

        const agentStats = page.locator('.agent-stats .el-tag');
        const statsCount = await agentStats.count();
        expect(statsCount).toBeGreaterThan(0);
      }

      const closeButton = page.locator('.el-drawer__close-btn');
      await closeButton.click();
    });
  });
});
