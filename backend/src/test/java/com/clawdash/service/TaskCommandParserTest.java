package com.clawdash.service;

import com.clawdash.dto.ParsedTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskCommandParserTest {

    private TaskCommandParser parser;

    @BeforeEach
    void setUp() {
        parser = new TaskCommandParser();
    }

    @Test
    void parseBasicTask() {
        ParsedTask result = parser.parse("#task 修一下登录bug");
        
        assertEquals("修一下登录bug", result.getTitle());
        assertEquals("medium", result.getPriority());
        assertFalse(result.getExplicitDecomposeRequested());
    }

    @Test
    void parseTaskWithUrgentKeyword_挂() {
        ParsedTask result = parser.parse("#task 支付接口挂了");
        assertEquals("urgent", result.getPriority());
    }

    @Test
    void parseTaskWithUrgentKeyword_崩() {
        ParsedTask result = parser.parse("#task App崩溃了");
        assertEquals("urgent", result.getPriority());
    }

    @Test
    void parseTaskWithHighPriorityKeyword_一直() {
        ParsedTask result = parser.parse("#task 用户登录一直报错");
        assertEquals("high", result.getPriority());
    }

    @Test
    void parseTaskWithHighPriorityKeyword_总是() {
        ParsedTask result = parser.parse("#task 总是出现这个问题");
        assertEquals("high", result.getPriority());
    }

    @Test
    void parseTaskWithDecomposeRequest_拆解() {
        ParsedTask result = parser.parse("#task 做一个登录注册流程 拆解一下");
        assertTrue(result.getExplicitDecomposeRequested());
    }

    @Test
    void parseTaskWithDecomposeRequest_分解() {
        ParsedTask result = parser.parse("#task 复杂任务 分解一下");
        assertTrue(result.getExplicitDecomposeRequested());
    }

    @Test
    void parseTaskWithWhitespace() {
        ParsedTask result = parser.parse("#task   修一下bug  ");
        assertEquals("修一下bug", result.getTitle());
    }

    @Test
    void parseTaskWithoutPrefix() {
        ParsedTask result = parser.parse("修一下bug");
        assertNull(result);
    }

    @Test
    void parseTaskDescriptionExtraction() {
        ParsedTask result = parser.parse("#task 用户登录一直报错，看下是怎么回事");
        assertEquals("用户登录一直报错 看下是怎么回事", result.getTitle());
        assertEquals("", result.getDescription());
    }
}
