import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class ClientConnection {
    private final Socket socket;
    private final ClientFilter filter;
    private final OutputStream out;

    public ClientConnection(Socket socket, ClientFilter filter) throws IOException {
        this.socket = socket;
        this.filter = filter;
        out = socket.getOutputStream();
    }

    public Socket getSocket() {
        return socket;
    }

    public ClientFilter getFilter() {
        return filter;
    }

    public OutputStream getOut() {
        return out;
    }


}
