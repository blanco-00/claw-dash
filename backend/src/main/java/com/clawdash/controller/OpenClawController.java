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
    public Result<Map<String, Object>> autoDetect() {
        return openClawService.autoDetect();
    }

    @PostMapping("/confirm-connect")
    public Result<Map<String, Object>> confirmConnect(@RequestBody Map<String, String> body) {
        String apiUrl = body.get("apiUrl");
        String token = body.get("token");
        return openClawService.confirmConnect(apiUrl, token);
    }
}
