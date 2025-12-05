package org.example;

import javafx.util.Pair;

import java.time.LocalDateTime;
import java.util.Base64;

public class CursorUtil {

    public static String encode(LocalDateTime createdAt, Long id) {
        String raw = createdAt.toString() + "|" + id;
        return Base64.getEncoder().encodeToString(raw.getBytes());
    }

    public static Pair<LocalDateTime, Long> decode(String cursor) {
        if (cursor == null) return null;

        String decoded = new String(Base64.getDecoder().decode(cursor));
        String[] parts = decoded.split("\\|");
        return new Pair<>(LocalDateTime.parse(parts[0]), Long.parseLong(parts[1]));
    }
}

