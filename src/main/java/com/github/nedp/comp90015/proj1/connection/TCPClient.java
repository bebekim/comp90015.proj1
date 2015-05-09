package com.github.nedp.comp90015.proj1.connection;

import com.github.nedp.comp90015.proj1.connection.message.BadMessageException;
import com.github.nedp.comp90015.proj1.connection.message.Authentication;
import com.github.nedp.comp90015.proj1.connection.message.JSONBoxedMessage;
import com.github.nedp.comp90015.proj1.syncclient.Update;
import com.github.nedp.comp90015.proj1.util.Log;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by nedp on 06/04/15.
 */
public class TCPClient {
    private final Authentication auth;
    private final Authentication serverAuth;

    private final TCPConnection conn;

    @SuppressWarnings("SameParameterValue")
    public TCPClient(String hostname, int port, Authentication auth, Authentication serverAuth) throws ConnectionException {
        try {
            this.auth = auth;
            this.serverAuth = serverAuth;
            final Socket socket;

            try {
                socket = new Socket(hostname, port);
            } catch (IOException e) {
                throw new IOException("requesting connection: " + e.getMessage(), e);
            }

            this.conn = new TCPConnection(socket);
            this.authenticate();
        } catch (IOException | AuthenticationException | BadMessageException e) {
            throw new ConnectionException("creating TCPClient: " + e.getMessage(), e);
        }
    }

    private void authenticate() throws IOException, AuthenticationException, BadMessageException {
        try {
            this.conn.write(this.auth.toJSON());
        } catch (IOException e) {
            throw new IOException("sending authentication: " + e.getMessage(), e);
        }
        Log.F("Authentication sent.");

        final Authentication receivedAuth;
        try {
            receivedAuth = JSONBoxedMessage.ReadFrom(conn).toMessage().toAuthentication();
        } catch (IOException e) {
            throw new IOException("receiving authentication: " + e.getMessage(), e);
        }
        Log.I("Authentication received: \"" + receivedAuth.key() + "\"");

        if (!receivedAuth.equals(this.serverAuth)) {
            throw new AuthenticationException("server failed authentication");
        }
    }

    public synchronized void sendUpdate(Update update) throws IOException, BadMessageException {
        update.sendThrough(this.conn);
    }
}

