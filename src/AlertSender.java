import com.google.gson.Gson;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class AlertSender extends Thread {

    private final Gson gson = new Gson();
    private final BlockingQueue<AlertMessage> alertMessages;
    private final List<ClientConnection> connectedClients;

    public AlertSender(BlockingQueue<AlertMessage> alertMessages, List<ClientConnection> connectedClients) {
        this.alertMessages = alertMessages;
        this.connectedClients = connectedClients;
    }

    @Override
    public void run(){
        while(true){
            AlertMessage alert = null;
            try {
                alert = alertMessages.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for(ClientConnection client: connectedClients){
                if(client.getFilter().matches(alert)){
                    String json = gson.toJson(alert);

                }
            }
        }
    }
}
