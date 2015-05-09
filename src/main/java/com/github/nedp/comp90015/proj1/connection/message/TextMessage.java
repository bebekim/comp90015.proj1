package com.github.nedp.comp90015.proj1.connection.message;

/**
 * Created by nedp on 06/04/15.
 */
public class TextMessage extends DefaultMessage {
    private final String msg;

    public TextMessage(String msg) {
        this.msg = msg;
    }

    public String msg() {
        return this.msg;
    }

    protected MessageType messageType() {
        return MessageType.TEXT;
    }
}
