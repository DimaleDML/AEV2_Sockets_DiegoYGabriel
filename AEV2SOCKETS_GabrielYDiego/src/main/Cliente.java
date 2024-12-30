package main;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

public class Cliente {
    public static void main(String[] args) throws IOException {
    	System.out.println("CLIENT >>> Iniciando cliente...");
        InetSocketAddress direccion = new InetSocketAddress("localhost", 5000);
        Socket socket = new Socket();
        try {
            socket.connect(direccion);
            Thread receptor = new Thread(new FilsClient("recibe", socket));
            Thread emisor = new Thread(new FilsClient("envia", socket));
            receptor.start();
            emisor.start();
            receptor.join();
            emisor.join();
        }  catch (SocketException e) {
            System.err.println("SERVIDOR >>> Error de conexión: El cliente se ha desconectado.");
        } catch (IOException | InterruptedException e) {
            System.err.println("CLIENT >>> Error al conectar con el servidor.");
            //e.printStackTrace();
        } finally {
            socket.close();
        }
    }
}
