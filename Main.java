package server;

import java.sql.SQLOutput;

public class Main {
    public static void main(String[] args) {
        server.Server server = new server.Server(Integer.parseInt(args[0]));
        server.start();
    }
}
