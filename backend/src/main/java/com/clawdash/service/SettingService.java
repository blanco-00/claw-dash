package com.clawdash.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clawdash.entity.Setting;
import com.clawdash.mapper.SettingMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SettingService extends ServiceImpl<SettingMapper, Setting> {

    public static final String SCOPE_GLOBAL = "global";
    public static final String SCOPE_GRAPH = "graph";

    public String getSetting(String scope, String scopeId, String key) {
        Setting setting = selectSetting(scope, scopeId, key);
        return setting != null ? setting.getSettingValue() : null;
    }

    public String getSetting(String scope, String key) {
        return getSetting(scope, null, key);
    }

    public boolean getBooleanSetting(String scope, String scopeId, String key, boolean defaultValue) {
        String value = getSetting(scope, scopeId, key);
        if (value == null) return defaultValue;
        return Boolean.parseBoolean(value);
    }

    public boolean getBooleanSetting(String scope, String key, boolean defaultValue) {
        return getBooleanSetting(scope, null, key, defaultValue);
    }

    public void setSetting(String scope, String scopeId, String key, String value) {
        Setting existing = selectSetting(scope, scopeId, key);
        if (existing != null) {
            existing.setSettingValue(value);
            baseMapper.updateById(existing);
        } else {
            Setting setting = new Setting();
            setting.setScope(scope);
            setting.setScopeId(scopeId);
            setting.setSettingKey(key);
            setting.setSettingValue(value);
            baseMapper.insert(setting);
        }
    }

    public void setSetting(String scope, String key, String value) {
        setSetting(scope, null, key, value);
    }

    public void setBooleanSetting(String scope, String scopeId, String key, boolean value) {
        setSetting(scope, scopeId, key, String.valueOf(value));
    }

    public void setBooleanSetting(String scope, String key, boolean value) {
        setSetting(scope, key, String.valueOf(value));
    }

    public List<Setting> getSettingsByScope(String scope, String scopeId) {
        LambdaQueryWrapper<Setting> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Setting::getScope, scope);
        if (scopeId != null) {
            wrapper.eq(Setting::getScopeId, scopeId);
        } else {
            wrapper.isNull(Setting::getScopeId);
        }
        return baseMapper.selectList(wrapper);
    }

    public List<Setting> getSettingsByScope(String scope) {
        return getSettingsByScope(scope, null);
    }

    private Setting selectSetting(String scope, String scopeId, String key) {
        LambdaQueryWrapper<Setting> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Setting::getScope, scope);
        if (scopeId != null) {
            wrapper.eq(Setting::getScopeId, scopeId);
        } else {
            wrapper.isNull(Setting::getScopeId);
        }
        wrapper.eq(Setting::getSettingKey, key);
        return baseMapper.selectOne(wrapper);
    }
}
