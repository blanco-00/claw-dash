package com.clawdash.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clawdash.entity.A2AMessage;
import com.clawdash.mapper.A2AMessageMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class A2AMessageService extends ServiceImpl<A2AMessageMapper, A2AMessage> {

    @Autowired(required = false)
    @Lazy
    private TaskMessageHandler taskMessageHandler;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    public A2AMessage logMessage(String fromAgentId, String toAgentId, String content, String type) {
        A2AMessage message = new A2AMessage();
        message.setFromAgentId(fromAgentId);
        message.setToAgentId(toAgentId);
        message.setContent(content);
        message.setMessageType(type);
        message.setStatus("PENDING");
        message.setSentAt(LocalDateTime.now());
        save(message);

        if (taskMessageHandler != null && taskMessageHandler.isTaskCommand(content)) {
            taskMessageHandler.handleIfTaskCommand(content, fromAgentId, toAgentId);
        }

        return message;
    }

    public List<A2AMessage> getMessagesByAgent(String agentId, LocalDateTime since) {
        return lambdaQuery()
                .and(wrapper -> wrapper
                        .eq(A2AMessage::getFromAgentId, agentId)
                        .or()
                        .eq(A2AMessage::getToAgentId, agentId)
                )
                .ge(since != null, A2AMessage::getSentAt, since)
                .orderByDesc(A2AMessage::getSentAt)
                .list();
    }

    public Map<String, Long> getMessageStats(String agentId) {
        Map<String, Long> stats = new HashMap<>();
        
        long sentCount = lambdaQuery()
                .eq(A2AMessage::getFromAgentId, agentId)
                .count();
        
        long receivedCount = lambdaQuery()
                .eq(A2AMessage::getToAgentId, agentId)
                .count();
        
        stats.put("sentCount", sentCount);
        stats.put("receivedCount", receivedCount);
        return stats;
    }

    public void markDelivered(String messageId) {
        A2AMessage message = getById(Long.parseLong(messageId));
        if (message != null) {
            message.setStatus("DELIVERED");
            message.setDeliveredAt(LocalDateTime.now());
            updateById(message);
        }
    }
}
