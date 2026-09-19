package LabUrl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public final class WebFramework {
    private WebFramework() {}

    private static final Map<String, BiFunction<HttpRequest, HttpResponse, String>> routes = new HashMap<>();
    private static String staticFilesPath = EnvironmentConfig.DEFAULT_STATIC_FILES_PATH;

    public static void staticfiles(String path) {
        staticFilesPath = path.startsWith("/") ? path : "/" + path;
    }

    public static void get(String path, BiFunction<HttpRequest, HttpResponse, String> handler) {
        routes.put(path, handler);
    }

    public static void start() throws IOException {
        EnvironmentConfig configuration = EnvironmentConfig.fromSystemEnvironment();
        start(configuration.port(), configuration);
    }

    public static void start(int port) throws IOException {
        start(port, EnvironmentConfig.fromSystemEnvironment());
    }

    private static void start(int port, EnvironmentConfig configuration) throws IOException {
        StaticFileHandler staticFiles = configuration.externalStaticFilesPath() == null
                ? new StaticFileHandler(staticFilesPath)
                : StaticFileHandler.fromExternalDirectory(configuration.externalStaticFilesPath());
        HttpServer.start(port, routes, staticFiles);
    }

    public static void stop() {
        HttpServer.stop();
    }

    static Map<String, BiFunction<HttpRequest, HttpResponse, String>> getRoutes() { return routes; }
    static String getStaticFilesPath() { return staticFilesPath; }
}
