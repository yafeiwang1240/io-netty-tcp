package com.github.yafeiwang124.common.tcp.pool;

public interface ConnectionManager<T> {
    T build() throws Exception;
    boolean isValid(T connection);
    void release(T connection);
}
