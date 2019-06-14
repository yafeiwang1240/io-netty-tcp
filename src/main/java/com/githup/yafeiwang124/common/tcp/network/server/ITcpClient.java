package com.githup.yafeiwang124.common.tcp.network.server;

import com.githup.yafeiwang124.common.tcp.network.handler.IRequestCallback;

import java.io.Closeable;

public interface ITcpClient extends Closeable {

    void tell(Object message, IRequestCallback callback) throws Exception;

    Object ask(Object request) throws Exception;

    Object ask(Object request, long timeout) throws Exception;

    boolean isActive();
}
