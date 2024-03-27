package server.request;

import server.exception.*;
import server.statistics.*;

// The RequestParser class, which is responsible for parsing different types of requests from clients
public class RequestParser {

    // This method handles the case where the client requests to quit
    public String parseQuitRequest(String request) throws InvalidRequestException {
        // If the request is "BYE", we return null to indicate the client wishes to end the session
        if (request.equals("BYE")) {
            return null;
        } else {
            // If the request does not match the expected format (there's something else after 'BYE'), throw an InvalidRequestException
            throw new InvalidRequestException("ERR; (InvalidRequestException) Invalid QuitRequest format.");
        }
    }

    // This method handles the case where the client requests statistics from the server
    public String parseStatRequest(String request, ServerStatistics stats) throws InvalidRequestException {
        // Check if the request matches the StatRequest format.
        // Depending on the format, we return the appropriate statistic
        if (request.equals("STAT_REQS")){
            return stats.getRequestCount();
        }
        else if (request.equals("STAT_AVG_TIME")){
            return stats.getAvgProcessingTime();
        }
        else if (request.equals("STAT_MAX_TIME")) {
            return stats.getMaxProcessingTime();
        } else {
            // If the request does not match any of the expected formats, throw an InvalidRequestException
            throw new InvalidRequestException("ERR; (InvalidRequestException) Invalid StatRequest format.");
        }
    }

    // This method handles the case where the client requests a computation
    public ComputationRequest parseComputationRequest(String request) throws InvalidRequestException {
        // Split the request into its components
        String[] components = request.split(";",3);
        String[] operations = components[0].split("_");
        // Validate the request, checking that it has enough components and that the operation and data type are valid
        if (components.length<3){
            throw new InvalidRequestException("ERR; (InvalidRequestException) Request parts are less than 3");
        }
        else if ((operations[0].equals("MIN") || operations[0].equals("MAX")
                || operations[0].equals("AVG") || operations[0].equals("COUNT"))
                && (operations[1].equals("GRID") || operations[1].equals("LIST"))) {
                return new ComputationRequest(operations[1], operations[0], components[1], components[2]);
        }
        else{
            // If the request is not valid, throw an InvalidRequestException
            throw new InvalidRequestException("ERR; (InvalidRequestException) Invalid computation request format.");
        }
    }
}


