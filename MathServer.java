import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.time.*;
import java.time.format.*;

public class MathServer {
    private static final int PORT = 9999;
    // Thread-safe map: clientName -> connect time
    public static final Map<String, Instant> activeClients =
        new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("[SERVER] Math Server started on port " + PORT);
        log("SERVER_START", "N/A", "Listening on port " + PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            // Spawn a new thread for each client
            Thread t = new Thread(new ClientHandler(clientSocket));
            t.start();
        }
        serverSocket.close();
    }

    // Call this from ClientHandler to write server logs
    public static synchronized void log(String event, String client, String detail) {
        String ts = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .format(LocalDateTime.now());
        String line = String.format("[%s] %-12s | Client: %-10s | %s",
            ts, event, client, detail);
        System.out.println(line);
        // TODO: also write to a log file with FileWriter if you want
    }
}
