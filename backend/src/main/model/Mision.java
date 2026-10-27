package main.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Mision{
    public static final int DURACION_POR_DEFECTO = 12;

    private final String nombreClave;
    private String ubicacion;
    private long botin;
    private double probFracaso;
    private int duracion;
    private TEstado estado;

    private final List<Mision> prerrequisitos = new ArrayList<>();
    private final List<Mision> dependientes = new ArrayList<>();
    private final List<PlantillaRol> plantillasRol = new ArrayList<>();
    private final List<RequisitoMaterial> requisitosMateriales = new ArrayList<>();
    private final List<Ejecucion> ejecuciones = new ArrayList<>();
    
    public Mision(String nombreClave, String ubicacion, long botin, double probFracaso, Integer duracion, Map<TRol, Integer> plantilla){
        if(nombreClave == null || nombreClave.trim().length() == 0){
            throw new IllegalArgumentException("El nombre clave de la mision no puede ser nulo ni estar vacio.");   
        }

        if(botin <= 0){
            throw new IllegalArgumentException("El botin de la mision ha de ser mayor que 0.");   
        }

        if(probFracaso < 0.0 || probFracaso > 100.0){
            throw new IllegalArgumentException("La probabilidad de fracaso de la mision ha de estar entre 0 y 100.");   
        }

        if(duracion != null && duracion <= 0){
            throw new IllegalArgumentException("La duracion de la mision ha de ser mayor que 0.");   
        }

        if(plantilla == null || plantilla.isEmpty()){
            throw new IllegalArgumentException("La plantilla de la mision no puede estar vacia.");   
        }

        boolean tieneConductor = false;
        for (Map.Entry<TRol, Integer> entry : plantilla.entrySet()) {
            if (entry.getValue() == null || entry.getValue() <= 0) {
                throw new IllegalArgumentException("Todas las cantidades de la plantilla han de ser mayores que 0.");
            }
            if (entry.getKey() == TRol.CONDUCTOR) {
                tieneConductor = true;
            }
        }

        if(!tieneConductor){
            throw new IllegalArgumentException("Todas las plantillas han de tener un conductor.");
        }

        this.nombreClave = nombreClave;
        this.ubicacion = ubicacion;
        this.botin = botin; 
        this.probFracaso = probFracaso;
        if(duracion != null){
            this.duracion = duracion;
        } else{
            this.duracion = DURACION_POR_DEFECTO;
        }

        for (Map.Entry<TRol, Integer> entry : plantilla.entrySet()){
            this.plantillasRol.add(new PlantillaRol(this, entry.getKey(), entry.getValue()));
        }

        actualizarEstado();
    }
    
    //getters
    public String getNombreClave(){
        return nombreClave;
    }

    public String getUbicacion(){
        return ubicacion;
    }

    public long getBotin(){
        return botin;
    }

    public double getProbFracaso(){
        return probFracaso;
    }

    public int getDuracion(){
        return duracion;
    }

    public TEstado getEstado(){
        return estado;
    }

    public List<Mision> getPrerrequisitos(){
        return Collections.unmodifiableList(prerrequisitos);
    }

    public List<Mision> getDependientes(){
        return Collections.unmodifiableList(dependientes);
    }

    public List<PlantillaRol> getPlantillasRol(){
        return Collections.unmodifiableList(plantillasRol);
    }

    public List<RequisitoMaterial> getRequisitosMateriales(){
        return Collections.unmodifiableList(requisitosMateriales);
    }

    public List<Ejecucion> getEjecuciones(){
        return Collections.unmodifiableList(ejecuciones);
    }

    //setters
    public void setUbicacion(String ubicacion){
        comprobarEditable();
        this.ubicacion = ubicacion;
    }

    public void setBotin(long botin){
        comprobarEditable();
        if (botin <= 0) {
            throw new IllegalArgumentException("El botin de la mision ha de ser mayor que 0.");
        }
        this.botin = botin;
    }

    public void setProbFracaso(double probFracaso){
        comprobarEditable();
        if (probFracaso < 0.0 || probFracaso > 100.0) {
            throw new IllegalArgumentException("La probabilidad de fracaso de la mision ha de estar entre 0 y 100.");
        }
        this.probFracaso = probFracaso;
    }

    public void setDuracion(int duracion){
        comprobarEditable();
        if (duracion <= 0) {
            throw new IllegalArgumentException("La duracion de la mision ha de ser mayor que 0.");
        }
        this.duracion = duracion;
    }

    public void comprobarEditable(){
        if (tieneEjecuciones()) {
            throw new IllegalStateException("No se puede editar una mision que ya ha sido ejecutada.");
        }
    }

    public boolean tieneEjecuciones(){
        return !ejecuciones.isEmpty();
    }

    //consultas no de atributos
    public int getNumIntentos(){
        int count = 0;
        for (Ejecucion e : ejecuciones) {
            if (e.getResultado() != TResultado.EN_CURSO) {
                count++;
            }
        }
        return count;
    }

    public int getNumFracasos(){
        int count = 0;
        for (Ejecucion e : ejecuciones) {
            if (e.getResultado() == TResultado.FRACASO) {
                count++;
            }
        }
        return count;
    }

    public int getPersonalTotal(){
        int total = 0;
        for (PlantillaRol p : plantillasRol) {
            total += p.getCantidad();
        }
        return total;
    }

    public int getCantidadRol(TRol rol){
        for (PlantillaRol p : plantillasRol) {
            if (p.getRol() == rol) {
                return p.getCantidad();
            }
        }
        return 0;
    }

    public void actualizarEstado(){
        if (tieneEjecuciones()) {
            boolean exito = false;
            boolean seHaEjecutado = false;

            for (Ejecucion e : ejecuciones) {
                if (e.estaEnCurso()) {
                    this.estado = TEstado.EN_CURSO;
                    return;
                }
                if (e.getResultado() == TResultado.EXITO) {
                    exito = true;
                }
                seHaEjecutado = true;
            }

            if (exito) {
                this.estado = TEstado.COMPLETADA;
                for (Mision dep : dependientes) {
                    dep.actualizarEstado();
                }
                return;
            } else if (seHaEjecutado) {
                this.estado = TEstado.FALLIDA;
                return;
            }
        }

        for (Mision pre : prerrequisitos) {
            if (pre.getEstado() != TEstado.COMPLETADA) {
                this.estado = TEstado.BLOQUEADA;
                return;
            }
        }

        this.estado = TEstado.DISPONIBLE;
    }

    public boolean puedeIniciarse(){
        return (estado == TEstado.DISPONIBLE || estado == TEstado.FALLIDA);
    }

    //PARA LA SEGUNDA ENTREGA!
    public void añadirRequisitoMaterial(Objeto objeto, int cantidad) {
        throw new UnsupportedOperationException("Pendiente 2.ª entrega!");
    }

    public void eliminarRequisitoMaterial(Objeto objeto) {
        throw new UnsupportedOperationException("Pendiente 2.ª entrega!");
    }

    public Map<Objeto, Integer> materialFaltante() {
        throw new UnsupportedOperationException("Pendiente 2.ª entrega!");
    }

    public long costeMaterialFaltante() {
        throw new UnsupportedOperationException("Pendiente 2.ª entrega!");
    }
}