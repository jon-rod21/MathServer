// MathServer.java
// Main server class. Listens for client connections and spawns a thread for each one.

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.time.*;
import java.time.format.*;

public class MathServer {

   // Port that the server runs on
   public static final int PORT = 9999;

   // Keeps track of which clients are currently connected
   // Key = client name, value = time they connected
   public static final ConcurrentHashMap<String, Instant> activeClients =
       new ConcurrentHashMap<>();

   // Format for log timestamps
   private static final DateTimeFormatter LOG_FMT =
       DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

   // Writes log entries to server.log file
   private static PrintWriter logFile;

   public static void main(String[] args) throws Exception {

       // Opens log file in append mode so old logs aren't overwritten on restart
       logFile = new PrintWriter(new FileWriter("server.log", true), true);

       // Ensures the socket closes correctly if server shuts down
       try (ServerSocket serverSocket = new ServerSocket(PORT)) {
           log("SERVER_START", "N/A", "Listening on port " + PORT);
           System.out.println("[SERVER] Math Server started on port " + PORT);
           System.out.println("[SERVER] Waiting for clients...\n");

           // Keeps accepting new clients
           while (true) {
               Socket clientSocket = serverSocket.accept();

               // Gives each client its own thread so multiple clients can connect at once
               Thread t = new Thread(new ClientHandler(clientSocket));
               t.setDaemon(true);
               t.start();
           }
       }
   }

   // Writes a log entry to both the console and server.log
   // synchronized so multiple threads don't write at the same time
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
