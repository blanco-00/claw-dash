package com.clawdash.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clawdash.common.Result;
import com.clawdash.entity.OpenClawConfig;
import com.clawdash.mapper.OpenClawConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
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
        
        try {
            String healthUrl = openClawApiUrl + "/health";
            restTemplate.getForObject(healthUrl, String.class);
            status.put("running", true);
            status.put("apiUrl", openClawApiUrl);
        } catch (Exception e) {
            status.put("running", false);
            status.put("apiUrl", openClawApiUrl);
            status.put("error", e.getMessage());
        }
        
        status.put("timestamp", LocalDateTime.now().toString());
        
        return Result.success(status);
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
}
