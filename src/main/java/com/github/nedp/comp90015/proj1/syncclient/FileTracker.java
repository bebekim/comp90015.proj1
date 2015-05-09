package com.github.nedp.comp90015.proj1.syncclient;

import com.github.nedp.comp90015.proj1.connection.message.BadMessageException;
import com.github.nedp.comp90015.proj1.connection.TCPClient;
import com.github.nedp.comp90015.proj1.util.Log;
import filesync.EndUpdateInstruction;
import filesync.Instruction;
import filesync.StartUpdateInstruction;
import filesync.SynchronisedFile;
import sun.awt.Mutex;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nedp on 06/04/15.
 */
class FileTracker implements Runnable {
    private final SynchronisedFile syncFile;
    private final Path filename;

    private boolean isUpdating = false;
    private boolean isDropped = false;
    private final Mutex mutex = new Mutex();

    private final TCPClient tcp;
    private Thread thread;

    public FileTracker(Path dir, Path filename, TCPClient tcp) throws IOException {
        this.filename = filename;
        try {
            this.syncFile = new SynchronisedFile(dir.resolve(filename).toString());
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw new IOException("creating SynchronisedFile: " + e.getMessage(), e);
        }
        this.tcp = tcp;
    }

    public synchronized Thread track() {
        if (this.thread != null) {
            return null;
        }
        // Handle the Instructions from SynchronisedFile in a new thread.
        this.thread = new Thread(this);
        this.thread.setDaemon(true);
        this.thread.start();
        return thread;
    }

    public synchronized void drop() {
        try {
            this.unsafeDrop();
        } catch (InterruptedException e) {
            Log.S("Couldn't drop file properly - aborting.");
            System.exit(SyncClient.EXIT_FAILURE);
        }
    }

    @SuppressWarnings("RedundantThrows")
    private void unsafeDrop() throws InterruptedException {
        if (this.isDropped) {
            return;
        }

        Log.F("Dropping file: " + this.filename);
        if (this.isUpdating) {
            this.mutex.lock();
        }

        this.isDropped = true;
        if (this.thread != null) {
            this.thread.interrupt();
        }
    }

    @Override
    public void run() {
        final List<Instruction> instList = new ArrayList<>();

        // Until the file is dropped.
        // (But don't stop sending mid update)
        while (this.isUpdating || !this.isDropped) {
            // Wait for the next instruction.
            final Instruction inst = this.syncFile.NextInstruction();
            if (inst == null) {
                this.drop();
                return;
            }

            // If it's the start of an update, flag the file as updating,
            // and make a new instruction list.
            if (inst instanceof StartUpdateInstruction) {
                if (this.isUpdating) {
                    Log.S("Received StartUpdateInstruction from FileSync when we shouldn't have. Aborting.");
                    System.exit(SyncClient.EXIT_FAILURE);
                }
                this.mutex.lock();
                this.isUpdating = true;
                Log.F("Starting update: " + this.filename);

            // If we aren't already updating, we needed the start of an update.
            } else if (!this.isUpdating) {
                Log.W("Didn't receive StartUpdateInstruction from FileSync when we need to.");
                this.drop();
                return;
            }

            // Store the instruction.
            instList.add(inst);

            // If it's the end of the update, send the instructions.
            if (inst instanceof EndUpdateInstruction) {
                try {
                    this.tcp.sendUpdate(new InstructionList(instList));
                    Log.I("File Update: " + this.filename);
                } catch (IOException | BadMessageException e) {
                    Log.S("sending instruction list: " + e.getMessage(), e);
                    System.exit(SyncClient.EXIT_FAILURE);
                }
                this.isUpdating = false;
                this.mutex.unlock();
            }
        }
    }

    public void triggerUpdate() throws InterruptedException, IOException {
        try {
            this.syncFile.CheckFileState();
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw new IOException("checking file state: " + e.getMessage(), e);
        }
    }
}
