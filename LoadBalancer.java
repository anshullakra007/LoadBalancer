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
            // Ignore favicon requests from browsers to avoid skipping servers in Round Robin
            if (exchange.getRequestURI().getPath().equals("/favicon.ico")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            // 1. ROUND ROBIN LOGIC: Pick the next server
            int index = Math.abs(counter.getAndIncrement() % BACKEND_SERVERS.size());
            String backendUrl = BACKEND_SERVERS.get(index);
            
            System.out.println("🔀 Forwarding request to: " + backendUrl);

            // 2. Forward request (Simulated for Demo)
            String responseBody = forwardRequest(backendUrl);

            // 3. Send HTML response back to the User
            byte[] responseBytes = responseBody.getBytes("UTF-8");
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        }

        private String forwardRequest(String urlString) {
            String statusHtml;
            String messageHtml;
            
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(2000);
                conn.setReadTimeout(2000);
                conn.setRequestMethod("GET");
                
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    
                    statusHtml = "<span style='color: #0f0'>✅ ONLINE</span>";
                    messageHtml = "<p><strong>Backend Response:</strong> <span style='color: cyan'>" + response.toString() + "</span></p>";
                } else {
                    statusHtml = "<span style='color: red'>❌ HTTP " + responseCode + "</span>";
                    messageHtml = "<p><strong>Error:</strong> <span style='color: red'>Unexpected response code from backend</span></p>";
                }
            } catch (Exception e) {
                statusHtml = "<span style='color: red'>❌ OFFLINE</span>";
                messageHtml = "<p><strong>Error:</strong> <span style='color: red'>Connection failed: " + e.getMessage() + "</span></p>"
                            + "<p><i>(Note: Connection failed because backend is offline. Refresh the page to see the backend URL change!)</i></p>";
            }

            return "<html><body style='font-family: monospace; background: #000; color: #0f0; padding: 20px;'>"
                 + "<h1>⚖️ Load Balancer Demo</h1>"
                 + "<div style='border: 1px solid #333; padding: 20px; border-radius: 8px;'>"
                 + "<p><strong>Algorithm:</strong> Round Robin</p>"
                 + "<p><strong>Selected Backend:</strong> <span style='color: yellow'>" + urlString + "</span></p>"
                 + "<p><strong>Status:</strong> " + statusHtml + "</p>"
                 + messageHtml
                 + "<p><i>(Refresh the page to see the load balancer select the next server!)</i></p>"
                 + "</div></body></html>";
        }
    }
}