package com.clawdash.controller;

import com.clawdash.common.Result;
import com.clawdash.entity.A2AMessage;
import com.clawdash.service.A2AMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/a2a")
public class A2AMessageController {

    @Autowired
    private A2AMessageService a2AMessageService;

    @GetMapping("/messages")
    public Result<List<A2AMessage>> getMessages(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String since) {
        
        LocalDateTime sinceTime = null;
        if (since != null && !since.isEmpty()) {
            try {
                sinceTime = LocalDateTime.parse(since, DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception ignored) {
            }
        }
        
        List<A2AMessage> messages;
        if (from != null) {
            messages = a2AMessageService.getMessagesByAgent(from, sinceTime);
        } else if (to != null) {
            messages = a2AMessageService.getMessagesByAgent(to, sinceTime);
        } else {
            messages = a2AMessageService.lambdaQuery()
                    .ge(sinceTime != null, A2AMessage::getSentAt, sinceTime)
                    .orderByDesc(A2AMessage::getSentAt)
                    .list();
        }
        
        return Result.success(messages);
    }

    @PostMapping("/send")
    public Result<A2AMessage> send(@RequestBody Map<String, String> body) {
        String fromAgentId = body.get("fromAgentId");
        String toAgentId = body.get("toAgentId");
        String content = body.get("content");
        String type = body.getOrDefault("type", "text");
        
        A2AMessage message = a2AMessageService.logMessage(fromAgentId, toAgentId, content, type);
        return Result.success(message);
    }

    @GetMapping("/stats/{agentId}")
    public Result<Map<String, Long>> getStats(@PathVariable String agentId) {
        Map<String, Long> stats = a2AMessageService.getMessageStats(agentId);
        return Result.success(stats);
    }
}
