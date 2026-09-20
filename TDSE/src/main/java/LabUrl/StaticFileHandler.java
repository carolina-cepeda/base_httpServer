package LabUrl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

final class StaticFileHandler {
    private final String resourceBasePath;
    private final Path externalBaseDirectory;

    StaticFileHandler(String staticFilesPath) {
        this.resourceBasePath = normalizeBasePath(staticFilesPath);
        this.externalBaseDirectory = null;
    }

    private StaticFileHandler(Path externalBaseDirectory) {
        this.resourceBasePath = null;
        this.externalBaseDirectory = externalBaseDirectory;
    }

    static StaticFileHandler fromExternalDirectory(Path configuredDirectory) throws IOException {
        if (!Files.isDirectory(configuredDirectory) || !Files.isReadable(configuredDirectory)) {
            throw new IOException("STATIC_FILES_PATH must be a readable directory: " + configuredDirectory);
        }
        return new StaticFileHandler(configuredDirectory.toRealPath());
    }

    boolean serve(String requestPath, HttpResponse response, OutputStream out) throws IOException {
        String relativePath = resolveRelativePath(requestPath);
        if (relativePath == null) {
            return false;
        }

        return externalBaseDirectory == null
                ? serveClasspathResource(relativePath, response, out)
                : serveExternalResource(relativePath, response, out);
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

    private boolean serveClasspathResource(String relativePath, HttpResponse response, OutputStream out) throws IOException {
        String resourcePath = resourceBasePath.isEmpty() ? relativePath : resourceBasePath + "/" + relativePath;
        URL resourceUrl = StaticFileHandler.class.getClassLoader().getResource(resourcePath);
        if (resourceUrl == null) {
            return false;
        }

        URLConnection connection = resourceUrl.openConnection();
        try (InputStream resource = connection.getInputStream()) {
            response.contentType(contentTypeFor(resourcePath)).send(out, resource, connection.getContentLengthLong());
            return true;
        }
    }

    private boolean serveExternalResource(String relativePath, HttpResponse response, OutputStream out) throws IOException {
        Path candidate = externalBaseDirectory.resolve(relativePath).normalize();
        if (!candidate.startsWith(externalBaseDirectory) || !Files.isRegularFile(candidate) || !Files.isReadable(candidate)) {
            return false;
        }

        Path realFile = candidate.toRealPath();
        if (!realFile.startsWith(externalBaseDirectory)) {
            return false;
        }

        try (InputStream resource = Files.newInputStream(realFile)) {
            response.contentType(contentTypeFor(relativePath)).send(out, resource, Files.size(realFile));
            return true;
        }
    }

    private String resolveRelativePath(String requestPath) {
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

        return relativePath;
    }

    private static String normalizeBasePath(String staticFilesPath) {
        if (staticFilesPath == null) {
            return "";
        }
        return staticFilesPath.replaceFirst("^/+", "").replaceFirst("/+$", "");
    }
}
