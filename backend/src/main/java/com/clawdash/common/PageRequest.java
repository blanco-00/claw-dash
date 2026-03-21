package com.clawdash.common;

import java.io.Serializable;

public class PageRequest implements Serializable {

    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String sortField;
    private String sortOrder;
    private String keyword;

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    // Alias methods for compatibility
    public Integer getPage() { return pageNum; }
    public void setPage(Integer page) { this.pageNum = page; }
}
