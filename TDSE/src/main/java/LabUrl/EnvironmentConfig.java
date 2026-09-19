package LabUrl;

import java.nio.file.Path;
import java.util.Map;

final class EnvironmentConfig {
    static final int DEFAULT_PORT = 8080;
    static final String DEFAULT_GREETING_PREFIX = "Hello";
    static final String DEFAULT_APP_ENV = "development";
    static final String DEFAULT_STATIC_FILES_PATH = "/webroot";

    private final int port;
    private final String greetingPrefix;
    private final String appEnv;
    private final Path externalStaticFilesPath;

    EnvironmentConfig(Map<String, String> environment) {
        this.port = parsePort(environment.get("PORT"));
        this.greetingPrefix = valueOrDefault(environment.get("GREETING_PREFIX"), DEFAULT_GREETING_PREFIX);
        this.appEnv = valueOrDefault(environment.get("APP_ENV"), DEFAULT_APP_ENV);
        this.externalStaticFilesPath = parseExternalStaticFilesPath(environment.get("STATIC_FILES_PATH"));
    }

    static EnvironmentConfig fromSystemEnvironment() {
        return new EnvironmentConfig(System.getenv());
    }

    int port() {
        return port;
    }

    String greetingPrefix() {
        return greetingPrefix;
    }

    String appEnv() {
        return appEnv;
    }

    Path externalStaticFilesPath() {
        return externalStaticFilesPath;
    }

    private static int parsePort(String configuredPort) {
        if (configuredPort == null || configuredPort.isBlank()) {
            return DEFAULT_PORT;
        }

        try {
            int port = Integer.parseInt(configuredPort);
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("PORT must be between 1 and 65535");
            }
            return port;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("PORT must be a number between 1 and 65535", exception);
        }
    }

    private static String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static Path parseExternalStaticFilesPath(String configuredPath) {
        if (configuredPath == null || configuredPath.isBlank()) {
            return null;
        }
        return Path.of(configuredPath).toAbsolutePath().normalize();
    }
}
