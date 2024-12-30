package main;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PeticioServer implements Runnable {

	private final Socket socket;

	private static ArrayList<String> canal1 = new ArrayList<String>();
	private static ArrayList<String> canal2 = new ArrayList<String>();
	private static ArrayList<String> canal3 = new ArrayList<String>();
	private static ArrayList<String> canal4 = new ArrayList<String>();

	private static ArrayList<Socket> socketsCanal1 = new ArrayList<Socket>();
	private static ArrayList<Socket> socketsCanal2 = new ArrayList<Socket>();
	private static ArrayList<Socket> socketsCanal3 = new ArrayList<Socket>();
	private static ArrayList<Socket> socketsCanal4 = new ArrayList<Socket>();

	private static ArrayList<ArrayList<String>> canales = new ArrayList<ArrayList<String>>();
	private static ArrayList<ArrayList<Socket>> canalesSockets = new ArrayList<ArrayList<Socket>>();

	private String nombre;
	private int canal;

	// CONSTRUCTOR
	public PeticioServer(Socket socket) {
		this.socket = socket;
		inicializarCanales();
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public int getCanal() {
		return canal;
	}

	public void setCanal(int canal) {
		this.canal = canal;
	}

	@Override
	public void run() {
		try {

	        
			BufferedReader bfr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
			// Enviar lista de canales
			ArrayList<String> canalesDisponibles = leerFichero();
			registrarAccion("Esperando seleccion de canal:");
			pw.println("Lista de canales disponibles: " + canalesDisponibles + "\nSeleccione uno:");

			// Leer opción del cliente
			String opcion = bfr.readLine();

			switch (Integer.parseInt(opcion)) {
			case 1:
				boolean repetido;
				boolean contieneEspacios = false;
				do {
					pw.println("Inserte Nombre: ");
					String nombreTemp = bfr.readLine();
					repetido = buscaRepetido(canal1, nombreTemp);
					if (repetido) {
						pw.println("Nombre ya en uso, intente con otro.");
					}
					if (nombreTemp.contains(" ")) {
						pw.println("Nombre no puede contener espacios.");
						contieneEspacios = true;
					} else {
						contieneEspacios = false;
					}
					if (!repetido && !contieneEspacios) {
						socketsCanal1.add(socket);
						canal1.add(nombreTemp);
						registrarAccion(nombreTemp + " ha entrado al canal 1.");
						setNombre(nombreTemp);
						setCanal(1);
						pw.println("OK");
					}
				} while (repetido || contieneEspacios);
				break;
			case 2:
				boolean repetido2;
				do {
					pw.println("Inserte Nombre: ");
					String nombreTemp = bfr.readLine();
					repetido2 = buscaRepetido(canal2, nombreTemp);

					if (repetido2) {
						pw.println("Nombre ya en uso, intente con otro.");
					} else {
						socketsCanal2.add(socket);
						canal2.add(nombreTemp);
						registrarAccion(nombreTemp + " ha entrado al canal 2.");
						setNombre(nombreTemp);
						setCanal(2);
						pw.println("OK");
					}
				} while (repetido2);
				break;
			case 3:
				boolean repetido3;
				do {
					pw.println("Inserte Nombre: ");
					String nombreTemp = bfr.readLine();
					repetido3 = buscaRepetido(canal3, nombreTemp);

					if (repetido3) {
						pw.println("Nombre ya en uso, intente con otro.");
					} else {
						socketsCanal3.add(socket);
						canal3.add(nombreTemp);
						registrarAccion(nombreTemp + " ha entrado al canal 3.");
						setNombre(nombreTemp);
						setCanal(3);
						pw.println("OK");
					}
				} while (repetido3);
				break;
			case 4:
				boolean repetido4;
				do {
					pw.println("Inserte Nombre: ");
					String nombreTemp = bfr.readLine();
					repetido4 = buscaRepetido(canal4, nombreTemp);

					if (repetido4) {
						pw.println("Nombre ya en uso, intente con otro.");
					} else {
						socketsCanal4.add(socket);
						canal4.add(nombreTemp);
						registrarAccion(nombreTemp + " ha entrado al canal 4.");
						setNombre(nombreTemp);
						setCanal(4);
						pw.println("OK");
					}
				} while (repetido4);
				break;
			default:
			}

			while (true) {
				String mensaje = bfr.readLine();
				if (mensaje == null || mensaje.equals("exit")) {
					salirDelCanal();
					break;
				} else if (mensaje.equals("whois")) {
					ArrayList<String> canalActual = canales.get(canal - 1);
					pw.println("Usuarios en el canal " + canal + ": " + canalActual);
				} else if (mensaje.equals("channels")) {
					pw.println("Canales disponibles: " + canalesDisponibles);
				} else if (mensaje.startsWith("@")) {
					enviaAdiferenteCanal(mensaje,nombre);
		        } else {
					refrescarChat(mensaje,nombre);
				}
				System.err.println("SERVIDOR >>> Mensaje recibido: " + mensaje + " de " + nombre + " al canal " + canal);
			}
			bfr.close();
			pw.close();

		} catch (SocketException e) {
			System.err.println("SERVIDOR >>> Error de conexión: El cliente se ha desconectado.");
		} catch (IOException e) {
			System.err.println("SERVIDOR >>> Error en la conexión.");
			// e.printStackTrace();
		}
	}
	
	/**
	 * Elimina el nombre y el socket del cliente de las listas de sockets y de noombres lo registra e informa
	 */
	public void salirDelCanal() {
        try {
            ArrayList<String> canalActual = canales.get(canal - 1);
            ArrayList<Socket> socketsCanalActual = canalesSockets.get(canal - 1);

            canalActual.remove(nombre);
            socketsCanalActual.remove(socket);

            registrarAccion(nombre + " ha salido del canal " + canal);

            for (Socket socketAenviar : socketsCanalActual) {
                PrintWriter pw = new PrintWriter(socketAenviar.getOutputStream(), true);
                socket.close();
                pw.println(nombre + " ha salido del canal.");
            }

            //socket.close();
        } catch (IOException e) {
            System.err.println("Error al cerrar la conexión para " + nombre + ": " + e.getMessage());
        }
    }
	
	
	/**
	 * Se encarga de mostrar los mensajes que se escriben en un canal a todos los miembros del mismo
	 * @param mensaje string que contiene el mensaje que se envia
	 * @param emisor string que contiene el nombre del usuario que envia el mensaje
	 */
	public void refrescarChat(String mensaje, String emisor) {
		try {
			for (Socket socketAenviar : canalesSockets.get(canal - 1)) {
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
				String fecha = sdf.format(new Date());
				PrintWriter pw = new PrintWriter(socketAenviar.getOutputStream(), true);
				if (socketAenviar.equals(socket)) {
					pw.println("[ "+fecha+" ]: " + mensaje);
				} else {
					pw.println("[ "+fecha+" ] = " + emisor + ": " + mensaje);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Se encarga de mostrar los mensajes que se escriben a un canal seleccionado dentro del mensaje
	 * @param mensaje string que contiene el mensaje que se envia
	 * @param emisor string que contiene el nombre del usuario que envia el mensaje
	 */
	public void enviaAdiferenteCanal(String mensaje, String emisor) {

	    if (mensaje.startsWith("@")) {
	        String[] partes = mensaje.substring(1).split(" ", 2);
	        if (partes.length == 2) {
	            String canalAbuscar = partes[0];
	            String mensajeSinCanal = partes[1];

	            if (canalAbuscar.matches("canal\\d")) {
	                Integer indiceCanal = Integer.parseInt(canalAbuscar.substring(5));

	                if (indiceCanal >= 1 && indiceCanal <= this.canales.size()) {
	                    try {
	                    	refrescarChat(mensaje,emisor);
	                        for (Socket socketAenviar : canalesSockets.get(indiceCanal - 1)) {
	                           
	                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	                            String fecha = sdf.format(new Date());
	                            PrintWriter pw = new PrintWriter(socketAenviar.getOutputStream(), true);
	                            pw.println("[ " + fecha + " ] = " + emisor + ": " + mensaje);
	                        }
	                    } catch (IOException e) {
	                        System.out.println("Error al enviar el mensaje: " + e.getMessage());
	                        e.printStackTrace();
	                    }
	                } else {
	                    enviarMensajeError("Canal no existe", emisor);
	                }
	            } else {
	                enviarMensajeError("Formato de canal incorrecto", emisor);
	            }
	        } else {
	            enviarMensajeError("Mensaje mal formateado", emisor);
	        }
	    } else {
	        enviarMensajeError("El mensaje debe comenzar con '@'", emisor);
	    }
	}

	
	/**
	 * Funcion encargada de generar mensajes de error con formato
	 * @param error string con el texto que se muestra en el error
	 * @param emisor emisor del error (actualmente en desuso)
	 */
	public void enviarMensajeError(String error, String emisor) {
	    try {
	        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	        String fecha = sdf.format(new Date());
	        PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
	        pw.println("[ " + fecha + " ] = Error: " + error);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}

	/**
	 * funcion encargada de leer un fichero .txt predefinido y devuelve el contenido
	 * @return ArrayList con el contenido del fichero separado por saltos de linea
	 */
	public ArrayList<String> leerFichero() {
		File fichero = new File("canals.txt");
		ArrayList<String> nombreCanales = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(fichero))) {
			String linea;
			while ((linea = br.readLine()) != null) {
				nombreCanales.add(linea);
			}
		} catch (IOException e) {
			// e.printStackTrace();
		}
		return nombreCanales;
	}
	/**
	 * funcion que registra una accion en la consola del servidor servidor
	 * @param accion string con la accion que se registra en la consola del servidor
	 */
	public static void registrarAccion(String accion) {
		System.err.println("SERVIDOR >>> " + accion);
	}
	
	/**
	 * busca si el nombre de usuario del cliente ya esta en una lista
	 * @param lista lista donde buscaremos el nombre
	 * @param nombre nombre a buscar en la lista
	 * @return boolean ( true si el nombre ya esta en la lista, false si no esta)
	 */
	public synchronized boolean buscaRepetido(ArrayList<String> lista, String nombre) {
		if (lista.isEmpty()) {
			return false;
		}
		for (String nombreActual : lista) {
			if (nombreActual.equals(nombre)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Funcion para rellenar las listas de canales con los diferentes canales y la de canalesSockets con las diferentes listas de sockets
	 */
	public static void inicializarCanales() {
		if (canales.size() == 4) {
			return;
		}
		canales.add(canal1);
		canales.add(canal2);
		canales.add(canal3);
		canales.add(canal4);
		canalesSockets.add(socketsCanal1);
		canalesSockets.add(socketsCanal2);
		canalesSockets.add(socketsCanal3);
		canalesSockets.add(socketsCanal4);
	}
}
