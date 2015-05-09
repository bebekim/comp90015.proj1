package com.github.nedp.comp90015.proj1.connection;

import java.io.*;
import java.net.Socket;

/**
 * Created by nedp on 06/04/15.
 */
public class TCPConnection<T> {
    @SuppressWarnings("WeakerAccess")
    public static final String GET_STREAM_MSG = "getting data streams";
    @SuppressWarnings("WeakerAccess")
    public static final String READ_MSG = "reading from data stream";
    @SuppressWarnings("WeakerAccess")
    public static final String WRITE_MSG = "writing to data stream";
    @SuppressWarnings("WeakerAccess")
    public static final String IN_CLOSE_MSG = "closing in stream: ";
    @SuppressWarnings("WeakerAccess")
    public static final String OUT_CLOSE_MSG = "closing out stream: ";
    @SuppressWarnings("WeakerAccess")
    public static final String SOCKET_CLOSE_MSG = "closing socket: ";

    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;

    public TCPConnection(Socket socket) throws IOException {
        this.socket = socket;
        try {
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new IOException(GET_STREAM_MSG + ": " + e.getMessage(), e);
        }
    }

    public String read() throws IOException {
        try {
            return this.in.readUTF();
        } catch (IOException e) {
            throw new IOException(READ_MSG + ": " + e.getMessage(), e);
        }
    }

    public void write(String msg) throws IOException {
        try {
            this.out.writeUTF(msg);
        } catch (IOException e) {
            throw new IOException(WRITE_MSG + ": " + e.getMessage(), e);
        }
        this.out.flush();
    }

    public void close() throws IOException {
        try {
            this.in.close();
        } catch (IOException e) {
            throw new IOException(IN_CLOSE_MSG + ": " + e.getMessage(), e);
        }
        try {
            this.out.close();
        } catch (IOException e) {
            throw new IOException(OUT_CLOSE_MSG + ": " + e.getMessage(), e);
        }
        try {
            this.socket.close();
        } catch (IOException e) {
            throw new IOException(SOCKET_CLOSE_MSG + ": " + e.getMessage(), e);
        }
    }
}
