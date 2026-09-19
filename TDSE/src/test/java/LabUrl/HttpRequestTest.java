package LabUrl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class HttpRequestTest {
    @Test
    void parsesAndDecodesMultipleQueryParameters() throws Exception {
        HttpRequest request = new HttpRequest(
                "GET /hello?name=Ana+Mar%C3%ADa&language=en&flag&%63ity=Bogot%C3%A1 HTTP/1.1");

        assertEquals("GET", request.getMethod());
        assertEquals("/hello", request.getPath());
        assertEquals("Ana María", request.getValue("name"));
        assertEquals("en", request.getValue("language"));
        assertEquals("", request.getValue("flag"));
        assertEquals("Bogotá", request.getValue("city"));
        assertNull(request.getValue("missing"));
    }

    @Test
    void keepsTheLastValueForRepeatedParameters() throws Exception {
        HttpRequest request = new HttpRequest("GET /hello?name=first&name=last HTTP/1.1");

        assertEquals("last", request.getValue("name"));
    }
}
