package LabUrl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExternalStaticFileHandlerTest {
    @TempDir
    Path externalDirectory;

    @TempDir
    Path outsideDirectory;

    @Test
    void servesRootAndNestedFilesFromTheExternalDirectory() throws Exception {
        Files.writeString(externalDirectory.resolve("index.html"), "<h1>external</h1>");
        Files.createDirectories(externalDirectory.resolve("assets"));
        Files.writeString(externalDirectory.resolve("assets/info.json"), "{\"source\":\"external\"}");
        StaticFileHandler handler = StaticFileHandler.fromExternalDirectory(externalDirectory);

        ByteArrayOutputStream rootOutput = new ByteArrayOutputStream();
        ByteArrayOutputStream nestedOutput = new ByteArrayOutputStream();

        assertTrue(handler.serve("/", new HttpResponse(), rootOutput));
        assertTrue(handler.serve("/assets/info.json", new HttpResponse(), nestedOutput));
        assertTrue(responseText(rootOutput).contains("Content-Type: text/html"));
        assertTrue(responseText(rootOutput).endsWith("<h1>external</h1>"));
        assertTrue(responseText(nestedOutput).contains("Content-Type: application/json"));
        assertTrue(responseText(nestedOutput).endsWith("{\"source\":\"external\"}"));
    }

    @Test
    void doesNotFallBackToClasspathResources() throws Exception {
        StaticFileHandler handler = StaticFileHandler.fromExternalDirectory(externalDirectory);

        assertFalse(handler.serve("/styles.css", new HttpResponse(), new ByteArrayOutputStream()));
    }

    @Test
    void streamsExternalBinaryResourcesWithoutConversion() throws Exception {
        byte[] expected = {0, 1, -1, 127};
        Files.write(externalDirectory.resolve("file.bin"), expected);
        StaticFileHandler handler = StaticFileHandler.fromExternalDirectory(externalDirectory);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        assertTrue(handler.serve("/file.bin", new HttpResponse(), output));

        byte[] response = output.toByteArray();
        byte[] actual = new byte[expected.length];
        System.arraycopy(response, response.length - expected.length, actual, 0, expected.length);
        assertArrayEquals(expected, actual);
    }

    @Test
    void rejectsSymlinksThatEscapeTheExternalDirectory() throws Exception {
        Path outsideFile = outsideDirectory.resolve("outside.txt");
        Files.writeString(outsideFile, "outside");
        Files.createSymbolicLink(externalDirectory.resolve("outside.txt"), outsideFile);
        StaticFileHandler handler = StaticFileHandler.fromExternalDirectory(externalDirectory);

        assertFalse(handler.serve("/outside.txt", new HttpResponse(), new ByteArrayOutputStream()));
    }

    @Test
    void rejectsAnUnreadableOrMissingExternalDirectory() {
        Path missingDirectory = externalDirectory.resolve("missing");

        assertThrows(java.io.IOException.class, () -> StaticFileHandler.fromExternalDirectory(missingDirectory));
    }

    private static String responseText(ByteArrayOutputStream output) {
        return output.toString(StandardCharsets.ISO_8859_1);
    }
}
