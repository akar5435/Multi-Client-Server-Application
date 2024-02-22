import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int PORT = 49152;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_IP, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Connected to the server.");
            new Thread(() -> {
                try {
                    String fromServer;
                    while ((fromServer = in.readLine()) != null) {
                        System.out.println(fromServer);
                    }
                } catch (IOException e) {
                    System.out.println("Server connection lost.");
                }
            }).start();

            Scanner scanner = new Scanner(System.in);
            while (true) {
                String userInput = scanner.nextLine();
                if ("quit".equalsIgnoreCase(userInput.trim())) {
                    out.println("QUIT");
                    break;
                }
                out.println(userInput);
            }
        } catch (IOException e) {
            System.err.println("Cannot connect to server at " + SERVER_IP + ":" + PORT);
            e.printStackTrace();
        }
    }
}
