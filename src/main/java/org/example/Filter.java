package org.example;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Filter {
    private String field;   // "userId", "amount", "time"
    private Operator op;      // "=", ">", "<", ">=", "<="
    private Object value;   // 10, 3, etc.
}
