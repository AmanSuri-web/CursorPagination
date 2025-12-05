package org.example;


import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
         TransactionRepo transactionRepo = new TransactionRepo();
        TransactionService transactionService = new CursorTransactionSvcImpl(transactionRepo);

        FetchTxnRequest fetchTxnRequest = new FetchTxnRequest();
        fetchTxnRequest.setLimit(2L);
        fetchTxnRequest.setStatus(Status.PAID);
        fetchTxnRequest.setPayer("aman");
        //fetchTxnRequest.setPayee("aman");
        fetchTxnRequest.setCreatedAtGreater(LocalDateTime.MIN);

        fetchTxnRequest.setCursor("MjAyNS0xMi0wNVQxMDoyNnwz");

        FetchTxnResponse res = transactionService.getPaginatedListOfTransactions(fetchTxnRequest);

        System.out.println(res.getNextCursor() + " " + res.isHasNextCursor());
        for (Transaction txn : res.getTransactionList()) {
            System.out.println(txn.toString());
        }


    }

}