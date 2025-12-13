package client;

import com.google.gson.Gson;
import model.FilterOfClient;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public abstract class Client {

    private PublicKey publicKey;
    private PrivateKey privateKey;
    private SecretKey sessionKey;
    protected FilterOfClient filter;

    // Create the filter for this client
    protected abstract FilterOfClient createFilter();

    // Start the client
    public void start() throws Exception {
        System.out.println(getClientDescription());

        generateKeys();
        filter = createFilter();
        connectToServer();
    }

    // Description of the client
    protected String getClientDescription() {
        return "Client connecting to server...";
    }

    // Generate RSA key pair
    private void generateKeys() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair pair = kpg.generateKeyPair();
        privateKey = pair.getPrivate();
        publicKey = pair.getPublic();
    }

    private void connectToServer() throws Exception {
        Socket socket = new Socket("localhost", 7070);
        System.out.println("Connected to server.");

        // Send public key
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        String base64Pub = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        bw.write(base64Pub);
        bw.newLine();
        bw.flush();
        System.out.println("Public key sent.");

        // Receive encrypted session key
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String base64EncryptedKey = br.readLine();
        System.out.println("Encrypted session key received.");

        byte[] encryptedBytes = Base64.getDecoder().decode(base64EncryptedKey);

        // Decrypt session key
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

        // Receive alerts
        String encryptedAlert;
        while ((encryptedAlert = br.readLine()) != null) {
            String alert = decryptAlert(encryptedAlert);
            System.out.println("ALERT RECEIVED: " + alert);
        }
    }

    // Decrypt received alert
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