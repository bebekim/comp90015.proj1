package com.github.nedp.comp90015.proj1.connection.message;

import com.google.gson.Gson;

/**
 * Created by nedp on 06/04/15.
 */
public abstract class DefaultMessage implements Message {
    @SuppressWarnings("WeakerAccess")
    protected static final Gson Gson = new Gson();

    public String toJSON() {
        final String msg = Gson.toJson(this);
        final JSONBoxedMessage jsonMsg = new JSONBoxedMessage(msg, this.messageType());
        return Gson.toJson(jsonMsg);
    }

    public static Message FromJSON(String json) {
        final JSONBoxedMessage jsonMsg = Gson.fromJson(json, JSONBoxedMessage.class);
        return jsonMsg.toMessage();
    }

    protected abstract MessageType messageType();

    public ClientMessage toClientMessage() throws BadMessageException {
        if (this instanceof ClientMessage) {
            return (ClientMessage) this;
        } else {
            throw new BadMessageException("want ClientMessage, got "
                    + this.getClass().getSimpleName());
        }
    }

    public ServerMessage toServerMessage() throws BadMessageException {
        if (this instanceof ServerMessage) {
            return (ServerMessage) this;
        } else {
            throw new BadMessageException("want ServerMessage, got "
                    + this.getClass().getSimpleName());
        }
    }

    public Authentication toAuthentication() throws BadMessageException {
        if (this instanceof Authentication) {
            return (Authentication) this;
        } else {
            throw new BadMessageException("want Authentication, got "
                    + this.getClass().getSimpleName());
        }
    }
}
