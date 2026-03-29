package com.clawdash.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clawdash.entity.Agent;
import com.clawdash.entity.ConfigGraphNode;
import com.clawdash.mapper.AgentMapper;
import com.clawdash.mapper.ConfigGraphNodeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AgentSelector extends ServiceImpl<AgentMapper, Agent> {

    @Autowired
    private ConfigGraphNodeMapper configGraphNodeMapper;

    private static final Map<String, List<String>> DOMAIN_KEYWORDS = new HashMap<>();
    
    static {
        DOMAIN_KEYWORDS.put("gongbu", Arrays.asList(
            "代码", "bug", "修复", "开发", "编程", "实现", "重构", "优化", "性能",
            "接口", "api", "功能", "模块", "组件", "部署", "测试", "debug", "fix",
            "code", "implement", "refactor", "optimize", "performance"
        ));
        
        DOMAIN_KEYWORDS.put("hubu", Arrays.asList(
            "财务", "账单", "预算", "费用", "报表", "统计", "数据", "日志", "记录",
            "分析", "汇总", "finance", "budget", "report", "statistics", "data", "log"
        ));
        
        DOMAIN_KEYWORDS.put("bingbu", Arrays.asList(
            "安全", "权限", "认证", "加密", "防护", "漏洞", "攻击", "风险",
            "security", "auth", "permission", "encrypt", "vulnerability"
        ));
        
        DOMAIN_KEYWORDS.put("xingbu", Arrays.asList(
            "合规", "审核", "政策", "规则", "检查", "验证", "分析", "原因",
            "legal", "compliance", "audit", "policy", "rule", "check", "analyze"
        ));
        
        DOMAIN_KEYWORDS.put("libu3", Arrays.asList(
            "协议", "标准", "接口规范", "文档", "流程", "规范", "protocol", "standard", 
            "specification", "document", "workflow"
        ));
        
        DOMAIN_KEYWORDS.put("libu4", Arrays.asList(
            "人员", "用户", "权限", "角色", "团队", "组织", "personnel", "user", 
            "permission", "role", "team", "organization"
        ));
    }

    private static final String DEFAULT_AGENT = "gongbu";

    public List<Agent> getAvailableAgents() {
        List<ConfigGraphNode> nodes = configGraphNodeMapper.selectList(null);
        Set<String> agentIds = nodes.stream()
                .map(ConfigGraphNode::getAgentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (agentIds.isEmpty()) {
            return lambdaQuery()
                    .eq(Agent::getStatus, "ACTIVE")
                    .list();
        }

        return lambdaQuery()
                .in(Agent::getAgentId, agentIds)
                .eq(Agent::getStatus, "ACTIVE")
                .list();
    }

    public String selectAgent(String taskDescription) {
        if (taskDescription == null || taskDescription.trim().isEmpty()) {
            return DEFAULT_AGENT;
        }

        String lowerDesc = taskDescription.toLowerCase();
        
        Map<String, Integer> scores = new HashMap<>();
        
        for (Map.Entry<String, List<String>> entry : DOMAIN_KEYWORDS.entrySet()) {
            String agentId = entry.getKey();
            List<String> keywords = entry.getValue();
            
            int matchCount = 0;
            for (String keyword : keywords) {
                if (lowerDesc.contains(keyword.toLowerCase())) {
                    matchCount++;
                }
            }
            
            if (matchCount > 0) {
                scores.put(agentId, matchCount);
            }
        }

        if (scores.isEmpty()) {
            return DEFAULT_AGENT;
        }

        return scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(DEFAULT_AGENT);
    }

    public String selectAgentWithFallback(String taskDescription, String preferredAgent) {
        if (preferredAgent != null && !preferredAgent.isEmpty()) {
            Agent agent = lambdaQuery()
                    .eq(Agent::getAgentId, preferredAgent)
                    .eq(Agent::getStatus, "ACTIVE")
                    .one();
            
            if (agent != null) {
                return preferredAgent;
            }
        }
        
        return selectAgent(taskDescription);
    }

    public List<String> getAgentCapabilities(String agentId) {
        return DOMAIN_KEYWORDS.getOrDefault(agentId, Collections.emptyList());
    }

    public boolean hasCapability(String agentId, String capability) {
        List<String> capabilities = getAgentCapabilities(agentId);
        return capabilities.stream()
                .anyMatch(cap -> cap.equalsIgnoreCase(capability));
    }
}
