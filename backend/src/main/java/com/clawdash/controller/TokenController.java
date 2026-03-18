package com.clawdash.controller;

import com.clawdash.common.PageRequest;
import com.clawdash.common.PageResponse;
import com.clawdash.common.Result;
import com.clawdash.entity.ApiToken;
import com.clawdash.service.ApiTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tokens")
public class TokenController {

    @Autowired
    private ApiTokenService apiTokenService;

    @GetMapping
    public Result<PageResponse<ApiToken>> list(PageRequest request) {
        return Result.success(apiTokenService.listPage(request));
    }

    @GetMapping("/{id}")
    public Result<ApiToken> getById(@PathVariable Long id) {
        return apiTokenService.getById(id);
    }

    @PostMapping
    public Result<ApiToken> create(@RequestBody ApiToken apiToken) {
        return apiTokenService.create(apiToken);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return apiTokenService.delete(id);
    }

    @PatchMapping("/{id}/toggle")
    public Result<Void> toggleEnabled(@PathVariable Long id) {
        return apiTokenService.toggleEnabled(id);
    }
}
