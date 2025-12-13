import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Log Monitoring System Starting ===\n");

        String logFilePath = "logs/app.log";

        BlockingQueue<String> logQueue = new LinkedBlockingQueue<>(1000);
        BlockingQueue<AlertMessage> alertQueue = new LinkedBlockingQueue<>(1000);
        List<ConnectionOfClient> connectedClients = Collections.synchronizedList(new ArrayList<>());

        Server server = new Server(7070, connectedClients);
        server.start();

        Thread.sleep(10000);

        LogWatcher logWatcher = new LogWatcher(logFilePath, logQueue);
        logWatcher.start();

        LogAnalyzer logAnalyzer = new LogAnalyzer(logQueue, alertQueue);
        logAnalyzer.start();

        AlertSender alertSender = new AlertSender(alertQueue, connectedClients);
        alertSender.start();

        System.out.println("DEBUG Main: List address = " + System.identityHashCode(connectedClients));


        try {
            server.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
