package com.github.nedp.comp90015.proj1.syncserver;

import com.github.nedp.comp90015.proj1.connection.TCPServer;
import com.github.nedp.comp90015.proj1.connection.message.Authentication;
import com.github.nedp.comp90015.proj1.syncclient.SyncClient;
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
public class SyncServer implements Runnable {
    public final static int EXIT_FAILURE = 1;
    public static final Authentication AUTHENTICATION = new Authentication("this is the server");

    @SuppressWarnings("unused")
    @Option(name="-f",metaVar="<folder path>", usage="the server folder",required=true)
    private String folder;

    @SuppressWarnings({"CanBeFinal", "FieldCanBeLocal"})
    @Option(name="-p",metaVar="<port number>",usage="the server port",required=false)
    private int port = 4444;

    @SuppressWarnings({"CanBeFinal", "FieldCanBeLocal"})
    @Option(name="-d",usage="enable debug mode",required=false,hidden=true)
    private boolean debug = false;

    @SuppressWarnings({"CanBeFinal", "FieldCanBeLocal"})
    @Option(name="-v",usage="use verbose logging",required=false,hidden=false)
    private boolean verbose = false;

    @SuppressWarnings("unused")
    @Option(name="--help",usage="display help message",required=false,help=false)
    private boolean help = false;

    public static void main(String[] args) {
        final SyncServer main = new SyncServer();
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
        Log.C("port: " + this.port);

        // If the folder neither exists, nor can be created, abort.
        final File folder = new File(this.folder);
        if (!(folder.exists() || folder.mkdirs())) {
            Log.S("failed to create specified folder (" + this.folder + "); exiting.");
            System.exit(EXIT_FAILURE);
        }

        // Start with a blank slate, update it based on client specification.
        for (File file : folder.listFiles()) {
            file.delete();
        }

        // Create a TCPServer to handle tcp connections.
        final TCPServer tcp;
        try {
            tcp = new TCPServer(this.port, Paths.get(this.folder), AUTHENTICATION, SyncClient.AUTHENTICATION);
        } catch (IOException e) {
            Log.S("creating tcp server: " + e.getMessage(), e);
            System.exit(EXIT_FAILURE);
            return;
        }
        
        // Don't need to make a new thread, so don't.
        tcp.run();
    }
}
