import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ClientGUI extends JFrame implements ActionListener {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int PORT = 49152;

    private JTextField messageField;
    private JTextArea chatArea;
    private JButton sendButton;
    private Socket socket;
    private PrintWriter writer;

    public ClientGUI() {
        super("Chat Client");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        messageField = new JTextField(20);
        sendButton = new JButton("Send");
        sendButton.addActionListener(this);
        bottomPanel.add(messageField);
        bottomPanel.add(sendButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        add(panel);
        setVisible(true);

        connectToServer();
    }

    private void connectToServer() {
        try {
            socket = new Socket(SERVER_IP, PORT);
            writer = new PrintWriter(socket.getOutputStream(), true);
            new Thread(new ServerListener()).start();
            chatArea.append("Connected to the server.\n");
        } catch (IOException e) {
            chatArea.append("Error connecting to server: " + e.getMessage() + "\n");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == sendButton) {
            sendMessage();
        }
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            writer.println(message);
            messageField.setText("");
        }
    }

    private class ServerListener implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message;
                while ((message = reader.readLine()) != null) {
                    chatArea.append(message + "\n");
                }
            } catch (IOException e) {
                chatArea.append("Server connection lost.\n");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }
}
