package org.example;


import javafx.util.Pair;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;


public class Main {

    public enum Operator {
        EQ, GT, LT, GTE, LTE, BETWEEN, IN
    }

    public enum Status {
        PAID,
        INITIATED,
        DROPPED
    }

    @Data
    @Accessors(chain = true)
    static public class Filter {
        private String field;   // "userId", "amount", "time"
        private Operator op;      // "=", ">", "<", ">=", "<="
        private Object value;   // 10, 3, etc.
    }


    @Data
    static public class FetchTxnRequest {
        private Long limit;
        private String cursor;
        private List<Filter> searchFilters;
    }

    @Getter
    @Builder
    @ToString
    static public class Transaction {
        private Long id;
        private Long amount;
        private String payee;
        private Long userId;
        private Long time;
        private Status status;
    }

    @Data
    static public class FetchTxnResponse {
        private List<Transaction> transactionList;
        private String nextCursor;
        private boolean hasNextCursor;
    }


    static interface TransactionService {
        FetchTxnResponse getPaginatedListOfTransactions(FetchTxnRequest fetchTxnRequest);
    }

    static class TransactionRepo {
        private final List<Transaction> transactionList = new ArrayList<>();

        TransactionRepo(){
            transactionList.add( Transaction.builder().id(1L).status(Status.PAID).time(1L).userId(1L).payee("vikas").amount(100L).build());
            transactionList.add( Transaction.builder().id(2L).status(Status.INITIATED).time(2L).userId(1L).payee("vikas").amount(100L).build());
            transactionList.add( Transaction.builder().id(3L).status(Status.PAID).time(3L).userId(1L).payee("vikas").amount(100L).build());
            transactionList.add( Transaction.builder().id(4L).status(Status.PAID).time(4L).userId(1L).payee("vikas").amount(100L).build());
            transactionList.add( Transaction.builder().id(5L).status(Status.PAID).time(5L).userId(1L).payee("vikas").amount(100L).build());
            transactionList.add( Transaction.builder().id(6L).status(Status.PAID).time(6L).userId(2L).payee("aman").amount(100L).build());


        }

        public List<Transaction> getTransactionList(){
            return transactionList;
        }
    }

    static class CursorUtil {

        public static String encode(Long time, Long id) {
            String raw = time + "|" + id;
            return Base64.getEncoder().encodeToString(raw.getBytes());
        }

        public static Pair<Long, Long> decode(String cursor) {
            if (cursor == null) return null;

            String decoded = new String(Base64.getDecoder().decode(cursor));
            String[] parts = decoded.split("\\|");
            return new Pair<>(Long.parseLong(parts[0]), Long.parseLong(parts[1]));
        }
    }

    static class ComparatorUtils {
        static boolean compareLong(long a, long b, Operator op) {
            return switch (op) {
                case EQ -> a == b;
                case GT -> a > b;
                case LT -> a < b;
                case GTE -> a >= b;
                case LTE -> a <= b;
                default -> throw new IllegalArgumentException("Invalid operator: " + op);
            };
        }
    }

    static class TransactionServiceImpl implements TransactionService {
        private final TransactionRepo transactionRepo;

        public TransactionServiceImpl(TransactionRepo transactionRepo) {
            this.transactionRepo = transactionRepo;
        }

        private final Integer DEFAULT_PAGE_SIZE = 20;

        @Override
        public FetchTxnResponse getPaginatedListOfTransactions(FetchTxnRequest req) {

            List<Transaction> txns = transactionRepo.getTransactionList();

            // 1. Apply filters
            List<Transaction> filtered = new ArrayList<>();
            for(Transaction tx : txns){
                if(matchesAllFilters(tx, req.getSearchFilters())){
                    filtered.add(tx);
                }
            }

            //sorting not required
//            filtered.sort(
//                    Comparator.comparing(Transaction::getCreatedAt).reversed()
//                            .thenComparing(Transaction::getId).reversed()
//            );

            // 3. Decode cursor and apply seek-based pagination
            List<Transaction> cursorFilter = new ArrayList<>();
            if(req.getCursor() != null) {
                Pair<Long, Long> cursor = CursorUtil.decode(req.getCursor());
                for(Transaction tx : filtered){
                    if(isBeforeCursor(tx, cursor)){
                        cursorFilter.add(tx);
                    }
                }
            }else{
                cursorFilter = filtered;
            }

            // 4. Pagination
            long limit = req.getLimit() == null ? DEFAULT_PAGE_SIZE : req.getLimit();

            boolean hasNext = cursorFilter.size() > limit;

            // page = first `limit` items
            List<Transaction> page = new ArrayList<>();
            for(int i = 0; i< limit;i++){
                page.add(cursorFilter.get(i));
            }

            FetchTxnResponse resp = new FetchTxnResponse();
            resp.setTransactionList(page);
            resp.setHasNextCursor(hasNext);

            // 5. NEXT cursor setter
            if (hasNext) {
                Transaction lastItem = page.get(page.size() -1);
                resp.setNextCursor(CursorUtil.encode(lastItem.getTime(), lastItem.getId()));
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
                case "id" -> ComparatorUtils.compareLong(tx.getId(), (Long) f.getValue(), f.getOp());
                case "time" ->
                        ComparatorUtils.compareLong(tx.getTime(), (Long) f.getValue(), f.getOp());
                case "userId" -> ComparatorUtils.compareLong(tx.getUserId(), (Long) f.getValue(), f.getOp());
                case "payee" -> f.getValue().equals(tx.getPayee());
                case "status" -> f.getValue().equals(tx.getStatus());
                case "amount" -> ComparatorUtils.compareLong(tx.getAmount(), (long) f.getValue(), f.getOp());
                default -> throw new IllegalArgumentException("Unknown filter field: " + f.getField());
            };
        }



        private boolean isBeforeCursor(Transaction tx, Pair<Long, Long> cursor) {
            Long cursorTime = cursor.getKey();
            Long cursorId = cursor.getValue();

            // First compare timestamps
            if (tx.getTime() < cursorTime) return false;

            if (tx.getTime().equals(cursorTime)) {
                return tx.getId() > cursorId; // id asc, so lower IDs come *before*
            }

            return true; // tx is newer → include

        }
    }
    public static void main(String[] args) {
        TransactionRepo transactionRepo = new TransactionRepo();
        TransactionService transactionService = new TransactionServiceImpl(transactionRepo);

        FetchTxnRequest fetchTxnRequest = new FetchTxnRequest();
        fetchTxnRequest.setLimit(2L);
        List<Filter> filterList = new ArrayList<>();
        filterList.add(new Filter().setOp(Operator.EQ).setValue(Status.PAID).setField("status"));
        filterList.add(new Filter().setOp(Operator.EQ).setValue(1L).setField("userId"));
        filterList.add(new Filter().setOp(Operator.GT).setValue(0L).setField("time"));
        fetchTxnRequest.setSearchFilters(filterList);

        fetchTxnRequest.setCursor("M3wz");

        FetchTxnResponse res = transactionService.getPaginatedListOfTransactions(fetchTxnRequest);

        System.out.println(res.getNextCursor() + " " + res.isHasNextCursor());
        for (Transaction txn : res.getTransactionList()) {
            System.out.println(txn.toString());
        }

    }

}