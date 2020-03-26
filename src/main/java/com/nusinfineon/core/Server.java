package com.nusinfineon.core;

// A Java program for a Server
import java.net.*;
import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Server {

    private static final Logger LOGGER = Logger.getLogger(RunCore.class.getName());

    //initialize socket and input stream
    private Socket socket = null;
    private ServerSocket server = null;
    private DataInputStream in = null;
    private int port;
    // constructor with port
    public Server(int port) {
        this.port = port;

    }

    public void checkForConnection() {
        try {
            // starts server and waits for a connection
            server = new ServerSocket(port);
            LOGGER.info("Server started");

            LOGGER.info("Waiting for Flexsim to finish and connect ...");

            socket = server.accept();
            LOGGER.info("Flexsim Connected , starting next run");

            // close connection
            socket.close();
            server.close();
            TimeUnit.SECONDS.sleep(5);
        } catch(IOException | InterruptedException i) {
            System.out.println(i);
        }
    }
}
