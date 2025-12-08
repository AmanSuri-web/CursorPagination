package org.example;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
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
                .filter(tx -> applyFilters(tx, req))
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

    private boolean applyFilters(Transaction tx, FetchTxnRequest req) {
        if (req.getStatus() != null && !req.getStatus().equals(tx.getStatus()))
            return false;

        if (req.getPayer() != null && !req.getPayer().equals(tx.getPayer()))
            return false;

        if (req.getPayee() != null && !req.getPayee().equals(tx.getPayee()))
            return false;

        if (req.getCreatedAtGreater() != null &&
                tx.getCreatedAt().isBefore(req.getCreatedAtGreater()))
            return false;

        return true;
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
