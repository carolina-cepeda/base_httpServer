package LabUrl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private final String method;
    private final String path;
    private final Map<String, String> queryParams = new HashMap<>();

    public HttpRequest(String requestLine) throws URISyntaxException {
        String[] parts = requestLine.split(" ");
        this.method = parts[0];

        URI uri = new URI(parts[1]);
        this.path = uri.getPath();
        parseQuery(uri.getQuery());
    }

    private void parseQuery(String query) {
        if (query == null || query.isBlank()) return;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            String key = kv[0];
            String value = kv.length > 1 ? kv[1] : "";
            queryParams.put(key, value);
        }
    }

    public String getValue(String key) {
        return queryParams.get(key);
    }

    public String getMethod() { return method; }
    public String getPath() { return path; }
}