import java.io.*;
import java.net.*;
import java.util.*;

// main server class for the Bulletin Board System
public class BBoard {
    public static void main(String[] args) {
        // validate command line arguments
        if (args.length < 6) {
            System.err.println("Usage: java BBoard <port> <board_width> <board_height> <note_width> <note_height> <color1> ... <colorN>");
            System.exit(1);
        }
        
        try {
            // parse arguments
            int port = Integer.parseInt(args[0]);
            int boardWidth = Integer.parseInt(args[1]);
            int boardHeight = Integer.parseInt(args[2]);
            int noteWidth = Integer.parseInt(args[3]);
            int noteHeight = Integer.parseInt(args[4]);
            
            // collect colors
            List<String> colors = new ArrayList<>();
            for (int i = 5; i < args.length; i++) {
                colors.add(args[i]);
            }
            
            // validate inputs
            if (port <= 0 || port > 65535) {
                System.err.println("Error: Port must be between 1 and 65535");
                System.exit(1);
            }
            
            if (boardWidth <= 0 || boardHeight <= 0 || noteWidth <= 0 || noteHeight <= 0) {
                System.err.println("Error: Dimensions must be positive");
                System.exit(1);
            }
            
            if (colors.isEmpty()) {
                System.err.println("Error: At least one color must be specified");
                System.exit(1);
            }
            
            // create the shared board
            Board board = new Board(boardWidth, boardHeight, noteWidth, noteHeight, colors);
            
            // start the server
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Bulletin Board Server started on port " + port);
            System.out.println("Board dimensions: " + boardWidth + "x" + boardHeight);
            System.out.println("Note dimensions: " + noteWidth + "x" + noteHeight);
            System.out.println("Valid colors: " + colors);
            System.out.println("Waiting for clients...");
            
            // accept client connections
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getInetAddress());
                    
                    // create and start a new handler thread for this client
                    ClientHandler handler = new ClientHandler(clientSocket, board);
                    handler.start();
                } catch (IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid number format in arguments");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
            System.exit(1);
        }
    }
}
