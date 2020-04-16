package com.github.yafeiwang124.tcp.network.handler;

public interface IRequestCallback {
    void invoke(Object message);
    void onFail(String exceptionMessage);
}
