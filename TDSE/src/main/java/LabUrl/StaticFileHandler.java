package LabUrl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

final class StaticFileHandler {
    private final String resourceBasePath;

    StaticFileHandler(String staticFilesPath) {
        this.resourceBasePath = normalizeBasePath(staticFilesPath);
    }

    boolean serve(String requestPath, HttpResponse response, OutputStream out) throws IOException {
        String resourcePath = resolveResourcePath(requestPath);
        if (resourcePath == null) {
            return false;
        }

        try (InputStream resource = StaticFileHandler.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (resource == null) {
                return false;
            }
            response.contentType(contentTypeFor(resourcePath)).send(out, resource);
            return true;
        }
    }

    static String contentTypeFor(String resourcePath) {
        String lowerCasePath = resourcePath.toLowerCase(Locale.ROOT);
        if (lowerCasePath.endsWith(".html") || lowerCasePath.endsWith(".htm")) return "text/html";
        if (lowerCasePath.endsWith(".css")) return "text/css";
        if (lowerCasePath.endsWith(".js")) return "application/javascript";
        if (lowerCasePath.endsWith(".json")) return "application/json";
        if (lowerCasePath.endsWith(".png")) return "image/png";
        if (lowerCasePath.endsWith(".jpg") || lowerCasePath.endsWith(".jpeg")) return "image/jpeg";
        if (lowerCasePath.endsWith(".gif")) return "image/gif";
        if (lowerCasePath.endsWith(".svg")) return "image/svg+xml";
        if (lowerCasePath.endsWith(".ico")) return "image/x-icon";
        if (lowerCasePath.endsWith(".webp")) return "image/webp";
        return "application/octet-stream";
    }

    private String resolveResourcePath(String requestPath) {
        if (requestPath == null || !requestPath.startsWith("/")) {
            return null;
        }

        String relativePath = requestPath.equals("/") ? "index.html" : requestPath.substring(1);
        if (relativePath.isBlank() || relativePath.contains("\\")) {
            return null;
        }

        for (String segment : relativePath.split("/")) {
            if (segment.isEmpty() || segment.equals(".") || segment.equals("..")) {
                return null;
            }
        }

        return resourceBasePath.isEmpty() ? relativePath : resourceBasePath + "/" + relativePath;
    }

    private static String normalizeBasePath(String staticFilesPath) {
        if (staticFilesPath == null) {
            return "";
        }
        return staticFilesPath.replaceFirst("^/+", "").replaceFirst("/+$", "");
    }
}
