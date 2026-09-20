package LabUrl;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private final String method;
    private final String path;
    private final Map<String, String> queryParams = new HashMap<>();

    public HttpRequest(String requestLine) throws URISyntaxException {
        if (requestLine == null) {
            throw new IllegalArgumentException("Request line is required");
        }

        String[] parts = requestLine.split(" ", -1);
        if (parts.length != 3 || parts[0].isBlank() || parts[1].isBlank() || parts[2].isBlank()) {
            throw new IllegalArgumentException("Malformed request line");
        }
        if (!parts[0].matches("[A-Z]+") || !"HTTP/1.1".equals(parts[2])) {
            throw new IllegalArgumentException("Unsupported request line");
        }
        if (!parts[1].startsWith("/")) {
            throw new IllegalArgumentException("Request target must be an origin-form path");
        }

        this.method = parts[0];

        URI uri = new URI(parts[1]);
        if (uri.isAbsolute() || uri.getRawAuthority() != null || uri.getPath() == null || uri.getPath().isBlank()) {
            throw new IllegalArgumentException("Invalid request target");
        }
        this.path = uri.getPath();
        parseQuery(uri.getRawQuery());
    }

    private void parseQuery(String query) {
        if (query == null || query.isBlank()) return;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            queryParams.put(key, value);
        }
    }

    public String getValue(String key) {
        return queryParams.get(key);
    }

    public String getMethod() { return method; }
    public String getPath() { return path; }
}
