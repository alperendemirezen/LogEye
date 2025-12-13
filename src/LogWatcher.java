import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class LogWatcher extends Thread {

    private final String filePath;
    private BlockingQueue<String> queue;

    public LogWatcher(String filePath, BlockingQueue<String> queue) {
        this.filePath = filePath;
        this.queue = queue;
    }

    @Override
    public void run() {

        System.out.println("LogWatcher started...");
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            while (true) {
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
            e.printStackTrace();
        }
    }
}
