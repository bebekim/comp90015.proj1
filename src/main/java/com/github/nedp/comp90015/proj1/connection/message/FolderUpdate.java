package com.github.nedp.comp90015.proj1.connection.message;

import com.github.nedp.comp90015.proj1.connection.TCPConnection;
import com.github.nedp.comp90015.proj1.syncclient.Update;
import com.github.nedp.comp90015.proj1.util.Log;

import java.io.IOException;
import java.nio.file.*;

/**
 * Created by nedp on 06/04/15.
 */
public class FolderUpdate extends DefaultMessage implements Update, ClientMessage {
    private final String filename;
    private final String kindName;
    
    private transient WatchEvent.Kind kind;

    public FolderUpdate(Path filename, WatchEvent.Kind kind) {
        this.filename = filename.toString();
        this.kind = kind;
        this.kindName = kind.name();
    }

    @Override
    protected MessageType messageType() {
        return MessageType.FOLDER;
    }

    @Override
    public void sendThrough(TCPConnection conn) throws BadMessageException, IOException {
        try {
            conn.write(this.toJSON());
        } catch (IOException e) {
            throw new IOException("sending update: " + e.getMessage(), e);
        }

        final ServerMessage response = JSONBoxedMessage.ReadFrom(conn).toMessage().toServerMessage();

        // If the response is a success report, continue to the next instruction.
        if (response instanceof SuccessMessage) {
            Log.F("Instruction received successfully.");
        } else {
            // Otherwise fail loudly.
            throw new BadMessageException("unexpected response: {" + response.toJSON() + "}");
        }
    }
    
    public WatchEvent.Kind kind() {
        if (this.kind == null) {
            this.kind = FolderUpdate.Kind.valueOf(this.kindName).kind;
        }
        return this.kind;
    }

    public String filename() {
        return this.filename;
    }

    private enum Kind {
        ENTRY_CREATE(StandardWatchEventKinds.ENTRY_CREATE),
        ENTRY_DELETE(StandardWatchEventKinds.ENTRY_DELETE),
        ENTRY_MODIFY(StandardWatchEventKinds.ENTRY_MODIFY),
        ;
        private final WatchEvent.Kind kind;

        Kind(WatchEvent.Kind kind) {
            this.kind = kind;
        }
    }
}
