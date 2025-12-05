package org.example;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
public class Transaction {
    private Long id;
    private Long amount;
    private String payee;
    private String payer;
    private LocalDateTime transactionTime;
    private LocalDateTime createdAt;
    private Status status;
}
