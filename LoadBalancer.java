import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadBalancer {
    // 🔄 Round Robin Counter (Thread Safe)
    private static final AtomicInteger counter = new AtomicInteger(0);
    
    // 📋 List of our Backend Servers
    private static final List<String> BACKEND_SERVERS = List.of(
        "http://localhost:8081",
        "http://localhost:8082",
        "http://localhost:8083"
    );

    public static void main(String[] args) throws IOException {
        System.out.println("⚖️ Load Balancer starting on Port 8000...");
        
        // Listen on Port 8000 (The Entry Point)
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new Router());
        server.setExecutor(null);
        server.start();
    }

    static class Router implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // 1. ROUND ROBIN LOGIC: Pick the next server
            int index = counter.getAndIncrement() % BACKEND_SERVERS.size();
            String backendUrl = BACKEND_SERVERS.get(index);
            
            System.out.println("🔀 Forwarding request to: " + backendUrl);

            // 2. Forward request to the selected Backend
            String response = forwardRequest(backendUrl);

            // 3. Send response back to the User
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private String forwardRequest(String urlString) {
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                return content.toString();
            } catch (Exception e) {
                return "Error connecting to backend";
            }
        }
    }
}