package com.example.wallet.service.saga;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.*;

@Data
@NoArgsConstructor
public class SagaContext {

    private Map<String, Object> data;

    public SagaContext(HashMap<String, Object> data) {
        this.data = data != null ? data : new HashMap<>();
    }

    public void put(String key, Object value) {
        this.data.put(key, value);
    }

    public Object get(String key) {
        return this.data.get(key);
    }

    public Long getLong(String key) {
        Object value = this.get(key);
        if(value instanceof Number) {
            return ((Number)value).longValue();
        }
        return null;
    }

    public BigDecimal getBigDecimal(String key) {
        Object value = this.get(key);
        if(value instanceof Number) {
            return BigDecimal.valueOf(((Number)value).doubleValue());
        }
        return null;
    }

    public String getString(String key) {
        Object value = this.get(key);
        if(value instanceof String) {
            return (String)value;
        }
        return null;
    }


}
