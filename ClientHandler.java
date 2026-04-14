import java.io.*;
import java.net.*;
import java.time.*;

/**
 * ClientHandler.java
 * CS/CE 4390 - Computer Networks, Spring 2026
 *
 * Handles one client connection from start to finish.
 * Runs in its own thread so the server can serve multiple
 * clients at the same time.
 *
 * Protocol this handler implements:
 *
 *   Client → Server          Server → Client
 *   ─────────────────────    ───────────────────────────
 *   JOIN:<name>          →   ACK:JOINED:<name>
 *   CALC:<expression>    →   RESULT:<expr>=<answer>
 *                            ERROR:<reason>   (on bad input)
 *   QUIT                 →   (server closes connection)
 *
 * Assumptions:
 *   - First message from any client MUST be JOIN — anything else is rejected
 *   - Client names are case-sensitive and whitespace-trimmed
 *   - If two clients join with the same name, both are tracked independently
 *     (the second overwrites the first in activeClients, but both stay connected)
 */
public class ClientHandler implements Runnable {

    private final Socket socket;

    /**
     * @param socket  the connected client socket from ServerSocket.accept()
     */
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        // These are set once the JOIN handshake succeeds.
        // Kept outside try block so the finally block can log them.
        String clientName = "unknown";
        Instant connectTime = Instant.now();

        try {
            // Set up text-based I/O streams on this socket.
            // PrintWriter with autoFlush=true means every println()
            // is immediately sent — no need to call flush() manually.
            BufferedReader in  = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            PrintWriter    out = new PrintWriter(
                socket.getOutputStream(), true);

            // ── PHASE 1: JOIN handshake ─────────────────────────────────────
            // The very first message must be "JOIN:<name>".
            // If it's anything else, reject and close.
            String firstMessage = in.readLine();

            if (firstMessage == null || !firstMessage.startsWith("JOIN:")) {
                out.println("ERROR:Expected JOIN:<name> as first message");
                MathServer.log("REJECTED", clientName,
                    "Bad opening message: " + firstMessage);
                return; // falls through to finally block
            }

            // Extract the name after "JOIN:"
            clientName = firstMessage.substring(5).trim();

            if (clientName.isEmpty()) {
                out.println("ERROR:Name cannot be empty");
                MathServer.log("REJECTED", clientName, "Empty name in JOIN");
                return;
            }

            // Record connect time and register in the shared active-clients map
            connectTime = Instant.now();
            MathServer.activeClients.put(clientName, connectTime);

            String clientIP = socket.getInetAddress().getHostAddress()
                            + ":" + socket.getPort();
            MathServer.log("CONNECTED", clientName, "IP: " + clientIP);

            // Send acknowledgement — client won't proceed until it gets this
            out.println("ACK:JOINED:" + clientName);

            // Also print a visible summary of who is currently online
            printActiveClients();

            // ── PHASE 2: Main request loop ──────────────────────────────────
            // Keep reading messages from this client until:
            //   (a) they send QUIT
            //   (b) the connection drops (readLine returns null)
            //   (c) an IOException occurs
            String message;
            while ((message = in.readLine()) != null) {

                if (message.equals("QUIT")) {
                    // Clean disconnect requested by client
                    MathServer.log("QUIT_RECEIVED", clientName, "Client requested disconnect");
                    break;

                } else if (message.startsWith("CALC:")) {
                    // Math request — extract the expression after "CALC:"
                    String expr = message.substring(5).trim();
                    MathServer.log("REQUEST", clientName, "Expr: " + expr);

                    if (expr.isEmpty()) {
                        out.println("ERROR:Empty expression");
                        MathServer.log("ERROR", clientName, "Empty expression received");
                        continue;
                    }

                    // Delegate math to Person 3's MathEngine
                    String result = MathEngine.evaluate(expr);

                    if (result.startsWith("ERROR:")) {
                        // Math engine returned an error — forward it to client
                        out.println(result);
                        MathServer.log("ERROR", clientName,
                            "Bad expr: " + expr + " => " + result);
                    } else {
                        // Success — send RESULT back in the agreed format
                        out.println("RESULT:" + expr + "=" + result);
                        MathServer.log("RESPONSE", clientName,
                            expr + " = " + result);
                    }

                } else {
                    // Unknown command — tell client but stay connected
                    out.println("ERROR:Unknown command: " + message);
                    MathServer.log("UNKNOWN_CMD", clientName,
                        "Unrecognized: " + message);
                }
            }

        } catch (IOException e) {
            // Client dropped unexpectedly (network error, process killed, etc.)
            MathServer.log("DROP", clientName,
                "Connection lost: " + e.getMessage());

        } finally {
            // ── PHASE 3: Cleanup — always runs, even if an exception occurred ─
            MathServer.activeClients.remove(clientName);

            long durationSeconds = Instant.now().getEpochSecond()
                                 - connectTime.getEpochSecond();
            MathServer.log("DISCONNECTED", clientName,
                "Duration: " + durationSeconds + "s");

            // Show who is still online after this client leaves
            printActiveClients();

            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    /**
     * Prints a summary of currently connected clients to the server console.
     * Called after every connect and disconnect event.
     */
    private void printActiveClients() {
        if (MathServer.activeClients.isEmpty()) {
            MathServer.log("STATUS", "SERVER", "No clients currently connected");
        } else {
            MathServer.log("STATUS", "SERVER",
                "Active clients (" + MathServer.activeClients.size() + "): "
                + String.join(", ", MathServer.activeClients.keySet()));
        }
    }
}