import java.io.*;
import java.net.*;
import java.util.*;

public class MathClient {
    private static final int PORT = 9999;

    public static void main(String[] args) {
        String clientName;
        String host;

        try {
            Scanner sc = new Scanner(System.in);
            // Get client name
            if (args.length > 0) {
                host = args[0];
            } else {
                System.out.print("Enter server IP address: ");
                host = sc.nextLine().trim();
            }

            if (args.length > 1)
            {
                clientName = args[1];
            }
            else
            {
                System.out.print("Enter your name: ");
                clientName = sc.nextLine().trim();
            }

            // Connect to the server
            Socket socket = new Socket(host, PORT);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(
                    socket.getOutputStream(), true);

            System.out.println("[CLIENT] Connected to server.");

            // Step 1: JOIN
            String joinMsg = "JOIN:" + clientName;
            System.out.println("[CLIENT] Sending: " + joinMsg);
            out.println(joinMsg);

            String ack = in.readLine();
            if (ack == null || !ack.startsWith("ACK:JOINED")) {
                System.out.println("[CLIENT] Join failed: " + ack);
                socket.close();
                return;
            }

            System.out.println("[CLIENT] Server response: " + ack);


            System.out.println("\n[CLIENT] You can now send math expressions.\n " + 
                    "[CLIENT] Supported opearator are: + - * / ()\n" + 
                    "[CLIENT] Ex: 5 + 3 | (2 + 3 * 7) | 10 / 3 * (2 + 4)\n" + 
                    "[CLIENT] Type 'quit' to disconnect\n");

            while (true)
            {
                System.out.println("[" + clientName + "] > ");
                String input = sc.nextLine().trim();
                if (input.matches("quit"))
                {
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

//            // Step 2: Send CALC requests
//            String[] expressions = {
//                "10 + 25",
//                "100 / 4",
//                "7 * 8",
//                "50 - 13",
//                "2 + 2"
//            };
//
//            Random rand = new Random();
//
//            for (int i = 0; i < 3; i++) {  // at least 3 required
//                String expr = expressions[rand.nextInt(expressions.length)];
//
//                int delay = 1000 + rand.nextInt(3000);
//                Thread.sleep(delay);
//
//                String calcMsg = "CALC:" + expr;
//                System.out.println("[CLIENT] Sending: " + calcMsg);
//                out.println(calcMsg);
//
//                String response = in.readLine();
//                System.out.println("[CLIENT] Received: " + response);
//            }
//
//            // Step 3: QUIT
//            System.out.println("[CLIENT] Sending: QUIT");
//            out.println("QUIT");

            socket.close();
            System.out.println("[CLIENT] Disconnected.");

        } catch (Exception e) {
            System.out.println("[CLIENT ERROR] " + e.getMessage());
        }
    }
}
