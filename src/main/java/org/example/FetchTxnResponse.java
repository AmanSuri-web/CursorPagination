package org.example;

import lombok.Data;

import java.util.List;

@Data
public class FetchTxnResponse {
    private List<Transaction> transactionList;
    private String nextCursor;
    private boolean hasNextCursor;
}
