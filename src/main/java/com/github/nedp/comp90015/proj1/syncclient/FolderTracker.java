package com.github.nedp.comp90015.proj1.syncclient;

import com.github.nedp.comp90015.proj1.connection.message.BadMessageException;
import com.github.nedp.comp90015.proj1.connection.TCPClient;
import com.github.nedp.comp90015.proj1.connection.message.FolderUpdate;
import com.github.nedp.comp90015.proj1.util.Log;
import org.apache.commons.lang3.Validate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Created by nedp on 06/04/15.
 */
public class FolderTracker implements Runnable {
    private final Path dir;
    private final TCPClient tcp;

    private final Map<Path, FileTracker> files = new HashMap<>();
    private WatchService watcher;

    public FolderTracker(Path dir, TCPClient tcp) throws IOException, BadMessageException, InterruptedException {
        this.dir = dir;
        this.tcp = tcp;

        // Register with the event driven API for folder updates.
        this.getNewWatcher();

        // Check that the server and client are starting out synchronised.
        // TODO

        // Create File monitors for all existing files.
        try {
            for (String file : dir.toFile().list()) {
                System.out.println(file);
                this.handleEntryCreate(Paths.get(file));
            }
        } catch (BadMessageException e) {
            throw new BadMessageException("tracking preexisting files: " + e.getMessage(), e);
        }
    }

    private void getNewWatcher() throws IOException {
        try {
            this.watcher = FileSystems.getDefault().newWatchService();
            FolderTracker.this.dir.register(
                    this.watcher,
                    ENTRY_CREATE,
                    ENTRY_DELETE,
                    ENTRY_MODIFY
            );
        } catch (IOException e) {
            throw new IOException("registering with event API: " + e.getMessage(), e);
        }
    }

    private void onWatchEvent(WatchEvent<?> event) throws IOException, InterruptedException, BadMessageException {
        final Path filename = Paths.get(event.context().toString());

        // If an entry is created, make a new File monitor,
        // and make a FolderUpdate for this event.
        if (event.kind().name().equals(ENTRY_CREATE.name())) {
            this.handleEntryCreate(filename);
            return;
        }

        // If an entry is deleted, drop the File monitor,
        // and make a FolderUpdate for this event.
        if (event.kind().name().equals(ENTRY_DELETE.name())) {
            this.handleEntryDelete(filename);
            return;
        }

        // If an entry is modified, trigger the creation and sending of instructions.
        if (event.kind().equals(ENTRY_MODIFY)) {
            this.handleEntryModify(filename);
            return;
        }

        // It's a logic error if we reach here.
        throw new RuntimeException("Incomplete branch coverage.");
    }

    private void handleEntryCreate(Path filename) throws IOException, InterruptedException, BadMessageException {
        Log.F("ENTRY_CREATE(" + filename + ")");

        // Prepare the file for tracking.
        // If it is deleted on disk before it can be tracked,
        // don't notify the server.
        final FileTracker file = this.prepareFile(filename);
        if (file == null) {
            return;
        }
        this.sendEntryCreate(filename);

        // Ensure that the initial states of the files are synchronised.
        try {
            file.triggerUpdate();
            Log.F("Triggered file update: " + filename);
        } catch (FileNotFoundException e) {
            Log.F("Ignored update for deleted file: " + filename);
        } catch (IOException e) {
            throw new IOException("triggering update: " + e.getMessage());
        }

        // Start tracking the file.
        file.track();
        Log.F("Started tracking: " + filename);
    }

    private void handleEntryModify(Path filename) throws InterruptedException, IOException {
        Log.F("ENTRY_MODIFY(" + filename + ")");
        final FileTracker file = this.files.get(filename);

        // If the file is null, the file isn't tracked.
        if (file == null) {
            Log.F("Skipped update for untracked file: " + filename);
            return;
        }

        // The file changed on disk, so trigger SynchronisedFile#CheckFileState.
        try {
            file.triggerUpdate();
            Log.F("Triggered file update: " + filename);
        } catch (FileNotFoundException e) {
            Log.F("Ignored update for deleted file: " + filename);
        } catch (IOException e) {
            throw new IOException("triggering update: " + e.getMessage());
        }
    }

    private void handleEntryDelete(Path filename) throws IOException, BadMessageException {
        Log.F("ENTRY_DELETE(" + filename + ")");

        // If the file is null, the file isn't currently tracked, so don't need to do anything.
        final FileTracker file = this.files.get(filename);
        if (file == null) {
            Log.F("An untracked file was deleted: " + filename);
            return;
        }

        // Stop tracking the file and tell the server to delete it.
        file.drop();
        this.files.remove(filename);
        Log.F("Stopped tracking: " + filename);
        this.sendEntryDelete(filename);
    }

    private void sendEntryCreate(Path filename) throws BadMessageException, IOException {
        try {
            final FolderUpdate update = new FolderUpdate(filename, ENTRY_CREATE);
            this.tcp.sendUpdate(update);
        } catch (BadMessageException e)  {
            throw new BadMessageException("sending ENTRY_CREATE: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new IOException("sending ENTRY_CREATE Update: " + e.getMessage(), e);
        }
        Log.I("File Creation: " + filename);
    }

    private void sendEntryDelete(Path filename) throws IOException, BadMessageException {
        this.files.remove(filename);

        final FolderUpdate update = new FolderUpdate(filename, ENTRY_DELETE);
        try {
            this.tcp.sendUpdate(update);
        } catch (BadMessageException e) {
            throw new BadMessageException("sending ENTRY_DELETE: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new IOException("sending ENTRY_DELETE: " + e.getMessage(), e);
        }
        Log.I("File Deletion: " + filename);
    }

    private FileTracker prepareFile(Path filename) throws IOException, InterruptedException {
        // Sanity check - the file must not already be tracked.
        Validate.isTrue(!this.files.containsKey(filename));

        final FileTracker file;
        try {
             file = new FileTracker(this.dir, filename, this.tcp);
        } catch (FileNotFoundException e) {

            // The file was deleted before we finished handling its creation.
            // Log it and otherwise ignore.
            Log.F("File deleted before creation handled: " + e.getMessage());
            return null;
        } catch (IOException e) {
            throw new IOException("creating File monitor: " + e.getMessage(), e);
        }
        this.files.put(filename, file);

        return file;
    }


    @Override
    public void run() {
        while (true) {
            // Wait for updates to the directory.
            final WatchKey key;
            try {
                key = this.watcher.take();
            } catch (InterruptedException e) {
                Log.S("taking watch key (InterruptedException): " + e.getMessage(), e);
                System.exit(SyncClient.EXIT_FAILURE);
                return;
            }

            // Handle each event.
            for (WatchEvent<?> event : key.pollEvents()) {
                // TODO do we just ignore this (other than the break)?
                if (event.kind().name().equals(OVERFLOW.name())) {
                    break;
                }
                try {
                    FolderTracker.this.onWatchEvent(event);
                } catch (IOException | InterruptedException | BadMessageException e) {
                    Log.S("handling update event (" + e.getClass().getSimpleName() + "): "
                            + e.getMessage(), e);
                    System.exit(SyncClient.EXIT_FAILURE);
                    return;
                }
            }

            if (!key.reset()) {
                Log.W("watcher no longer valid. Trying to get a new one...");
                try {
                    this.getNewWatcher();
                } catch (IOException e) {
                    Log.S("renewing folder watcher (IOException): " + e.getMessage(), e);
                    System.exit(SyncClient.EXIT_FAILURE);
                    return;
                }
            }
        }
    }
}
