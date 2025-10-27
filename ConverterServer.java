import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.math.BigInteger;

public class ConverterServer {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/convert", new ConvertHandler());
        server.setExecutor(null);
        System.out.println("✅ Server running on http://localhost:8080");
        server.start();
    }

    static class ConvertHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            // 1) ✅ Handle CORS preflight (OPTIONS)
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            // 2) ✅ Handle POST
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                // Extract JSON fields manually
                String value = body.replaceAll(".*\"value\"\\s*:\\s*\"([^\"]+)\".*", "$1");
                int fromBase = Integer.parseInt(body.replaceAll(".*\"fromBase\"\\s*:\\s*(\\d+).*", "$1"));
                int toBase = Integer.parseInt(body.replaceAll(".*\"toBase\"\\s*:\\s*(\\d+).*", "$1"));

                String result;
                try {
                    // Using BigInteger to support large numbers too
                    BigInteger decimalValue = new BigInteger(value, fromBase);
                    result = decimalValue.toString(toBase).toUpperCase();
                } catch (Exception e) {
                    result = "Invalid Input!";
                }

                String jsonResponse = "{\"result\":\"" + result + "\"}";

                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);

                OutputStream os = exchange.getResponseBody();
                os.write(jsonResponse.getBytes());
                os.close();
            }
        }
    }
}
