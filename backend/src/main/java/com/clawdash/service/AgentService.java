package com.clawdash.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clawdash.entity.Agent;
import com.clawdash.mapper.AgentMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AgentService extends ServiceImpl<AgentMapper, Agent> {

    public List<Agent> listActive() {
        return lambdaQuery()
                .eq(Agent::getStatus, "ACTIVE")
                .list();
    }

    public Agent createAgent(String name, String role, String description, String config) {
        Agent agent = new Agent();
        agent.setAgentId("agent-" + System.currentTimeMillis());
        agent.setName(name);
        agent.setRole(role);
        agent.setDescription(description);
        agent.setConfig(config);
        agent.setStatus("ACTIVE");
        agent.setLastActiveAt(LocalDateTime.now());
        save(agent);
        return agent;
    }

    public void updateStatus(String agentId, String status) {
        Agent agent = lambdaQuery().eq(Agent::getAgentId, agentId).one();
        if (agent != null) {
            agent.setStatus(status);
            if ("ACTIVE".equals(status)) {
                agent.setLastActiveAt(LocalDateTime.now());
            }
            updateById(agent);
        }
    }

    public long count() {
        return baseMapper.selectCount(null);
    }

    public long countByStatus(String status) {
        return lambdaQuery().eq(Agent::getStatus, status).count();
    }
}
