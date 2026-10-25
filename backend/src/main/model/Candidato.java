package main.model;

import java.util.List;
import java.util.Random;

public class Candidato {

    private String nombre;
    private String apodo;
    private long precioFichaje;
    private TRol especialidad;
    private boolean enOferta;
    private boolean fichado;

    public static final int MAX_EN_OFERTA = 4;

    public Candidato(String nombre, String apodo, long precioFichaje, TRol especialidad){
        if(nombre == null) throw new IllegalArgumentException("El nombre no puede ser nulo");
        if(especialidad == null) throw new IllegalArgumentException("La especialidad no puede ser nula");
        if(precioFichaje <= 0) throw new IllegalArgumentException("El precio de fichaje debe ser mayor a 0");

        this.nombre = nombre;
        this.apodo = apodo;
        this.precioFichaje = precioFichaje;
        this.especialidad = especialidad;

        enOferta = false;
        fichado = false;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApodo() {
        return apodo;
    }

    public long getPrecioFichaje() {
        return precioFichaje;
    }

    public TRol getEspecialidad() {
        return especialidad;
    }

    public boolean isEnOferta() {
        return enOferta;
    }

    public boolean isFichado() {
        return fichado;
    }

    public boolean esElegible() {
        return !fichado;
    }

    public void ponerEnOferta() {
        if(!this.esElegible()) throw new IllegalStateException("El candidato no se puede fichar");
        enOferta = true;
    }

    public void retirarDeOferta() {
        enOferta = false;
    }

    public void marcarFichado() {
        if(!enOferta) throw new IllegalStateException("El candidato no estaba en oferta");
        enOferta = false;
        fichado = true;
    }

}