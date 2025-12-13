import com.google.gson.Gson;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.security.*;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.BlockingQueue;


public class AlertSender extends Thread {

    private final Gson gson = new Gson();
    private final BlockingQueue<AlertMessage> alertMessages;
    private final List<ConnectionOfClient> connectedClients;

    public AlertSender(BlockingQueue<AlertMessage> alertMessages, List<ConnectionOfClient> connectedClients) {
        this.alertMessages = alertMessages;
        this.connectedClients = connectedClients;
    }

    @Override
    public void run(){

        System.out.println("AlertSender started...");
        while(true){
            AlertMessage alert = null;

            try {
                alert = alertMessages.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for(ConnectionOfClient client: connectedClients){
                if(client.getFilter().matches(alert)){
                    try {
                        String json = gson.toJson(alert);

                        String encryptedJson = encrypt(json, client.getSessionKey());

                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(client.getOut()));
                        bw.write(encryptedJson);
                        bw.newLine();
                        bw.flush();

                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private String encrypt(String plainText, SecretKey key) throws Exception{
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

        byte[] encrypted = cipher.doFinal(plainText.getBytes());

        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

}
