import java.io.Serializable;

public class Articulo implements Serializable {
    private String nombre;
    private String descripcion;
    private float precioBase;

    public Articulo(String nombre, String descripcion, float precioBase){
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioBase = precioBase;
    }

    public String getNombre(){return nombre;}
    public String getDescripcion(){return descripcion;}
    public float getPrecioBase(){return precioBase;}

    @Override
    public String toString(){
        return String.format(
                        "Articulo: %s \n" +
                        "Descripcion: %s \n" +
                        "Precio Base: $%.2f"
                        ,nombre,descripcion,precioBase);
    }
}
