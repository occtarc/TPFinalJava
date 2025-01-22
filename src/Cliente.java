import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class Cliente {
    private static Usuario usuario;
    private static final String DIRECCION_SERVIDOR = "localhost";
    private static final int PUERTO = 5555;
    private static Socket socket;
    private static boolean subastadorConectado;
    private static boolean subastaActiva;

    public Cliente(){}

    public static void main (String[] args){
        Cliente.definirUsuario();

        try {
            Cliente.socket = new Socket(Cliente.DIRECCION_SERVIDOR,Cliente.PUERTO);
            ObjectOutputStream objectOut = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectIn = new ObjectInputStream(socket.getInputStream());
            DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
            DataInputStream dataIn = new DataInputStream(socket.getInputStream());
            objectOut.writeObject(Cliente.usuario);
            Scanner scanner = new Scanner(System.in);
            int opcion;

            new Thread(()->{
                try{
                    while(true){
                        Object mensaje = objectIn.readObject();
                        System.out.println("\n[Notificacion del servidor]: " + mensaje);
                        System.out.print("[Opcion (1 | 2)]> ");
                        if(mensaje instanceof String){
                            String msg = (String) mensaje;
                            if(msg.contains("Ya hay una subasta activa") || msg.contains("Se ha iniciado una subasta") || msg.contains("Hay una subasta en curso")){
                                subastaActiva = true;
                            }
                            else if(msg.contains("La subasta ha finalizado") || msg.contains("Subastador desconectado! Fin de la subasta.")){
                                subastaActiva = false;
                            }
                        }
                    }
                }catch (EOFException | SocketException e){
                    System.exit(0);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }).start();

            if(Cliente.usuario.getRol() == Rol.SUBASTADOR){
                Subastador subastador = (Subastador)Cliente.usuario;
                while(true){
                    System.out.println("1- Iniciar una subasta");
                    System.out.println("2- Salir del sistema");
                    opcion = scanner.nextInt();
                    dataOut.writeInt(opcion);

                    switch (opcion){
                        case 1:
                            if(!subastaActiva){
                                objectOut.writeObject(new Subasta(subastador.generarArticuloASubastar(), subastador.fijarTiempoSubasta(), (Subastador)Cliente.usuario));
                                subastaActiva = true;
                            }
                            break;
                        case 2:
                            desconexion();
                            break;
                    }
                }
            }else{
                Participante participante = (Participante) Cliente.usuario;
                while(true){
                    System.out.println("1- Realizar una oferta");
                    System.out.println("2- Salir del sistema");
                    opcion = scanner.nextInt();
                    dataOut.writeInt(opcion);
                    switch (opcion){
                        case 1:
                            if(subastaActiva){
                                objectOut.writeObject(participante.realizarOferta());
                            }else{
                                System.out.println("Espera a que haya una subasta activa");
                            }
                            break;
                        case 2:
                            desconexion();
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void definirUsuario(){
        Scanner scanner = new Scanner(System.in);
        String nombreUsuario;
        String emailUsuario;
        String rolUsuario;

        System.out.println("Ingrese su nombre: ");
        nombreUsuario = scanner.nextLine();

        System.out.println("Ingrese su email: ");
        emailUsuario = scanner.nextLine();

        System.out.println("¿Desea ser Subastador o Participante? (Ingrese S o P): ");
        boolean rolValido = false;
        do{
            rolUsuario = scanner.nextLine();
            if(rolUsuario.equals("S") || rolUsuario.equals("P")){
                rolValido = true;
            }else{
                System.out.println("Debes ingresar un rol valido. Intente nuevamente");
            }
        }while(!rolValido);

        if (rolUsuario.equals("S")){
            Cliente.setUsuario(new Subastador(nombreUsuario,emailUsuario));
        }else{
            Cliente.setUsuario(new Participante(nombreUsuario,emailUsuario));
        }
    }

    public static void desconexion(){
        try {
            if (socket != null) socket.close();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static Usuario getUsuario(){return usuario;}
    public static void setUsuario(Usuario usuario){Cliente.usuario = usuario;}
}
