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
                ? expandPath(customPath) 
                : getConfigPath();
            
            File configFile = new File(configPath);
            if (configFile.isDirectory()) {
                configFile = new File(configFile, "openclaw.json");
            }
            
            if (!configFile.exists()) {
                return Result.error(1, "OpenClaw 配置文件不存在: " + configFile.getPath());
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

    public Result<Map<String, Object>> installClawdashSkill(String clawdashUrl) {
        Map<String, Object> result = new HashMap<>();

        try {
            String skillDir = System.getProperty("user.home") + "/.openclaw/workspace/skills/clawdash";
            String skillFile = skillDir + "/SKILL.md";

            File dir = new File(skillDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    return Result.error(1, "无法创建 Skill 目录: " + skillDir);
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("---\n");
            sb.append("name: clawdash\n");
            sb.append("description: ClawDash 任务队列集成 - 通过 REST API 与 ClawDash 任务队列交互。适用于：当需要创建、管理或查询任务队列时；当需要在多个会话中持续追踪任务状态时；当需要获取任务统计信息时。\n");
            sb.append("---\n\n");
            sb.append("# ClawDash 任务队列\n\n");
            sb.append("ClawDash 是一个可视化的 Agent 管理系统，提供任务队列功能。通过 REST API 与 ClawDash 交互。\n\n");
            sb.append("## 基础信息\n\n");
            sb.append("- **API 地址**: ").append(clawdashUrl).append("\n");
            sb.append("- **任务队列端点**: /api/tasks\n\n");
            sb.append("## 任务操作\n\n");
            sb.append("### 创建任务\n\n");
            sb.append("```bash\n");
            sb.append("curl -X POST ").append(clawdashUrl).append("/api/tasks \\\n");
            sb.append("  -H \"Content-Type: application/json\" \\\n");
            sb.append("  -d '{\n");
            sb.append("    \"type\": \"agent_task\",\n");
            sb.append("    \"payload\": {\"task\": \"your task description\"},\n");
            sb.append("    \"priority\": 5\n");
            sb.append("  }'\n");
            sb.append("```\n\n");
            sb.append("### 查询任务列表\n\n");
            sb.append("```bash\n");
            sb.append("curl ").append(clawdashUrl).append("/api/tasks\n");
            sb.append("```\n\n");
            sb.append("### 查询单个任务\n\n");
            sb.append("```bash\n");
            sb.append("curl ").append(clawdashUrl).append("/api/tasks/{taskId}\n");
            sb.append("```\n\n");
            sb.append("### 更新任务状态\n\n");
            sb.append("```bash\n");
            sb.append("# 标记完成\n");
            sb.append("curl -X PUT ").append(clawdashUrl).append("/api/tasks/{taskId}/complete\n\n");
            sb.append("# 标记失败\n");
            sb.append("curl -X PUT ").append(clawdashUrl).append("/api/tasks/{taskId}/fail \\\n");
            sb.append("  -H \"Content-Type: application/json\" \\\n");
            sb.append("  -d '{\"error\": \"error message\"}'\n\n");
            sb.append("# 取消任务\n");
            sb.append("curl -X PUT ").append(clawdashUrl).append("/api/tasks/{taskId}/cancel\n");
            sb.append("```\n\n");
            sb.append("### 获取任务统计\n\n");
            sb.append("```bash\n");
            sb.append("curl ").append(clawdashUrl).append("/api/tasks/stats\n");
            sb.append("```\n\n");
            sb.append("## 任务类型\n\n");
            sb.append("| 类型 | 说明 |\n");
            sb.append("|------|------|\n");
            sb.append("| `agent_task` | Agent 执行任务 |\n");
            sb.append("| `scheduled_task` | 定时任务 |\n");
            sb.append("| `batch_task` | 批量任务 |\n\n");
            sb.append("## 优先级\n\n");
            sb.append("优先级范围 1-10，数字越大优先级越高。\n\n");
            sb.append("## 使用场景\n\n");
            sb.append("1. **创建后台任务**: 当用户请求需要长时间处理的操作时，创建任务而不是阻塞等待\n");
            sb.append("2. **追踪跨会话任务**: 在多个 OpenClaw 会话中追踪同一个任务的进度\n");
            sb.append("3. **定时任务**: 设置周期性执行的任务\n\n");
            sb.append("## 注意事项\n\n");
            sb.append("- API 返回 JSON 格式数据\n");
            sb.append("- 任务 ID 是 UUID 格式\n");
            sb.append("- 任务状态变更后，OpenClaw 可以查询最新状态\n");

            java.nio.file.Files.writeString(java.nio.file.Paths.get(skillFile), sb.toString());

            result.put("success", true);
            result.put("skillPath", skillFile);
            result.put("message", "ClawDash Skill 安装成功");

            return Result.success(result);

        } catch (Exception e) {
            return Result.error(2, "安装 Skill 失败: " + e.getMessage());
        }
    }

    /**
     * 卸载 ClawDash Skill
     */
    public Result<Map<String, Object>> uninstallClawdashSkill() {
        Map<String, Object> result = new HashMap<>();

        try {
            String skillDir = System.getProperty("user.home") + "/.openclaw/workspace/skills/clawdash";

            // 删除目录
            File dir = new File(skillDir);
            if (dir.exists()) {
                deleteDirectory(dir);
            }

            result.put("success", true);
            result.put("message", "ClawDash Skill 卸载成功");

            return Result.success(result);

        } catch (Exception e) {
            return Result.error(2, "卸载 Skill 失败: " + e.getMessage());
        }
    }

    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }
}
