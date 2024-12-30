package main;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JOptionPane;


public class FilsClient implements Runnable {
	private final String accion;
	private final Socket socket;

	public FilsClient(String accion, Socket socket) {
		this.accion = accion;
		this.socket = socket;
	}

	@Override
	public void run() {
		if ("envia".equals(accion)) {
			enviar();
		} else {
			recibir();
		}
	}
	
	/**
	 * Funcion que esta activamente escuchando a la espera de mensajes del servidor
	 */
	private void recibir() {
		try (BufferedReader bfr = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
			String linea;
			while ((linea = bfr.readLine()) != null) {
				System.out.println(linea);
			}
		} catch (IOException e) {
			// System.err.println("CLIENT >>> Error recibiendo mensajes.");
			// e.printStackTrace();
		}
	}

	/**
	 * Funcion encargada de enviar mensajes al servidor
	 */
	private void enviar() {
		try (PrintWriter pw = new PrintWriter(socket.getOutputStream(), true)) {
			Scanner sc = new Scanner(System.in);

			for (int i = 0; i < 2; i++) {
				String mensaje = sc.nextLine();

				if (mensaje.equals("exit")) {
					pw.println(mensaje);
					break;
				}

				if (mensaje != null && !mensaje.trim().isEmpty()) {
					pw.println(mensaje);
				}
			}
			System.out.println("Presiona Enter para enviar un mensaje:");
			while (true) {

				String mensaje = sc.nextLine();

				if (mensaje.equals("exit")) {
					pw.println(mensaje);
					socket.close();
					System.exit(0);
					break;
				}

				if (mensaje.isEmpty()) {
					JFrame parentFrame = new JFrame();
                    parentFrame.setAlwaysOnTop(true);
                    String popupMensaje = JOptionPane.showInputDialog(parentFrame, "Introduce 'exit' para cerrar:", "Input", JOptionPane.PLAIN_MESSAGE);

                    if (popupMensaje != null && popupMensaje.equalsIgnoreCase("exit")) {
                        pw.println("exit");
                        socket.close();
    					System.exit(0);
                        break;
                    }
                    
					if (popupMensaje != null && !popupMensaje.isEmpty()) {
						pw.println(popupMensaje);
					}

				} else {
					pw.println(mensaje);
				}
			}

			sc.close();
		} catch (IOException e) {
			System.err.println("CLIENT >>> Error enviando mensajes.");
			// e.printStackTrace();
		}
	}
}
