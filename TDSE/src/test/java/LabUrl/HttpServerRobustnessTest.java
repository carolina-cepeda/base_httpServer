package LabUrl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.BiFunction;
import org.junit.jupiter.api.Test;

class HttpServerRobustnessTest {
    @Test
    void returns400ForMalformedRequestLines() {
        assertBadRequest("");
        assertBadRequest("GET HTTP/1.1");
        assertBadRequest("GET / HTTP/1.0");
        assertBadRequest("GET / extra HTTP/1.1");
        assertBadRequest("GET /?name=%ZZ HTTP/1.1");
        assertBadRequest("GET hello HTTP/1.1");
    }

    @Test
    void returnsDetailed500ForHandlerFailuresAndContinuesHandlingRequests() {
        Map<String, BiFunction<HttpRequest, HttpResponse, String>> brokenRoutes = Map.of(
                "/broken", (request, response) -> {
                    throw new IllegalStateException("handler exploded");
                });

        String errorResponse = responseFor("GET /broken HTTP/1.1", brokenRoutes);
        String validResponse = responseFor("GET /ok HTTP/1.1", Map.of(
                "/ok", (request, response) -> "working"));

        assertTrue(errorResponse.startsWith("HTTP/1.1 500 Internal Server Error"));
        assertTrue(errorResponse.endsWith("Internal Server Error: handler exploded"));
        assertCompleteHeaders(errorResponse);
        assertTrue(validResponse.endsWith("working"));
    }

    @Test
    void includesCompleteHeadersForNotFoundAndStaticResponses() {
        String notFound = responseFor("GET /missing.css HTTP/1.1", Map.of());
        String staticResponse = responseFor("GET /styles.css HTTP/1.1", Map.of());

        assertTrue(notFound.startsWith("HTTP/1.1 404 Not Found"));
        assertCompleteHeaders(notFound);
        assertTrue(staticResponse.startsWith("HTTP/1.1 200 OK"));
        assertTrue(staticResponse.contains("Content-Type: text/css"));
        assertCompleteHeaders(staticResponse);
    }

    @Test
    void doesNotAssignOkAsTheReasonForUnknownStatusCodes() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        new HttpResponse().status(418).body("tea").send(output);

        String response = output.toString(StandardCharsets.ISO_8859_1);
        assertTrue(response.startsWith("HTTP/1.1 418\r\n"));
        assertFalse(response.startsWith("HTTP/1.1 418 OK"));
        assertCompleteHeaders(response);
    }

    @Test
    void completesShutdownResponseBeforeStoppingTheSequentialServer() throws Exception {
        Map<String, BiFunction<HttpRequest, HttpResponse, String>> routes = Map.of(
                "/shutdown", (request, response) -> {
                    HttpServer.stop();
                    return "Server will stop after this response.";
                });

        String response = responseFor("GET /shutdown HTTP/1.1", routes);

        assertTrue(response.startsWith("HTTP/1.1 200 OK"));
        assertTrue(response.endsWith("Server will stop after this response."));
        assertCompleteHeaders(response);
    }

    private static void assertBadRequest(String requestLine) {
        String response = responseFor(requestLine, Map.of());

        assertTrue(response.startsWith("HTTP/1.1 400 Bad Request"));
        assertCompleteHeaders(response);
    }

    private static String responseFor(String requestLine,
                                      Map<String, BiFunction<HttpRequest, HttpResponse, String>> routes) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String request = requestLine.isEmpty() ? "" : requestLine + "\r\nHost: test\r\n\r\n";
        HttpServer.handleRequest(new ByteArrayInputStream(request.getBytes(StandardCharsets.ISO_8859_1)), output,
                routes, new StaticFileHandler("/webroot"));
        return output.toString(StandardCharsets.ISO_8859_1);
    }

    private static void assertCompleteHeaders(String response) {
        int headersEnd = response.indexOf("\r\n\r\n");
        assertTrue(headersEnd > 0);
        assertTrue(response.contains("Content-Type: "));
        assertTrue(response.contains("Content-Length: "));
        assertTrue(response.contains("Connection: close"));

        int contentLengthStart = response.indexOf("Content-Length: ") + "Content-Length: ".length();
        int contentLengthEnd = response.indexOf("\r\n", contentLengthStart);
        int declaredLength = Integer.parseInt(response.substring(contentLengthStart, contentLengthEnd));
        int actualLength = response.substring(headersEnd + 4).getBytes(StandardCharsets.ISO_8859_1).length;
        assertEquals(declaredLength, actualLength);
    }
}
