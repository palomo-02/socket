package es.iescamas.socket;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class HiloPorClienteServidorTest {

    private HiloPorClienteServidor server;
    private Thread serverThread;
    private int port;

    @BeforeEach
    void startServer() throws Exception {
        try (ServerSocket tmp = new ServerSocket(0)) {
            port = tmp.getLocalPort();
        }
        server = new HiloPorClienteServidor(port);
        serverThread = new Thread(server, "test-server");
        serverThread.start();
        waitUntilListening("127.0.0.1", port, 1000);
    }

    @AfterEach
    void stopServer() throws Exception {
        server.stop();
        serverThread.join(500);
    }

    // --- TEST 1 ---
    @Test
    @DisplayName("GET /nombre/Ana devuelve 200 OK y saludo")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    @Tag("http")
    void shouldSayHelloFromNombreRoute() throws Exception {
        String response = httpGet("/nombre/Ana");
        assertTrue(response.contains("200 OK"));
        assertTrue(response.contains("Hola Ana"));
    }

    // --- TEST 2 ---
    @Test
    @DisplayName("GET /ruta-inventada devuelve 404 Not Found")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    @Tag("http")
    void shouldReturn404ForUnknownRoute() throws Exception {
        String response = httpGet("/no-existe-1234");
        assertTrue(response.contains("404 Not Found"));
        assertTrue(response.contains("no existe"));
    }

    // --- TEST 3: CONCURRENCIA ---
    @Test
    @DisplayName("Dos clientes simultáneos reciben respuesta correcta")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    @Tag("concurrency")
    void shouldHandleConcurrentRequests() throws InterruptedException {
        int numberOfClients = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfClients);
        CountDownLatch latch = new CountDownLatch(numberOfClients);
        String[] responses = new String[numberOfClients];

        executor.submit(() -> {
            try { responses[0] = httpGet("/nombre/ClienteUno"); } 
            catch (Exception e) { e.printStackTrace(); } 
            finally { latch.countDown(); }
        });

        executor.submit(() -> {
            try { responses[1] = httpGet("/nombre/ClienteDos"); } 
            catch (Exception e) { e.printStackTrace(); } 
            finally { latch.countDown(); }
        });

        boolean finished = latch.await(3, TimeUnit.SECONDS);
        assertTrue(finished, "Los clientes no terminaron a tiempo");
        assertTrue(responses[0] != null && responses[0].contains("Hola ClienteUno"));
        assertTrue(responses[1] != null && responses[1].contains("Hola ClienteDos"));
    }

    // --- AUXILIARES ---
    private String httpGet(String path) throws Exception {
        try (Socket s = new Socket("127.0.0.1", port);
             OutputStream out = s.getOutputStream();
             InputStream in = s.getInputStream()) {
            String req = "GET " + path + " HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n";
            out.write(req.getBytes(StandardCharsets.US_ASCII));
            out.flush();
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void waitUntilListening(String host, int port, long maxMs) throws Exception {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < maxMs) {
            try (Socket ignored = new Socket(host, port)) { return; } 
            catch (IOException e) { Thread.sleep(50); }
        }
        fail("El servidor no abrió el puerto a tiempo");
    }
}