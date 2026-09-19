package LabUrl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import org.junit.jupiter.api.Test;

class StaticFileHandlerTest {
    @Test
    void servesIndexForTheRootPath() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        boolean served = new StaticFileHandler("/webroot").serve("/", new HttpResponse(), output);

        assertTrue(served);
        assertTrue(responseText(output).contains("Content-Type: text/html"));
        assertTrue(responseText(output).endsWith("<h1>Static home</h1>\n"));
    }

    @Test
    void usesExpectedMimeTypes() {
        assertEquals("text/css", StaticFileHandler.contentTypeFor("webroot/styles.css"));
        assertEquals("application/javascript", StaticFileHandler.contentTypeFor("webroot/app.js"));
        assertEquals("image/png", StaticFileHandler.contentTypeFor("webroot/logo.png"));
        assertEquals("application/octet-stream", StaticFileHandler.contentTypeFor("webroot/file.bin"));
    }

    @Test
    void servesNestedClasspathResources() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        boolean served = new StaticFileHandler("/webroot").serve("/assets/info.json", new HttpResponse(), output);

        assertTrue(served);
        assertTrue(responseText(output).contains("Content-Type: application/json"));
        assertTrue(responseText(output).endsWith("{\"resource\":\"nested\"}\n"));
    }

    @Test
    void streamsBinaryBodiesWithoutTextConversion() throws Exception {
        byte[] payload = {0, 1, -1, 127, 10};
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        new HttpResponse().contentType("application/octet-stream")
                .send(output, new ByteArrayInputStream(payload));

        byte[] response = output.toByteArray();
        byte[] actualPayload = new byte[payload.length];
        System.arraycopy(response, response.length - payload.length, actualPayload, 0, payload.length);
        assertArrayEquals(payload, actualPayload);
    }

    @Test
    void rejectsMissingAndTraversalResources() throws Exception {
        StaticFileHandler handler = new StaticFileHandler("/webroot");

        assertFalse(handler.serve("/missing.css", new HttpResponse(), new ByteArrayOutputStream()));
        assertFalse(handler.serve("/../secret.txt", new HttpResponse(), new ByteArrayOutputStream()));
        assertFalse(handler.serve("/nested\\secret.txt", new HttpResponse(), new ByteArrayOutputStream()));
    }

    @Test
    void givesRegisteredRoutesPriorityOverStaticResources() {
        Map<String, BiFunction<HttpRequest, HttpResponse, String>> routes = new HashMap<>();
        routes.put("/index.html", (request, response) -> "dynamic route");
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        HttpServer.handleRequest(request("/index.html"), output, routes, new StaticFileHandler("/webroot"));

        assertTrue(responseText(output).endsWith("dynamic route"));
    }

    @Test
    void returns404WhenNoRouteOrResourceMatches() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        HttpServer.handleRequest(request("/missing.css"), output, Map.of(), new StaticFileHandler("/webroot"));

        assertTrue(responseText(output).startsWith("HTTP/1.1 404 Not Found"));
    }

    private static ByteArrayInputStream request(String path) {
        String request = "GET " + path + " HTTP/1.1\r\nHost: test\r\n\r\n";
        return new ByteArrayInputStream(request.getBytes(StandardCharsets.ISO_8859_1));
    }

    private static String responseText(ByteArrayOutputStream output) {
        return output.toString(StandardCharsets.ISO_8859_1);
    }
}
