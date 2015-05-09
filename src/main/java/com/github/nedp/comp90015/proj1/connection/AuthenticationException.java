package com.github.nedp.comp90015.proj1.connection;

/**
 * Created by nedp on 06/04/15.
 */
class AuthenticationException extends Exception {
    AuthenticationException(String msg) {
        super(msg);
    }

    AuthenticationException(String msg, Throwable e) {
        super(msg, e);
    }
}
