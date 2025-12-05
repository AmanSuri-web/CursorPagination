package org.example;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class TransactionRepo {
    private final List<Transaction> transactionList = new ArrayList<>();

    TransactionRepo(){
        LocalDateTime timeStamp = LocalDateTime.parse("2025-12-05T10:30:00");
        transactionList.add( Transaction.builder().id(1L).status(Status.PAID).transactionTime(timeStamp.minusMinutes(6)).createdAt(timeStamp.minusMinutes(6)).payer("aman").payee("vikas").amount(100L).build());
        transactionList.add( Transaction.builder().id(2L).status(Status.INITIATED).transactionTime(timeStamp.minusMinutes(5)).createdAt(timeStamp.minusMinutes(5)).payer("aman").payee("vikas").amount(100L).build());
        transactionList.add( Transaction.builder().id(3L).status(Status.PAID).transactionTime(timeStamp.minusMinutes(4)).createdAt(timeStamp.minusMinutes(4)).payer("aman").payee("vikas").amount(100L).build());
        transactionList.add( Transaction.builder().id(4L).status(Status.PAID).transactionTime(timeStamp.minusMinutes(3)).createdAt(timeStamp.minusMinutes(3)).payer("aman").payee("vikas").amount(100L).build());
        transactionList.add( Transaction.builder().id(5L).status(Status.PAID).transactionTime(timeStamp.minusMinutes(2)).createdAt(timeStamp.minusMinutes(2)).payer("aman").payee("vikas").amount(100L).build());
        transactionList.add( Transaction.builder().id(6L).status(Status.PAID).transactionTime(timeStamp.minusMinutes(1)).createdAt(timeStamp.minusMinutes(1)).payer("vikas").payee("aman").amount(100L).build());


    }



    public List<Transaction> getTransactionList(){
        return transactionList;
    }
}
