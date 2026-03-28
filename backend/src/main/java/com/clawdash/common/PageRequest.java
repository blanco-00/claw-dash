package com.clawdash.common;

import java.io.Serializable;

public class PageRequest implements Serializable {

    private Integer page = 1;
    private Integer pageSize = 10;
    private String sortField;
    private String sortOrder;
    private String keyword;
    private String status;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page == null || page < 1 ? 1 : page;
    }

    public Integer getPageNum() {
        return page;
    }

    public void setPageNum(Integer pageNum) {
        setPage(pageNum);
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
