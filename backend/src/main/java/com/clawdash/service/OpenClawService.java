package com.clawdash.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clawdash.common.Result;
import com.clawdash.entity.AgentTaskBinding;
import com.clawdash.entity.OpenClawConfig;
import com.clawdash.mapper.AgentTaskBindingMapper;
import com.clawdash.mapper.OpenClawConfigMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OpenClawService {

    @Autowired
    private OpenClawConfigMapper configMapper;

    @Autowired
    private AgentTaskBindingMapper agentTaskBindingMapper;

    @Autowired
    private com.clawdash.mapper.ConfigGraphMapper configGraphMapper;

    @Autowired
    private com.clawdash.mapper.ConfigGraphEdgeMapper configGraphEdgeMapper;

    @Autowired
    private com.clawdash.service.TaskTypeService taskTypeService;

    @Value("${openclaw.api-url:http://localhost:3000}")
    private String openClawApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public Result<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        
        String apiUrl = getSavedApiUrl();
        String dashboardUrl = apiUrl;
        String token = "";
        String version = "unknown";
        List<String> workspaces = new ArrayList<>();
        
        try {
            File configFile = new File(OPENCLAW_DIR, "openclaw.json");
            if (configFile.exists()) {
                JsonNode root = objectMapper.readTree(configFile);
                JsonNode gw = root.get("gateway");
                if (gw != null) {
                    if (gw.has("auth") && gw.get("auth").has("token")) {
                        token = gw.get("auth").get("token").asText();
                        dashboardUrl = apiUrl + "/#token=" + token;
                    }
                    if (gw.has("version")) {
                        version = gw.get("version").asText();
                    }
                }
                JsonNode meta = root.get("meta");
                if (meta != null && meta.has("lastTouchedVersion")) {
                    version = meta.get("lastTouchedVersion").asText();
                }
                JsonNode ws = root.get("workspaces");
                if (ws != null && ws.isArray()) {
                    for (JsonNode w : ws) {
                        if (w.has("id")) {
                            workspaces.add(w.get("id").asText());
                        }
                    }
                }
            }
        } catch (Exception e) {}
        
        // If no workspaces found in config, scan for workspace directories
        if (workspaces.isEmpty()) {
            File openclawDir = new File(OPENCLAW_DIR);
            if (openclawDir.exists() && openclawDir.isDirectory()) {
                File[] dirs = openclawDir.listFiles(File::isDirectory);
                if (dirs != null) {
                    for (File dir : dirs) {
                        String name = dir.getName();
                        if (name.startsWith("workspace") || name.equals("main")) {
                            workspaces.add(name);
                        }
                    }
                }
            }
        }
        
        try {
            String healthUrl = apiUrl + "/health";
            restTemplate.getForObject(healthUrl, String.class);
            status.put("running", true);
            status.put("apiUrl", dashboardUrl);
            status.put("token", token);
        } catch (Exception e) {
            status.put("running", false);
            status.put("apiUrl", dashboardUrl);
            status.put("token", token);
            status.put("error", e.getMessage());
        }
        
        status.put("version", version);
        status.put("workspaces", workspaces);
        status.put("configPath", OPENCLAW_DIR + "/openclaw.json");
        status.put("timestamp", LocalDateTime.now().toString());
        
        return Result.success(status);
    }

    public String getSavedApiUrl() {
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
        try {
            ProcessBuilder pb = new ProcessBuilder("openclaw", "plugins", "list", "--json");
            pb.directory(new File(OPENCLAW_DIR));
            Process process = pb.start();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
            process.waitFor();
            
            if (process.exitValue() == 0) {
                JsonNode root = objectMapper.readTree(output.toString());
                JsonNode pluginsNode = root.get("plugins");
                List<String> available = new ArrayList<>();
                List<String> enabled = new ArrayList<>();
                if (pluginsNode != null && pluginsNode.isArray()) {
                    for (JsonNode plugin : pluginsNode) {
                        String name = plugin.get("id").asText();
                        available.add(name);
                        if (plugin.has("enabled") && plugin.get("enabled").asBoolean()) {
                            enabled.add(name);
                        }
                    }
                }
                Map<String, Object> result = new HashMap<>();
                result.put("available", available);
                result.put("enabled", enabled);
                return Result.success(result);
            } else {
                return Result.error("Failed to get plugins");
            }
        } catch (Exception e) {
            return Result.error("Failed to get plugins: " + e.getMessage());
        }
    }

    public Result<Map<String, Object>> getSkills() {
        try {
            ProcessBuilder pb = new ProcessBuilder("openclaw", "skills", "list", "--json");
            pb.directory(new File(OPENCLAW_DIR));
            pb.redirectErrorStream(true);
            Process process = pb.start();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
            process.waitFor();
            
            if (process.exitValue() == 0) {
                JsonNode root = objectMapper.readTree(output.toString());
                JsonNode skillsNode = root.get("skills");
                List<Map<String, Object>> skills = new ArrayList<>();
                if (skillsNode != null && skillsNode.isArray()) {
                    for (JsonNode skill : skillsNode) {
                        Map<String, Object> skillMap = new HashMap<>();
                        skillMap.put("name", skill.has("name") ? skill.get("name").asText() : "");
                        skillMap.put("description", skill.has("description") ? skill.get("description").asText() : "");
                        skillMap.put("emoji", skill.has("emoji") ? skill.get("emoji").asText() : "");
                        skillMap.put("eligible", skill.has("eligible") ? skill.get("eligible").asBoolean() : false);
                        skillMap.put("disabled", skill.has("disabled") ? skill.get("disabled").asBoolean() : false);
                        skillMap.put("source", skill.has("source") ? skill.get("source").asText() : "");
                        skillMap.put("bundled", skill.has("bundled") ? skill.get("bundled").asBoolean() : false);
                        skillMap.put("homepage", skill.has("homepage") ? skill.get("homepage").asText() : "");
                        
                        JsonNode missing = skill.get("missing");
                        List<String> missingList = new ArrayList<>();
                        if (missing != null) {
                            if (missing.has("bins")) {
                                for (JsonNode bin : missing.get("bins")) {
                                    missingList.add(bin.asText());
                                }
                            }
                        }
                        skillMap.put("missing", missingList);
                        
                        skills.add(skillMap);
                    }
                }
                Map<String, Object> result = new HashMap<>();
                result.put("skills", skills);
                result.put("workspaceDir", root.has("workspaceDir") ? root.get("workspaceDir").asText() : "");
                result.put("managedSkillsDir", root.has("managedSkillsDir") ? root.get("managedSkillsDir").asText() : "");
                return Result.success(result);
            } else {
                return Result.error("Failed to get skills");
            }
        } catch (Exception e) {
            return Result.error("Failed to get skills: " + e.getMessage());
        }
    }

    public Result<Void> togglePlugin(String pluginId) {
        try {
            ProcessBuilder pbCheck = new ProcessBuilder("openclaw", "plugins", "list", "--json");
            pbCheck.directory(new File(OPENCLAW_DIR));
            Process processCheck = pbCheck.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(processCheck.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
            processCheck.waitFor();
            
            JsonNode root = objectMapper.readTree(output.toString());
            JsonNode pluginsNode = root.get("plugins");
            boolean isCurrentlyEnabled = false;
            if (pluginsNode != null && pluginsNode.isArray()) {
                for (JsonNode plugin : pluginsNode) {
                    if (plugin.get("id").asText().equals(pluginId)) {
                        if (plugin.has("enabled") && plugin.get("enabled").asBoolean()) {
                            isCurrentlyEnabled = true;
                        }
                        break;
                    }
                }
            }
            
            String command = isCurrentlyEnabled ? "disable" : "enable";
            ProcessBuilder pb = new ProcessBuilder("openclaw", "plugins", command, pluginId);
            pb.directory(new File(OPENCLAW_DIR));
            Process process = pb.start();
            process.waitFor();
            
            if (process.exitValue() == 0) {
                return Result.success();
            } else {
                return Result.error("Failed to toggle plugin");
            }
        } catch (Exception e) {
            return Result.error("Failed to toggle plugin: " + e.getMessage());
        }
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

    private String getOpenClawDir() {
        OpenClawConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<OpenClawConfig>()
                        .eq(OpenClawConfig::getConfigKey, "OPENCLAW_CONFIG_PATH")
        );
        if (config != null && config.getConfigValue() != null && !config.getConfigValue().isEmpty()) {
            String configFilePath = expandPath(config.getConfigValue());
            File file = new File(configFilePath);
            if (file.isDirectory()) {
                return file.getAbsolutePath();
            }
            return file.getParent();
        }
        return System.getProperty("user.home") + "/.openclaw";
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

            String host = System.getenv("OPENCLAW_HOST") != null 
                ? System.getenv("OPENCLAW_HOST") 
                : "localhost";
            String apiUrl = "http://" + host + ":" + port;

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

    public List<Map<String, String>> listAgentsWithDetails() {
        List<Map<String, String>> agents = new ArrayList<>();
        File agentsDir = new File(OPENCLAW_DIR, "agents");
        if (!agentsDir.exists() || !agentsDir.isDirectory()) {
            return agents;
        }
        
        File[] agentDirs = agentsDir.listFiles(File::isDirectory);
        if (agentDirs == null) return agents;
        
        for (File agentDir : agentDirs) {
            String agentName = agentDir.getName();
            Map<String, String> agent = new HashMap<>();
            agent.put("name", agentName);
            
            String workspacePath = getWorkspacePath(agentName);
            File identityFile = new File(workspacePath, "IDENTITY.md");
            agent.put("workspace", workspacePath);
            agent.put("identityPath", identityFile.exists() ? identityFile.getPath() : "");
            
            if (identityFile.exists()) {
                try {
                    String content = java.nio.file.Files.readString(identityFile.toPath());
                    agent.put("identityContent", content);
                    parseIdentityMarkdown(content, agent);
                } catch (Exception e) {}
            }
            agents.add(agent);
        }
        return agents;
    }
    
    private void parseIdentityMarkdown(String content, Map<String, String> agent) {
        agent.put("title", "");
        agent.put("role", "");
        agent.put("description", "");
        
        for (String line : content.split("\n")) {
            String trimmed = line.trim();
            
            if (trimmed.contains("**封号") && (trimmed.contains("：**") || trimmed.contains(":**"))) {
                String val = extractValue(trimmed, "封号");
                if (!val.isEmpty()) agent.put("title", val);
            } else if (trimmed.contains("**官职") && (trimmed.contains("：**") || trimmed.contains(":**"))) {
                String val = extractValue(trimmed, "官职");
                if (!val.isEmpty()) agent.put("role", val);
            } else if (trimmed.contains("**职责") && (trimmed.contains("：**") || trimmed.contains(":**"))) {
                String val = extractValue(trimmed, "职责");
                if (!val.isEmpty()) agent.put("description", val);
            }
        }
    }
    
    private String extractValue(String line, String key) {
        try {
            int idx = line.indexOf("**" + key);
            if (idx >= 0) {
                int colonIdx = -1;
                int searchStart = idx + 2 + key.length();
                int chineseColon = line.indexOf("：**", searchStart);
                int asciiColon = line.indexOf(":**", searchStart);
                if (chineseColon >= 0 && asciiColon >= 0) {
                    colonIdx = Math.min(chineseColon, asciiColon);
                } else if (chineseColon >= 0) {
                    colonIdx = chineseColon;
                } else if (asciiColon >= 0) {
                    colonIdx = asciiColon;
                }
                if (colonIdx >= 0) {
                    int start = colonIdx + 3;
                    int end = line.length();
                    int newlineIdx = line.indexOf("\n", start);
                    if (newlineIdx > start) end = newlineIdx;
                    int dashIdx = line.indexOf("**", start);
                    if (dashIdx > start && dashIdx < end) end = dashIdx;
                    return line.substring(start, end).trim();
                }
            }
        } catch (Exception e) {}
        return "";
    }
    
    public List<Map<String, String>> listAgentNames() {
        List<Map<String, String>> agents = new ArrayList<>();
        File agentsDir = new File(OPENCLAW_DIR, "agents");
        if (!agentsDir.exists() || !agentsDir.isDirectory()) {
            return agents;
        }
        
        File[] agentDirs = agentsDir.listFiles(File::isDirectory);
        if (agentDirs == null) return agents;
        
        for (File agentDir : agentDirs) {
            Map<String, String> agent = new HashMap<>();
            agent.put("name", agentDir.getName());
            agents.add(agent);
        }
        return agents;
    }

    public List<Map<String, String>> listAgentFiles(String agentName) {
        List<Map<String, String>> files = new ArrayList<>();
        String workspacePath = getWorkspacePath(agentName);
        File workspace = new File(workspacePath);
        
        if (!workspace.exists() || !workspace.isDirectory()) {
            return files;
        }
        
        String[] mdExtensions = {".md", ".json"};
        File[] allFiles = workspace.listFiles();
        if (allFiles == null) return files;
        
        for (File file : allFiles) {
            String lowerName = file.getName().toLowerCase();
            for (String ext : mdExtensions) {
                if (lowerName.endsWith(ext) && !file.getName().startsWith(".")) {
                    Map<String, String> fileInfo = new HashMap<>();
                    fileInfo.put("name", file.getName());
                    fileInfo.put("path", file.getPath());
                    fileInfo.put("size", String.valueOf(file.length()));
                    fileInfo.put("modified", String.valueOf(file.lastModified()));
                    files.add(fileInfo);
                    break;
                }
            }
        }
        
        return files;
    }
    
    public Result<Map<String, Object>> getAgentFileContent(String agentName, String filename) {
        try {
            String workspacePath = getWorkspacePath(agentName);
            File file = new File(workspacePath, filename);
            
            if (!file.exists() || !file.isFile()) {
                return Result.error(404, "File not found: " + filename);
            }
            
            String content = java.nio.file.Files.readString(file.toPath());
            Map<String, Object> result = new HashMap<>();
            result.put("name", filename);
            result.put("path", file.getPath());
            result.put("content", content);
            result.put("size", file.length());
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, "Failed to read file: " + e.getMessage());
        }
    }
    
    public Result<Void> saveAgentFile(String agentName, String filename, String content) {
        try {
            String workspacePath = getWorkspacePath(agentName);
            File file = new File(workspacePath, filename);
            
            if (!file.exists() || !file.isFile()) {
                return Result.error(404, "File not found: " + filename);
            }
            
            java.nio.file.Files.write(file.toPath(), content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return Result.success();
        } catch (Exception e) {
            return Result.error(500, "Failed to save file: " + e.getMessage());
        }
    }
    
    public String getWorkspacePath(String agentName) {
        if ("main".equals(agentName)) {
            return OPENCLAW_DIR + "/workspace";
        }
        return OPENCLAW_DIR + "/workspace-" + agentName;
    }

    public boolean addAgent(String name, String workspace) {
        return addAgent(name, workspace, null);
    }

    public boolean addAgent(String name, String workspace, String copyFrom) {
        try {
            String fullWorkspacePath = new File(OPENCLAW_DIR, workspace).getAbsolutePath();
            ProcessBuilder pb = new ProcessBuilder("openclaw", "agents", "add", name, "--workspace", fullWorkspacePath);
            pb.directory(new File(OPENCLAW_DIR));
            Process process = pb.start();
            process.waitFor();
            boolean cliSuccess = process.exitValue() == 0;
            
            if (cliSuccess && copyFrom != null && !copyFrom.isBlank()) {
                copyAgentFiles(copyFrom, name);
            }
            
            return cliSuccess;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean copyAgentFiles(String sourceName, String targetName) {
        File sourceWorkspace = new File(OPENCLAW_DIR, "workspace-" + sourceName);
        File targetWorkspace = new File(OPENCLAW_DIR, "workspace-" + targetName);
        
        if (!sourceWorkspace.exists() || !sourceWorkspace.isDirectory()) {
            return false;
        }
        
        String[] templateFiles = {
            "AGENTS.md", "BOOTSTRAP.md", "HEARTBEAT.md", 
            "IDENTITY.md", "MEMORY.md", "SOUL.md", 
            "TOOLS.md", "USER.md"
        };
        
        for (String filename : templateFiles) {
            File sourceFile = new File(sourceWorkspace, filename);
            File targetFile = new File(targetWorkspace, filename);
            
            if (sourceFile.exists()) {
                try {
                    java.nio.file.Files.copy(
                        sourceFile.toPath(), 
                        targetFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                    );
                } catch (Exception e) {
                    System.err.println("Failed to copy " + filename + ": " + e.getMessage());
                }
            }
        }
        return true;
    }

    public boolean deleteAgent(String name) {
        if ("main".equals(name)) {
            return false;
        }
        
        try {
            String workspacePath = getWorkspacePath(name);
            File workspaceFile = new File(workspacePath);
            File agentDir = new File(OPENCLAW_DIR, "agents/" + name);
            
            boolean workspaceDeleted = true;
            boolean agentDirDeleted = true;
            boolean cliSuccess = false;
            
            if (workspaceFile.exists()) {
                workspaceDeleted = deleteDirectory(workspaceFile);
            }
            
            if (agentDir.exists()) {
                agentDirDeleted = deleteDirectory(agentDir);
            }
            
            ProcessBuilder pb = new ProcessBuilder("openclaw", "agents", "delete", name, "--force");
            pb.directory(new java.io.File(OPENCLAW_DIR));
            Process process = pb.start();
            process.waitFor();
            cliSuccess = process.exitValue() == 0;
            
            if (!cliSuccess && workspaceDeleted && agentDirDeleted) {
                return true;
            }
            
            return cliSuccess && workspaceDeleted && agentDirDeleted;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean deleteDirectory(File dir) {
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
        return dir.delete();
    }

    public List<String> getOrphanedAgents() {
        List<String> orphaned = new ArrayList<>();
        File agentsDir = new File(OPENCLAW_DIR, "agents");
        if (!agentsDir.exists() || !agentsDir.isDirectory()) {
            return orphaned;
        }
        
        File[] agentDirs = agentsDir.listFiles(File::isDirectory);
        if (agentDirs == null) return orphaned;
        
        for (File agentDir : agentDirs) {
            String agentName = agentDir.getName();
            if ("main".equals(agentName)) {
                continue;
            }
            String workspacePath = getWorkspacePath(agentName);
            File workspace = new File(workspacePath);
            
            if (!workspace.exists()) {
                orphaned.add(agentName);
            }
        }
        return orphaned;
    }

    public Result<Map<String, Object>> cleanupOrphanedAgents() {
        Map<String, Object> result = new HashMap<>();
        List<String> orphaned = getOrphanedAgents();
        List<String> deleted = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        
        for (String name : orphaned) {
            if (deleteAgent(name)) {
                deleted.add(name);
            } else {
                File agentDir = new File(OPENCLAW_DIR, "agents/" + name);
                if (agentDir.exists() && agentDir.delete()) {
                    deleted.add(name);
                } else {
                    failed.add(name);
                }
            }
        }
        
        result.put("deleted", deleted);
        result.put("failed", failed);
        result.put("total", orphaned.size());
        
        return Result.success(result);
    }

    public Result<Map<String, Object>> setIdentity(String name, String identityName, String emoji) {
        Map<String, Object> result = new HashMap<>();
        try {
            ProcessBuilder pb = new ProcessBuilder("openclaw", "agents", "set-identity", "--agent", name);
            if (identityName != null && !identityName.isBlank()) {
                pb.command().add("--name");
                pb.command().add(identityName);
            }
            if (emoji != null && !emoji.isBlank()) {
                pb.command().add("--emoji");
                pb.command().add(emoji);
            }
            pb.directory(new java.io.File(OPENCLAW_DIR));
            Process process = pb.start();
            StringBuilder output = new StringBuilder();
            StringBuilder error = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            while ((line = errorReader.readLine()) != null) {
                error.append(line).append("\n");
            }
            process.waitFor();
            
            boolean success = process.exitValue() == 0;
            result.put("success", success);
            result.put("agentName", name);
            if (identityName != null) result.put("name", identityName);
            if (emoji != null) result.put("emoji", emoji);
            result.put("output", output.toString());
            return success ? Result.success(result) : Result.error(1, "Failed to set identity: " + error);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return Result.error(1, "Exception during set-identity: " + e.getMessage());
        }
    }

    /**
     * Bind an agent to a channel and return binding details.
     * @param name Agent name
     * @param channel Channel name
     * @return Map containing success status and binding details
     */
    public Result<Map<String, Object>> bindAgent(String name, String channel) {
        Map<String, Object> result = new HashMap<>();
        try {
            ProcessBuilder pb = new ProcessBuilder("openclaw", "agents", "bind", "--agent", name, "--bind", channel);
            pb.directory(new java.io.File(OPENCLAW_DIR));
            Process process = pb.start();
            StringBuilder output = new StringBuilder();
            StringBuilder error = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            while ((line = errorReader.readLine()) != null) {
                error.append(line).append("\n");
            }
            process.waitFor();
            
            boolean success = process.exitValue() == 0;
            result.put("success", success);
            result.put("agentName", name);
            result.put("channel", channel);
            result.put("output", output.toString());
            if (!success) {
                result.put("error", error.toString());
            }
            return success ? Result.success(result) : Result.error(1, "Failed to bind agent: " + error);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return Result.error(1, "Exception during bind: " + e.getMessage());
        }
    }

    /**
     * Get current agent bindings from OpenClaw.
     * @return Map containing bindings list or error
     */
    public Result<Map<String, Object>> getBindings() {
        Map<String, Object> result = new HashMap<>();
        try {
            ProcessBuilder pb = new ProcessBuilder("openclaw", "agents", "bindings", "--json");
            pb.directory(new java.io.File(OPENCLAW_DIR));
            Process process = pb.start();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
            process.waitFor();
            
            if (process.exitValue() == 0) {
                String jsonOutput = output.toString();
                // Try to parse as JSON array or object
                try {
                    JsonNode bindingsNode = objectMapper.readTree(jsonOutput);
                    List<Map<String, Object>> bindings = new ArrayList<>();
                    if (bindingsNode.isArray()) {
                        for (JsonNode binding : bindingsNode) {
                            bindings.add(parseBindingNode(binding));
                        }
                    } else if (bindingsNode.isObject()) {
                        // Some CLI output format wraps in an object
                        JsonNode agents = bindingsNode.get("agents");
                        if (agents != null && agents.isArray()) {
                            for (JsonNode agent : agents) {
                                bindings.add(parseBindingNode(agent));
                            }
                        }
                    }
                    result.put("bindings", bindings);
                    result.put("count", bindings.size());
                    return Result.success(result);
                } catch (Exception e) {
                    // If not JSON, return raw output
                    result.put("raw", jsonOutput);
                    result.put("count", 0);
                    return Result.success(result);
                }
            } else {
                return Result.error(1, "Failed to get bindings");
            }
        } catch (Exception e) {
            return Result.error(1, "Exception getting bindings: " + e.getMessage());
        }
    }

    private Map<String, Object> parseBindingNode(JsonNode binding) {
        Map<String, Object> map = new HashMap<>();
        map.put("agent", binding.has("agent") ? binding.get("agent").asText() : "");
        map.put("channels", binding.has("channels") ? binding.get("channels").asText() : "");
        map.put("status", binding.has("status") ? binding.get("status").asText() : "unknown");
        return map;
    }

    /**
     * Unbind an agent from a channel.
     * @param name Agent name
     * @param channel Channel name
     * @return Result indicating success or failure
     */
    public Result<Map<String, Object>> unbindAgent(String name, String channel) {
        Map<String, Object> result = new HashMap<>();
        try {
            ProcessBuilder pb = new ProcessBuilder("openclaw", "agents", "unbind", "--agent", name, "--bind", channel);
            pb.directory(new java.io.File(OPENCLAW_DIR));
            Process process = pb.start();
            StringBuilder output = new StringBuilder();
            StringBuilder error = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            while ((line = errorReader.readLine()) != null) {
                error.append(line).append("\n");
            }
            process.waitFor();
            
            boolean success = process.exitValue() == 0;
            result.put("success", success);
            result.put("agentName", name);
            result.put("channel", channel);
            if (!success) {
                result.put("error", error.toString());
                return Result.error(1, "Failed to unbind agent: " + error);
            }
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(1, "Exception during unbind: " + e.getMessage());
        }
    }

    public String getMainAgentId() {
        return "main";
    }

    public Result<Map<String, Object>> installClawdashSkill(String clawdashUrl) {
        Map<String, Object> result = new HashMap<>();

        try {
            String skillDir = getOpenClawDir() + "/workspace/skills/clawdash";
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
            String skillDir = getOpenClawDir() + "/workspace/skills/clawdash";

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

    public Result<Void> syncClawdashSkill() {
        try {
            String skillDir = getOpenClawDir() + "/workspace/skills/clawdash";
            String skillFile = skillDir + "/SKILL.md";

            File dir = new File(skillDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    return Result.error("无法创建 Skill 目录: " + skillDir);
                }
            }

            String clawdashUrl = "http://localhost:5178";

            List<com.clawdash.entity.TaskType> taskTypes = taskTypeService.getAll();

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
            sb.append("    \"type\": \"agent-execute\",\n");
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

            sb.append("## TaskGroup 操作\n\n");
            sb.append("TaskGroup 用于管理复杂任务的分解和执行追踪。将大任务分解为多个子任务，分配给不同 Agent 并行执行。\n\n");
            
            sb.append("### 创建 TaskGroup\n\n");
            sb.append("```bash\n");
            sb.append("curl -X POST ").append(clawdashUrl).append("/api/task-groups \\\n");
            sb.append("  -H \"Content-Type: application/json\" \\\n");
            sb.append("  -d '{\n");
            sb.append("    \"name\": \"任务组名称\",\n");
            sb.append("    \"description\": \"任务组描述\",\n");
            sb.append("    \"totalGoal\": \"总体目标\",\n");
            sb.append("    \"overallDesign\": \"整体设计方案\"\n");
            sb.append("  }'\n");
            sb.append("```\n\n");
            
            sb.append("### 查询 TaskGroup 列表\n\n");
            sb.append("```bash\n");
            sb.append("# 查询所有状态\n");
            sb.append("curl ").append(clawdashUrl).append("/api/task-groups\n");
            sb.append("# 查询待处理\n");
            sb.append("curl ").append(clawdashUrl).append("/api/task-groups?status=PENDING\n");
            sb.append("# 查询执行中\n");
            sb.append("curl ").append(clawdashUrl).append("/api/task-groups?status=IN_PROGRESS\n");
            sb.append("```\n\n");
            
            sb.append("### 获取 TaskGroup 详情\n\n");
            sb.append("```bash\n");
            sb.append("curl ").append(clawdashUrl).append("/api/task-groups/{groupId}/detail\n");
            sb.append("```\n\n");
            
            sb.append("### 获取 TaskGroup 进度\n\n");
            sb.append("```bash\n");
            sb.append("curl ").append(clawdashUrl).append("/api/task-groups/{groupId}/progress\n");
            sb.append("```\n\n");
            
            sb.append("### 更新 TaskGroup 状态\n\n");
            sb.append("```bash\n");
            sb.append("curl -X PATCH ").append(clawdashUrl).append("/api/task-groups/{groupId}/status \\\n");
            sb.append("  -H \"Content-Type: application/json\" \\\n");
            sb.append("  -d '{\"status\": \"IN_PROGRESS\"}'\n");
            sb.append("```\n\n");
            
            sb.append("### 放弃 TaskGroup\n\n");
            sb.append("```bash\n");
            sb.append("curl -X POST ").append(clawdashUrl).append("/api/task-groups/{groupId}/abandon \\\n");
            sb.append("  -H \"Content-Type: application/json\" \\\n");
            sb.append("  -d '{\"reason\": \"放弃原因\"}'\n");
            sb.append("```\n\n");
            
            sb.append("### TaskGroup 内创建子任务\n\n");
            sb.append("```bash\n");
            sb.append("curl -X POST ").append(clawdashUrl).append("/api/tasks \\\n");
            sb.append("  -H \"Content-Type: application/json\" \\\n");
            sb.append("  -d '{\n");
            sb.append("    \"type\": \"agent-execute\",\n");
            sb.append("    \"payload\": {\"task\": \"子任务描述\"},\n");
            sb.append("    \"priority\": 5,\n");
            sb.append("    \"taskGroupId\": {groupId}\n");
            sb.append("  }'\n");
            sb.append("```\n\n");

            if (taskTypes != null && !taskTypes.isEmpty()) {
                sb.append("## 任务类型\n\n");
                sb.append("| 类型 | 说明 |\n");
                sb.append("|------|------|\n");
                for (com.clawdash.entity.TaskType tt : taskTypes) {
                    sb.append("| `").append(tt.getName()).append("` | ").append(tt.getDescription() != null ? tt.getDescription() : "").append(" |\n");
                }
                sb.append("\n");
            }

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

            return Result.success(null);

        } catch (Exception e) {
            return Result.error("同步 Skill 失败: " + e.getMessage());
        }
    }

    public Result<Map<String, Object>> getSkillContent() {
        try {
            String skillFile = System.getProperty("user.home") + "/.openclaw/workspace/skills/clawdash/SKILL.md";
            File file = new File(skillFile);
            
            Map<String, Object> result = new HashMap<>();
            
            if (!file.exists()) {
                result.put("description", "Skill 文件不存在，请先同步");
                result.put("apiUrl", "http://localhost:5178");
                result.put("endpoints", java.util.Collections.emptyList());
                result.put("taskTypes", java.util.Collections.emptyList());
                return Result.success(result);
            }
            
            String content = java.nio.file.Files.readString(file.toPath());
            
            // 提取 description
            String description = "";
            if (content.contains("description:")) {
                int start = content.indexOf("description:") + 13;
                int end = content.indexOf("\n", start);
                if (end > start) {
                    description = content.substring(start, end).trim();
                }
            }
            
            // 提取 API 地址
            String apiUrl = "http://localhost:5178";
            if (content.contains("**API 地址**:")) {
                int start = content.indexOf("**API 地址**:") + 12;
                int end = content.indexOf("\n", start);
                if (end > start) {
                    apiUrl = content.substring(start, end).trim();
                }
            }
            
            // 解析 curl 命令提取 API 端点
            List<Map<String, String>> endpoints = new ArrayList<>();
            java.util.regex.Pattern curlPattern = java.util.regex.Pattern.compile(
                "curl(?:\\s+-X\\s+(\\w+))?\\s+(?:-\\w+\\s+)*'?([^\"']+?)(?:'|\")?\\s*-d\\s*'(.+?)'|^curl\\s+(?:-\\w+\\s+)*'?([^\"']+?)(?:'|\")?$",
                java.util.regex.Pattern.MULTILINE | java.util.regex.Pattern.CASE_INSENSITIVE
            );
            
            // 简单解析：提取所有 curl 命令行
            java.util.regex.Pattern simplePattern = java.util.regex.Pattern.compile(
                "^curl(?:\\s+-X\\s+(\\w+))?\\s+'?([^\\s']+?)'?(?:\\s+-d\\s*'(.+?)')?$",
                java.util.regex.Pattern.MULTILINE
            );
            
            String[] lines = content.split("\n");
            String currentMethod = "GET";
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("curl")) {
                    String method = "GET";
                    String path = "";
                    String desc = "";
                    
                    if (line.contains("-X POST") || line.contains("-X PUT")) {
                        java.util.regex.Matcher m = java.util.regex.Pattern.compile("-X\\s+(POST|PUT|DELETE)").matcher(line);
                        if (m.find()) {
                            method = m.group(1);
                        }
                    }
                    
                    // 提取路径
                    if (line.contains("http://") || line.contains("https://")) {
                        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?:http[sv]?://[^/]+)?(/[^\\s'\"]+)").matcher(line);
                        if (m.find()) {
                            path = m.group(1);
                        }
                    } else {
                        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(/api[^\\s'\"]*)").matcher(line);
                        if (m.find()) {
                            path = m.group(1);
                        }
                    }
                    
                    // 提取描述（上一行或下一行的中文）
                    if (line.contains("创建任务")) desc = "创建任务";
                    else if (line.contains("查询任务列表")) desc = "查询任务列表";
                    else if (line.contains("查询单个任务")) desc = "查询单个任务";
                    else if (line.contains("标记完成")) desc = "标记完成";
                    else if (line.contains("标记失败")) desc = "标记失败";
                    else if (line.contains("取消任务")) desc = "取消任务";
                    else if (line.contains("获取统计")) desc = "获取统计";
                    else if (line.contains("创建 TaskGroup")) desc = "创建 TaskGroup";
                    else if (line.contains("查询 TaskGroup 列表")) desc = "查询 TaskGroup 列表";
                    else if (line.contains("获取 TaskGroup 详情")) desc = "获取 TaskGroup 详情";
                    else if (line.contains("获取 TaskGroup 进度")) desc = "获取 TaskGroup 进度";
                    else if (line.contains("更新 TaskGroup 状态")) desc = "更新 TaskGroup 状态";
                    else if (line.contains("放弃 TaskGroup")) desc = "放弃 TaskGroup";
                    else if (line.contains("TaskGroup 内创建子任务")) desc = "创建子任务";
                    
                    if (!path.isEmpty()) {
                        Map<String, String> endpoint = new HashMap<>();
                        endpoint.put("method", method);
                        endpoint.put("path", path);
                        endpoint.put("description", desc);
                        endpoints.add(endpoint);
                    }
                }
            }
            
            // 解析任务类型表格
            List<Map<String, String>> taskTypes = new ArrayList<>();
            if (content.contains("## 任务类型")) {
                int tableStart = content.indexOf("## 任务类型");
                int tableEnd = content.indexOf("\n##", tableStart + 1);
                if (tableEnd == -1) tableEnd = content.length();
                
                String tableSection = content.substring(tableStart, tableEnd);
                String[] tableLines = tableSection.split("\n");
                for (String tableLine : tableLines) {
                    if (tableLine.trim().startsWith("|") && !tableLine.contains("类型") && tableLine.contains("`")) {
                        String[] parts = tableLine.split("\\|");
                        if (parts.length >= 3) {
                            String type = parts[1].trim().replace("`", "");
                            String desc = parts[2].trim();
                            if (!type.isEmpty()) {
                                Map<String, String> tt = new HashMap<>();
                                tt.put("name", type);
                                tt.put("description", desc);
                                taskTypes.add(tt);
                            }
                        }
                    }
                }
            }
            
            result.put("description", description);
            result.put("apiUrl", apiUrl);
            result.put("endpoints", endpoints);
            result.put("taskTypes", taskTypes);
            
            return Result.success(result);
            
        } catch (Exception e) {
            return Result.error("读取 Skill 内容失败: " + e.getMessage());
        }
    }

    public Result<Map<String, Object>> getInstalledSkillContent() {
        Map<String, Object> result = new HashMap<>();
        try {
            String skillFile = System.getProperty("user.home") + "/.openclaw/workspace/skills/clawdash/SKILL.md";
            File file = new File(skillFile);
            
            if (!file.exists()) {
                result.put("installed", false);
                result.put("content", "");
                result.put("path", skillFile);
                return Result.success(result);
            }
            
            String content = java.nio.file.Files.readString(file.toPath());
            result.put("installed", true);
            result.put("content", content);
            result.put("path", skillFile);
            return Result.success(result);
            
        } catch (Exception e) {
            return Result.error("读取已安装 Skill 失败: " + e.getMessage());
        }
    }

    public Result<Map<String, Object>> installTaskDistributionSkill(String clawdashUrl) {
        Map<String, Object> result = new HashMap<>();

        try {
            String skillDir = System.getProperty("user.home") + "/.openclaw/workspace/skills/task-distribution";
            String skillFile = skillDir + "/SKILL.md";

            File dir = new File(skillDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    return Result.error(1, "无法创建 Skill 目录: " + skillDir);
                }
            }

            // Read SKILL.md from resources
            ClassLoader classLoader = getClass().getClassLoader();
            java.io.InputStream skillStream = classLoader.getResourceAsStream("skills/task-distribution/SKILL.md");
            
            if (skillStream == null) {
                return Result.error(2, "无法找到 task-distribution skill 资源文件");
            }
            
            String skillContent = new String(skillStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            skillStream.close();
            
            // Replace placeholder URL with actual ClawDash URL
            skillContent = skillContent.replace("http://localhost:5178", clawdashUrl);
            
            java.nio.file.Files.writeString(java.nio.file.Paths.get(skillFile), skillContent);

            result.put("success", true);
            result.put("skillPath", skillFile);
            result.put("message", "Task Distribution Skill 安装成功");

            return Result.success(result);

        } catch (Exception e) {
            return Result.error(2, "安装 Task Distribution Skill 失败: " + e.getMessage());
        }
    }

    /**
     * 卸载 Task Distribution Skill
     */
    public Result<Map<String, Object>> uninstallTaskDistributionSkill() {
        Map<String, Object> result = new HashMap<>();

        try {
            String skillDir = System.getProperty("user.home") + "/.openclaw/workspace/skills/task-distribution";

            File dir = new File(skillDir);
            if (dir.exists()) {
                deleteDirectory(dir);
            }

            result.put("success", true);
            result.put("message", "Task Distribution Skill 卸载成功");

            return Result.success(result);

        } catch (Exception e) {
            return Result.error(2, "卸载 Task Distribution Skill 失败: " + e.getMessage());
        }
    }

    public Map<String, Object> getA2AConfig() {
        Map<String, Object> result = new HashMap<>();
        try {
            File configFile = new File(OPENCLAW_DIR, "openclaw.json");
            JsonNode root = objectMapper.readTree(configFile);
            JsonNode tools = root.get("tools");
            if (tools != null && tools.has("agentToAgent")) {
                JsonNode a2a = tools.get("agentToAgent");
                result.put("enabled", a2a.has("enabled") ? a2a.get("enabled").asBoolean() : false);
                List<String> allow = new ArrayList<>();
                if (a2a.has("allow")) {
                    a2a.get("allow").forEach(item -> allow.add(item.asText()));
                }
                result.put("allow", allow);
            } else {
                result.put("enabled", false);
                result.put("allow", new ArrayList<String>());
            }
            result.put("availableAgents", listAgents());
        } catch (Exception e) {
            result.put("enabled", false);
            result.put("allow", new ArrayList<String>());
            result.put("availableAgents", listAgents());
        }
        return result;
    }

    public boolean syncA2AConfigFromGraph(Long graphId) {
        Set<String> agents = new HashSet<>();
        List<com.clawdash.entity.ConfigGraphEdge> edges = configGraphEdgeMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.clawdash.entity.ConfigGraphEdge>()
                        .eq(com.clawdash.entity.ConfigGraphEdge::getGraphId, graphId)
                        .eq(com.clawdash.entity.ConfigGraphEdge::getDeleted, 0)
        );
        for (com.clawdash.entity.ConfigGraphEdge edge : edges) {
            if (edge.getSourceId() != null) agents.add(edge.getSourceId());
            if (edge.getTargetId() != null) agents.add(edge.getTargetId());
        }
        return updateA2AConfig(true, new ArrayList<>(agents));
    }

    public boolean updateA2AConfig(boolean enabled, List<String> allow) {
        try {
            File configFile = new File(OPENCLAW_DIR, "openclaw.json");
            JsonNode root = objectMapper.readTree(configFile);
            
            List<String> sortedAllow = allow.stream().sorted().toList();
            boolean currentEnabled = false;
            List<String> currentAllow = new ArrayList<>();
            
            JsonNode tools = root.get("tools");
            if (tools != null && tools.has("agentToAgent")) {
                JsonNode a2a = tools.get("agentToAgent");
                currentEnabled = a2a.has("enabled") && a2a.get("enabled").asBoolean();
                if (a2a.has("allow")) {
                    a2a.get("allow").forEach(item -> currentAllow.add(item.asText()));
                }
            }
            List<String> sortedCurrentAllow = currentAllow.stream().sorted().toList();
            
            if (currentEnabled == enabled && sortedCurrentAllow.equals(sortedAllow)) {
                return true;
            }
            
            ObjectNode toolsNode = (ObjectNode) (tools != null ? tools : objectMapper.createObjectNode());
            if (tools == null) {
                ((ObjectNode) root).set("tools", toolsNode);
            }
            ObjectNode a2aNode = objectMapper.createObjectNode();
            a2aNode.put("enabled", enabled);
            ArrayNode allowArray = objectMapper.createArrayNode();
            sortedAllow.forEach(allowArray::add);
            a2aNode.set("allow", allowArray);
            toolsNode.set("agentToAgent", a2aNode);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(configFile, root);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Result<List<Map<String, Object>>> getTaskBindings() {
        try {
            List<AgentTaskBinding> bindings = agentTaskBindingMapper.selectList(null);
            List<Map<String, Object>> result = new ArrayList<>();
            for (AgentTaskBinding binding : bindings) {
                Map<String, Object> map = new HashMap<>();
                map.put("agentName", binding.getAgentName());
                map.put("taskType", binding.getTaskType());
                map.put("createdAt", binding.getCreatedAt());
                result.add(map);
            }
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("Failed to get task bindings: " + e.getMessage());
        }
    }

    public Result<Void> addTaskBinding(String agentName, String taskType) {
        try {
            LambdaQueryWrapper<AgentTaskBinding> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AgentTaskBinding::getAgentName, agentName)
                   .eq(AgentTaskBinding::getTaskType, taskType);
            Long count = agentTaskBindingMapper.selectCount(wrapper);
            if (count > 0) {
                return Result.error("Binding already exists");
            }
            AgentTaskBinding binding = new AgentTaskBinding();
            binding.setAgentName(agentName);
            binding.setTaskType(taskType);
            binding.setCreatedAt(LocalDateTime.now());
            binding.setUpdatedAt(LocalDateTime.now());
            agentTaskBindingMapper.insert(binding);
            return Result.success();
        } catch (Exception e) {
            return Result.error("Failed to add task binding: " + e.getMessage());
        }
    }

    public Result<Void> removeTaskBinding(String agentName, String taskType) {
        try {
            LambdaQueryWrapper<AgentTaskBinding> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AgentTaskBinding::getAgentName, agentName)
                   .eq(AgentTaskBinding::getTaskType, taskType);
            agentTaskBindingMapper.delete(wrapper);
            return Result.success();
        } catch (Exception e) {
            return Result.error("Failed to remove task binding: " + e.getMessage());
        }
    }

    public Result<Map<String, String>> getDistributor() {
        try {
            String distributor = "";
            File configFile = new File(OPENCLAW_DIR, "clawdash.json");
            if (configFile.exists()) {
                JsonNode root = objectMapper.readTree(configFile);
                JsonNode distNode = root.get("taskDistributor");
                if (distNode != null) {
                    distributor = distNode.asText();
                }
            }
            return Result.success(Map.of("agentName", distributor));
        } catch (Exception e) {
            return Result.success(Map.of("agentName", ""));
        }
    }

    public Result<Void> setDistributor(String agentName) {
        try {
            File configFile = new File(OPENCLAW_DIR, "clawdash.json");
            JsonNode root;
            if (configFile.exists()) {
                root = objectMapper.readTree(configFile);
            } else {
                root = objectMapper.createObjectNode();
            }
            ((ObjectNode) root).put("taskDistributor", agentName);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(configFile, root);
            return Result.success();
        } catch (Exception e) {
            return Result.error("Failed to set distributor: " + e.getMessage());
        }
    }

}
