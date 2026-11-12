package main.model;


/**
 * @author Adrià
 */
public class RequisitoMaterial {
    private final Mision mision;
    private final Objeto objeto;
    private int cantidad;

    //constructor
    public RequisitoMaterial(Mision mision, Objeto objeto, int cantidad) {
        if (mision == null) {
            throw new IllegalArgumentException("La misión no puede ser nula.");
        }
        if (objeto == null) {
            throw new IllegalArgumentException("El objeto no puede ser nulo.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad tiene que ser mayor que 0."); 
        }

        this.mision = mision;
        this.objeto = objeto;
        this.cantidad = cantidad;
    }

    //getters
    public Mision getMision() {
        return mision;
    }

    public Objeto getObjeto() {
        return objeto;
    }

    public int getCantidad() {
        return cantidad;
    }

    //modifica la cantidad 
    public void setCantidad(int cantidad) {
        if (mision.tieneEjecuciones()) {
            throw new IllegalStateException("No se puede modificar un requisito material de una misión ya ejecutada."); 
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que 0.");
        }
        this.cantidad = cantidad;
    }

    //comprueba si hay stock suficiente
    public boolean esSuficienteEnAlmacen(){
        return objeto.estaDisponible(cantidad);
    }

    //devuelve el numero de unidades que faltan para llegar a la cantidad
    public int unidadesFaltantes(){
        int cant = cantidad - objeto.getStock();
        if(cant < 0){
            return 0; //si ya hay mas stock q el necesario devuelve 0
        }
        else{
            return cant;
        }
    }
}