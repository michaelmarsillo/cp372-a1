import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

// GUI client for the Bulletin Board System
public class BulletinBoardClient extends JFrame {
    // connection components
    private JTextField serverField;
    private JTextField portField;
    private JButton connectButton;
    private JButton disconnectButton;
    private JLabel statusLabel;
    
    // POST components
    private JTextField postXField;
    private JTextField postYField;
    private JComboBox<String> postColorCombo;
    private JTextField postMessageField;
    private JButton postButton;
    
    // GET components
    private JComboBox<String> getColorCombo;
    private JTextField getXField;
    private JTextField getYField;
    private JTextField getRefersToField;
    private JButton getNotesButton;
    private JButton getPinsButton;
    
    // PIN/UNPIN components
    private JTextField pinXField;
    private JTextField pinYField;
    private JButton pinButton;
    private JButton unpinButton;
    
    // board action components
    private JButton shakeButton;
    private JButton clearButton;
    
    // output display
    private JTextArea outputArea;
    private JScrollPane scrollPane;
    
    // network components
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Thread readerThread;
    
    // board configuration
    private int boardWidth;
    private int boardHeight;
    private int noteWidth;
    private int noteHeight;
    private String[] colors;
    
    public BulletinBoardClient() {
        setTitle("Bulletin Board Client");
        setSize(700, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // create GUI components
        createConnectionPanel();
        createCommandPanel();
        createOutputPanel();
        
        // initially disable command buttons
        enableCommandButtons(false);
        
        setVisible(true);
    }
    
    private void createConnectionPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Connection"));
        
        panel.add(new JLabel("Server:"));
        serverField = new JTextField("localhost", 12);
        panel.add(serverField);
        
        panel.add(new JLabel("Port:"));
        portField = new JTextField("4554", 6);
        panel.add(portField);
        
        connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> connect());
        panel.add(connectButton);
        
        disconnectButton = new JButton("Disconnect");
        disconnectButton.addActionListener(e -> disconnect());
        disconnectButton.setEnabled(false);
        panel.add(disconnectButton);
        
        statusLabel = new JLabel("Status: Disconnected");
        statusLabel.setForeground(Color.RED);
        panel.add(statusLabel);
        
        add(panel, BorderLayout.NORTH);
    }
    
    private void createCommandPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createTitledBorder("Commands"));
        
        // POST panel
        JPanel postPanel = new JPanel();
        postPanel.setBorder(BorderFactory.createTitledBorder("POST Note"));
        postPanel.add(new JLabel("X:"));
        postXField = new JTextField(5);
        postPanel.add(postXField);
        postPanel.add(new JLabel("Y:"));
        postYField = new JTextField(5);
        postPanel.add(postYField);
        postPanel.add(new JLabel("Color:"));
        postColorCombo = new JComboBox<>();
        postPanel.add(postColorCombo);
        postPanel.add(new JLabel("Message:"));
        postMessageField = new JTextField(20);
        postPanel.add(postMessageField);
        postButton = new JButton("Post Note");
        postButton.addActionListener(e -> postNote());
        postPanel.add(postButton);
        mainPanel.add(postPanel);
        
        // GET panel
        JPanel getPanel = new JPanel();
        getPanel.setBorder(BorderFactory.createTitledBorder("GET Notes"));
        getPanel.add(new JLabel("Color:"));
        getColorCombo = new JComboBox<>();
        getColorCombo.addItem("(any)");
        getPanel.add(getColorCombo);
        getPanel.add(new JLabel("Contains X:"));
        getXField = new JTextField(5);
        getPanel.add(getXField);
        getPanel.add(new JLabel("Y:"));
        getYField = new JTextField(5);
        getPanel.add(getYField);
        getPanel.add(new JLabel("RefersTo:"));
        getRefersToField = new JTextField(10);
        getPanel.add(getRefersToField);
        getNotesButton = new JButton("Get Notes");
        getNotesButton.addActionListener(e -> getNotes());
        getPanel.add(getNotesButton);
        getPinsButton = new JButton("Get Pins");
        getPinsButton.addActionListener(e -> getPins());
        getPanel.add(getPinsButton);
        mainPanel.add(getPanel);
        
        // PIN/UNPIN panel
        JPanel pinPanel = new JPanel();
        pinPanel.setBorder(BorderFactory.createTitledBorder("PIN / UNPIN"));
        pinPanel.add(new JLabel("X:"));
        pinXField = new JTextField(5);
        pinPanel.add(pinXField);
        pinPanel.add(new JLabel("Y:"));
        pinYField = new JTextField(5);
        pinPanel.add(pinYField);
        pinButton = new JButton("Pin");
        pinButton.addActionListener(e -> addPin());
        pinPanel.add(pinButton);
        unpinButton = new JButton("Unpin");
        unpinButton.addActionListener(e -> removePin());
        pinPanel.add(unpinButton);
        mainPanel.add(pinPanel);
        
        // board actions panel
        JPanel actionPanel = new JPanel();
        actionPanel.setBorder(BorderFactory.createTitledBorder("Board Actions"));
        shakeButton = new JButton("Shake");
        shakeButton.addActionListener(e -> shake());
        actionPanel.add(shakeButton);
        clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clear());
        actionPanel.add(clearButton);
        mainPanel.add(actionPanel);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private void createOutputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Output"));
        
        outputArea = new JTextArea(15, 60);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        scrollPane = new JScrollPane(outputArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        add(panel, BorderLayout.SOUTH);
    }
    
    private void connect() {
        String server = serverField.getText().trim();
        String portStr = portField.getText().trim();
        
        if (server.isEmpty() || portStr.isEmpty()) {
            appendOutput("Error: Server and port cannot be empty\n");
            return;
        }
        
        try {
            int port = Integer.parseInt(portStr);
            socket = new Socket(server, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            appendOutput("Connected to " + server + ":" + port + "\n");
            statusLabel.setText("Status: Connected");
            statusLabel.setForeground(Color.GREEN);
            
            connectButton.setEnabled(false);
            disconnectButton.setEnabled(true);
            
            // start reader thread
            readerThread = new Thread(() -> readServerMessages());
            readerThread.start();
            
        } catch (NumberFormatException e) {
            appendOutput("Error: Invalid port number\n");
        } catch (IOException e) {
            appendOutput("Error connecting to server: " + e.getMessage() + "\n");
        }
    }
    
    private void disconnect() {
        try {
            if (out != null) {
                out.println("DISCONNECT");
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            appendOutput("Disconnected from server\n");
            statusLabel.setText("Status: Disconnected");
            statusLabel.setForeground(Color.RED);
            
            connectButton.setEnabled(true);
            disconnectButton.setEnabled(false);
            enableCommandButtons(false);
            
        } catch (IOException e) {
            appendOutput("Error disconnecting: " + e.getMessage() + "\n");
        }
    }
    
    private void readServerMessages() {
        try {
            String line;
            boolean handshakeComplete = false;
            
            while ((line = in.readLine()) != null) {
                final String message = line;
                SwingUtilities.invokeLater(() -> appendOutput("< " + message + "\n"));
                
                // parse handshake
                if (!handshakeComplete && message.startsWith("BOARD")) {
                    String[] parts = message.split("\\s+");
                    boardWidth = Integer.parseInt(parts[1]);
                    boardHeight = Integer.parseInt(parts[2]);
                } else if (!handshakeComplete && message.startsWith("NOTE")) {
                    String[] parts = message.split("\\s+");
                    noteWidth = Integer.parseInt(parts[1]);
                    noteHeight = Integer.parseInt(parts[2]);
                } else if (!handshakeComplete && message.startsWith("COLOURS")) {
                    String[] parts = message.split("\\s+");
                    int colorCount = Integer.parseInt(parts[1]);
                    colors = new String[colorCount];
                    for (int i = 0; i < colorCount; i++) {
                        colors[i] = parts[i + 2];
                    }
                    
                    // update color dropdowns
                    SwingUtilities.invokeLater(() -> {
                        postColorCombo.removeAllItems();
                        getColorCombo.removeAllItems();
                        getColorCombo.addItem("(any)");
                        for (String color : colors) {
                            postColorCombo.addItem(color);
                            getColorCombo.addItem(color);
                        }
                        enableCommandButtons(true);
                    });
                    
                    handshakeComplete = true;
                }
            }
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> {
                appendOutput("Connection lost\n");
                statusLabel.setText("Status: Disconnected");
                statusLabel.setForeground(Color.RED);
                connectButton.setEnabled(true);
                disconnectButton.setEnabled(false);
                enableCommandButtons(false);
            });
        }
    }
    
    private void postNote() {
        String xStr = postXField.getText().trim();
        String yStr = postYField.getText().trim();
        String color = (String) postColorCombo.getSelectedItem();
        String message = postMessageField.getText();
        
        // validate
        if (xStr.isEmpty() || yStr.isEmpty()) {
            appendOutput("Error: X and Y coordinates required\n");
            return;
        }
        
        if (color == null || color.isEmpty()) {
            appendOutput("Error: Color must be selected\n");
            return;
        }
        
        if (message.isEmpty()) {
            appendOutput("Error: Message cannot be empty\n");
            return;
        }
        
        try {
            int x = Integer.parseInt(xStr);
            int y = Integer.parseInt(yStr);
            
            if (x < 0 || y < 0) {
                appendOutput("Error: Coordinates must be non-negative\n");
                return;
            }
            
            String command = "POST " + x + " " + y + " " + color + " " + message;
            sendCommand(command);
            
        } catch (NumberFormatException e) {
            appendOutput("Error: Coordinates must be integers\n");
        }
    }
    
    private void getNotes() {
        StringBuilder command = new StringBuilder("GET");
        
        // add color filter
        String color = (String) getColorCombo.getSelectedItem();
        if (color != null && !color.equals("(any)")) {
            command.append(" color=").append(color);
        }
        
        // add contains filter
        String xStr = getXField.getText().trim();
        String yStr = getYField.getText().trim();
        if (!xStr.isEmpty() && !yStr.isEmpty()) {
            try {
                int x = Integer.parseInt(xStr);
                int y = Integer.parseInt(yStr);
                command.append(" contains=").append(x).append(" ").append(y);
            } catch (NumberFormatException e) {
                appendOutput("Error: Contains coordinates must be integers\n");
                return;
            }
        }
        
        // add refersTo filter
        String refersTo = getRefersToField.getText().trim();
        if (!refersTo.isEmpty()) {
            command.append(" refersTo=").append(refersTo);
        }
        
        sendCommand(command.toString());
    }
    
    private void getPins() {
        sendCommand("GET PINS");
    }
    
    private void addPin() {
        String xStr = pinXField.getText().trim();
        String yStr = pinYField.getText().trim();
        
        if (xStr.isEmpty() || yStr.isEmpty()) {
            appendOutput("Error: X and Y coordinates required\n");
            return;
        }
        
        try {
            int x = Integer.parseInt(xStr);
            int y = Integer.parseInt(yStr);
            
            if (x < 0 || y < 0) {
                appendOutput("Error: Coordinates must be non-negative\n");
                return;
            }
            
            sendCommand("PIN " + x + " " + y);
            
        } catch (NumberFormatException e) {
            appendOutput("Error: Coordinates must be integers\n");
        }
    }
    
    private void removePin() {
        String xStr = pinXField.getText().trim();
        String yStr = pinYField.getText().trim();
        
        if (xStr.isEmpty() || yStr.isEmpty()) {
            appendOutput("Error: X and Y coordinates required\n");
            return;
        }
        
        try {
            int x = Integer.parseInt(xStr);
            int y = Integer.parseInt(yStr);
            
            if (x < 0 || y < 0) {
                appendOutput("Error: Coordinates must be non-negative\n");
                return;
            }
            
            sendCommand("UNPIN " + x + " " + y);
            
        } catch (NumberFormatException e) {
            appendOutput("Error: Coordinates must be integers\n");
        }
    }
    
    private void shake() {
        sendCommand("SHAKE");
    }
    
    private void clear() {
        sendCommand("CLEAR");
    }
    
    private void sendCommand(String command) {
        if (out == null) {
            appendOutput("Error: Not connected to server\n");
            return;
        }
        
        appendOutput("> " + command + "\n");
        out.println(command);
    }
    
    private void appendOutput(String text) {
        SwingUtilities.invokeLater(() -> {
            outputArea.append(text);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        });
    }
    
    private void enableCommandButtons(boolean enabled) {
        postButton.setEnabled(enabled);
        getNotesButton.setEnabled(enabled);
        getPinsButton.setEnabled(enabled);
        pinButton.setEnabled(enabled);
        unpinButton.setEnabled(enabled);
        shakeButton.setEnabled(enabled);
        clearButton.setEnabled(enabled);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BulletinBoardClient());
    }
}
