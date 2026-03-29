package com.clawdash.service;

import com.clawdash.dto.ParsedTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MenxiaShengDecompositionServiceTest {

    private MenxiaShengDecompositionService service;

    @BeforeEach
    void setUp() {
        service = new MenxiaShengDecompositionService();
    }

    private ParsedTask createTask(String title, String description, boolean explicitDecompose) {
        ParsedTask task = new ParsedTask();
        task.setTitle(title);
        task.setDescription(description);
        task.setExplicitDecomposeRequested(explicitDecompose);
        return task;
    }

    @Test
    void isComplexTask_SimpleTask_ReturnsFalse() {
        ParsedTask task = createTask("修一下登录bug", "", false);
        assertFalse(service.isComplexTask(task));
    }

    @Test
    void isComplexTask_SingleGoal_ReturnsFalse() {
        ParsedTask task = createTask("检查支付接口", "", false);
        assertFalse(service.isComplexTask(task));
    }

    @Test
    void isComplexTask_ExplicitDecomposeRequest_ReturnsTrue() {
        ParsedTask task = createTask("修一下bug", "", true);
        assertTrue(service.isComplexTask(task));
    }

    @Test
    void isComplexTask_WithExplicitFlag_ReturnsTrue() {
        ParsedTask task = createTask("登录和注册", "包括登录和找回密码，并且做第三方登录", true);
        assertTrue(service.isComplexTask(task));
    }

    @Test
    void isComplexTask_SystemPlusMultiGoal_ReturnsTrue() {
        ParsedTask task = createTask("做一个系统", "包括登录和注册，同时还要做第三方登录", false);
        assertTrue(service.isComplexTask(task));
    }
}
