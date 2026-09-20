package LabUrl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HttpResponse {
    private int status = 200;
    private String contentType = "text/plain";
    private String body = "";

    public HttpResponse status(int status) { this.status = status; return this; }
    public HttpResponse contentType(String type) { this.contentType = type; return this; }
    public HttpResponse body(String body) { this.body = body == null ? "" : body; return this; }

    public void send(OutputStream out) throws IOException {
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        writeHeaders(out, bodyBytes.length);
        out.write(bodyBytes);
        out.flush();
    }

    public void send(OutputStream out, InputStream bodyStream) throws IOException {
        send(out, bodyStream, -1);
    }

    public void send(OutputStream out, InputStream bodyStream, long contentLength) throws IOException {
        if (contentLength < 0) {
            byte[] bodyBytes = bodyStream.readAllBytes();
            writeHeaders(out, bodyBytes.length);
            out.write(bodyBytes);
            out.flush();
            return;
        }

        writeHeaders(out, contentLength);
        bodyStream.transferTo(out);
        out.flush();
    }

    private void writeHeaders(OutputStream out, long contentLength) throws IOException {
        String reason = statusText(status);
        String headers = "HTTP/1.1 " + status + (reason.isEmpty() ? "" : " " + reason) + "\r\n" +
                         "Content-Type: " + contentType + "\r\n" +
                         "Content-Length: " + contentLength + "\r\n" +
                         "Connection: close\r\n\r\n";
        out.write(headers.getBytes(StandardCharsets.ISO_8859_1));
    }

    private String statusText(int status) {
        return switch (status) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 202 -> "Accepted";
            case 204 -> "No Content";
            case 301 -> "Moved Permanently";
            case 302 -> "Found";
            case 304 -> "Not Modified";
            case 404 -> "Not Found";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 405 -> "Method Not Allowed";
            case 409 -> "Conflict";
            case 413 -> "Payload Too Large";
            case 415 -> "Unsupported Media Type";
            case 429 -> "Too Many Requests";
            case 500 -> "Internal Server Error";
            case 501 -> "Not Implemented";
            case 502 -> "Bad Gateway";
            case 503 -> "Service Unavailable";
            default -> "";
        };
    }

    // Package-private accessor for HttpServer
    String getBody() { return body; }
    void setBody(String body) { this.body = body == null ? "" : body; }
}
