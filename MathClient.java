import java.io.*;
import java.net.*;
import java.util.*;

public class MathClient {
    // Port number the server is listening on
    private static final int PORT = 9999;

    public static void main(String[] args) {
        String clientName; // Stores the user's name
        String host;       // Stores the server IP/hostname

        try {
            // Scanner to read user input from console
            Scanner sc = new Scanner(System.in);

            // Get server IP address (from args or prompt user)
            if (args.length > 0) {
                host = args[0]; // Use command-line argument
            } else {
                System.out.print("Enter server IP address: ");
                host = sc.nextLine().trim(); // Read and clean input
            }

            // Get client name (from args or prompt user)
            if (args.length > 1) {
                clientName = args[1];
            } else {
                System.out.print("Enter your name: ");
                clientName = sc.nextLine().trim();
            }

            // Establish connection to the server
            Socket socket = new Socket(host, PORT);

            // Input stream (from server)
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            // Output stream (to server)
            PrintWriter out = new PrintWriter(
                    socket.getOutputStream(), true);

            System.out.println("[CLIENT] Connected to server.");

            // Step 1: Send JOIN request
            String joinMsg = "JOIN:" + clientName;
            System.out.println("[CLIENT] Sending: " + joinMsg);
            out.println(joinMsg);

            // Wait for server acknowledgment
            String ack = in.readLine();

            // Validate server response
            if (ack == null || !ack.startsWith("ACK:JOINED")) {
                System.out.println("[CLIENT] Join failed: " + ack);
                socket.close();
                return; // Exit program if join fails
            }

            System.out.println("[CLIENT] Server response: " + ack);

            // Instructions for the user
            System.out.println("\n[CLIENT] You can now send math expressions.\n " + 
                    "[CLIENT] Supported operators are: + - * / ()\n" + 
                    "[CLIENT] Ex: 5 + 3 | (2 + 3 * 7) | 10 / 3 * (2 + 4)\n" + 
                    "[CLIENT] Type 'quit' to disconnect\n");

            // Main loop: send math expressions to server
            while (true) {
                // Prompt user
                System.out.print("[" + clientName + "] > ");
                String input = sc.nextLine().trim();

                // Check for quit command
                if (input.matches("quit")) {
                    System.out.println("[CLIENT] Sending: QUIT");
                    out.println("QUIT");
                    break; // Exit loop
                }

                // Ignore empty input
                if (input.isEmpty()) continue;

                // Send calculation request
                String calcMsg = "CALC:" + input;
                System.out.println("[CLIENT] Sending: " + calcMsg);
                out.println(calcMsg);

                // Read and display server response
                String response = in.readLine();
                System.out.println("[CLIENT] Received: " + response);
            }

            /*
            // Sends random math expressions with delays

            String[] expressions = {
                "10 + 25",
                "100 / 4",
                "7 * 8",
                "50 - 13",
                "2 + 2"
            };

            Random rand = new Random();

            for (int i = 0; i < 3; i++) {  // send at least 3 requests
                String expr = expressions[rand.nextInt(expressions.length)];

                // Random delay between 1–4 seconds
                int delay = 1000 + rand.nextInt(3000);
                Thread.sleep(delay);

                String calcMsg = "CALC:" + expr;
                System.out.println("[CLIENT] Sending: " + calcMsg);
                out.println(calcMsg);

                String response = in.readLine();
                System.out.println("[CLIENT] Received: " + response);
            }

            // Send quit command
            System.out.println("[CLIENT] Sending: QUIT");
            out.println("QUIT");
            */

            // Clean up resources
            socket.close();
            sc.close();
            System.out.println("[CLIENT] Disconnected.");

        } catch (Exception e) {
            // Catch and display any errors
            System.out.println("[CLIENT ERROR] " + e.getMessage());
        }
    }
}
