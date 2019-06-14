package com.githup.yafeiwang124.common.tcp.network.exception;

public class NotFoundServerException extends Exception{

    public NotFoundServerException() {
        super();
    }

    public NotFoundServerException(String message) {
        super(message);
    }

    public NotFoundServerException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
