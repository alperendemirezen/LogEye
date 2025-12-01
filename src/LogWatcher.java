import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class LogWatcher extends Thread {

    private final String filePath;
    private final BlockingQueue<String> queue;

    public LogWatcher(String filePath, BlockingQueue<String> queue) {
        this.filePath = filePath;
        this.queue = queue;
    }

    @Override
    public void run() {

        //I created the second try block later because I don't want the thread to stop when an error occurs and I don't want the buffer to be recreated in each loop.

        try (BufferedReader br = new BufferedReader(new FileReader(new File(filePath)))) {

            while (true) {
                try {
                    String newLine = br.readLine();

                    if (newLine != null) {
                        queue.put(newLine);
                    } else {
                        Thread.sleep(100);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
