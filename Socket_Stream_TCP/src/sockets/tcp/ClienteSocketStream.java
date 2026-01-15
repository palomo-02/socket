package sockets.tcp;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClienteSocketStream {

    public static void main(String[] args) {
        try {
            System.out.println("Creando socket cliente");

            Socket clientSocket = new Socket();

            System.out.println("Estableciendo la conexión");
            // Mi direccion IP es  192.168.1.67, la sustituyo por localHost
            InetSocketAddress addr = new InetSocketAddress("192.168.1.67", 5555);
            clientSocket.connect(addr);

            InputStream is = clientSocket.getInputStream();
            OutputStream os = clientSocket.getOutputStream();

            System.out.println("Enviando mensaje");

            // cambio la longitud del mensaje para ver los bytes 
            String mensaje = "olaCaracola";
            os.write(mensaje.getBytes());

            System.out.println("Mensaje enviado");

            System.out.println("Cerrando el socket cliente");
            clientSocket.close();

            System.out.println("Terminado");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}