import com.google.gson.Gson;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.util.Base64;

public abstract class Client {

    private PublicKey publicKey;
    private PrivateKey privateKey;
    private SecretKey sessionKey;
    protected FilterOfClient filter;

    protected abstract FilterOfClient createFilter();


    public void start() throws Exception {
        System.out.println(getClientDescription() + "\n");

        generateKeys();
        filter = createFilter();
        connectToServer();
    }

    protected String getClientDescription() {
        return "Client connecting to server...";
    }

    private void generateKeys() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair pair = kpg.generateKeyPair();
        privateKey = pair.getPrivate();
        publicKey = pair.getPublic();
    }

    private void connectToServer() throws Exception {
        Socket socket = new Socket("localhost", 7070);
        System.out.println("Connected to server.");

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Send public key
        String base64Pub = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        bw.write(base64Pub);
        bw.newLine();
        bw.flush();
        System.out.println("Public key sent.");

        // Receive encrypted session key
        String base64EncryptedKey = br.readLine();
        System.out.println("Encrypted session key received.");

        byte[] encryptedBytes = Base64.getDecoder().decode(base64EncryptedKey);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] sessionKeyBytes = cipher.doFinal(encryptedBytes);
        sessionKey = new SecretKeySpec(sessionKeyBytes, "AES");

        System.out.println("Session key decrypted.");

        // Send filter
        Gson gson = new Gson();
        String filterJson = gson.toJson(filter);
        bw.write(filterJson);
        bw.newLine();
        bw.flush();
        System.out.println("Filter sent: " + filterJson);

        System.out.println("Ready to receive alerts!\n");

        //  Receive alerts
        String encryptedAlert;
        while ((encryptedAlert = br.readLine()) != null) {
            String alert = decryptAlert(encryptedAlert);
            System.out.println("ALERT RECEIVED: " + alert);
        }
    }

    private String decryptAlert(String base64Data) throws Exception {
        byte[] combined = Base64.getDecoder().decode(base64Data);

        byte[] iv = new byte[16];
        System.arraycopy(combined, 0, iv, 0, 16);

        byte[] ciphertext = new byte[combined.length - 16];
        System.arraycopy(combined, 16, ciphertext, 0, ciphertext.length);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, sessionKey, ivSpec);

        byte[] plainBytes = cipher.doFinal(ciphertext);

        return new String(plainBytes);
    }
}