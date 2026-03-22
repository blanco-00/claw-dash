package com.clawdash.controller;

import com.clawdash.common.Result;
import com.clawdash.entity.OpenClawConfig;
import com.clawdash.service.OpenClawService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/openclaw")
public class OpenClawController {

    @Autowired
    private OpenClawService openClawService;

    @GetMapping("/status")
    public Result<Map<String, Object>> getStatus() {
        return openClawService.getStatus();
    }

    @PostMapping("/install")
    public Result<Map<String, Object>> install() {
        return openClawService.install();
    }

    @PostMapping("/uninstall")
    public Result<Map<String, Object>> uninstall() {
        return openClawService.uninstall();
    }

    @GetMapping("/config")
    public Result<Map<String, String>> getConfig() {
        return openClawService.getConfig();
    }

    @PutMapping("/config")
    public Result<Void> updateConfig(@RequestBody Map<String, String> config) {
        return openClawService.updateConfig(config);
    }

    @GetMapping("/plugins")
    public Result<Map<String, Object>> getPlugins() {
        return openClawService.getPlugins();
    }

    @PostMapping("/plugins/{pluginId}/toggle")
    public Result<Void> togglePlugin(@PathVariable String pluginId) {
        return openClawService.togglePlugin(pluginId);
    }

    @PostMapping("/auto-detect")
    public Result<Map<String, Object>> autoDetect(@RequestBody(required = false) Map<String, String> body) {
        String configPath = body != null ? body.get("configPath") : null;
        return openClawService.autoDetect(configPath);
    }

    @PostMapping("/confirm-connect")
    public Result<Map<String, Object>> confirmConnect(@RequestBody Map<String, String> body) {
        String apiUrl = body.get("apiUrl");
        String token = body.get("token");
        String configPath = body.get("configPath");
        return openClawService.confirmConnect(apiUrl, token, configPath);
    }

    @PostMapping("/skill/install")
    public Result<Map<String, Object>> installSkill(@RequestBody Map<String, String> body) {
        String clawdashUrl = body.get("clawdashUrl");
        return openClawService.installClawdashSkill(clawdashUrl);
    }

    @PostMapping("/skill/uninstall")
    public Result<Map<String, Object>> uninstallSkill() {
        return openClawService.uninstallClawdashSkill();
    }

    @GetMapping("/a2a-config")
    public Result<Map<String, Object>> getA2AConfig() {
        return Result.success(openClawService.getA2AConfig());
    }

    @PostMapping("/a2a-config/sync")
    public Result<Void> syncA2AConfig(@RequestParam Long graphId) {
        boolean success = openClawService.syncA2AConfigFromGraph(graphId);
        if (success) {
            return Result.success(null);
        }
        return Result.error("A2A config sync failed");
    }

}
