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
    
    // 📋 List of our Backend Servers (Virtual for Demo)
    private static final List<String> BACKEND_SERVERS = List.of(
        "http://localhost:8081",
        "http://localhost:8082",
        "http://localhost:8083"
    );

    public static void main(String[] args) throws IOException {
        // ☁️ CLOUD PORT LOGIC
        int port = 8000;
        String envPort = System.getenv("PORT");
        if (envPort != null) {
            port = Integer.parseInt(envPort);
        }

        System.out.println("⚖️ Load Balancer starting on Port " + port + "...");
        
        // Listen on the dynamic Cloud Port
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
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

            // 2. Forward request (Simulated for Demo)
            String responseBody = forwardRequest(backendUrl);

            // 3. Send HTML response back to the User
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, responseBody.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBody.getBytes());
            os.close();
        }

        private String forwardRequest(String urlString) {
            // In a real scenario, we would connect. 
            // For this Portfolio Demo, we show which server WAS chosen.
            try {
                // Try to connect (will fail on Render since backends don't exist)
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(100); // Fail fast
                conn.setRequestMethod("GET");
                conn.getResponseCode(); 
                return "Connected to " + urlString; 
            } catch (Exception e) {
                // 🎨 DEMO RESPONSE: Prove the Algorithm works
                return "<html><body style='font-family: monospace; background: #000; color: #0f0; padding: 20px;'>"
                     + "<h1>⚖️ Load Balancer Demo</h1>"
                     + "<div style='border: 1px solid #333; padding: 20px; border-radius: 8px;'>"
                     + "<p><strong>Algorithm:</strong> Round Robin</p>"
                     + "<p><strong>Selected Backend:</strong> <span style='color: yellow'>" + urlString + "</span></p>"
                     + "<p><i>(Note: Connection failed because backend is offline in this cloud container. <br>Refesh the page to see the backend URL change!)</i></p>"
                     + "</div></body></html>";
            }
        }
    }
}