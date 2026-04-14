import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.time.*;
import java.time.format.*;

/**
 * MathServer.java
 * CS/CE 4390 - Computer Networks, Spring 2026
 *
 * Main server entry point. Opens a TCP server socket on PORT,
 * accepts incoming client connections, and spawns a new thread
 * (ClientHandler) for each one so multiple clients can be
 * served simultaneously.
 *
 * Assumptions:
 *   - Clients speak the agreed protocol: JOIN, CALC, QUIT messages
 *   - All messages are newline-terminated strings
 *   - Port 9999 is available on the machine running this server
 */
public class MathServer {

    // Port the server listens on — clients must connect to this same port
    public static final int PORT = 9999;

    // Thread-safe map tracking currently connected clients.
    // Key = client name, Value = time they connected.
    // ConcurrentHashMap is safe to read/write from multiple threads.
    public static final ConcurrentHashMap<String, Instant> activeClients =
        new ConcurrentHashMap<>();

    // Formatter for log timestamps
    private static final DateTimeFormatter LOG_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Log file writer — all server events also get written to server.log
    private static PrintWriter logFile;

    public static void main(String[] args) throws Exception {

        // Open log file (append mode so it survives restarts)
        logFile = new PrintWriter(new FileWriter("server.log", true), true);

        // try-with-resources ensures serverSocket.close() is called
        // if the server ever shuts down (e.g. Ctrl+C or an exception)
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            log("SERVER_START", "N/A", "Listening on port " + PORT);
            System.out.println("[SERVER] Math Server started on port " + PORT);
            System.out.println("[SERVER] Waiting for clients...\n");

            // Main accept loop — runs forever until process is killed
            while (true) {
                // accept() blocks here until a client connects
                Socket clientSocket = serverSocket.accept();

                // Spawn a dedicated thread for this client so we don't block
                // the accept loop — other clients can still connect while this
                // one is being served.
                Thread t = new Thread(new ClientHandler(clientSocket));
                t.setDaemon(true); // thread dies automatically if server shuts down
                t.start();
            }
        }
    }

    /**
     * Writes a formatted log entry to both stdout and server.log.
     * Synchronized so concurrent threads don't garble each other's lines.
     *
     * @param event   short event label, e.g. "CONNECTED", "REQUEST"
     * @param client  client name, or "N/A" for server-level events
     * @param detail  additional context for the event
     */
    public static synchronized void log(String event, String client, String detail) {
        String timestamp = LOG_FMT.format(LocalDateTime.now());
        String line = String.format("[%s] %-12s | Client: %-12s | %s",
                                    timestamp, event, client, detail);
        System.out.println(line);
        if (logFile != null) {
            logFile.println(line);
        }
    }
}