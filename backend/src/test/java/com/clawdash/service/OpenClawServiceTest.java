package com.clawdash.service;

import com.clawdash.common.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OpenClawServiceTest {

    @Mock
    private com.clawdash.mapper.OpenClawConfigMapper configMapper;

    @InjectMocks
    private OpenClawService openClawService;

    @Test
    void testGetMainAgentId() {
        String mainAgentId = openClawService.getMainAgentId();
        
        assertEquals("main", mainAgentId);
    }

    @Test
    void testBindAgent_ReturnsError_WhenNameIsEmpty() {
        Result<Map<String, Object>> result = openClawService.bindAgent("", "channel");
        
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    void testBindAgent_ReturnsError_WhenChannelIsEmpty() {
        Result<Map<String, Object>> result = openClawService.bindAgent("agent", "");
        
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    void testGetBindings_ReturnsResult() {
        Result<Map<String, Object>> result = openClawService.getBindings();
        
        assertNotNull(result);
    }

    @Test
    void testUnbindAgent_ReturnsError_WhenNameIsEmpty() {
        Result<Map<String, Object>> result = openClawService.unbindAgent("", "channel");
        
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }

    @Test
    void testUnbindAgent_ReturnsError_WhenChannelIsEmpty() {
        Result<Map<String, Object>> result = openClawService.unbindAgent("agent", "");
        
        assertNotNull(result);
        assertNotEquals(200, result.getCode());
    }
}
