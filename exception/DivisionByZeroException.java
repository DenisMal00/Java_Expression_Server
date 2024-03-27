package server.exception;

public class DivisionByZeroException extends ComputationException{

    public DivisionByZeroException(String message) {
        super(message);
    }
}

