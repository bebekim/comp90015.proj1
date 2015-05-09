package com.github.nedp.comp90015.proj1.connection.message;

/**
 * Created by nedp on 06/04/15.
 */
public class Authentication extends DefaultMessage implements Message {
    private final String key;

    public Authentication(String key) {
        this.key = key;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean equals(Authentication other) {
        return this.key.equals(other.key);
    }

    public String key() {
        return this.key;
    }

    protected MessageType messageType() {
        return MessageType.AUTH;
    }
}
