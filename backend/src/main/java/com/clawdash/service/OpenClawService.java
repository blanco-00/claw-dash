package com.clawdash.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clawdash.common.Result;
import com.clawdash.entity.OpenClawConfig;
import com.clawdash.mapper.OpenClawConfigMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OpenClawService {

    @Autowired
    private OpenClawConfigMapper configMapper;

    @Value("${openclaw.api-url:http://localhost:3000}")
    private String openClawApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public Result<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        
        String apiUrl = getSavedApiUrl();
        
        try {
            String healthUrl = apiUrl + "/health";
            restTemplate.getForObject(healthUrl, String.class);
            status.put("running", true);
            status.put("apiUrl", apiUrl);
        } catch (Exception e) {
            status.put("running", false);
            status.put("apiUrl", apiUrl);
            status.put("error", e.getMessage());
        }
        
        status.put("timestamp", LocalDateTime.now().toString());
        
        return Result.success(status);
    }

    private String getSavedApiUrl() {
        OpenClawConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<OpenClawConfig>()
                        .eq(OpenClawConfig::getConfigKey, "OPENCLAW_API_URL")
        );
        if (config != null && config.getConfigValue() != null) {
            return config.getConfigValue();
        }
        return openClawApiUrl;
    }

    public Result<Map<String, Object>> install() {
        Map<String, Object> result = new HashMap<>();
        
        saveConfig("OPENCLAW_INSTALLED", "true");
        saveConfig("OPENCLAW_INSTALL_DATE", LocalDateTime.now().toString());
        
        result.put("installed", true);
        result.put("message", "OpenClaw installation recorded");
        
        return Result.success(result);
    }

    public Result<Map<String, Object>> uninstall() {
        Map<String, Object> result = new HashMap<>();
        
        configMapper.delete(new LambdaQueryWrapper<OpenClawConfig>()
                .eq(OpenClawConfig::getConfigKey, "OPENCLAW_INSTALLED"));
        
        result.put("uninstalled", true);
        result.put("message", "OpenClaw uninstallation recorded");
        
        return Result.success(result);
    }

    public Result<Map<String, String>> getConfig() {
        List<OpenClawConfig> configs = configMapper.selectList(null);
        Map<String, String> configMap = configs.stream()
                .collect(Collectors.toMap(
                        OpenClawConfig::getConfigKey,
                        OpenClawConfig::getConfigValue,
                        (v1, v2) -> v1
                ));
        
        return Result.success(configMap);
    }

    public Result<Void> updateConfig(Map<String, String> config) {
        for (Map.Entry<String, String> entry : config.entrySet()) {
            saveConfig(entry.getKey(), entry.getValue());
        }
        return Result.success(null);
    }

    public Result<Map<String, Object>> getPlugins() {
        Map<String, Object> plugins = new HashMap<>();
        plugins.put("available", List.of("memory", "code-executor", "web-search"));
        plugins.put("enabled", List.of("memory"));
        return Result.success(plugins);
    }

    public Result<Void> togglePlugin(String pluginId) {
        return Result.success(null);
    }

    // ========== 一键对接相关方法 ==========

    private String getConfigPath() {
        OpenClawConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<OpenClawConfig>()
                        .eq(OpenClawConfig::getConfigKey, "OPENCLAW_CONFIG_PATH")
        );
        if (config != null && config.getConfigValue() != null && !config.getConfigValue().isEmpty()) {
            return expandPath(config.getConfigValue());
        }
        return System.getProperty("user.home") + "/.openclaw/openclaw.json";
    }

    private String expandPath(String path) {
        if (path != null && path.startsWith("~")) {
            String home = System.getProperty("user.home");
            if (path.length() > 1 && path.charAt(1) == '/') {
                return home + path.substring(1);
            }
            return home;
        }
        return path;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 自动检测 OpenClaw 配置
     * 1. 读取配置文件获取候选端口
     * 2. 调用 API 验证运行状态
     * 3. 返回检测结果
     */
    public Result<Map<String, Object>> autoDetect(String customPath) {
        Map<String, Object> result = new HashMap<>();

        try {
            String configPath = customPath != null && !customPath.isEmpty() 
                ? customPath 
                : getConfigPath();
            
            // 1. 读取配置文件
            File configFile = new File(configPath);
            if (!configFile.exists()) {
                return Result.error(1, "OpenClaw 配置文件不存在: " + configPath);
            }

            JsonNode root = objectMapper.readTree(configFile);

            // 2. 获取 gateway 配置
            JsonNode gateway = root.get("gateway");
            if (gateway == null) {
                return Result.error(2, "配置文件缺少 gateway 配置");
            }

            int port = gateway.has("port") ? gateway.get("port").asInt() : 18789;
            String token = gateway.has("auth") && gateway.has("token")
                ? gateway.get("auth").get("token").asText() : "";

            String apiUrl = "http://localhost:" + port;

            // 3. 验证 API 是否运行
            boolean running = false;
            String errorMsg = null;
            try {
                String healthUrl = apiUrl + "/health";
                restTemplate.getForObject(healthUrl, String.class);
                running = true;
            } catch (Exception e) {
                errorMsg = "OpenClaw 未运行: " + e.getMessage();
            }

            // 4. 获取插件列表
            Map<String, Object> plugins = new HashMap<>();
            JsonNode pluginsNode = root.get("plugins");
            if (pluginsNode != null && pluginsNode.has("entries")) {
                JsonNode entries = pluginsNode.get("entries");
                entries.fields().forEachRemaining(entry -> {
                    Map<String, Object> pluginInfo = new HashMap<>();
                    pluginInfo.put("enabled", entry.getValue().has("enabled") && entry.getValue().get("enabled").asBoolean());
                    if (entry.getValue().has("config")) {
                        pluginInfo.put("config", entry.getValue().get("config"));
                    }
                    plugins.put(entry.getKey(), pluginInfo);
                });
            }

            // 5. 获取工作空间列表
            List<String> workspaces = new ArrayList<>();
            JsonNode workspacesNode = root.get("workspaces");
            if (workspacesNode != null && workspacesNode.isArray()) {
                workspacesNode.forEach(ws -> workspaces.add(ws.get("id").asText()));
            }

            // 6. 组装结果
            result.put("running", running);
            result.put("apiUrl", apiUrl);
            result.put("token", running ? "" : token); // 运行时不返回 token
            result.put("plugins", plugins);
            result.put("workspaces", workspaces);

            if (!running) {
                result.put("error", errorMsg);
            }

            return Result.success(result);

        } catch (Exception e) {
            return Result.error(3, "读取配置失败: " + e.getMessage());
        }
    }

    /**
     * 确认对接 - 保存配置到数据库
     */
    public Result<Map<String, Object>> confirmConnect(String apiUrl, String token, String configPath) {
        Map<String, Object> result = new HashMap<>();

        // 验证 API 是否可访问
        try {
            String healthUrl = apiUrl + "/health";
            restTemplate.getForObject(healthUrl, String.class);
        } catch (Exception e) {
            return Result.error(1, "无法连接到 OpenClaw: " + e.getMessage());
        }

        // 保存配置
        saveConfig("OPENCLAW_API_URL", apiUrl);
        saveConfig("OPENCLAW_TOKEN", token);
        saveConfig("OPENCLAW_CONNECTED", "true");
        saveConfig("OPENCLAW_CONNECT_DATE", LocalDateTime.now().toString());
        if (configPath != null && !configPath.isEmpty()) {
            saveConfig("OPENCLAW_CONFIG_PATH", configPath);
        }

        result.put("connected", true);
        result.put("apiUrl", apiUrl);
        result.put("message", "对接成功");

        return Result.success(result);
    }

    /**
     * 一键配置 MCP - 将 MCP 配置写入 OpenClaw 配置文件
     */
    public Result<Map<String, Object>> configureMcp(String configPath, String clawdashUrl) {
        Map<String, Object> result = new HashMap<>();

        try {
            String path = configPath != null && !configPath.isEmpty() 
                ? expandPath(configPath)
                : getConfigPath();
            
            File configFile = new File(path);
            if (!configFile.exists()) {
                return Result.error(1, "OpenClaw 配置文件不存在: " + path);
            }

            JsonNode root = objectMapper.readTree(configFile);
            boolean modified = false;

            ObjectNode mcpServersNode = (ObjectNode) root.get("mcpServers");
            if (mcpServersNode == null) {
                mcpServersNode = objectMapper.createObjectNode();
                ((ObjectNode) root).set("mcpServers", mcpServersNode);
                modified = true;
            }

            ObjectNode existingClawdash = (ObjectNode) mcpServersNode.get("clawdash");
            if (existingClawdash == null || !clawdashUrl.equals(existingClawdash.get("url").asText())) {
                ObjectNode clawdashNode = objectMapper.createObjectNode();
                clawdashNode.put("url", clawdashUrl);
                clawdashNode.put("transport", "sse");
                mcpServersNode.set("clawdash", clawdashNode);
                modified = true;
            }

            if (modified) {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(configFile, root);
            }

            result.put("success", true);
            result.put("configPath", path);
            result.put("mcpUrl", clawdashUrl);
            result.put("message", "MCP 配置已成功写入: " + path);

            return Result.success(result);

        } catch (Exception e) {
            return Result.error(2, "写入 MCP 配置失败: " + e.getMessage());
        }
    }

    private void saveConfig(String key, String value) {
        OpenClawConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<OpenClawConfig>()
                        .eq(OpenClawConfig::getConfigKey, key)
        );
        
        if (config == null) {
            config = new OpenClawConfig();
            config.setConfigKey(key);
            config.setCreatedAt(LocalDateTime.now());
        }
        
        config.setConfigValue(value);
        config.setUpdatedAt(LocalDateTime.now());
        
        if (config.getId() == null) {
            configMapper.insert(config);
        } else {
            configMapper.updateById(config);
        }
    }

    // CLI Operations
    private static final String OPENCLAW_DIR = System.getProperty("user.home") + "/.openclaw";

    public List<String> listAgents() {
        List<String> agents = new ArrayList<>();
        try {
            ProcessBuilder pb = new ProcessBuilder("openclaw", "agents", "list");
            pb.directory(new java.io.File(OPENCLAW_DIR));
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    agents.add(line.trim());
                }
            }
            process.waitFor();
        } catch (Exception e) {
            // Return empty list on error
        }
        return agents;
    }

    public boolean addAgent(String name, String workspace) {
        try {
            ProcessBuilder pb = new ProcessBuilder("openclaw", "agents", "add", name, "--workspace", workspace);
            pb.directory(new java.io.File(OPENCLAW_DIR));
            Process process = pb.start();
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteAgent(String name) {
        try {
            ProcessBuilder pb = new ProcessBuilder("openclaw", "agents", "delete", name);
            pb.directory(new java.io.File(OPENCLAW_DIR));
            Process process = pb.start();
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean bindAgent(String name, String channel) {
        try {
            ProcessBuilder pb = new ProcessBuilder("openclaw", "agents", "bind", "--agent", name, "--bind", channel);
            pb.directory(new java.io.File(OPENCLAW_DIR));
            Process process = pb.start();
            process.waitFor();
            return process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public String getMainAgentId() {
        return "main";
    }
}
