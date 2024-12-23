import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Client extends JFrame {
    private JTextField messageField;
    private JTextArea messageArea;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private JButton quitButton;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    private String clientId;
    private String coordinatorId;

    public Client(String host, int port) {
        super("Chat Client");

        // Initialise components
        messageField = new JTextField(50);
        messageArea = new JTextArea(16, 50);
        messageArea.setEditable(false);
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);

        // Layout GUI
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(new JScrollPane(messageArea), BorderLayout.CENTER);
        messagePanel.add(messageField, BorderLayout.SOUTH);

        quitButton = new JButton("Quit");
        quitButton.addActionListener(e -> quit());

        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.add(new JScrollPane(userList), BorderLayout.CENTER);
        userPanel.add(quitButton, BorderLayout.SOUTH);

        add(messagePanel, BorderLayout.CENTER);
        add(userPanel, BorderLayout.EAST);

        // Action for messageField
        messageField.addActionListener(e -> {
            out.println(messageField.getText());
            messageField.setText("");
        });

        // Connect to server
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            new ServerListener().start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Unable to connect to the server at " + host + ":" + port,
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Set up the frame
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quit();
            }
        });
        setSize(700, 400);
        setVisible(true);
    }

    private class ServerListener extends Thread {
        public void run() {
            try {
                while (true) {
                    String line = in.readLine();
                    if (line == null)
                        break;
                    if (line.startsWith("COORDINATOR:")) {
                        coordinatorId = line.substring(12);
                        displayMessage("New coordinator is: " + coordinatorId);
                    } else {
                        displayMessage(line);
                    }
                }
            } catch (IOException e) {
                if (!socket.isClosed()) {
                    e.printStackTrace();
                }
            } finally {
                quit();
            }
        }
    }

    private void displayMessage(String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        messageArea.append("[" + timestamp + "] " + message + "\n");
    }

    private void quit() {
        try {
            if (out != null) {
                out.println("/quit");
            }
            if (socket != null) {
                socket.close();
            }
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // server's IP and the port number
        String serverIp = "localhost";
        int serverPort = 12345;
        SwingUtilities.invokeLater(() -> new Client(serverIp, serverPort));
    }
}
