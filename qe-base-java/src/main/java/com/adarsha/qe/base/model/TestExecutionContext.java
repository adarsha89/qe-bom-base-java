package com.adarsha.qe.base.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Thread-safe metadata model representing a test execution context.
 * Designed for fully parallel execution (parallel test methods and parallel DataProviders).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestExecutionContext {

    private String testSuiteName;
    private String testClassName;
    private String testMethodName;
    private long threadId;
    private String threadName;
    private String environment;
    private String browserOrPlatform;
    private Instant executionTimestamp;

    @Builder.Default
    private List<Object> dataProviderParameters = Collections.synchronizedList(new ArrayList<>());

    @Builder.Default
    private ConcurrentMap<String, Object> attributes = new ConcurrentHashMap<>();

    /**
     * Associates the specified value with the specified key in the context.
     * If the value is null, any existing mapping is removed.
     *
     * @param key   attribute key
     * @param value attribute value
     */
    public void setAttribute(String key, Object value) {
        if (value == null) {
            attributes.remove(key);
        } else {
            attributes.put(key, value);
        }
    }

    /**
     * Retrieves the attribute value associated with the specified key.
     *
     * @param key attribute key
     * @return attribute value or null if not found
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * Type-safe attribute retrieval.
     *
     * @param key  attribute key
     * @param type expected target class type
     * @param <T>  target return type
     * @return typed attribute value or null if not found
     * @throws ClassCastException if the attribute cannot be cast to the specified type
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        if (value == null) {
            return null;
        }
        if (!type.isInstance(value)) {
            throw new ClassCastException("Attribute '" + key + "' with type "
                    + value.getClass().getName() + " cannot be cast to " + type.getName());
        }
        return (T) value;
    }

    /**
     * Retrieves an attribute or returns a default value if absent.
     *
     * @param key          attribute key
     * @param defaultValue default value if attribute is absent
     * @param <T>          target type
     * @return attribute value or default value
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, T defaultValue) {
        Object value = attributes.get(key);
        return value != null ? (T) value : defaultValue;
    }

    /**
     * Checks if the context contains the specified attribute key.
     *
     * @param key attribute key
     * @return true if key exists, false otherwise
     */
    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }

    /**
     * Removes an attribute from the context.
     *
     * @param key attribute key
     * @return previous value associated with key, or null
     */
    public Object removeAttribute(String key) {
        return attributes.remove(key);
    }

    /**
     * Clears all attributes from this context.
     */
    public void clearAttributes() {
        attributes.clear();
    }
}
