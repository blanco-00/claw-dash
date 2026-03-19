package com.clawdash.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageRequest implements Serializable {

    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String sortField;
    private String sortOrder;
    private String keyword;

    // Alias methods for compatibility
    public Integer getPage() { return pageNum; }
    public void setPage(Integer page) { this.pageNum = page; }
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
}
