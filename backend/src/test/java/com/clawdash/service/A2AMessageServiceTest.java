package com.clawdash.service;

import com.clawdash.entity.A2AMessage;
import com.clawdash.mapper.A2AMessageMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class A2AMessageServiceTest {

    @Mock
    private A2AMessageMapper messageMapper;

    @InjectMocks
    private A2AMessageService a2AMessageService;

    @Test
    void testLogMessage_CreatesMessageWithCorrectFields() {
        A2AMessage result = a2AMessageService.logMessage("agent-1", "agent-2", "Hello", "CHAT");
        
        assertNotNull(result);
        assertEquals("agent-1", result.getFromAgentId());
        assertEquals("agent-2", result.getToAgentId());
        assertEquals("Hello", result.getContent());
        assertEquals("CHAT", result.getMessageType());
        assertEquals("PENDING", result.getStatus());
        assertNotNull(result.getSentAt());
        verify(messageMapper, times(1)).insert(any(A2AMessage.class));
    }

    @Test
    void testGetMessageStats_ReturnsCorrectCounts() {
        when(messageMapper.selectCount(any())).thenReturn(5L, 3L);

        Map<String, Long> stats = a2AMessageService.getMessageStats("agent-1");

        assertNotNull(stats);
        assertEquals(5L, stats.get("sentCount"));
        assertEquals(3L, stats.get("receivedCount"));
    }

    @Test
    void testMarkDelivered_UpdatesStatus() {
        A2AMessage message = new A2AMessage();
        message.setId(1L);
        message.setStatus("PENDING");
        when(messageMapper.selectById(1L)).thenReturn(message);

        a2AMessageService.markDelivered("1");

        assertEquals("DELIVERED", message.getStatus());
        assertNotNull(message.getDeliveredAt());
        verify(messageMapper, times(1)).updateById(message);
    }

    @Test
    void testMarkDelivered_DoesNothing_WhenMessageNotFound() {
        when(messageMapper.selectById(999L)).thenReturn(null);

        a2AMessageService.markDelivered("999");

        verify(messageMapper, never()).updateById(any());
    }
}
