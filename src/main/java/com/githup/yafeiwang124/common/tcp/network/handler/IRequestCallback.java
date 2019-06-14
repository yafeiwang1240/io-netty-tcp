package com.githup.yafeiwang124.common.tcp.network.handler;

public interface IRequestCallback {
    void invoke(Object message);
    void onFail(String exceptionMessage);
}
