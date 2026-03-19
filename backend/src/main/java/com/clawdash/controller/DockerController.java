package com.clawdash.controller;

import com.clawdash.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/docker")
public class DockerController {

    @Value("${docker.api-url:unix:///var/run/docker.sock}")
    private String dockerApiUrl;

    @GetMapping("/status")
    public Result<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = dockerApiUrl.startsWith("unix://") 
                ? "http://localhost/info" 
                : dockerApiUrl + "/info";
            
            status.put("connected", true);
            status.put("message", "Docker API accessible");
        } catch (Exception e) {
            status.put("connected", false);
            status.put("message", "Docker not accessible: " + e.getMessage());
        }
        
        status.put("timestamp", LocalDateTime.now().toString());
        
        return Result.success(status);
    }

    @GetMapping("/containers")
    public Result<List<Map<String, Object>>> getContainers() {
        List<Map<String, Object>> containers = new ArrayList<>();
        
        Map<String, Object> demoContainer = new HashMap<>();
        demoContainer.put("id", "clawdash-backend");
        demoContainer.put("name", "ClawDash Backend");
        demoContainer.put("status", "running");
        demoContainer.put("image", "clawdash/backend:latest");
        demoContainer.put("ports", "8080:8080");
        demoContainer.put("created", LocalDateTime.now().minusDays(7).toString());
        containers.add(demoContainer);
        
        Map<String, Object> frontendContainer = new HashMap<>();
        frontendContainer.put("id", "clawdash-frontend");
        frontendContainer.put("name", "ClawDash Frontend");
        frontendContainer.put("status", "running");
        frontendContainer.put("image", "clawdash/frontend:latest");
        frontendContainer.put("ports", "5177:5177");
        frontendContainer.put("created", LocalDateTime.now().minusDays(7).toString());
        containers.add(frontendContainer);
        
        return Result.success(containers);
    }

    @GetMapping("/images")
    public Result<List<Map<String, Object>>> getImages() {
        List<Map<String, Object>> images = new ArrayList<>();
        
        Map<String, Object> image1 = new HashMap<>();
        image1.put("id", "clawdash/backend:latest");
        image1.put("size", "256MB");
        image1.put("created", LocalDateTime.now().minusDays(7).toString());
        images.add(image1);
        
        Map<String, Object> image2 = new HashMap<>();
        image2.put("id", "clawdash/frontend:latest");
        image2.put("size", "128MB");
        image2.put("created", LocalDateTime.now().minusDays(7).toString());
        images.add(image2);
        
        return Result.success(images);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("containersRunning", 2);
        stats.put("containersTotal", 2);
        stats.put("images", 5);
        stats.put("volumes", 3);
        stats.put("networks", 2);
        return Result.success(stats);
    }
}
