package com.labix.api.common.dto;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PageResponse<T> {
    private List<T> items;
    private int page;
    private int size;
    private long total;
    private int totalPages;

    public static <T> PageResponse<T> of(Page<T> page) {
        PageResponse<T> r = new PageResponse<>();
        r.items = page.getContent();
        r.page = page.getNumber();
        r.size = page.getSize();
        r.total = page.getTotalElements();
        r.totalPages = page.getTotalPages();
        return r;
    }
}
