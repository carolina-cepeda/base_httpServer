package LabUrl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EnvironmentConfigTest {
    @Test
    void usesDefaultsForMissingOrBlankValues() {
        EnvironmentConfig missing = new EnvironmentConfig(Map.of());
        EnvironmentConfig blank = new EnvironmentConfig(Map.of(
                "PORT", " ", "GREETING_PREFIX", "", "APP_ENV", " ", "STATIC_FILES_PATH", ""));

        assertConfigurationDefaults(missing);
        assertConfigurationDefaults(blank);
    }

    @Test
    void readsConfiguredValues() {
        EnvironmentConfig configuration = new EnvironmentConfig(Map.of(
                "PORT", "9090",
                "GREETING_PREFIX", "Hola",
                "APP_ENV", "production",
                "STATIC_FILES_PATH", "assets"));

        assertEquals(9090, configuration.port());
        assertEquals("Hola", configuration.greetingPrefix());
        assertEquals("production", configuration.appEnv());
        assertEquals(Path.of("assets").toAbsolutePath().normalize(), configuration.externalStaticFilesPath());
    }

    @Test
    void rejectsInvalidPorts() {
        assertThrows(IllegalArgumentException.class, () -> new EnvironmentConfig(Map.of("PORT", "invalid")));
        assertThrows(IllegalArgumentException.class, () -> new EnvironmentConfig(Map.of("PORT", "0")));
        assertThrows(IllegalArgumentException.class, () -> new EnvironmentConfig(Map.of("PORT", "65536")));
    }

    private static void assertConfigurationDefaults(EnvironmentConfig configuration) {
        assertEquals(8080, configuration.port());
        assertEquals("Hello", configuration.greetingPrefix());
        assertEquals("development", configuration.appEnv());
        assertNull(configuration.externalStaticFilesPath());
    }
}
