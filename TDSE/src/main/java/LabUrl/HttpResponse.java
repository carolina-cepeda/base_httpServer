package LabUrl;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HttpResponse {
    private int status = 200;
    private String contentType = "text/plain";
    private String body = "";

    public HttpResponse status(int status) { this.status = status; return this; }
    public HttpResponse contentType(String type) { this.contentType = type; return this; }
    public HttpResponse body(String body) { this.body = body; return this; }

    public void send(OutputStream out) throws Exception {
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        String headers = "HTTP/1.1 " + status + " " + statusText(status) + "\r\n" +
                         "Content-Type: " + contentType + "\r\n" +
                         "Content-Length: " + bodyBytes.length + "\r\n" +
                         "Connection: close\r\n\r\n";
        out.write(headers.getBytes(StandardCharsets.ISO_8859_1));
        out.write(bodyBytes);
        out.flush();
    }

    private String statusText(int status) {
        return switch (status) {
            case 200 -> "OK";
            case 404 -> "Not Found";
            case 400 -> "Bad Request";
            case 500 -> "Internal Server Error";
            default -> "OK";
        };
    }

    // Package-private accessor for HttpServer
    String getBody() { return body; }
    void setBody(String body) { this.body = body; }
}