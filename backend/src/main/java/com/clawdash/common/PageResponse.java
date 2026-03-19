package com.clawdash.common;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class PageResponse<T> implements Serializable {

    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private Integer pages;
    private List<T> records;

    public PageResponse(List<T> records, Long total, Integer pageNum, Integer pageSize) {
        this.records = records;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = (int) Math.ceil((double) total / pageSize);
    }

    public PageResponse(Long total, Integer pageNum, Integer pageSize, List<T> records) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.records = records;
        this.pages = (int) Math.ceil((double) total / pageSize);
    }

    public static <T> PageResponse<T> of(Long total, Integer pageNum, Integer pageSize, List<T> records) {
        return new PageResponse<>(total, pageNum, pageSize, records);
    }
}
