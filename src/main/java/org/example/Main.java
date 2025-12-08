package org.example;


import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.example.Status.PAID;

public class Main {
    public static void main(String[] args) {
         TransactionRepo transactionRepo = new TransactionRepo();
        TransactionService transactionService = new CursorTransactionSvcImpl(transactionRepo);

        FetchTxnRequest fetchTxnRequest = new FetchTxnRequest();
        fetchTxnRequest.setLimit(2L);
        List<Filter> filterList = new ArrayList<>();
        filterList.add(new Filter().setOp(Operator.EQ).setValue(PAID).setField("status"));
        filterList.add(new Filter().setOp(Operator.EQ).setValue("aman").setField("payer"));
        filterList.add(new Filter().setOp(Operator.GT).setValue(LocalDateTime.MIN.toEpochSecond(ZoneOffset.UTC)).setField("transactionTime"));
        fetchTxnRequest.setSearchFilters(filterList);

        //fetchTxnRequest.setCursor("MjAyNS0xMi0wNVQxMDoyNnwz");

        FetchTxnResponse res = transactionService.getPaginatedListOfTransactions(fetchTxnRequest);

        System.out.println(res.getNextCursor() + " " + res.isHasNextCursor());
        for (Transaction txn : res.getTransactionList()) {
            System.out.println(txn.toString());
        }


    }

}