package com.clawdash.controller;

import com.clawdash.common.Result;
import com.clawdash.entity.EdgeType;
import com.clawdash.service.EdgeTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/edge-types")
public class EdgeTypeController {

    @Autowired
    private EdgeTypeService edgeTypeService;

    @GetMapping
    public Result<List<EdgeType>> getAll() {
        return Result.success(edgeTypeService.findAll());
    }

    @GetMapping("/by-value/{value}")
    public Result<EdgeType> getByValue(@PathVariable String value) {
        Optional<EdgeType> edgeType = edgeTypeService.findByValue(value);
        if (edgeType.isEmpty()) {
            return Result.error("Edge type not found");
        }
        return Result.success(edgeType.get());
    }

    @PostMapping
    public Result<EdgeType> create(@RequestBody EdgeType edgeType) {
        EdgeType created = edgeTypeService.create(edgeType);
        return Result.success(created);
    }

    @PatchMapping("/{id}")
    public Result<EdgeType> update(@PathVariable Long id, @RequestBody EdgeType edgeType) {
        EdgeType updated = edgeTypeService.update(id, edgeType);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean removed = edgeTypeService.delete(id);
        if (removed) {
            return Result.success(null);
        }
        return Result.error("Failed to delete edge type");
    }
}
