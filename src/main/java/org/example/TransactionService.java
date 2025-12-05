package org.example;

public interface TransactionService {
    FetchTxnResponse getPaginatedListOfTransactions(FetchTxnRequest fetchTxnRequest);
}
