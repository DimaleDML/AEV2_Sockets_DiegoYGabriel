package main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class Servidor {
    public static void main(String[] args) {
        System.err.println("SERVIDOR >>> Iniciando servidor...");
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            while (true) {
                Socket conexion = serverSocket.accept();
                System.err.println("SERVIDOR >>> Conexión aceptada. Lanzando hilo...");
                new Thread(new PeticioServer(conexion)).start();
            }
        } catch (SocketException e) {
            System.err.println("SERVIDOR >>> Error de conexión: El cliente se ha desconectado.");
        } catch (IOException e ) {
            System.err.println("SERVIDOR >>> Error al iniciar el servidor.");
            //e.printStackTrace();
        }
    }
}
