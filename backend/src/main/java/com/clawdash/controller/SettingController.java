package com.clawdash.controller;

import com.clawdash.common.Result;
import com.clawdash.service.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingController {

    @Autowired
    private SettingService settingService;

    @GetMapping
    public Result<List<?>> list(@RequestParam String scope, @RequestParam(required = false) String scopeId) {
        if (scopeId != null) {
            return Result.success(settingService.getSettingsByScope(scope, scopeId));
        } else {
            return Result.success(settingService.getSettingsByScope(scope));
        }
    }

    @GetMapping("/global/{key}")
    public Result<String> getGlobalSetting(@PathVariable String key) {
        String value = settingService.getSetting(SettingService.SCOPE_GLOBAL, key);
        return Result.success(value);
    }

    @PutMapping("/global/{key}")
    public Result<Void> setGlobalSetting(@PathVariable String key, @RequestBody Map<String, String> body) {
        String value = body.get("value");
        settingService.setSetting(SettingService.SCOPE_GLOBAL, key, value);
        return Result.success(null);
    }

    @GetMapping("/graph/{graphId}/{key}")
    public Result<String> getGraphSetting(@PathVariable Long graphId, @PathVariable String key) {
        String value = settingService.getSetting(SettingService.SCOPE_GRAPH, String.valueOf(graphId), key);
        return Result.success(value);
    }

    @PutMapping("/graph/{graphId}/{key}")
    public Result<Void> setGraphSetting(@PathVariable Long graphId, @PathVariable String key, @RequestBody Map<String, String> body) {
        String value = body.get("value");
        settingService.setSetting(SettingService.SCOPE_GRAPH, String.valueOf(graphId), key, value);
        return Result.success(null);
    }
}
