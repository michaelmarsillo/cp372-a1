import java.io.*;
import java.net.*;

// handles communication with a single client
public class ClientHandler extends Thread {
    private Socket socket;
    private Board board;
    private BufferedReader in;
    private PrintWriter out;
    
    public ClientHandler(Socket socket, Board board) {
        this.socket = socket;
        this.board = board;
    }
    
    @Override
    public void run() {
        try {
            // setup streams
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            // send handshake
            sendHandshake();
            
            // process commands
            String command;
            while ((command = in.readLine()) != null) {
                command = command.trim();
                if (command.isEmpty()) {
                    continue;
                }
                
                String response = processCommand(command);
                out.println(response);
                
                // check if client disconnected
                if (command.equals("DISCONNECT")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + socket.getInetAddress());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
    
    // send initial handshake to client
    private void sendHandshake() {
        out.println("OK HANDSHAKE");
        out.println("BOARD " + board.getBoardWidth() + " " + board.getBoardHeight());
        out.println("NOTE " + board.getNoteWidth() + " " + board.getNoteHeight());
        
        // build colors line
        StringBuilder colorsLine = new StringBuilder("COLOURS ");
        colorsLine.append(board.getValidColors().size());
        for (String color : board.getValidColors()) {
            colorsLine.append(" ").append(color);
        }
        out.println(colorsLine.toString());
    }
    
    // process a command from the client
    private String processCommand(String command) {
        String[] parts = command.split("\\s+");
        
        if (parts.length == 0) {
            return "ERROR INVALID_FORMAT Empty command";
        }
        
        String cmd = parts[0];
        
        try {
            switch (cmd) {
                case "POST":
                    return handlePost(command);
                    
                case "GET":
                    return handleGet(command);
                    
                case "PIN":
                    return handlePin(parts);
                    
                case "UNPIN":
                    return handleUnpin(parts);
                    
                case "SHAKE":
                    return board.shake();
                    
                case "CLEAR":
                    return board.clear();
                    
                case "DISCONNECT":
                    return "OK DISCONNECTED";
                    
                default:
                    return "ERROR INVALID_FORMAT Unknown command: " + cmd;
            }
        } catch (Exception e) {
            return "ERROR INVALID_FORMAT " + e.getMessage();
        }
    }
    
    // handle POST command
    private String handlePost(String command) {
        // parse: POST x y color message
        String[] parts = command.split("\\s+", 5);
        
        if (parts.length < 5) {
            return "ERROR INVALID_FORMAT POST requires coordinates, color, and message";
        }
        
        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            String color = parts[3];
            String message = parts[4];
            
            return board.postNote(x, y, color, message);
        } catch (NumberFormatException e) {
            return "ERROR INVALID_FORMAT Coordinates must be integers";
        }
    }
    
    // handle GET command
    private String handleGet(String command) {
        String[] parts = command.split("\\s+");
        
        // check for GET PINS
        if (parts.length == 2 && parts[1].equals("PINS")) {
            return board.getPins();
        }
        
        // parse filtered GET
        String colorFilter = null;
        Integer containsX = null;
        Integer containsY = null;
        String refersToFilter = null;
        
        // simple approach: parse the command string
        String remaining = command.substring(3).trim(); // Remove "GET"
        
        if (remaining.isEmpty()) {
            // GET with no filters - return all notes
            return board.getNotes(null, null, null, null);
        }
        
        // parse filters in order: color, contains, refersTo
        String[] filters = remaining.split("\\s+");
        int i = 0;
        
        while (i < filters.length) {
            String filter = filters[i];
            
            if (filter.startsWith("color=")) {
                colorFilter = filter.substring(6);
                i++;
            } else if (filter.startsWith("colour=")) {
                colorFilter = filter.substring(7);
                i++;
            } else if (filter.equals("contains") || filter.startsWith("contains=")) {
                // handle both "contains x y" and "contains=x"
                if (filter.equals("contains")) {
                    if (i + 2 < filters.length) {
                        try {
                            containsX = Integer.parseInt(filters[i + 1]);
                            containsY = Integer.parseInt(filters[i + 2]);
                            i += 3;
                        } catch (NumberFormatException e) {
                            return "ERROR INVALID_FORMAT contains requires two integer coordinates";
                        }
                    } else {
                        return "ERROR INVALID_FORMAT contains requires two coordinates";
                    }
                } else {
                    // contains=x format
                    String coords = filter.substring(9);
                    if (i + 1 < filters.length) {
                        try {
                            containsX = Integer.parseInt(coords);
                            containsY = Integer.parseInt(filters[i + 1]);
                            i += 2;
                        } catch (NumberFormatException e) {
                            return "ERROR INVALID_FORMAT contains requires two integer coordinates";
                        }
                    } else {
                        return "ERROR INVALID_FORMAT contains requires two coordinates";
                    }
                }
            } else if (filter.startsWith("refersTo=")) {
                refersToFilter = filter.substring(9);
                // refersTo might have spaces, collect rest of string
                StringBuilder sb = new StringBuilder(refersToFilter);
                for (int j = i + 1; j < filters.length; j++) {
                    sb.append(" ").append(filters[j]);
                }
                refersToFilter = sb.toString();
                break;
            } else {
                return "ERROR INVALID_FORMAT Unknown filter: " + filter;
            }
        }
        
        return board.getNotes(colorFilter, containsX, containsY, refersToFilter);
    }
    
    // handle PIN command
    private String handlePin(String[] parts) {
        if (parts.length < 3) {
            return "ERROR INVALID_FORMAT PIN requires x and y coordinates";
        }
        
        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            return board.addPin(x, y);
        } catch (NumberFormatException e) {
            return "ERROR INVALID_FORMAT Coordinates must be integers";
        }
    }
    
    // handle UNPIN command
    private String handleUnpin(String[] parts) {
        if (parts.length < 3) {
            return "ERROR INVALID_FORMAT UNPIN requires x and y coordinates";
        }
        
        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            return board.removePin(x, y);
        } catch (NumberFormatException e) {
            return "ERROR INVALID_FORMAT Coordinates must be integers";
        }
    }
}
