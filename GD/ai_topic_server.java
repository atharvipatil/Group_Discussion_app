import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Random;

public class ai_topic_server {

    private static final String[] GD_TOPICS = {
        "Impact of Social Media on Youth",
        "Should AI Replace Human Jobs?",
        "Climate Change and Its Global Impact",
        "Future of Electric Vehicles",
        "Online Education vs Traditional Education",
        "Cryptocurrency  Future or Hype?",
        "Role of Technology in Healthcare",
        "Is India Ready for 100% Digital Economy?",
        "Should Plastic Be Banned Completely?",
        "How Startups are Changing India"
    };

    public static void main(String[] args) throws IOException {
        int port = 9000;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Route: /get-topic
        server.createContext("/get-topic", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("GET".equals(exchange.getRequestMethod())) {
                    String topic = getRandomTopic();
                    String response = "{\"topic\": \"" + topic + "\"}";

                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    exchange.getResponseHeaders().add("Cache-Control", "no-cache, no-store, must-revalidate");
                    exchange.sendResponseHeaders(200, response.getBytes().length);

                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                }
            }
        });

        // Default route: /
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String response = "{\"status\": \"Server running\"}";

                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, response.getBytes().length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        });

        server.setExecutor(null); // creates a default executor
        System.out.println("✅ AI Topic server running on port " + port + "...");
        server.start();
    }

    private static String getRandomTopic() {
        Random random = new Random();
        return GD_TOPICS[random.nextInt(GD_TOPICS.length)];
    }
}
