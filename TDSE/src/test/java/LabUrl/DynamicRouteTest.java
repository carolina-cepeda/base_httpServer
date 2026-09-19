package LabUrl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import org.junit.jupiter.api.Test;

class DynamicRouteTest {
    @Test
    void invokesGetHandlersWithDecodedQueryParameters() {
        Map<String, BiFunction<HttpRequest, HttpResponse, String>> routes = Map.of(
                "/hello", (request, response) -> "Hello " + request.getValue("name"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        HttpServer.handleRequest(request("GET", "/hello?name=Ada+Lovelace"), output, routes,
                new StaticFileHandler("/webroot"));

        assertTrue(responseText(output).endsWith("Hello Ada Lovelace"));
    }

    @Test
    void doesNotInvokeGetHandlersForOtherMethods() {
        AtomicBoolean invoked = new AtomicBoolean();
        Map<String, BiFunction<HttpRequest, HttpResponse, String>> routes = Map.of(
                "/index.html", (request, response) -> {
                    invoked.set(true);
                    return "dynamic route";
                });
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        HttpServer.handleRequest(request("POST", "/index.html"), output, routes,
                new StaticFileHandler("/webroot"));

        assertFalse(invoked.get());
        assertTrue(responseText(output).startsWith("HTTP/1.1 404 Not Found"));
    }

    private static ByteArrayInputStream request(String method, String path) {
        String request = method + " " + path + " HTTP/1.1\r\nHost: test\r\n\r\n";
        return new ByteArrayInputStream(request.getBytes(StandardCharsets.ISO_8859_1));
    }

    private static String responseText(ByteArrayOutputStream output) {
        return output.toString(StandardCharsets.ISO_8859_1);
    }
}
