package server;

import com.google.gson.Gson;
import model.ConnectionOfClient;
import model.FilterOfClient;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

public class Server extends Thread {

    private volatile boolean running = true;
    private List<ConnectionOfClient> connectedClients;
    private final int port;

    public Server(int port, List<ConnectionOfClient> connectedClients) {
        this.port = port;
        this.connectedClients = connectedClients;
    }

    @Override
    public void run() {

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port + "...");

            while (running) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket.getRemoteSocketAddress());

                PublicKey publicKey = readClientPublicKey(socket);
                SecretKey secretKey = generateSecretKey();

                // Encrypt session key with client's public key
                byte[] encryptedSecretKey = encryptWithPublicKey(secretKey.getEncoded(), publicKey);
                String base64EncryptedKey = Base64.getEncoder().encodeToString(encryptedSecretKey);
                sendSessionKey(socket, base64EncryptedKey);

                // Read filter from client
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String filterJson = br.readLine();
                System.out.println("Filter received: " + filterJson);

                // Create ConnectionOfClient and store it
                FilterOfClient filter = parseFilter(filterJson);
                ConnectionOfClient client = new ConnectionOfClient(socket, filter);
                client.setPublicKey(publicKey);
                client.setSessionKey(secretKey);

                connectedClients.add(client);


                System.out.println("Client registered successfully.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Read client's public key sent in Base64
    public PublicKey readClientPublicKey(Socket socket) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String base64Key = br.readLine();

        byte[] decoded = Base64.getDecoder().decode(base64Key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePublic(spec);
    }

    // Generate AES secret key
    public SecretKey generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256);
        return kg.generateKey();
    }

    // Send encrypted session key to client
    public void sendSessionKey(Socket socket, String base64EncryptedKey) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        bw.write(base64EncryptedKey);
        bw.newLine();
        bw.flush();
    }

    // Encrypt data with client's public key
    public byte[] encryptWithPublicKey(byte[] data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    // Parse filter from JSON string
    public FilterOfClient parseFilter(String json) {
        Gson gson = new Gson();
        FilterOfClient filter = gson.fromJson(json, FilterOfClient.class);
        return filter;
    }

    // Graceful shutdown
    public void shutdown() {
        System.out.println("Shutting down Server...");
        running = false;
        this.interrupt();
    }
}
