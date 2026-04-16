// ClientHandler.java
// Handles one client connection from start to finish.
// Runs in its own thread so the server can serve multiple clients at the same time.
//
// Protocol:
//   JOIN:<name> -> ACK:JOINED:<name>
//   CALC:<expression> -> RESULT:<expr>=<answer>  or  ERROR:<reason>
//   QUIT -> server closes the connection

import java.io.*;
import java.net.*;
import java.time.*;

public class ClientHandler implements Runnable {

    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        String clientName = "unknown";
        Instant connectTime = Instant.now();

        try {
            // Sets up reading and writing on this socket
            BufferedReader in  = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            PrintWriter    out = new PrintWriter(
                socket.getOutputStream(), true);

            // Joins
            // first message must be JOIN:<name>
            String firstMessage = in.readLine();

            if (firstMessage == null || !firstMessage.startsWith("JOIN:")) {
                out.println("ERROR:Expected JOIN:<name> as first message");
                MathServer.log("REJECTED", clientName, "Bad opening message: " + firstMessage);
                return;
            }

            clientName = firstMessage.substring(5).trim();

            if (clientName.isEmpty()) {
                out.println("ERROR:Name cannot be empty");
                MathServer.log("REJECTED", clientName, "Empty name in JOIN");
                return;
            }

            // Registers client and logs the connection
            connectTime = Instant.now();
            MathServer.activeClients.put(clientName, connectTime);

            String clientIP = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
            MathServer.log("CONNECTED", clientName, "IP: " + clientIP);

            // Sends ACK so client knows it can start sending requests
            out.println("ACK:JOINED:" + clientName);
            printActiveClients();

            // Handles requests
            // keeps reading until client sends QUIT or disconnects
            String message;
            while ((message = in.readLine()) != null) {

                if (message.equals("QUIT")) {
                    MathServer.log("QUIT_RECEIVED", clientName, "Client requested disconnect");
                    break;

                } else if (message.startsWith("CALC:")) {
                    String expr = message.substring(5).trim();
                    MathServer.log("REQUEST", clientName, "Expr: " + expr);

                    if (expr.isEmpty()) {
                        out.println("ERROR:Empty expression");
                        MathServer.log("ERROR", clientName, "Empty expression received");
                        continue;
                    }

                    // Passes expression to MathEngine and sends back the result
                    String result = MathEngine.evaluate(expr);

                    if (result.startsWith("ERROR:")) {
                        out.println(result);
                        MathServer.log("ERROR", clientName, "Bad expr: " + expr + " => " + result);
                    } else {
                        out.println("RESULT:" + expr + "=" + result);
                        MathServer.log("RESPONSE", clientName, expr + " = " + result);
                    }

                } else {
                    // Unknown command. Tells client and keeps the connection open
                    out.println("ERROR:Unknown command: " + message);
                    MathServer.log("UNKNOWN_CMD", clientName, "Unrecognized: " + message);
                }
            }

        } catch (IOException e) {
            // Client disconnected unexpectedly
            MathServer.log("DROP", clientName, "Connection lost: " + e.getMessage());

        } finally {
            // Clean up
            MathServer.activeClients.remove(clientName);
            long duration = Instant.now().getEpochSecond() - connectTime.getEpochSecond();
            MathServer.log("DISCONNECTED", clientName, "Duration: " + duration + "s");
            printActiveClients();
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    // Prints the client who is currently connected to the server console
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