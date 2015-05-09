package com.github.nedp.comp90015.proj1.connection;

/**
 * Created by nedp on 06/04/15.
 */
public class ConnectionException extends Exception {
    public ConnectionException(String msg) {
        super(msg);
    }

    public ConnectionException(String msg, Throwable e) {
        super(msg, e);
    }
}
