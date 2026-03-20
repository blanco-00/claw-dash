package com.clawdash.controller;

import com.clawdash.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/agents")
public class AgentFileController {

    @Value("${openclaw.dir:/Users/hannah/openclaw}")
    private String openclawDir;

    @GetMapping("/{agentId}/files/{fileName}")
    public Result<Map<String, String>> getAgentFile(
            @PathVariable String agentId,
            @PathVariable String fileName) {
        try {
            Path filePath = Paths.get(openclawDir, "agents", agentId, fileName);
            if (Files.exists(filePath)) {
                String content = Files.readString(filePath);
                Map<String, String> result = new HashMap<>();
                result.put("content", content);
                result.put("path", filePath.toString());
                return Result.success(result);
            } else {
                return Result.error("File not found: " + fileName);
            }
        } catch (IOException e) {
            return Result.error("Failed to read file: " + e.getMessage());
        }
    }

    @PutMapping("/{agentId}/files/{fileName}")
    public Result<Void> saveAgentFile(
            @PathVariable String agentId,
            @PathVariable String fileName,
            @RequestBody Map<String, String> request) {
        try {
            String content = request.get("content");
            if (content == null) {
                return Result.error("Content is required");
            }
            
            Path filePath = Paths.get(openclawDir, "agents", agentId, fileName);
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, content);
            return Result.success();
        } catch (IOException e) {
            return Result.error("Failed to save file: " + e.getMessage());
        }
    }

    @GetMapping("/{agentId}/files")
    public Result<Map<String, Boolean>> listAgentFiles(@PathVariable String agentId) {
        try {
            Path agentDir = Paths.get(openclawDir, "agents", agentId);
            Map<String, Boolean> files = new HashMap<>();
            
            String[] fileNames = {"SOUL.md", "AGENT.md", "USER.md", "MEMORY.md"};
            for (String fileName : fileNames) {
                Path filePath = agentDir.resolve(fileName);
                files.put(fileName, Files.exists(filePath));
            }
            
            return Result.success(files);
        } catch (Exception e) {
            return Result.error("Failed to list files: " + e.getMessage());
        }
    }
}
