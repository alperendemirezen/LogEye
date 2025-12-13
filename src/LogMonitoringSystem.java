import model.AlertMessage;
import model.ConnectionOfClient;
import server.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LogMonitoringSystem {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Log Monitoring System Starting ===\n");

        // Configuration
        String logFilePath = "logs/app.log";
        String dbUrl = "jdbc:sqlite:C:/Users/Alperen Bey/Documents/mydb.sqlite";

        // Shared resources
        BlockingQueue<String> logQueue = new LinkedBlockingQueue<>(1000);
        BlockingQueue<AlertMessage> alertQueue = new LinkedBlockingQueue<>(1000);
        List<ConnectionOfClient> connectedClients = Collections.synchronizedList(new ArrayList<>());

        // System components
        Server server = new Server(7070, connectedClients);
        LogWatcher watcher = new LogWatcher(logFilePath, logQueue);
        LogAnalyzer analyzer = new LogAnalyzer(logQueue, alertQueue);
        AlertSender sender = new AlertSender(alertQueue, connectedClients);
        LogArchiver archiver = new LogArchiver(dbUrl, logFilePath, 10);

        // Shutdown hook for graceful termination
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n=== Shutting down system ===");

            server.shutdown();
            watcher.shutdown();
            analyzer.shutdown();
            sender.shutdown();
            archiver.shutdown();

            try {
                watcher.join(3000);
                analyzer.join(3000);
                sender.join(3000);
                System.out.println("=== System stopped gracefully ===");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));

        // Start all components
        server.start();
        Thread.sleep(5000);  // Wait for the clients to connect.

        watcher.start();
        analyzer.start();
        sender.start();
        archiver.start();

        System.out.println("=== All systems running ===");

        server.join();
    }
}