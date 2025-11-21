package app.soundlab.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.Socket;
import java.util.Map;

public class TcpClient {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8888;
    private static final Gson gson = new GsonBuilder().create();

    private final String host;
    private final int port;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public TcpClient() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public TcpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public ServerResponse sendRequest(String command, Map<String, Object> parameters) throws IOException {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        ClientRequest request = new ClientRequest(command, parameters);
        String requestJson = gson.toJson(request);
        
        out.println(requestJson.length());
        out.print(requestJson);
        out.print("\n");
        out.flush();

        String responseLengthStr = in.readLine();
        if (responseLengthStr == null) {
            throw new IOException("Server closed connection");
        }
        int responseLength = Integer.parseInt(responseLengthStr);
        
        char[] buffer = new char[responseLength];
        int totalRead = 0;
        while (totalRead < responseLength) {
            int read = in.read(buffer, totalRead, responseLength - totalRead);
            if (read == -1) {
                throw new IOException("Unexpected end of stream");
            }
            totalRead += read;
        }
        
        String responseJson = new String(buffer, 0, totalRead);
        return gson.fromJson(responseJson, ServerResponse.class);
    }

    public void disconnect() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}

