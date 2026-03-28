package com.clawdash.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clawdash.common.PageRequest;
import com.clawdash.common.PageResponse;
import com.clawdash.common.Result;
import com.clawdash.entity.ApiToken;
import com.clawdash.mapper.ApiTokenMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
public class ApiTokenService extends ServiceImpl<ApiTokenMapper, ApiToken> {

    private final SecureRandom secureRandom = new SecureRandom();

    public PageResponse<ApiToken> listPage(PageRequest request) {
        Page<ApiToken> page = new Page<>(request.getPage(), request.getPageSize());
        LambdaQueryWrapper<ApiToken> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.like(ApiToken::getName, request.getKeyword());
        }
        
        wrapper.orderByDesc(ApiToken::getCreatedAt);
        IPage<ApiToken> result = page(page, wrapper);
        
        return PageResponse.fromIPage(result, result.getRecords());
    }

    public Result<ApiToken> create(ApiToken apiToken) {
        String token = generateToken();
        apiToken.setToken(token);
        apiToken.setEnabled(true);
        apiToken.setCreatedAt(LocalDateTime.now());
        apiToken.setUpdatedAt(LocalDateTime.now());
        
        if (apiToken.getExpiresAt() == null) {
            apiToken.setExpiresAt(LocalDateTime.now().plusYears(1));
        }
        
        save(apiToken);
        return Result.success(apiToken);
    }

    public Result<Void> delete(Long id) {
        removeById(id);
        return Result.success(null);
    }

    public Result<ApiToken> getById(Long id) {
        ApiToken token = super.getById(id);
        if (token == null) {
            return Result.error("Token not found");
        }
        return Result.success(token);
    }

    public Result<Void> toggleEnabled(Long id) {
        ApiToken token = super.getById(id);
        if (token == null) {
            return Result.error("Token not found");
        }
        token.setEnabled(!token.getEnabled());
        token.setUpdatedAt(LocalDateTime.now());
        updateById(token);
        return Result.success(null);
    }

    public boolean validateToken(String token) {
        ApiToken apiToken = lambdaQuery()
                .eq(ApiToken::getToken, token)
                .eq(ApiToken::getEnabled, true)
                .one();
        
        if (apiToken == null) {
            return false;
        }
        
        if (apiToken.getExpiresAt() != null && apiToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }
        
        apiToken.setLastUsedAt(LocalDateTime.now());
        updateById(apiToken);
        
        return true;
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return "cd_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
