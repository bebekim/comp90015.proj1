package com.github.nedp.comp90015.proj1.connection.message;

/**
 * Created by nedp on 06/04/15.
 */
public class BadMessageException extends Exception {
    public BadMessageException(String s) {
        super(s);
    }

    public BadMessageException(String s, Throwable e) {
        super(s, e);
    }
}
