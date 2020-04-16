package com.github.yafeiwang124.tcp.network.handler;

public interface IRequestHandler<P, R> {
    Class<P> messageType();
    R handle(P message) throws Exception;
}
