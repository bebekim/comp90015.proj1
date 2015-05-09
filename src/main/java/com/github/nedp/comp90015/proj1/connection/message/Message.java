package com.github.nedp.comp90015.proj1.connection.message;

/**
 * Created by nedp on 07/04/15.
 */
public interface Message {
    String toJSON();

    ClientMessage toClientMessage() throws BadMessageException;

    ServerMessage toServerMessage() throws BadMessageException;

    Authentication toAuthentication() throws BadMessageException;
}
