package org.example.robo.core.capture;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration-Tests für CaptureServer (eingebetteter HTTP-Server auf Port 7891).
 */
class CaptureServerTest {

    private static final int TEST_PORT = 7891;

    private CaptureServer server;
    private HttpClient http;

    @BeforeEach
    void setUp() throws Exception {
        server = new CaptureServer(TEST_PORT);
        server.start();
        http = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    @Test
    void testServerIsRunningAfterStart() {
        assertTrue(server.isRunning());
        assertEquals(TEST_PORT, server.getPort());
    }

    @Test
    void testServerIsNotRunningAfterStop() {
        server.stop();
        assertFalse(server.isRunning());
    }

    // ── /status ────────────────────────────────────────────────────────────────

    @Test
    void testStatusEndpointReturns200() throws Exception {
        HttpResponse<String> res = get("/status");
        assertEquals(200, res.statusCode());
    }

    @Test
    void testStatusBodyContainsRecordingAndPort() throws Exception {
        HttpResponse<String> res = get("/status");
        String body = res.body();
        assertTrue(body.contains("recording"), "body: " + body);
        assertTrue(body.contains(String.valueOf(TEST_PORT)), "body: " + body);
    }

    // ── /capture POST ──────────────────────────────────────────────────────────

    @Test
    void testCaptureSingleClickEvent() throws Exception {
        List<CaptureEvent> received = new CopyOnWriteArrayList<>();
        server.addListener(received::add);

        String json = """
                {"eventType":"CLICK","cssSelector":"button#submit","xpath":"//button",
                 "textContent":"Submit","timingMs":100,"url":"https://example.com"}
                """;

        HttpResponse<String> res = post("/capture", json);

        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("\"ok\":true"), "body: " + res.body());
        assertEquals(1, received.size());
        assertEquals("CLICK", received.get(0).eventType);
        assertEquals("button#submit", received.get(0).cssSelector);
        assertEquals("https://example.com", received.get(0).url);
    }

    @Test
    void testCaptureTypeEventWithPayload() throws Exception {
        List<CaptureEvent> received = new CopyOnWriteArrayList<>();
        server.addListener(received::add);

        String json = """
                {"eventType":"TYPE","cssSelector":"input#email",
                 "payload":"test@example.com","timingMs":50}
                """;

        post("/capture", json);

        assertEquals(1, received.size());
        assertEquals("TYPE", received.get(0).eventType);
        assertEquals("test@example.com", received.get(0).payload);
    }

    @Test
    void testCaptureNotifiesMultipleListeners() throws Exception {
        List<CaptureEvent> received1 = new CopyOnWriteArrayList<>();
        List<CaptureEvent> received2 = new CopyOnWriteArrayList<>();
        server.addListener(received1::add);
        server.addListener(received2::add);

        post("/capture", """
                {"eventType":"CLICK","cssSelector":"a.link","timingMs":10}
                """);

        assertEquals(1, received1.size());
        assertEquals(1, received2.size());
        assertEquals("CLICK", received1.get(0).eventType);
    }

    @Test
    void testCaptureRejectsGetMethod() throws Exception {
        HttpResponse<String> res = get("/capture");
        assertEquals(405, res.statusCode());
    }

    @Test
    void testCaptureReturnsBadRequestOnInvalidJson() throws Exception {
        HttpResponse<String> res = post("/capture", "{not valid json!}");
        assertEquals(400, res.statusCode());
    }

    // ── CORS / OPTIONS ─────────────────────────────────────────────────────────

    @Test
    void testOptionsPreflightOnCaptureReturns204() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + TEST_PORT + "/capture"))
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(204, res.statusCode());
    }

    @Test
    void testOptionsPreflightOnStatusReturns204() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + TEST_PORT + "/status"))
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(204, res.statusCode());
    }

    @Test
    void testCorsHeaderPresentOnStatus() throws Exception {
        HttpResponse<String> res = get("/status");
        assertTrue(
            res.headers().firstValue("access-control-allow-origin").isPresent(),
            "CORS header missing"
        );
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + TEST_PORT + path))
                .GET()
                .build();
        return http.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String json) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + TEST_PORT + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return http.send(req, HttpResponse.BodyHandlers.ofString());
    }
}
