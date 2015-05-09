package com.github.nedp.comp90015.proj1.syncserver;

/**
 * Created by nedp on 07/04/15.
 */
public class UpdateFailedException extends Exception {
    @SuppressWarnings("SameParameterValue")
    public UpdateFailedException(String s) {
        super(s);
    }

    public UpdateFailedException(String s, Throwable e) {
        super(s, e);
    }
}
