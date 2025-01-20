import java.io.Serializable;

public class Oferta implements Serializable {
    private float monto;
    private Participante participante;

    public Oferta(float monto, Participante participante){
        this.monto = monto;
        this.participante = participante;
    }

    public float getMonto(){return monto;}
    public Participante getParticipante(){return participante;}

    @Override
    public String toString() {
        return "Oferta{" +
                "monto=" + monto +
                ", participante=" + participante +
                '}';
    }
}
