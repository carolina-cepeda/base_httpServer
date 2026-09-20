package LabUrl;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.BiFunction;

public class HttpServer {
    private static volatile boolean running = false;

    public static void start(int port,
                             Map<String, BiFunction<HttpRequest, HttpResponse, String>> routes,
                             StaticFileHandler staticFiles) throws IOException {
        running = true;

        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), port));
            System.out.println("Server started on port " + port + " (0.0.0.0)");

            while (running) {
                try (Socket clientSocket = serverSocket.accept();
                     InputStream in = clientSocket.getInputStream();
                     OutputStream out = clientSocket.getOutputStream()) {

                    handleRequest(in, out, routes, staticFiles);
                } catch (IOException e) {
                    if (running) System.err.println("Client error: " + e.getMessage());
                }
            }
        }
        System.out.println("Server stopped gracefully.");
    }

    public static void stop() {
        running = false;
    }

    static void handleRequest(InputStream in, OutputStream out,
                              Map<String, BiFunction<HttpRequest, HttpResponse, String>> routes,
                              StaticFileHandler staticFiles) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.ISO_8859_1));
            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.isBlank()) {
                sendError(out, 400, "Bad Request");
                return;
            }

            HttpRequest request;
            try {
                request = new HttpRequest(requestLine);
            } catch (IllegalArgumentException | java.net.URISyntaxException exception) {
                sendError(out, 400, "Bad Request");
                return;
            }

            String line;
            while ((line = reader.readLine()) != null && !line.isBlank()) {}

            if (!"GET".equals(request.getMethod())) {
                sendError(out, 404, "Not Found");
                return;
            }

            HttpResponse response = new HttpResponse();

            BiFunction<HttpRequest, HttpResponse, String> handler = routes.get(request.getPath());
            if (handler != null) {
                String body = handler.apply(request, response);
                if (response.getBody() == null || response.getBody().isEmpty()) {
                    response.setBody(body);
                }
                response.send(out);
            } else if (!staticFiles.serve(request.getPath(), response, out)) {
                sendError(out, 404, "Not Found");
            }
        } catch (Exception exception) {
            sendInternalServerError(out, exception);
        }
    }

    private static void sendError(OutputStream out, int status, String message) {
        try {
            new HttpResponse().status(status).body(message).send(out);
        } catch (Exception ignored) {}
    }

    private static void sendInternalServerError(OutputStream out, Exception exception) {
        System.err.println("Request failed: " + exception);
        String detail = exception.getMessage();
        String message = detail == null || detail.isBlank()
                ? "Internal Server Error"
                : "Internal Server Error: " + detail;
        sendError(out, 500, message);
    }
}
