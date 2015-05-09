package com.github.nedp.comp90015.proj1.connection.message;

/**
 * Created by nedp on 06/04/15.
 */
public class SuccessMessage extends DefaultMessage implements ServerMessage {

    @Override
    protected MessageType messageType() {
        return MessageType.OK;
    }
}
