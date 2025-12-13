package model;

import javax.crypto.SecretKey;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.PublicKey;

public class ConnectionOfClient {
    private final Socket socket;
    private final OutputStream out;
    private final BufferedWriter writer;
    private final FilterOfClient filter;
    private PublicKey publicKey;
    private SecretKey sessionKey;

    public ConnectionOfClient(Socket socket, FilterOfClient filter) throws IOException {
        this.socket = socket;
        this.filter = filter;
        out = socket.getOutputStream();
        writer = new BufferedWriter(new OutputStreamWriter(out));
    }


    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public void setSessionKey(SecretKey sessionKey) {
        this.sessionKey = sessionKey;
    }

    public SecretKey getSessionKey() {
        return sessionKey;
    }


    public Socket getSocket() {
        return socket;
    }

    public BufferedWriter getWriter() {
        return writer;
    }

    public FilterOfClient getFilter() {
        return filter;
    }

    public OutputStream getOut() {
        return out;
    }


}







