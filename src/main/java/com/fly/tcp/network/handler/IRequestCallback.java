package com.fly.tcp.network.handler;

public interface IRequestCallback {
    void invoke(Object message);
    void onFail(String exceptionMessage);
}
