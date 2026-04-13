import java.io.*;
import java.net.*;
import java.util.*;

public class MathClient {
    private static final String HOST = "127.0.0.1";
    private static final int    PORT = 9999;

    public static void main(String[] args) throws Exception {
        // Client name from command-line arg, or prompt user
        String clientName;
        if (args.length > 0) {
            clientName = args[0];
        } else {
            Scanner sc = new Scanner(System.in);
            System.out.print("Enter your name: ");
            clientName = sc.nextLine().trim();
            sc.close();
        }

        Socket socket = new Socket(HOST, PORT);
        BufferedReader in  = new BufferedReader(
            new InputStreamReader(socket.getInputStream()));
        PrintWriter    out = new PrintWriter(
            socket.getOutputStream(), true);

        // ── Step 1: Join the server ──────────────────────────
        out.println("JOIN:" + clientName);
        String ack = in.readLine();
        if (ack == null || !ack.startsWith("ACK:JOINED")) {
            System.out.println("[CLIENT] Join failed: " + ack);
            socket.close();
            return;
        }
        System.out.println("[CLIENT] Connected as: " + clientName);

        // ── Step 2: Send 3+ math requests at random times ────
        String[] expressions = {
            "10 + 25",
            "100 / 4",
            "7 * 8",
            "50 - 13",
            "2 + 2"
        };

        Random rand = new Random();
        for (String expr : expressions) {
            // Random delay 1-4 seconds between requests
            int delay = 1000 + rand.nextInt(3000);
            Thread.sleep(delay);

            System.out.println("[CLIENT] Sending: CALC:" + expr);
            out.println("CALC:" + expr);

            String response = in.readLine();
            System.out.println("[CLIENT] Received: " + response);
        }

        // ── Step 3: Disconnect cleanly ───────────────────────
        out.println("QUIT");
        System.out.println("[CLIENT] Sent QUIT, disconnecting.");
        socket.close();
    }
}
