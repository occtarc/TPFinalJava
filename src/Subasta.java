import java.io.Serializable;

public class Subasta implements Serializable {
    private Articulo articulo;
    private int tiempo;
    private Subastador subastador;
    private boolean finalizada;
    private Oferta ofertaMayor;

    public Subasta(Articulo articulo, int tiempo, Subastador subastador){
        this.articulo = articulo;
        this.tiempo = tiempo;
        this.subastador = subastador;
        this.finalizada = false;
        this.ofertaMayor = null;
    }

    public Articulo getArticulo() {return articulo;}
    public int getTiempo(){return tiempo;}
    public Subastador getSubastador() {return subastador;}
    public boolean getFinalizada(){return finalizada;}
    public void setFinalizada(boolean finalizada){this.finalizada = finalizada;}
    public Oferta getOfertaMayor(){return ofertaMayor;}
    public void setOfertaMayor(Oferta ofertaMayor){this.ofertaMayor = ofertaMayor;}
}
