package LabUrl;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HttpResponse {
    private int status = 200;
    private String contentType = "text/plain";
    private String body = "";

    public HttpResponse status(int status) { this.status = status; return this; }
    public HttpResponse contentType(String type) { this.contentType = type; return this; }
    public HttpResponse body(String body) { this.body = body; return this; }

    public void send(OutputStream out) throws IOException {
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        writeHeaders(out, bodyBytes.length);
        out.write(bodyBytes);
        out.flush();
    }

    public void send(OutputStream out, InputStream bodyStream) throws IOException {
        writeHeaders(out, -1);
        bodyStream.transferTo(out);
        out.flush();
    }

    private void writeHeaders(OutputStream out, long contentLength) throws IOException {
        String headers = "HTTP/1.1 " + status + " " + statusText(status) + "\r\n" +
                         "Content-Type: " + contentType + "\r\n" +
                         (contentLength >= 0 ? "Content-Length: " + contentLength + "\r\n" : "") +
                         "Connection: close\r\n\r\n";
        out.write(headers.getBytes(StandardCharsets.ISO_8859_1));
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
