package org.example;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FetchTxnRequest {
    private Long limit;
    private String cursor;
    private List<Filter> searchFilters;
}
