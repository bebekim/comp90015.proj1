package com.github.nedp.comp90015.proj1.connection.message;

import com.github.nedp.comp90015.proj1.connection.TCPConnection;
import com.google.gson.Gson;

import java.io.IOException;

/**
 * Created by nedp on 06/04/15.
 */
public class JSONBoxedMessage {
    private final String json;
    private String typeName;
    private static final Gson Gson = new Gson();

    public JSONBoxedMessage(String json, MessageType type) {
        this.json = json;
        // Have to allow null type for gson.
        if (type != null) {
            this.typeName = type.name();
        }
    }

    public Message toMessage() {
        return Gson.fromJson(this.json, MessageType.valueOf(this.typeName).type);
    }

    public static JSONBoxedMessage ReadFrom(TCPConnection conn) throws IOException {
        return Gson.fromJson(conn.read(), JSONBoxedMessage.class);
    }
}
