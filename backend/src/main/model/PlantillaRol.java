package main.model;

/**
 * @author Adrià
 */
public class PlantillaRol{
    private final Mision mision; //nunca se pueden cambiar por eso el final
    private final TRol rol;
    private int cantidad;

    public PlantillaRol(Mision mision, TRol rol, int cantidad){
        if(mision == null){
            throw new IllegalArgumentException("Mision no puede ser null.");
        }
        
        if(rol == null){
            throw new IllegalArgumentException("Rol no puede ser null.");
        }

        if(cantidad<=0){
            throw new IllegalArgumentException("La cantidad ha de ser mayor que 0.");
        }

        this.mision = mision;
        this.rol = rol;
        this.cantidad = cantidad;
    }

    //getters de los atributos
    public Mision getMision(){
        return mision;
    }

    public TRol getRol(){
        return rol;
    }

    public int getCantidad(){
        return cantidad;
    }
    
    //cambia la cantidad de personas con el rol "rol" en "mision"
    public void setCantidad(int cantidad){
        if(mision.tieneEjecuciones()){
            throw new IllegalStateException("No se puede modifical la cantidad de una mision ya ejecutada.");
        }

        if(cantidad<=0){
            throw new IllegalArgumentException("La cantidad ha de ser mayor que 0.");
        }
        this.cantidad = cantidad;
    }
}