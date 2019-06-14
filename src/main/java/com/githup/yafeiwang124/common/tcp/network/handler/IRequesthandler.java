package com.githup.yafeiwang124.common.tcp.network.handler;

public interface IRequesthandler<P, R> {
    Class<P> messageType();
    R handle(P message) throws Exception;
}
