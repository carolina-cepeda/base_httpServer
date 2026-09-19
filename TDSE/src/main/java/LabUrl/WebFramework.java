package LabUrl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public final class WebFramework {
    private WebFramework() {}

    private static final Map<String, BiFunction<HttpRequest, HttpResponse, String>> routes = new HashMap<>();
    private static String staticFilesPath = "/webroot";

    public static void staticfiles(String path) {
        staticFilesPath = path.startsWith("/") ? path : "/" + path;
    }

    public static void get(String path, BiFunction<HttpRequest, HttpResponse, String> handler) {
        routes.put(path, handler);
    }

    public static void start() throws IOException {
        start(8080);
    }

    public static void start(int port) throws IOException {
        HttpServer.start(port, routes, staticFilesPath);
    }

    public static void stop() {
        HttpServer.stop();
    }

    static Map<String, BiFunction<HttpRequest, HttpResponse, String>> getRoutes() { return routes; }
    static String getStaticFilesPath() { return staticFilesPath; }
}
