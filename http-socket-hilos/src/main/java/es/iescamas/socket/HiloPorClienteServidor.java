package es.iescamas.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Servidor TCP que atiende clientes mediante un hilo por conexión.
 * Sirve HTML básico y un favicon desde src/main/resources/favicon.ico
 */
public class HiloPorClienteServidor implements Runnable {

    /** Puerto donde escucha el servidor. */
    protected int serverPort = 9001;

    /** Socket servidor. */
    protected ServerSocket serversocket = null;

    /** Flag de parada. */
    protected boolean isStopped;

    /** Referencia al hilo que ejecuta run(). */
    protected Thread runningThread = null;

    public HiloPorClienteServidor(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        synchronized (this) {
            this.runningThread = Thread.currentThread();
        }

        openServerSocket();

        while (!isStopped()) {
            try {
                Socket clientSocket = this.serversocket.accept();

                new Thread(() -> {
                    try {
                        processClientRequest(clientSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, "client-" + clientSocket.getPort()).start();

            } catch (IOException e) {
                if (isStopped()) {
                    System.out.println("Server stopped.");
                    return;
                }
                throw new RuntimeException("Error accepting client connection", e);
            }
        }

        System.out.println("Server Stopped");
    }

    /**
     * Procesa la conexión de un cliente.
     */
    /**
     * Procesa la conexión de un cliente.
     */
    private void processClientRequest(Socket clientSocket) throws IOException {
        try (clientSocket;
             InputStream in = clientSocket.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.US_ASCII));
             OutputStream out = clientSocket.getOutputStream()) {

            String requestLine = br.readLine();
            if (requestLine == null || requestLine.isBlank()) return;

            String path = "/";
            if (requestLine.startsWith("GET ")) {
                int start = 4;
                int end = requestLine.indexOf(' ', start);
                if (end > start) 
                    path = requestLine.substring(start, end);
            }

            if ("/favicon.ico".equals(path)) {
                serveFavicon(out);
                return;
            }

            // --- MEJORA 3: Lógica de Estado HTTP y Rutas ---
            String status = "200 OK"; // Por defecto todo va bien
            String tituloPrincipal = "Información de la Petición";
            String mensajeCuerpo = "";

            if (path.equals("/")) {
                mensajeCuerpo = "Servidor Activo. Prueba la ruta <b>/nombre/TuNombre</b>";
            } 
            else if (path.startsWith("/nombre/")) {
                mensajeCuerpo = "Hola " + path.substring(8); 
            } 
            else {
                // MEJORA 3: Error 404 Real
                status = "404 Not Found";
                tituloPrincipal = "Error 404 - No Encontrado";
                mensajeCuerpo = "Lo sentimos, la ruta <b>" + path + "</b> no existe.";
            }

            String clientIp = clientSocket.getInetAddress().getHostAddress();
            String fecha = new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(new Date());

            String body = "<html>"
                    + "<head><title>PSP - Monitor</title>"
                    + "<style>"
                    + "  body { font-family: sans-serif; background-color: #f0f2f5; color: #333; padding: 20px; }"
                    + "  .container { background: #fff; border: 1px solid #ddd; padding: 20px; border-radius: 4px; max-width: 600px; margin: 0 auto; }"
                    + "  h2 { border-bottom: 2px solid " + (status.equals("200 OK") ? "#3498db" : "#e74c3c") + "; color: " + (status.equals("200 OK") ? "#3498db" : "#e74c3c") + "; padding-bottom: 10px; }"
                    + "  ul { list-style: none; padding: 0; }"
                    + "  li { margin-bottom: 8px; border-bottom: 1px solid #eee; padding-bottom: 5px; }"
                    + "  .tag { font-weight: bold; color: #555; width: 120px; display: inline-block; }"
                    + "</style></head>"
                    + "<body><div class='container'>"
                    + "  <h2>" + tituloPrincipal + "</h2>"
                    + "  <ul>"
                    + "    <li><span class='tag'>Estado:</span> " + status + "</li>"
                    + "    <li><span class='tag'>Resultado:</span> " + mensajeCuerpo + "</li>"
                    + "    <li><span class='tag'>Ruta:</span> <code>" + path + "</code></li>"
                    + "    <li><span class='tag'>Hilo:</span> " + Thread.currentThread().getName() + "</li>"
                    + "    <li><span class='tag'>IP Cliente:</span> " + clientIp + "</li>"
                    + "    <li><span class='tag'>Fecha:</span> " + fecha + "</li>"
                    + "  </ul>"
                    + "  <p style='font-size: 0.8em; text-align: right; color: #999;'>Práctica 4 - Servidor Concurrente</p>"
                    + " </div></body></html>";

            byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

            String headers = "HTTP/1.1 " + status + "\r\n" +
                             "Content-Type: text/html; charset=UTF-8\r\n" +
                             "Content-Length: " + bodyBytes.length + "\r\n" +
                             "Connection: close\r\n\r\n";

            out.write(headers.getBytes(StandardCharsets.US_ASCII));
            out.write(bodyBytes);
            out.flush();

            System.out.println("[" + Thread.currentThread().getName() + "] " + requestLine + " -> " + status);
        }
    }

    /**
     * Sirve el favicon real desde el classpath: src/main/resources/favicon.ico
     */
    private void serveFavicon(OutputStream out) throws IOException {
        try (InputStream iconStream = HiloPorClienteServidor.class.getResourceAsStream("/favicon.ico")) {

            if (iconStream == null) {
                out.write(("HTTP/1.1 404 Not Found\r\nConnection: close\r\n\r\n")
                        .getBytes(StandardCharsets.US_ASCII));
                out.flush();
                return;
            }

            byte[] iconBytes = iconStream.readAllBytes();

            String headers =
                    "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: image/x-icon\r\n" +
                    "Content-Length: " + iconBytes.length + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";

            out.write(headers.getBytes(StandardCharsets.US_ASCII));
            out.write(iconBytes);
            out.flush();
        }
    }

    private synchronized boolean isStopped() {
        return isStopped;
    }

    private void openServerSocket() {
        try {
            this.serversocket = new ServerSocket(this.serverPort);
        } catch (IOException ex) {
            throw new RuntimeException("Cannot open port " + serverPort, ex);
        }
    }

    public synchronized void stop() {
        this.isStopped = true;
        try {
            if (this.serversocket != null) {
                this.serversocket.close();
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}
