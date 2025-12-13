import java.util.concurrent.BlockingQueue;

public class LogAnalyzer extends Thread{
    private BlockingQueue<String> queue;
    private BlockingQueue<AlertMessage> alertMessages;

    public LogAnalyzer(BlockingQueue<String> queue, BlockingQueue<AlertMessage> alertMessages){
        this.queue = queue;
        this.alertMessages = alertMessages;
    }

    @Override
    public void run(){
        System.out.println("LogAnalyzer started...");
        while (true) {
            try {
                String logLine = queue.take();
                createAlert(logLine);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void createAlert(String logLine) throws InterruptedException{
        String timestamp = logLine.substring(0, 19);

        int levelStart = logLine.indexOf("[") + 1;
        int levelEnd = logLine.indexOf("]");
        String level = logLine.substring(levelStart, levelEnd);

        if(level.equals("ERROR") || level.equals("CRITICAL")){
            int secStart = logLine.indexOf("[", levelEnd) + 1;
            int secEnd   = logLine.indexOf("]", secStart);
            String section = logLine.substring(secStart, secEnd);

            String message = logLine.substring(secEnd + 2);
            alertMessages.put(new AlertMessage(timestamp,level,section,message));
        }
    }
}
