import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class BackendServers {
    public static void main(String[] args) throws IOException {
        // We will start 3 "Servers" on ports 8081, 8082, 8083
        startServer(8081, "Server 1 (US-East)");
        startServer(8082, "Server 2 (US-West)");
        startServer(8083, "Server 3 (Europe)");
    }

    private static void startServer(int port, String name) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new Handler(name));
        server.setExecutor(null);
        server.start();
        System.out.println("✅ " + name + " listening on port " + port);
    }

    static class Handler implements HttpHandler {
        private final String serverName;
        public Handler(String name) { this.serverName = name; }

        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "Hello from " + serverName;
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}