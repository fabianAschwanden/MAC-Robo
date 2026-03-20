package ch.aschwanden.robo.core.capture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;

/**
 * Embedded HTTP server for receiving browser capture events.
 * Binds to 127.0.0.1 on the configured port.
 */
public class CaptureServer {
    private static final Logger logger = LoggerFactory.getLogger(CaptureServer.class);

    private final int port;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<CaptureEventListener> listeners = new CopyOnWriteArrayList<>();

    private HttpServer server;
    private volatile boolean running = false;

    public CaptureServer(int port) {
        this.port = port;
    }

    public void addListener(CaptureEventListener listener) {
        listeners.add(listener);
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/capture", this::handleCapture);
        server.createContext("/status",  this::handleStatus);
        server.setExecutor(Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "capture-server-worker");
            t.setDaemon(true);
            return t;
        }));
        server.start();
        running = true;
        logger.info("CaptureServer gestartet auf localhost:{}", port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
        running = false;
        logger.info("CaptureServer gestoppt");
    }

    public boolean isRunning() {
        return running;
    }

    public int getPort() {
        return port;
    }

    // ── Handlers ──────────────────────────────────────────────────────────────

    private void handleCapture(HttpExchange exchange) throws IOException {
        logger.debug("[capture] {} from {}", exchange.getRequestMethod(), exchange.getRemoteAddress());
        addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 204, "");
            return;
        }

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            return;
        }

        try (InputStream body = exchange.getRequestBody()) {
            byte[] bytes = body.readAllBytes();
            CaptureEvent event = objectMapper.readValue(bytes, CaptureEvent.class);
            logger.debug("Browser-Event empfangen: type={} url={}", event.eventType, event.url);
            for (CaptureEventListener listener : listeners) {
                try {
                    listener.onCaptureEvent(event);
                } catch (Exception e) {
                    logger.warn("Listener-Fehler bei Browser-Event", e);
                }
            }
            sendResponse(exchange, 200, "{\"ok\":true}");
        } catch (Exception e) {
            logger.warn("Fehler beim Verarbeiten des Browser-Events", e);
            sendResponse(exchange, 400, "{\"error\":\"Bad Request\"}");
        }
    }

    private void handleStatus(HttpExchange exchange) throws IOException {
        logger.debug("[status] {} from {}", exchange.getRequestMethod(), exchange.getRemoteAddress());
        addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 204, "");
            return;
        }

        String body = "{\"status\":\"recording\",\"port\":" + port + "}";
        sendResponse(exchange, 200, body);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin",  "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().set("Access-Control-Allow-Private-Network", "true");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
