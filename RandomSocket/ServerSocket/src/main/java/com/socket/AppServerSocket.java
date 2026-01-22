package com.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class AppServerSocket 
{
    private final static int PORT = 7777;
    private static int numGen;

    public static void main( String[] args ) 
    {
        // MEJORA 1: Dificultad aumentada (1 al 100)
        numGen = (new Random()).nextInt(100)+1;
        System.out.println("Numero secreto generado (para pruebas): " + numGen);
        
        try {
            ServerSocket srvSock = new ServerSocket(PORT);
            System.out.println("ServerSocket esperando en puerto: " + PORT);

            // Abrimos el socket y escuchamos
            Socket client = srvSock.accept();
            mostrarInfoCliente(client);
            
            // Canales de comunicación
            PrintWriter salida = new PrintWriter(client.getOutputStream(), true);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(client.getInputStream()));
        
            String datoRec, datoEnv;
            
            // MEJORA 3 (parte A): Contador de intentos
            int intentos = 0;

            // Bucle de lectura
            while((datoRec = entrada.readLine())!= null) {
                
                // MEJORA 2: Comando EXIT para apagar servidor
                if (datoRec.equalsIgnoreCase("EXIT")) {
                    salida.println("Cerrando servidor... ¡Adios!");
                    System.out.println("Cliente ordenó cerrar. Apagando.");
                    break; 
                }

                // Incrementamos intentos
                intentos++;
                
                // MEJORA 3 (parte B): Pasamos los intentos al método
                datoEnv = checkNumero(datoRec, intentos);
                
                // Enviamos respuesta
                salida.println(datoEnv);
            }
            
            // Cerrar recursos al salir
            srvSock.close();
            
        } catch(IOException e) {
            System.err.println("Problemas en el socket");
            e.printStackTrace();
        }
    }

    private static void mostrarInfoCliente(Socket client) {
            InetAddress clientAddress = client.getInetAddress();
            String clientIP = clientAddress.getHostAddress();
            System.out.println("Cliente conectado desde IP: " + clientIP);
    }

    // He modificado este método para que acepte 'int intentos'
    private static String checkNumero(String datoRec, int intentos) {
        try {
            int numero = Integer.parseInt(datoRec);
            
            if(numero > numGen) {
                return "<server>El número es mayor que el número mágico";
            } else if(numero < numGen) {
                return "<server>El número es menor que el número mágico";
            } else {
                // MEJORA 3 (parte C): Mensaje de victoria con intentos
                return "<server>¡CORRECTO! Has adivinado el número en " + intentos + " intentos.";
            }
            
        } catch(NumberFormatException e) {
            return "<Server>Por favor, introduzca un número válido";
        }
    }
}