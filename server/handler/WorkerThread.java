package server.handler;
import server.statistics.*;
import server.request.*;
import server.computation.DataComputation;
import server.exception.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

// Each WorkerThread represents a connection with a client
public class WorkerThread implements Runnable {
    // Assume that stats is shared across all worker threads
    private static ServerStatistics stats = new ServerStatistics();
    private static final Logger LOGGER = Logger.getLogger(WorkerThread.class.getName());

    private Socket clientSocket;
    private RequestParser reqParser = new RequestParser();
    private DataComputation dataComp;
    public WorkerThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
        LOGGER.log(Level.INFO, "Client connected: " + clientSocket.getInetAddress());
    }

    @Override
    public void run() {
        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

            String requestString;
            String statRequest;

            // Continue reading from the client until the client closes the connection
            while ((requestString = in.readLine()) != null) {
                // Instantiate a new DataComputation object for each request
                dataComp=new DataComputation();
                long startTime = System.currentTimeMillis();

                try {
                    // Check if the client wants to end the connection
                    if (requestString.startsWith("BYE")) {
                        statRequest = reqParser.parseQuitRequest(requestString);
                        if (statRequest == null)
                            break; // Breaks the loop and ends the connection.
                    }
                    // Check if the client is requesting server stats
                    else if (requestString.startsWith("STAT")) {
                        statRequest = reqParser.parseStatRequest(requestString,stats);
                    }
                    // Else, the client is requesting a computation
                    else {
                        ComputationRequest compRequest = reqParser.parseComputationRequest(requestString);
                        String[] variables = compRequest.getVariable().split(",");
                        for (String variable : variables) {
                            dataComp.addVariableRange(variable);
                        }
                        statRequest = dataComp.computeExpression(compRequest.getExpression(),compRequest.getComputationKind(), compRequest.getValuesKind());
                    }

                    // If an error occurred during the computation, send the error message to the client
                    if (statRequest.startsWith("ERR;")) {
                        // If there was a computation error, send the error message back to the client.
                        out.println(statRequest);
                    }
                    else{
                        // Calculate the processing time and update the server stats
                        long processingTime = System.currentTimeMillis() - startTime;
                        stats.updateStats(processingTime);

                        // Send a success message to the client
                        out.println("OK;" + processingTime / 1000.0 + ";"+statRequest);
                    }
                }
                // If an exception occurred while parsing the request or performing the computation, send the error message to the client
                catch (InvalidRequestException | InvalidVariableRangeException e) {
                    out.println(e.getMessage());
                }
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Worker thread error: ", e);
        } finally {
            // Always attempt to close the client socket when finished
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                    LOGGER.log(Level.INFO, "Client disconnected: " + clientSocket.getInetAddress());
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Could not close client socket: ", e);
            }
        }
    }
}
