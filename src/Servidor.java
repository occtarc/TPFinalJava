import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor{
    private static final int PUERTO = 5555;
    private static ServerSocket server;
    private static Subasta subasta;
    private static boolean subastaActiva = false;

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
                    if(subastaActiva){
                        System.out.println("Ya hay un subastador. No se permite otro subastador.");
                        dataOut.writeUTF("Ya hay un subastador. No puedes conectarte como Subastador.");
                        socket.close();
                    }else{
                        System.out.println("Subastador conectado: " + usuario.getNombre());
                        dataOut.writeUTF("Te has conectado correctamente al servidor como Subastador");
                        new Thread(new HiloSubastador(socket,objectOut,objectIn,dataOut,dataIn)).start();
                    }
                }else{
                    System.out.println("Participante conectado: " + usuario.getNombre());
                    dataOut.writeUTF("Te has conectado correctamente al servidor como Participante");
                    new Thread(new HiloParticipante(socket)).start();
                }
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

        public HiloParticipante(Socket socket){
            this.socket = socket;
            try{
                objectOut = new ObjectOutputStream(socket.getOutputStream());
                objectIn = new ObjectInputStream(socket.getInputStream());
                dataOut = new DataOutputStream(socket.getOutputStream());
                dataIn = new DataInputStream(socket.getInputStream());
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            System.out.println("Hola Participante");

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
            System.out.println("Hola subastador");
            int opcion;
            while(true){
                try{
                    opcion = dataIn.readInt();
                    switch (opcion){
                        case 1:
                            if(Servidor.subastaActiva){
                                dataOut.writeUTF("Ya hay una subasta en curso, debes esperar a que finalice");
                            }else{
                                Subasta subasta =(Subasta)objectIn.readObject();
                                Servidor.subasta = subasta;
                                Servidor.subastaActiva = true;
                                System.out.println("Subasta iniciada correctamente");
                                dataOut.writeUTF("Subasta iniciada correctamente");
                                new Thread(new Temporizador(30,dataOut)).start();
                                break;
                            }
                        case 2:
                            if(Servidor.subastaActiva){
                                objectOut.writeObject(Servidor.subasta);
                            }else{
                                objectOut.writeObject(null);
                            }
                            break;
                        default:
                            dataOut.writeUTF("Debes ingresar una opción valida");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
    }

    private static class Temporizador implements Runnable{
        private int segundosIniciales;
        private int segundosRestantes;
        private DataOutputStream dataOut;
        public Temporizador(int segundos, DataOutputStream dataOut){
            this.segundosIniciales = segundos;
            this.segundosRestantes = segundos;
            this.dataOut = dataOut;
        }

        @Override
        public void run() {
            correrTiempo();
        }
        public void correrTiempo(){
            while(segundosRestantes > 0){
                System.out.println("Tiempo restante: " + segundosRestantes + " segundos");
                synchronized (this){
                    segundosRestantes--;
                }
                try{
                    Thread.sleep(1000);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

        public void reiniciarTiempo(){
            synchronized (this){
                segundosRestantes = segundosIniciales;
            }
        }
    }
}