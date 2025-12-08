package org.example;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.util.Pair;


public class CursorTransactionSvcImpl implements TransactionService {

    private final TransactionRepo transactionRepo;

    public CursorTransactionSvcImpl(TransactionRepo transactionRepo) {
        this.transactionRepo = transactionRepo;
    }

    @Override
    public FetchTxnResponse getPaginatedListOfTransactions(FetchTxnRequest req) {

        List<Transaction> txns = transactionRepo.getTransactionList();

        // 1. Apply filters
        List<Transaction> filtered = txns.stream()
                .filter(tx -> matchesAllFilters(tx, req.getSearchFilters()))
                .collect(Collectors.toList());

        // 2. Sort by transactionTime DESC, id DESC
        filtered.sort(
                Comparator.comparing(Transaction::getTransactionTime).reversed()
                        .thenComparing(Transaction::getId).reversed()
        );

        // 3. Decode cursor and apply seek-based pagination
        Pair<LocalDateTime, Long> cursor = CursorUtil.decode(req.getCursor());
        if (cursor != null) {
            filtered = filtered.stream()
                    .filter(tx -> isAfterCursor(tx, cursor))
                    .collect(Collectors.toList());
        }

        // 4. Pagination
        long limit = req.getLimit() == null ? 20 : req.getLimit();

        boolean hasNext = filtered.size() > limit;

        // page = first `limit` items
        List<Transaction> page = filtered.stream()
                .limit(limit)
                .collect(Collectors.toList());

        FetchTxnResponse resp = new FetchTxnResponse();
        resp.setTransactionList(page);
        resp.setHasNextCursor(hasNext);

        // 5. NEXT cursor setter
        if (hasNext) {
            Transaction lastItem = page.get(page.size() -1);
            resp.setNextCursor(CursorUtil.encode(lastItem.getTransactionTime(), lastItem.getId()));
        }

        return resp;
    }

    private boolean matchesAllFilters(Transaction tx, List<Filter> filters) {
        for (Filter f : filters) {
            if (!matches(tx, f)) return false;
        }
        return true;
    }

    private boolean matches(Transaction tx, Filter f) {

        return switch (f.getField()) {
            case "id" -> compareLong(tx.getId(), (Long) f.getValue(), f.getOp());
            case "transactionTime" ->
                    compareLong(tx.getTransactionTime().toEpochSecond(ZoneOffset.UTC), (Long) f.getValue(), f.getOp());
            case "payer" -> f.getValue().equals(tx.getPayer());
            case "payee" -> f.getValue().equals(tx.getPayee());
            case "status" -> f.getValue().equals(tx.getStatus());
            case "amount" -> compareLong(tx.getAmount(), (long) f.getValue(), f.getOp());
            default -> throw new IllegalArgumentException("Unknown filter field: " + f.getField());
        };
    }

    private boolean compareLong(long a, long b, Operator op) {
        return switch (op) {
            case EQ -> a == b;
            case GT -> a > b;
            case LT -> a < b;
            case GTE -> a >= b;
            case LTE -> a <= b;
            default -> throw new IllegalArgumentException("Invalid operator: " + op);
        };
    }


    private boolean isAfterCursor(Transaction tx, Pair<LocalDateTime, Long> cursor) {
        LocalDateTime cursorTime = cursor.getKey();
        Long cursorId = cursor.getValue();

        // First compare timestamps
        if (tx.getTransactionTime().isBefore(cursorTime)) return false;

        if (tx.getTransactionTime().isEqual(cursorTime)) {
            return tx.getId() < cursorId; // id descending, so lower IDs come *after*
        }

        return true; // tx is newer → include

    }
}
