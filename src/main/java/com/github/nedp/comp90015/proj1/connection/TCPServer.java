package com.github.nedp.comp90015.proj1.connection;

import com.github.nedp.comp90015.proj1.connection.message.*;
import com.github.nedp.comp90015.proj1.syncserver.Router;
import com.github.nedp.comp90015.proj1.syncserver.UpdateFailedException;
import com.github.nedp.comp90015.proj1.syncserver.SyncServer;
import com.github.nedp.comp90015.proj1.util.Log;
import filesync.BlockUnavailableException;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Path;

/**
 * Created by nedp on 06/04/15.
 */
public class TCPServer implements Runnable {
    private final Authentication auth;
    private final Authentication clientAuth;

    private final ServerSocket listener;

    private final Router router;

    @SuppressWarnings("SameParameterValue")
    public TCPServer(int port, Path dir, Authentication auth, Authentication clientAuth) throws IOException {
        this.auth = auth;
        this.clientAuth = clientAuth;
        this.router = new Router(dir);
        try {
            this.listener = new ServerSocket(port);
        } catch (IOException e) {
            throw new IOException("listening for connection: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("UnnecessaryContinue")
    @Override
    public void run() {
        while (true) {
            // Accept a new connection.
            final TCPConnection conn;
            try {
                conn = new TCPConnection(this.listener.accept());
            } catch (IOException e) {
                Log.S("listening for connection: " + e.getMessage(), e);
                Log.S("Aborting");
                System.exit(SyncServer.EXIT_FAILURE);
                return;
            }

            try {
                this.authenticate(conn);
            } catch (AuthenticationException e) {
                Log.W("During authentication: " + e.getMessage());
                Log.W("Continuing to the next connection.");
                continue;
            }

            // Handle the connection.
            try {
                try {
                    this.handle(conn);

                // If an invalid message is received, refuse it, and close the connection.
                } catch (BadMessageException e) {
                    conn.write((new RefuseMessage()).toJSON());
                    Log.W("While handling connection (BadMessageException): " + e.getMessage());
                    Log.W("Continuing to the next connection.");
                    continue;

                // If we fail to handle an update for some reason, shut down the server -
                // we now have inconsistent state.
                } catch (UpdateFailedException e) {
                    conn.write((new RefuseMessage()).toJSON());
                    Log.S("While handling connection (UpdateFailedException): " + e.getMessage());
                    Log.S("Aborting.");
                    System.exit(SyncServer.EXIT_FAILURE);
                    return;
                }
            } catch (IOException e) {
                Log.W("While handling connection (IOException): " + e.getMessage());
                Log.W("Continuing to the next connection.");
                continue;
            } finally {
                // Drop connection.
                try {
                    conn.close();
                } catch (IOException e) {
                    Log.S("closing connection: " + e.getMessage(), e);
                    Log.S("Aborting");
                    System.exit(SyncServer.EXIT_FAILURE);
                }
            }
        }
    }

    private void authenticate(TCPConnection conn) throws AuthenticationException {
        try {
            final Authentication receivedAuth;
            try {
                receivedAuth = JSONBoxedMessage.ReadFrom(conn).toMessage().toAuthentication();
            } catch (IOException e) {
                throw new IOException("receiving authentication: " + e.getMessage(), e);
            }
            Log.I("Authentication received: \"" + receivedAuth.key() + "\"");

            if (!receivedAuth.equals(this.clientAuth)) {
                throw new AuthenticationException("client failed authentication");
            }

            conn.write(this.auth.toJSON());
            Log.F("Authentication sent.");
        } catch (IOException | BadMessageException e) {
            throw new AuthenticationException(e.getMessage(), e);
        }
    }

    private void handle(TCPConnection conn) throws IOException, BadMessageException, UpdateFailedException {
        try {
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    // Read and route the next message.
                    final ClientMessage msg =
                            JSONBoxedMessage.ReadFrom(conn).toMessage().toClientMessage();
                    this.router.routeMessage(msg);
                    conn.write(new SuccessMessage().toJSON());

                // If the block is unavailable, request an upgrade.
                } catch (BlockUnavailableException e) {
                    conn.write((new UpgradeMessage()).toJSON());
                    continue;
                }
            }
        // If the connection fails, stop handling it.
        } catch (IOException e) {
            throw new IOException("reading from connection: " + e.getMessage(), e);
        }
    }
}


