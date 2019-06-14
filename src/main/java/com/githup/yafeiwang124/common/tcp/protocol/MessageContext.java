package com.githup.yafeiwang124.common.tcp.protocol;

import java.io.Serializable;

public class MessageContext implements Serializable {
    private String messageId;
    private Object message;
    private boolean isSucceed = true;
    private String exceptionMessage;

    public MessageContext(String messageId, Object message) {
        this.message = message;
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public boolean isSucceed() {
        return isSucceed;
    }

    public void setSucceed(boolean succeed) {
        isSucceed = succeed;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }
}
