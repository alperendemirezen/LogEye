import com.google.gson.Gson;

import javax.crypto.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.*;
import java.util.*;

public class Server extends Thread {

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

            while (true) {
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
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private PublicKey readClientPublicKey(Socket socket) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String base64Key = br.readLine();

        byte[] decoded = Base64.getDecoder().decode(base64Key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePublic(spec);
    }

    private SecretKey generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256);
        return kg.generateKey();
    }

    private void sendSessionKey(Socket socket, String base64EncryptedKey) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        bw.write(base64EncryptedKey);
        bw.newLine();
        bw.flush();
    }

    public byte[] encryptWithPublicKey(byte[] data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    private FilterOfClient parseFilter(String json) {
        Gson gson = new Gson();
        FilterOfClient filter = gson.fromJson(json, FilterOfClient.class);
        return filter;
    }
}
