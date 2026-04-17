import java.io.*;
import java.net.*;
import java.util.*;

public class MathClient {
    private static final int PORT = 9999;

    // Set to true to run automated demo mode (no user input needed, single machine)
    // Set to false for interactive mode (user types expressions manually) across multiple devices on the same network
    private static final boolean DEMO_MODE = true;

    public static void main(String[] args) {
        String clientName;
        String host;

        try {
            // Get server ip and username either from args or input
            Scanner sc = new Scanner(System.in);

            if (args.length > 0) {
                host = args[0];
            } else {
                System.out.print("Enter server IP address: ");
                host = sc.nextLine().trim();
            }

            if (args.length > 1) {
                clientName = args[1];
            } else {
                System.out.print("Enter your name: ");
                clientName = sc.nextLine().trim();
            }

            // Establish connection to server
            Socket socket = new Socket(host, PORT);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(
                    socket.getOutputStream(), true);

            System.out.println("[CLIENT] Connected to server.");

            // Step 1: Send JOIN request
            String joinMsg = "JOIN:" + clientName;
            System.out.println("[CLIENT] Sending: " + joinMsg);
            out.println(joinMsg);

            // Wait for ACK
            String ack = in.readLine();
            if (ack == null || !ack.startsWith("ACK:JOINED")) {
                System.out.println("[CLIENT] Join failed: " + ack);
                socket.close();
                sc.close();
                return; //Exit and close all sockets/scanners
            }

            System.out.println("[CLIENT] Server response: " + ack);

            // Step 2: Send requests: interactive or demo depending on DEMO_MODE
            if (DEMO_MODE) {
                // Automated demo mode: sends 5 random expressions with random delays
                // Used for default testing
                String[] expressions = {
                    "10 + 25",
                    "100 / 4",
                    "7 * 8",
                    "50 - 13",
                    "2 / 0"
                };

                // Choosing random times for sending CALC request to server
                Random rand = new Random();

                for (int i = 0; i < 3; i++) {
                    String expr = expressions[rand.nextInt(expressions.length)];

                    int delay = 1000 + rand.nextInt(3000);
                    Thread.sleep(delay);

                    String calcMsg = "CALC:" + expr;
                    System.out.println("[CLIENT] Sending: " + calcMsg);
                    out.println(calcMsg);

                    String response = in.readLine();
                    System.out.println("[CLIENT] Received: " + response);
                }

                System.out.println("[CLIENT] Sending: QUIT");
                out.println("QUIT");

            } else {
                // Interactive mode: user types expressions manually
                System.out.println("[CLIENT] You can now send math expressions.");
                System.out.println("[CLIENT] Supported operators are: + - * / ()");
                System.out.println("[CLIENT] Ex: 5 + 3 | (2 + 3 * 7) | 10 / 3 * (2 + 4)");
                System.out.println("[CLIENT] Type 'quit' to disconnect\n");

                //Constantly check for user input for quit or request and outputing server request
                while (true) {
                    System.out.print("[" + clientName + "] > ");
                    String input = sc.nextLine().trim();

                    if (input.equalsIgnoreCase("quit")) {
                        System.out.println("[CLIENT] Sending: QUIT");
                        out.println("QUIT");
                        break;
                    }

                    if (input.isEmpty()) continue;

                    String calcMsg = "CALC:" + input;
                    System.out.println("[CLIENT] Sending: " + calcMsg);
                    out.println(calcMsg);

                    String response = in.readLine();
                    System.out.println("[CLIENT] Received: " + response);
                }
            }

            // Clean up resources
            socket.close();
            sc.close();
            System.out.println("[CLIENT] Disconnected.");

        } catch (Exception e) {
            // Display error, for example, server not found
            System.out.println("[CLIENT ERROR] " + e.getMessage());
        }
    }
}
