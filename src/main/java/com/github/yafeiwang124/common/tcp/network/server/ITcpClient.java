package com.github.yafeiwang124.common.tcp.network.server;

import com.github.yafeiwang124.common.tcp.network.handler.IRequestCallback;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

public interface ITcpClient extends Closeable {

    void tell(Object message, IRequestCallback callback) throws Exception;

    Object ask(Object request) throws Exception;

    Object ask(Object request, long timeout, TimeUnit unit) throws Exception;

    boolean isActive();
}
