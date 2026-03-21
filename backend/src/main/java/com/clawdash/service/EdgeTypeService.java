package com.clawdash.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clawdash.entity.EdgeType;
import com.clawdash.mapper.EdgeTypeMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EdgeTypeService extends ServiceImpl<EdgeTypeMapper, EdgeType> {

    public List<EdgeType> findAll() {
        return list(new LambdaQueryWrapper<EdgeType>()
                .orderByAsc(EdgeType::getId));
    }

    public Optional<EdgeType> findByValue(String value) {
        EdgeType edgeType = getOne(new LambdaQueryWrapper<EdgeType>()
                .eq(EdgeType::getValue, value));
        return Optional.ofNullable(edgeType);
    }

    public EdgeType create(EdgeType edgeType) {
        edgeType.setCreatedAt(LocalDateTime.now());
        save(edgeType);
        return edgeType;
    }

    public EdgeType update(Long id, EdgeType edgeType) {
        edgeType.setId(id);
        updateById(edgeType);
        return getById(id);
    }

    public boolean delete(Long id) {
        return removeById(id);
    }
}
