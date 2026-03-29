package com.clawdash.service;

import com.clawdash.dto.ParsedTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskMessageHandler {

    @Autowired
    private TaskCommandParser taskCommandParser;

    @Autowired
    private MenxiaShengAssignmentService assignmentService;

    public boolean handleIfTaskCommand(String content, String fromAgentId, String toAgentId) {
        if (!taskCommandParser.isTaskCommand(content)) {
            return false;
        }

        ParsedTask parsedTask = taskCommandParser.parse(content);
        if (parsedTask == null) {
            return false;
        }

        assignmentService.assignTask(parsedTask, fromAgentId);
        
        return true;
    }

    public ParsedTask parseTaskCommand(String content) {
        if (!taskCommandParser.isTaskCommand(content)) {
            return null;
        }
        return taskCommandParser.parse(content);
    }

    public boolean isTaskCommand(String content) {
        return taskCommandParser.isTaskCommand(content);
    }
}
