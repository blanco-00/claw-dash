package com.clawdash.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clawdash.common.BaseEntity;

import java.util.Objects;

@TableName("settings")
public class Setting extends BaseEntity {

    private String scope;
    private String scopeId;
    private String settingKey;
    private String settingValue;

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getScopeId() {
        return scopeId;
    }

    public void setScopeId(String scopeId) {
        this.scopeId = scopeId;
    }

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Setting setting = (Setting) o;
        return Objects.equals(scope, setting.scope) &&
                Objects.equals(scopeId, setting.scopeId) &&
                Objects.equals(settingKey, setting.settingKey) &&
                Objects.equals(settingValue, setting.settingValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), scope, scopeId, settingKey, settingValue);
    }
}
