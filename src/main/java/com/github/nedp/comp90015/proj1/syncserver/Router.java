package com.github.nedp.comp90015.proj1.syncserver;

import com.github.nedp.comp90015.proj1.connection.message.BadMessageException;
import com.github.nedp.comp90015.proj1.connection.message.ClientMessage;
import com.github.nedp.comp90015.proj1.connection.message.FolderUpdate;
import com.github.nedp.comp90015.proj1.connection.message.InstructionMessage;
import com.github.nedp.comp90015.proj1.util.Log;
import filesync.*;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nedp on 06/04/15.
 */
public class Router {
    private final Path dir;
    private final Map<Path, SynchronisedFile> files = new HashMap<>();
    private SynchronisedFile currentFile = null;
    private Path currentFilename = null;

    public Router(Path dir) {
        this.dir = dir;

        // Manage preexisting files
        // TODO
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    public void routeMessage(ClientMessage msg) throws BadMessageException, IOException, BlockUnavailableException, UpdateFailedException {
        // Handle FileSync Instructions
        if (msg instanceof InstructionMessage) {
            this.routeInstruction(((InstructionMessage) msg).instruction());
            return;
        }

        // If we're in the middle of an update, we need an Instruction.
        if (this.currentFile != null) {
            throw new BadMessageException("want InstructionMessage, got {" + msg.toJSON() + "}");
        }

        // Handle folder updates
        if (msg instanceof FolderUpdate) {
            this.routeFolderUpdate((FolderUpdate) msg);
            return;
        }

    }

    private void routeFolderUpdate(FolderUpdate folderUpdate) throws BadMessageException, IOException, UpdateFailedException {
        if (folderUpdate.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
            this.routeEntryCreate(folderUpdate);
            return;
        }

        if (folderUpdate.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
            this.routeEntryDelete(folderUpdate);
            return;
        }

        // Otherwise fail loudly.
        throw new BadMessageException("folder update kind: " + folderUpdate.kind().name());
    }

    private void routeEntryCreate(FolderUpdate folderUpdate) throws BadMessageException, IOException {
        // If the file is already added, log and ignore the instruction.
        if (this.files.containsKey(Paths.get(folderUpdate.filename()))) {
            Log.I("File Creation Ignored: " + folderUpdate.filename() + " (already known)");
            return;
        }

        // If the file is not already added, add it.
        if (!this.addFile(Paths.get(folderUpdate.filename()))) {
            throw new BadMessageException("create preexisting file");
        }
        Log.I("File Creation: " + folderUpdate.filename());
    }

    private void routeEntryDelete(FolderUpdate folderUpdate) throws UpdateFailedException {
        if (!this.removeFile(Paths.get(folderUpdate.filename()))) {
            throw new UpdateFailedException("couldn't delete file");
        }
        Log.I("File Deletion: " + folderUpdate.filename());
    }

    private boolean addFile(Path filename) throws IOException {
        final Path path = this.dir.resolve(filename);
        try {
            if (!path.toFile().createNewFile()) {
                return false;
            }
        } catch (IOException e) {
            throw new IOException("creating new file: " + e.getMessage(), e);
        }

        this.files.put(filename, new SynchronisedFile(path.toString()));
        return true;
    }

    // Removes the file from our file list, and from the actual folder.
    private boolean removeFile(Path filename) {
        Validate.isTrue(this.files.containsKey(filename));

        this.files.remove(filename);
        final Path path = this.dir.resolve(filename);
        return path.toFile().delete();
    }

    private void routeInstruction(Instruction inst) throws BadMessageException, IOException, BlockUnavailableException {
        // Check if we got the right message for this part of the update.
        // If this is the start of the update, additionally set the file to be updated.
        if (currentFile == null) {
            if (inst instanceof StartUpdateInstruction) {
                final Path filepath = Paths.get(((StartUpdateInstruction) inst).getFileName());
                this.startUpdate(filepath);
            } else {
                throw new BadMessageException("non-StartUpdateInstruction at start of update");
            }
        } else {
            if (inst instanceof StartUpdateInstruction) {
                throw new BadMessageException("StartUpdateInstruction after start of update");
            }
        }

        // Handle the instruction, ending the update if appropriate.
        this.routeToCurrentFile(inst);
        Log.F("routed Instruction: `" + inst.Type() + "` for " + this.currentFilename);

        if (inst instanceof EndUpdateInstruction) {
            this.endUpdate();
        }
    }

    private void startUpdate(Path filepath) throws BadMessageException {
        this.currentFile = this.files.get(filepath);
        if (this.currentFile == null) {
            throw new BadMessageException("file update for non-existent file: " + filepath);
        }
        this.currentFilename = filepath;
        Log.F("beginning of update for: " + this.currentFilename);
    }

    private void endUpdate() {
        Log.I("File Update: " + this.currentFilename);
        this.currentFile = null;
        this.currentFilename = null;
    }

    private void routeToCurrentFile(Instruction inst) throws IOException, BlockUnavailableException {
        assert(this.currentFile != null);
        try {
            this.currentFile.ProcessInstruction(inst);
        } catch (IOException e) {
            throw new IOException("processing instruction: " + e.getMessage(), e);
        }
    }
}
