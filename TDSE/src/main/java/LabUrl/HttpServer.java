package LabUrl;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.function.BiFunction;

public class HttpServer {
    private static volatile boolean running = false;

    public static void start(int port,
                             Map<String, BiFunction<HttpRequest, HttpResponse, String>> routes,
                             String staticFilesPath) throws IOException {
        running = true;
        StaticFileHandler staticFiles = new StaticFileHandler(staticFilesPath);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
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
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.isBlank()) {
                sendError(out, 400, "Bad Request");
                return;
            }

            HttpRequest request = new HttpRequest(requestLine);

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
        } catch (Exception e) {
            sendError(out, 500, "Internal Server Error");
        }
    }

    private static void sendError(OutputStream out, int status, String message) {
        try {
            new HttpResponse().status(status).body(message).send(out);
        } catch (Exception ignored) {}
    }
}
