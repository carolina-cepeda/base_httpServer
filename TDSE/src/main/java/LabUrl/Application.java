package LabUrl;

import java.io.IOException;

/** Demonstrates registering dynamic routes and serving classpath resources. */
public final class Application {
    private Application() {}

    public static void main(String[] args) throws IOException {
        EnvironmentConfig configuration = EnvironmentConfig.fromSystemEnvironment();

        WebFramework.staticfiles("/webroot");
        WebFramework.get("/hello", (request, response) -> hello(request, configuration.greetingPrefix()));
        WebFramework.get("/pi", (request, response) -> pi());

        if ("development".equals(configuration.appEnv())) {
            WebFramework.get("/shutdown", (request, response) -> {
                WebFramework.stop();
                return "Server will stop after this response.";
            });
        }

        WebFramework.start();
    }

    static String hello(HttpRequest request, String greetingPrefix) {
        String name = request.getValue("name");
        String recipient = name == null || name.isBlank() ? "World" : name;
        return greetingPrefix + ", " + recipient + "!";
    }

    static String pi() {
        return String.valueOf(Math.PI);
    }
}
