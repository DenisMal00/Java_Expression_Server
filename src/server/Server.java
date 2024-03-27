package server;
import server.handler.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    // The port the server will listen on
    private int port;

    // The ServerSocket object that will accept client connections
    private ServerSocket serverSocket;

    // The thread pool that will manage WorkerThreads
    private ExecutorService threadPool;

    // The number of processors available on the system
    private int numberOfProcessors;

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    public Server(int port) {
        this.port = port;
        // Get the number of available processors
        this.numberOfProcessors = Runtime.getRuntime().availableProcessors();
        // Initialize the thread pool with a number of threads equal to the number of processors
        this.threadPool = Executors.newFixedThreadPool(numberOfProcessors);
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);

            // The server loop, runs indefinitely until the server is stopped
            while (true) {
                // Accept a new client connection
                Socket clientSocket = serverSocket.accept();
                // Create a new WorkerThread to handle the client's requests
                WorkerThread worker = new WorkerThread(clientSocket);
                // Pass the WorkerThread to the thread pool for execution
                threadPool.execute(worker);
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error in server: ", e);
        } finally {
            // Always attempt to close the ServerSocket when finished
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
               LOGGER.log(Level.SEVERE,"Could not close server socket: " + e.getMessage());
            }
        }
    }
}

