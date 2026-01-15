package sockets.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.ServerSocket;

public class ServidorSocketStream {

    public static void main(String[] args) {
        try {
            System.out.println("Creando socket servidor");

            ServerSocket serverSocket = new ServerSocket();

            System.out.println("Realizando el bind");

            //Se cambia de que solo escuche conexiones internas con localhost y pongo que escuche cualquier peticion con 0.0.0.0
            InetSocketAddress addr = new InetSocketAddress("0.0.0.0", 5555);    
            serverSocket.bind(addr);

            System.out.println("Aceptando conexiones");

            Socket newSocket = serverSocket.accept();

            System.out.println("Conexión recibida");

            InputStream is = newSocket.getInputStream();
            OutputStream os = newSocket.getOutputStream();

            byte[] mensaje = new byte[25];
            
            //almacenamos los bytes leidos 
            int bytesLeidos = is.read(mensaje);

            
            if (bytesLeidos != -1) {
                // Creo el String desde el índice 0 hasta bytesLeidos
                // 
            	//la basura que queda en el resto del array se ignora
                String mensajeLimpio = new String(mensaje, 0, bytesLeidos);
                
                System.out.println("Mensaje recibido: " + mensajeLimpio);
                System.out.println("Bytes reales leídos: " + bytesLeidos); 
            }
            
            
            
            System.out.println("Mensaje recibido: " + new String(mensaje));

            System.out.println("Cerrando el nuevo socket");
            newSocket.close();

            System.out.println("Cerrando el socket servidor");
            serverSocket.close();

            System.out.println("Terminado");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}