package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class LogWatcher extends Thread {

    private volatile boolean running = true;
    private final String filePath;
    private BlockingQueue<String> queue;

    public LogWatcher(String filePath, BlockingQueue<String> queue) {
        this.filePath = filePath;
        this.queue = queue;
    }

    @Override
    public void run() {

        // Watch the log file
        System.out.println("LogWatcher started...");
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            while (running) {
                String newLine = br.readLine();
                if (newLine != null) {
                    queue.put(newLine);
                } else {
                    Thread.sleep(1000);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Graceful shutdown
    public void shutdown() {
        System.out.println("Shutting down LogWatcher...");
        running = false;
        this.interrupt();
    }

}
