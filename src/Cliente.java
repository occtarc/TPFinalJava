import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {
    private static Usuario usuario;
    private static final String DIRECCION_SERVIDOR = "localhost";
    private static final int PUERTO = 5555;
    private static Socket socket;

    public Cliente(){}

    public static void main (String[] args){
        Cliente.definirUsuario();
        ObjectInputStream objectIn = null;
        ObjectOutputStream objectOut = null;
        DataInputStream dataIn = null;
        DataOutputStream dataOut = null;

        try {
            Cliente.socket = new Socket(Cliente.DIRECCION_SERVIDOR,Cliente.PUERTO);
            objectOut = new ObjectOutputStream(socket.getOutputStream());
            objectIn = new ObjectInputStream(socket.getInputStream());
            dataOut = new DataOutputStream(socket.getOutputStream());
            dataIn = new DataInputStream(socket.getInputStream());
            objectOut.writeObject(Cliente.usuario);
            String mensajeConexionEstablecida = dataIn.readUTF();
            System.out.println(mensajeConexionEstablecida);
            Scanner scanner = new Scanner(System.in);
            int opcion;
            boolean salir = false;

            if(Cliente.usuario.getRol() == Rol.SUBASTADOR){
                Subastador subastador = (Subastador)Cliente.usuario;
                while(!salir){
                    System.out.println("1- Iniciar una subasta");
                    System.out.println("2- Ver subasta actual");
                    System.out.println("3- Salir del sistema");

                    opcion = scanner.nextInt();
                    dataOut.writeInt(opcion);
                    switch (opcion){
                        case 1:
                            objectOut.writeObject(new Subasta(subastador.generarArticuloASubastar(), subastador.fijarTiempoSubasta(), (Subastador)Cliente.usuario));
                            System.out.println(dataIn.readUTF());
                            break;
                        case 2:
                            Subasta subasta = (Subasta) objectIn.readObject();
                            if (subasta == null){
                                System.out.println("Aún no hay una subasta en curso");
                            }else{
                                System.out.println(subasta);
                            }
                            break;
                        case 3:
                            salir = true;
                            break;
                        default:
                            System.out.println(dataIn.readUTF());
                    }
                }
            }else{
                Participante participante = (Participante) Cliente.usuario;
                while(!salir){
                    System.out.println("1- Realizar una oferta");
                    System.out.println("2- Ver subasta actual");
                    System.out.println("3- Salir del sistema");

                    opcion = scanner.nextInt();
                    dataOut.writeInt(opcion);
                    switch (opcion){
                        case 1:
                            objectOut.writeObject(participante.realizarOferta());
                            break;
                        case 2:
                            Subasta subasta = (Subasta) objectIn.readObject();
                            System.out.println(subasta);
                            break;
                        case 3:
                            salir = true;
                            break;
                        default:
                            System.out.println(dataIn.readUTF());
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

    public static Usuario getUsuario(){return usuario;}
    public static void setUsuario(Usuario usuario){Cliente.usuario = usuario;}

}
