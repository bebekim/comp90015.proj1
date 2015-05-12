## comp90015.proj1
Project 1 (file synchronisation service) for the subject
Distributed Systems at the University of Melbourne.
It provides a client and server to ensure that two
directories are kept synchronised.

To be run, the sources should be compiled into two jars using
SyncServer and SyncClient as the main classes.

## SyncServer
SyncServer must be run first, using:

```shell
java -jar syncserver.jar -f <folder> -p <port> [-v]
```

**Flags:**

Flag | Meaning 
----:|:--------
-f   | Specifies the path to the server folder, required. 
-p   | Specified the port to listen on, required.         
-v   | Use verbose logging, optional.


## SyncClient
SyncClient should be run after SyncServer is started, using:

```shell
java -jar syncclient.jar -f <folder> -h <hostname> -p <port> [-v]
```

**Flags:**

Flag | Meaning 
----:|:--------
-f   | Specifies the path to the client folder, required. 
-h   | Specified the hostname of the server to connect to, required.         
-p   | Specified the server port to connect to, required.         
-v   | Use verbose logging, optional.

# FileSync
The project used the FileSync package which was provided
for educational purposes by Professor Aaron Harwood
of the University of Melbourne.
