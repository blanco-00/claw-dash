package com.clawdash.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.io.Serializable;
import java.util.List;

public class PageResponse<T> implements Serializable {

    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int size;
    private int number;
    private boolean first;
    private boolean last;

    public PageResponse() {
    }

    public PageResponse(List<T> content, long totalElements, int number, int size) {
        this.content = content;
        this.totalElements = totalElements;
        this.number = number;
        this.size = size;
        this.totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        this.first = number == 1;
        this.last = number >= totalPages;
    }

    public static <T> PageResponse<T> of(List<T> content, long totalElements, int number, int size) {
        return new PageResponse<>(content, totalElements, number, size);
    }

    public static <T> PageResponse<T> fromIPage(IPage<?> page, List<T> content) {
        return new PageResponse<>(
            content,
            page.getTotal(),
            (int) page.getCurrent(),
            (int) page.getSize()
        );
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }
}
