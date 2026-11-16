package main.model;

import java.util.Random;


/**
 * @author Adrià
 */
public class Objeto {

    public static final double PROB_PERDIDA_MATERIAL = 25.0;
    private final String nombre;
    private final String descripcion;
    private final long precio;
    private final TUso uso;
    private final TCategoria categoria;
    private int stock;
    
    public Objeto(String nombre, String descripcion, long precio, TUso uso, TCategoria categoria) {
        if (nombre == null || nombre.trim().length() == 0) {
            throw new IllegalArgumentException("El nombre no puede ser nulo ni estar vacío.");
        }
        if (descripcion == null || descripcion.trim().length() == 0) {
            throw new IllegalArgumentException("La descripción no puede ser nula ni estar vacía.");
        }
        if (precio <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que 0.");
        }
        if (uso == null) {
            throw new IllegalArgumentException("El uso del objeto no puede ser nulo.");
        }
        if (categoria == null) {
            throw new IllegalArgumentException("La categoría del objeto no puede ser nula.");
        }

        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.uso = uso;
        this.categoria = categoria;
        this.stock = 0; //se empieza con stock 0
    }
    
    //getters
    public String getNombre() {
        return nombre;
    }
    public String getDescripcion() {
        return descripcion;
    }

    public long getPrecio() {
        return precio;
    }

    public TUso getUso() {
        return uso;
    }

    public TCategoria getCategoria() {
        return categoria;
    }

    public int getStock() {
        return stock;
    }

    //funciones
    public boolean estaDisponible(int cantidad) {
        if(cantidad <= 0){
            throw new IllegalArgumentException("La cantidad debe ser mayor que 0.");
        }
        return stock >= cantidad; 
    }

    //aumenta el stock en "cantidad"
    public void añadir(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a añadir debe ser mayor que 0.");
        }
        this.stock += cantidad; 
    }

    //reduce el stock en "cantidad"
    public boolean descontar(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a añadir debe ser mayor que 0.");
        }
        if (!estaDisponible(cantidad)) {
            return false;
        }
        this.stock -= cantidad;
        return true;
    }

    public boolean esReutilizable() {
        return uso == TUso.REUTILIZABLE;
    }

    public int calcularUnidadesPerdidas(int cantidad, boolean exito, Random random) {
        
        //si el objeto es de tipo "se gasta" se pierden todos
        if (uso == TUso.SE_GASTA) {
            return cantidad; 
        }

        //si la mision tiene exito no se pierde ninguno
        if (exito) {
            return 0; 
        }

        if (random == null) {
            throw new IllegalArgumentException("El generador Random no puede ser nulo.");
        }

        //si la mision no tiene exito y el objeto no es de tipo "reutilizable" se calcula cuantos se pierden
        int perdidas = 0;
        for (int i = 0; i < cantidad; i++) {
            if (random.nextDouble() * 100.0 < PROB_PERDIDA_MATERIAL) { 
                perdidas++;
            }
        }
        return perdidas;
    }
}