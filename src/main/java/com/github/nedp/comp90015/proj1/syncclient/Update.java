package com.github.nedp.comp90015.proj1.syncclient;

import com.github.nedp.comp90015.proj1.connection.message.BadMessageException;
import com.github.nedp.comp90015.proj1.connection.TCPConnection;

import java.io.IOException;

/**
 * Created by nedp on 06/04/15.
 */
public interface Update {
    void sendThrough(TCPConnection conn) throws BadMessageException, IOException;
}
