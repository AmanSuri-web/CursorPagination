package org.example;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FetchTxnRequest {
    private Long limit;
    private String cursor;
    private Status status;
    private String payer;
    private String payee;
    private LocalDateTime createdAtGreater;
}
