# ⚖️ Java Load Balancer (Round Robin)

A custom HTTP Load Balancer built from scratch in Java. It sits in front of multiple backend servers and distributes incoming traffic using the **Round Robin** algorithm, ensuring no single server is overwhelmed.

## 🚀 Tech Stack
* **Core:** Java 21 (`com.sun.net.httpserver`)
* **Networking:** `HttpURLConnection` for forwarding requests.
* **Concurrency:** `AtomicInteger` for thread-safe server rotation.

## 🛠️ How It Works
1. **The Balancer:** Listens on Port `8000`.
2. **The Backends:** 3 Simulated servers run on ports `8081`, `8082`, `8083`.
3. **The Logic:**
   * Request 1 → Server 1 (US-East)
   * Request 2 → Server 2 (US-West)
   * Request 3 → Server 3 (Europe)
   * Request 4 → Server 1... (Repeats)

## 📸 Usage
1. **Start the Backend Servers:**
   ```bash
   javac BackendServers.java && java BackendServers