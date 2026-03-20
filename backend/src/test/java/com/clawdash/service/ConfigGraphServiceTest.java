package com.clawdash.service;

import com.clawdash.entity.ConfigGraph;
import com.clawdash.entity.ConfigGraphEdge;
import com.clawdash.entity.ConfigGraphNode;
import com.clawdash.mapper.ConfigGraphEdgeMapper;
import com.clawdash.mapper.ConfigGraphMapper;
import com.clawdash.mapper.ConfigGraphNodeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigGraphServiceTest {

    @Mock
    private ConfigGraphMapper graphMapper;

    @Mock
    private ConfigGraphNodeMapper nodeMapper;

    @Mock
    private ConfigGraphEdgeMapper edgeMapper;

    @InjectMocks
    private ConfigGraphService configGraphService;

    @Test
    void testCreateDefaultGraph() {
        ConfigGraph result = configGraphService.createDefaultGraph();
        
        assertNotNull(result);
        assertEquals("Default Graph", result.getName());
        assertEquals(1, result.getVersion());
        verify(graphMapper, times(1)).insert(any(ConfigGraph.class));
    }

    @Test
    void testGetGraphWithDetails_ReturnsNull_WhenGraphNotFound() {
        when(graphMapper.selectById(999L)).thenReturn(null);
        
        Map<String, Object> result = configGraphService.getGraphWithDetails(999L);
        
        assertNull(result);
    }

    @Test
    void testAddNode() {
        ConfigGraphNode result = configGraphService.addNode(1L, "agent-1", 100.0, 200.0);
        
        assertNotNull(result);
        assertEquals(1L, result.getGraphId());
        assertEquals("agent-1", result.getAgentId());
        assertEquals(100.0, result.getX());
        assertEquals(200.0, result.getY());
        assertFalse(result.getCollapsed());
        verify(nodeMapper, times(1)).insert(any(ConfigGraphNode.class));
    }

    @Test
    void testAddEdge() {
        ConfigGraphEdge result = configGraphService.addEdge(1L, "agent-1", "agent-2", "communicates", "test");
        
        assertNotNull(result);
        assertEquals(1L, result.getGraphId());
        assertEquals("agent-1", result.getSourceId());
        assertEquals("agent-2", result.getTargetId());
        assertEquals("communicates", result.getEdgeType());
        assertEquals("test", result.getLabel());
        assertTrue(result.getEnabled());
        verify(edgeMapper, times(1)).insert(any(ConfigGraphEdge.class));
    }

    @Test
    void testRemoveEdge() {
        configGraphService.removeEdge(1L);
        
        verify(edgeMapper, times(1)).deleteById(1L);
    }

    @Test
    void testUpdateEdge_WithEnabledFlag() {
        ConfigGraphEdge edge = new ConfigGraphEdge();
        edge.setId(1L);
        edge.setEnabled(true);
        when(edgeMapper.selectById(1L)).thenReturn(edge);
        
        configGraphService.updateEdge(1L, false, null);
        
        assertFalse(edge.getEnabled());
        verify(edgeMapper, times(1)).updateById(edge);
    }

    @Test
    void testUpdateEdge_WithLabel() {
        ConfigGraphEdge edge = new ConfigGraphEdge();
        edge.setId(1L);
        edge.setLabel("old");
        when(edgeMapper.selectById(1L)).thenReturn(edge);
        
        configGraphService.updateEdge(1L, null, "new-label");
        
        assertEquals("new-label", edge.getLabel());
        verify(edgeMapper, times(1)).updateById(edge);
    }
}
