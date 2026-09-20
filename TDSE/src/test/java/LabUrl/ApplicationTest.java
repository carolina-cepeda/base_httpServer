package LabUrl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ApplicationTest {
    @Test
    void helloUsesTheConfiguredPrefixAndDecodedName() throws Exception {
        HttpRequest request = new HttpRequest("GET /hello?name=Ada+Lovelace HTTP/1.1");

        assertEquals("Hola, Ada Lovelace!", Application.hello(request, "Hola"));
    }

    @Test
    void helloUsesWorldWhenNameIsMissingOrBlank() throws Exception {
        assertEquals("Hello, World!", Application.hello(new HttpRequest("GET /hello HTTP/1.1"), "Hello"));
        assertEquals("Hello, World!", Application.hello(new HttpRequest("GET /hello?name= HTTP/1.1"), "Hello"));
    }

    @Test
    void piReturnsJavaMathPi() {
        assertEquals(String.valueOf(Math.PI), Application.pi());
    }
}
