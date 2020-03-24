package com.nusinfineon.core;

// A Java program for a Server
import java.net.*;
import java.io.*;
import java.util.concurrent.TimeUnit;

public class Server
{
    //initialize socket and input stream
    private Socket          socket   = null;
    private ServerSocket    server   = null;
    private DataInputStream in       =  null;

    // constructor with port
    public Server(int port)
    {
        // starts server and waits for a connection
        try
        {
            server = new ServerSocket(port);
            System.out.println("Server started");

            System.out.println("Waiting for a client ...");

            socket = server.accept();
            System.out.println("Client accepted");

            // close connection
            socket.close();
            server.close();
            TimeUnit.SECONDS.sleep(10);
        }
        catch(IOException | InterruptedException i)
        {
            System.out.println(i);
        }
    }

}