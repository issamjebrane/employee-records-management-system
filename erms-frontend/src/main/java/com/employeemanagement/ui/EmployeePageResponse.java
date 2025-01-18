package com.employeemanagement.ui;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class EmployeePageResponse {
    @JsonProperty("content")
    private List<Employee> employees;

    @JsonProperty("pageable")
    private PageableInfo pageable;

    @JsonProperty("last")
    private boolean last;

    @JsonProperty("totalPages")
    private int totalPages;

    @JsonProperty("totalElements")
    private long totalElements;

    @JsonProperty("size")
    private int size;

    @JsonProperty("number")
    private int number;

    @JsonProperty("sort")
    private SortInfo sort;

    @JsonProperty("first")
    private boolean first;

    @JsonProperty("numberOfElements")
    private int numberOfElements;

    @JsonProperty("empty")
    private boolean empty;

    @Data
    public static class PageableInfo {
        @JsonProperty("pageNumber")
        private int pageNumber;

        @JsonProperty("pageSize")
        private int pageSize;

        @JsonProperty("sort")
        private SortInfo sort;

        @JsonProperty("offset")
        private long offset;

        @JsonProperty("paged")
        private boolean paged;

        @JsonProperty("unpaged")
        private boolean unpaged;
    }

    @Data
    public static class SortInfo {
        @JsonProperty("empty")
        private boolean empty;

        @JsonProperty("unsorted")
        private boolean unsorted;

        @JsonProperty("sorted")
        private boolean sorted;
    }
}
