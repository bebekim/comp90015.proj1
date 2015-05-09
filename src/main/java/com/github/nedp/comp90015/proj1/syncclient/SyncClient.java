package com.github.nedp.comp90015.proj1.syncclient;

import com.github.nedp.comp90015.proj1.connection.ConnectionException;
import com.github.nedp.comp90015.proj1.connection.TCPClient;
import com.github.nedp.comp90015.proj1.connection.message.Authentication;
import com.github.nedp.comp90015.proj1.connection.message.BadMessageException;
import com.github.nedp.comp90015.proj1.syncserver.SyncServer;
import com.github.nedp.comp90015.proj1.util.Log;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Level;

/**
 * Created by nedp on 06/04/15.
 */
public class SyncClient implements Runnable {
    final static int EXIT_SUCCESS = 0;
    public final static int EXIT_FAILURE = 1;
    public static final Authentication AUTHENTICATION = new Authentication("this is the client");

    @Option(name="-f",metaVar="<folder path>", usage="the client folder",required=true)
    private String folder;

    @Option(name="-h",metaVar="<ip address>",usage="the server hostname",required=true)
    private String hostname;

    @SuppressWarnings({"CanBeFinal", "FieldCanBeLocal"})
    @Option(name="-p",metaVar="<port number>",usage="the server port",required=false)
    private int port = 4444;

    @SuppressWarnings({"CanBeFinal", "FieldCanBeLocal"})
    @Option(name="-d",usage="enable debug mode (overrides -v)",required=false,hidden=true)
    private boolean debug = false;

    @SuppressWarnings({"CanBeFinal", "FieldCanBeLocal"})
    @Option(name="-v",usage="use verbose logging",required=false,hidden=false)
    private boolean verbose = false;

    @Option(name="--help",usage="display help message",required=false,help=false)
    private boolean help = false;

    public static void main(String[] args) {
        final SyncClient main = new SyncClient();
        try {
            new CmdLineParser(main).parseArgument(args);
        } catch (CmdLineException e) {
            e.printStackTrace();
            System.exit(EXIT_FAILURE);
        }
        main.run();
    }

    @Override
    public void run() {
        if (this.debug) {
            Log.SetLevel(Level.ALL);
            Log.C("debug mode");
        } else if (this.verbose) {
            Log.SetLevel(Level.CONFIG);
            Log.C("verbose mode");
        } else {
            Log.SetLevel(Level.WARNING);
        }
        Log.C("folder: " + this.folder);
        Log.C("hostname: " + this.hostname);
        Log.C("port: " + this.port);

        // If the folder neither exists, nor can be created, abort.
        final File folder = new File(this.folder);
        if (!(folder.exists() || folder.mkdirs())) {
            Log.S("failed to create specified folder (" + this.folder + "); exiting.");
            System.exit(EXIT_FAILURE);
            return;
        }

        // Create a TCPClient to handle tcp connections.
        final TCPClient tcp;
        try {
            tcp = new TCPClient(this.hostname, this.port,
                    AUTHENTICATION, SyncServer.AUTHENTICATION);
        } catch (ConnectionException e) {
            Log.S("creating tcp client: " + e.getMessage(), e);
            System.exit(EXIT_FAILURE);
            return;
        }

        // Monitor the folderTracker.
        final FolderTracker folderTracker;
        try {
            folderTracker = new FolderTracker(Paths.get(this.folder), tcp);
        } catch (IOException | BadMessageException | InterruptedException e) {
            Log.S("creating folder monitor: " + e.getMessage(), e);
            System.exit(EXIT_FAILURE);
            return;
        }

        // Don't need to create a new thread, so don't create one.
        folderTracker.run();
    }
}
