package com.clawdash.service;

import com.clawdash.common.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuntimeGraphServiceTest {

    @Mock
    private OpenClawService openClawService;

    @InjectMocks
    private RuntimeGraphService runtimeGraphService;

    @Test
    void testGetCurrentRuntimeGraph_ReturnsMap() {
        Result<Map<String, Object>> mockResult = Result.success(Map.of("bindings", java.util.List.of()));
        when(openClawService.getBindings()).thenReturn(mockResult);

        Map<String, Object> result = runtimeGraphService.getCurrentRuntimeGraph();

        assertNotNull(result);
        assertTrue(result.containsKey("nodes"));
        assertTrue(result.containsKey("edges"));
        assertTrue(result.containsKey("timestamp"));
    }

    @Test
    void testGetRuntimeGraphHistory_ReturnsMessage() {
        Map<String, Object> result = runtimeGraphService.getRuntimeGraphHistory("1h");

        assertNotNull(result);
        assertEquals("1h", result.get("timeRange"));
        assertTrue(result.containsKey("message"));
    }

    @Test
    void testGetCurrentRuntimeGraph_HandlesNullBindings() {
        Result<Map<String, Object>> mockResult = Result.success((Map<String, Object>) null);
        when(openClawService.getBindings()).thenReturn(mockResult);

        Map<String, Object> result = runtimeGraphService.getCurrentRuntimeGraph();

        assertNotNull(result);
        assertTrue(result.containsKey("nodes"));
        assertTrue(result.containsKey("edges"));
    }

    @Test
    void testGetCurrentRuntimeGraph_HandlesErrorResult() {
        Result<Map<String, Object>> mockResult = Result.error("Failed");
        when(openClawService.getBindings()).thenReturn(mockResult);

        Map<String, Object> result = runtimeGraphService.getCurrentRuntimeGraph();

        assertNotNull(result);
        assertTrue(result.containsKey("nodes"));
        assertTrue(result.containsKey("edges"));
    }
}
