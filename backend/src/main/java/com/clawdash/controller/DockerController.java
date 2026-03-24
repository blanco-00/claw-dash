package com.clawdash.controller;

import com.clawdash.common.Result;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/docker")
public class DockerController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/status")
    public Result<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            String output = dockerApi("/version");
            if (output != null && !output.isEmpty()) {
                JsonNode info = objectMapper.readTree(output);
                status.put("connected", true);
                status.put("message", "Docker connected: " + info.path("Version").asText("unknown"));
            } else {
                status.put("connected", false);
                status.put("message", "Docker not accessible - no output");
            }
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
        
        try {
            String output = dockerApi("/containers/json?all=true");
            if (output != null && !output.isEmpty()) {
                JsonNode root = objectMapper.readTree(output);
                if (root.isArray()) {
                    for (JsonNode container : root) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", container.path("Id").asText(""));
                        map.put("name", container.path("Names").asText("").replaceFirst("^/", ""));
                        map.put("status", container.path("State").asText(""));
                        map.put("image", container.path("Image").asText(""));
                        
                        String ports = "";
                        JsonNode portsNode = container.path("Ports");
                        if (portsNode.isArray()) {
                            List<String> portList = new ArrayList<>();
                            for (JsonNode port : portsNode) {
                                String ip = port.path("IP").asText("0.0.0.0");
                                int privatePort = port.path("PrivatePort").asInt();
                                int publicPort = port.path("PublicPort").asInt();
                                String type = port.path("Type").asText("tcp");
                                if (publicPort > 0) {
                                    portList.add(ip + ":" + publicPort + "->" + privatePort + "/" + type);
                                } else {
                                    portList.add(privatePort + "/" + type);
                                }
                            }
                            ports = String.join(", ", portList);
                        }
                        map.put("ports", ports);
                        
                        map.put("created", container.path("Created").asText(""));
                        containers.add(map);
                    }
                }
            }
        } catch (Exception ignored) {}
        
        return Result.success(containers);
    }

    @GetMapping("/images")
    public Result<List<Map<String, Object>>> getImages() {
        List<Map<String, Object>> images = new ArrayList<>();
        
        try {
            String output = dockerApi("/images/json");
            if (output != null && !output.isEmpty()) {
                JsonNode root = objectMapper.readTree(output);
                if (root.isArray()) {
                    for (JsonNode image : root) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", image.path("Id").asText("").replaceFirst("sha256:", ""));
                        map.put("size", formatSize(image.path("Size").asLong(0)));
                        map.put("created", image.path("Created").asText(""));
                        images.add(map);
                    }
                }
            }
        } catch (Exception ignored) {}
        
        return Result.success(images);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            String containersOutput = dockerApi("/containers/json?all=true");
            int running = 0;
            int total = 0;
            if (containersOutput != null && !containersOutput.isEmpty()) {
                JsonNode root = objectMapper.readTree(containersOutput);
                if (root.isArray()) {
                    total = root.size();
                    for (JsonNode container : root) {
                        if ("running".equalsIgnoreCase(container.path("State").asText())) {
                            running++;
                        }
                    }
                }
            }
            
            String imagesOutput = dockerApi("/images/json");
            int images = 0;
            if (imagesOutput != null && !imagesOutput.isEmpty()) {
                JsonNode root = objectMapper.readTree(imagesOutput);
                if (root.isArray()) {
                    images = root.size();
                }
            }
            
            String volumesOutput = dockerApi("/volumes");
            int volumes = 0;
            if (volumesOutput != null && !volumesOutput.isEmpty()) {
                JsonNode root = objectMapper.readTree(volumesOutput);
                JsonNode volumesNode = root.path("Volumes");
                if (volumesNode.isArray()) {
                    volumes = volumesNode.size();
                }
            }
            
            stats.put("containersRunning", running);
            stats.put("containersTotal", total);
            stats.put("images", images);
            stats.put("volumes", volumes);
        } catch (Exception e) {
            stats.put("containersRunning", 0);
            stats.put("containersTotal", 0);
            stats.put("images", 0);
            stats.put("volumes", 0);
        }
        
        return Result.success(stats);
    }

    private String dockerApi(String path) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "curl", "--unix-socket", "/var/run/docker.sock",
                "-s", "http://localhost" + path
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return null;
            }
            return output;
        } catch (Exception e) {
            return null;
        }
    }
    
    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / 1024.0 / 1024.0);
        return String.format("%.1f GB", bytes / 1024.0 / 1024.0 / 1024.0);
    }
}
