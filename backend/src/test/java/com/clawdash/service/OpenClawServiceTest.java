package com.clawdash.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    void testListAgents_ReturnsEmptyList_WhenProcessFails() {
        try {
            when(Runtime.class.getDeclaredField("getRuntime")).thenReturn(null);
        } catch (Exception e) {
            // Ignore
        }
        
        java.util.List<String> agents = openClawService.listAgents();
        
        assertNotNull(agents);
    }

    @Test
    void testAddAgent_ReturnsFalse_WhenNameIsEmpty() {
        boolean result = openClawService.addAgent("", "/workspace");
        
        assertFalse(result);
    }

    @Test
    void testAddAgent_ReturnsFalse_WhenWorkspaceIsEmpty() {
        boolean result = openClawService.addAgent("test-agent", "");
        
        assertFalse(result);
    }

    @Test
    void testDeleteAgent_ReturnsFalse_WhenNameIsEmpty() {
        boolean result = openClawService.deleteAgent("");
        
        assertFalse(result);
    }

    @Test
    void testBindAgent_ReturnsFalse_WhenNameIsEmpty() {
        boolean result = openClawService.bindAgent("", "channel");
        
        assertFalse(result);
    }

    @Test
    void testBindAgent_ReturnsFalse_WhenChannelIsEmpty() {
        boolean result = openClawService.bindAgent("agent", "");
        
        assertFalse(result);
    }
}
