import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Servidor{
    private static final int PUERTO = 5555;
    private static ServerSocket server;
    private static Subasta subasta;
    private static boolean subastaActiva = false;
    private static boolean subastadorConectado = false;
    private static int tiempoRestante;
    private static Timer temporizador;
    private static ArrayList<ObjectOutputStream> objectosActualizaciones = new ArrayList<>();

    private static void enviarActualizacionGlobal(int opcion){
        String mensaje = null;
        switch (opcion){
            case 1 :
                mensaje = String.format("Se ha iniciado una subasta. \n" +
                        "Subastador: %s\n" +
                        "Producto a subastar: \n%s\n" +
                        "Duracion de la subasta: %d",subasta.getSubastador().getNombre(), subasta.getArticulo() , subasta.getTiempo());
                break;
            case 2:
                if(subasta.getOfertaMayor() == null){
                    mensaje = "La subasta ha finalizado sin ofertas para el siguiente articulo: \n" + subasta.getArticulo() ;
                }else{
                    mensaje = String.format( "La subasta ha finalizado.\n" +
                            "Ganador: %s\n" +
                            "Monto final: $%.2f", subasta.getOfertaMayor().getParticipante().getNombre(), subasta.getOfertaMayor().getMonto());

                }
                break;
            case 3:
                mensaje = String.format("Se ha registrado una nueva oferta mayor \n" +
                        "Ofertante: %s\n" +
                        "Monto: $%.2f", subasta.getOfertaMayor().getParticipante().getNombre(), subasta.getOfertaMayor().getMonto());;
                break;
            case 4:
                mensaje = "Quedan 10 segundos para que finalice la subasta!";
        }

        for (ObjectOutputStream objOut : objectosActualizaciones){
            try{
                objOut.writeObject(mensaje);
                objOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void enviarMensajeIndividual(String mensaje, ObjectOutputStream objOut){
        try {
            objOut.writeObject(mensaje);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void iniciarTemporizador(){
        temporizador = new Timer();
        tiempoRestante = subasta.getTiempo();
        temporizador.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(tiempoRestante == 10){
                    enviarActualizacionGlobal(4);
                }
                if(tiempoRestante == 0){
                    subastaActiva = false;
                    enviarActualizacionGlobal(2);
                    temporizador.cancel();
                    System.out.println("Subasta finalizada. Aguardando comienzo de la siguiente.");
                }else{
                    tiempoRestante--;
                    System.out.println("Quedan " + tiempoRestante + " segundos");
                }
            }
        },0,1000);
    }

    public static void main(String[] args){
        try{
            server = new ServerSocket(Servidor.PUERTO);
            System.out.println("Servidor corriendo en el puerto " + Servidor.PUERTO);

            while(true){
                Socket socket = server.accept();
                ObjectOutputStream objectOut = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream objectIn = new ObjectInputStream(socket.getInputStream());
                DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                DataInputStream dataIn = new DataInputStream(socket.getInputStream());
                Usuario usuario = (Usuario)objectIn.readObject();
                if(usuario.getRol() == Rol.SUBASTADOR){
                    if(subastadorConectado){
                        System.out.println("Ya hay un subastador. No se permite otro subastador.");
                        enviarMensajeIndividual("Ya hay un subastador. No puedes conectarte como subastador en este momento. Intentalo mas tarde", objectOut);
                        socket.close();
                    }else{
                        System.out.println("Subastador conectado: " + usuario.getNombre());
                        enviarMensajeIndividual("Te has conectado correctamente al servidor como subastador", objectOut);
                        subastadorConectado = true;
                        new Thread(new HiloSubastador(socket,objectOut,objectIn,dataOut,dataIn)).start();
                    }
                }else{
                    System.out.println("Participante conectado: " + usuario.getNombre());
                    enviarMensajeIndividual("Te has conectado correctamente al servidor como participante", objectOut);
                    new Thread(new HiloParticipante(socket,objectOut,objectIn,dataOut,dataIn)).start();
                }
                objectosActualizaciones.add(objectOut);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static class HiloParticipante implements Runnable{
        private Socket socket;
        private ObjectOutputStream objectOut;
        private ObjectInputStream objectIn;
        private DataOutputStream dataOut;
        private DataInputStream dataIn;

        public HiloParticipante(Socket socket, ObjectOutputStream objectOut, ObjectInputStream objectIn,
                                DataOutputStream dataOut, DataInputStream dataIn){
            this.socket = socket;
            this.objectOut = objectOut;
            this.objectIn = objectIn;
            this.dataOut = dataOut;
            this.dataIn = dataIn;
            System.out.println("Hilo Participante creado correctamente");
        }

        @Override
        public void run() {
            int opcion;
            while(true){
                try {
                    opcion = dataIn.readInt();
                        switch(opcion){
                            case 1:
                                System.out.println("Prueba 1");
                                if(!subastaActiva){
                                    System.out.println("Prueba 2");
                                    enviarMensajeIndividual("Espera a que haya una subasta activa para realizar una oferta",objectOut);
                                }else{
                                    System.out.println("Prueba 3");
                                    Oferta ofertaCliente = (Oferta) objectIn.readObject();
                                    if(Servidor.subasta.getOfertaMayor() == null || ofertaCliente.getMonto() > Servidor.subasta.getOfertaMayor().getMonto() ){
                                        Servidor.subasta.setOfertaMayor(ofertaCliente);
                                        temporizador.cancel();
                                        tiempoRestante = subasta.getTiempo();
                                        iniciarTemporizador();
                                        System.out.println("Actualizacion realizada, nueva oferta mayor: " + Servidor.subasta.getOfertaMayor().getMonto());
                                        System.out.println(Servidor.subasta.getOfertaMayor());
                                        enviarMensajeIndividual("Oferta recibida correctamente. Actualmente tu oferta es la mayor", objectOut);
                                        enviarActualizacionGlobal(3);
                                    }else{
                                        enviarMensajeIndividual("Oferta rechazada. La oferta realizada no supera el monto de la oferta mayor", objectOut);
                                    }
                                }
                                break;
                            case 2:
                            default:
                                enviarMensajeIndividual("Debes ingresar una opción valida", objectOut);
                        }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    private static class HiloSubastador implements Runnable{
        private final Socket socket;
        private final ObjectOutputStream objectOut;
        private final ObjectInputStream objectIn;
        private final DataOutputStream dataOut;
        private final DataInputStream dataIn;


        public HiloSubastador(Socket socket, ObjectOutputStream objectOut, ObjectInputStream objectIn,
                              DataOutputStream dataOut, DataInputStream dataIn){
            this.socket = socket;
            this.objectOut = objectOut;
            this.objectIn = objectIn;
            this.dataOut = dataOut;
            this.dataIn = dataIn;
            System.out.println("Hilo Subastador creado correctamente");
        }

        @Override
        public void run() {
            int opcion;
            while(true){
                try{
                    opcion = dataIn.readInt();
                    switch (opcion){
                        case 1:
                            if(Servidor.subastaActiva){
                                enviarMensajeIndividual("Ya hay una subasta en curso, debes esperar a que finalice", objectOut);
                            }else{
                                Subasta subasta =(Subasta)objectIn.readObject();
                                Servidor.subasta = subasta;
                                Servidor.subastaActiva = true;
                                iniciarTemporizador();
                                System.out.println("Subasta iniciada correctamente");
                                enviarMensajeIndividual("Subasta iniciada correctamente", objectOut);
                                Servidor.enviarActualizacionGlobal(1);
                            }
                            break;
                        case 2:
                            break;
                        default:
                            enviarMensajeIndividual("Debes ingresar una opción valida", objectOut);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
    }
}