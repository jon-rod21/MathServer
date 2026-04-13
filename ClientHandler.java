import java.io.*;
import java.net.*;
import java.time.*;

public class ClientHandler implements Runnable {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String clientName = "unknown";
        Instant connectTime = Instant.now();

        try {
            BufferedReader  in  = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            PrintWriter     out = new PrintWriter(
                socket.getOutputStream(), true); // auto-flush

            // ── Step 1: Wait for JOIN message ──────────────
            String joinMsg = in.readLine();
            if (joinMsg == null || !joinMsg.startsWith("JOIN:")) {
                out.println("ERROR:Expected JOIN message");
                socket.close();
                return;
            }
            clientName = joinMsg.substring(5).trim();
            connectTime = Instant.now();
            MathServer.activeClients.put(clientName, connectTime);

            String ip = socket.getInetAddress().getHostAddress()
                       + ":" + socket.getPort();
            MathServer.log("CONNECTED", clientName, "IP: " + ip);
            out.println("ACK:JOINED:" + clientName);

            // ── Step 2: Main request loop ──────────────────
            String message;
            while ((message = in.readLine()) != null) {
                if (message.equals("QUIT")) {
                    // Client wants to disconnect
                    break;
                } else if (message.startsWith("CALC:")) {
                    String expr = message.substring(5).trim();
                    MathServer.log("REQUEST", clientName, "Expr: " + expr);

                    // Call Person 3's math engine
                    String result = MathEngine.evaluate(expr);
                    out.println("RESULT:" + expr + "=" + result);
                    MathServer.log("RESPONSE", clientName, "Result: " + result);
                } else {
                    out.println("ERROR:Unknown command");
                }
            }

        } catch (IOException e) {
            MathServer.log("ERROR", clientName, e.getMessage());
        } finally {
            // ── Step 3: Clean up on disconnect ─────────────
            MathServer.activeClients.remove(clientName);
            long seconds = Instant.now().getEpochSecond()
                         - connectTime.getEpochSecond();
            MathServer.log("DISCONNECTED", clientName,
                "Duration: " + seconds + "s");
            try { socket.close(); } catch (IOException e) {}
        }
    }
}
