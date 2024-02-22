import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    private static final int PORT = 49152;
    private static final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private static String coordinatorID = null;
    private static final AtomicInteger ID_COUNTER = new AtomicInteger(100000);

    public static void main(String[] args) throws IOException {
        System.out.println("Server is listening on port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandler.start();
            }
        }
    }

    static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private String clientId;
        private final String clientUniqueAddress;

        ClientHandler(Socket socket) {
            this.socket = socket;
            this.clientId = String.valueOf(ID_COUNTER.getAndIncrement());
            this.clientUniqueAddress = socket.getInetAddress().getHostAddress();
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                if (coordinatorID == null) {
                    coordinatorID = clientId;
                    out.println("COORDINATOR " + clientId);
                } else {
                    out.println("MEMBER " + clientId + " COORDINATOR " + coordinatorID);
                }

                broadcastMessage("SERVER: Client " + clientId + " (Address: " + clientUniqueAddress + ") joined as " + (coordinatorID.equals(clientId) ? "COORDINATOR" : "member"));
                clients.put(clientId, this);

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if ("QUIT".equalsIgnoreCase(inputLine.trim())) {
                        this.socket.close();
                        break;
                    }
                    broadcastMessage(clientId + ": " + inputLine);
                }
            } catch (IOException e) {
                System.out.println("Exception handling client #" + clientId + ": " + e.getMessage());
            } finally {
                handleClientExit();
            }
        }

        private void broadcastMessage(String message) {
            for (ClientHandler client : clients.values()) {
                client.out.println(message);
            }
        }

        private void handleClientExit() {
            clients.remove(clientId);
            broadcastMessage("SERVER: Client " + clientId + " has left.");
            if (clientId.equals(coordinatorID)) {
                if (!clients.isEmpty()) {
                    coordinatorID = clients.values().iterator().next().clientId;
                    clients.get(coordinatorID).out.println("You are the new COORDINATOR");
                    broadcastMessage("SERVER: New coordinator is Client " + coordinatorID);
                } else {
                    coordinatorID = null;
                }
            }
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing the socket for client " + clientId);
            }
        }
    }
}
