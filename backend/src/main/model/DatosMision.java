package main.model;


import java.util.List;
import java.util.Map;

// Actua como una tupla
public class DatosMision {
    private final String nombreClave;
    private final long botin;
    private final Map<TRol, Integer> plantilla;
    private final double probFracaso;
    private final List<String> dependencias;

    public DatosMision(String nombreClave,
                       long botin,
                       Map<TRol, Integer> plantilla,
                       double probFracaso,
                       List<String> dependencias) {
        if (nombreClave == null || nombreClave.isBlank()) {
            throw new IllegalArgumentException("El nombre clave de la mision no puede ser null ni vacio");
        }
        if(plantilla == null){
            throw new IllegalArgumentException("La plantilla de la mision no puede ser null.");
        }
        if(dependencias == null){
            throw new IllegalArgumentException("Las dependencias de la mision no pueden ser null");
        }
        // El resto de comprobaciones se hacen en confirmarMisiones

        this.nombreClave = nombreClave;
        this.botin = botin;
        this.plantilla = Map.copyOf(plantilla);
        this.probFracaso = probFracaso;
        this.dependencias = List.copyOf(dependencias);
    }

    public String getNombreClave() {
        return nombreClave;
    }

    public long getBotin() {
        return botin;
    }

    public Map<TRol, Integer> getPlantilla() {
        return plantilla;
    }

    public double getProbFracaso() {
        return probFracaso;
    }

    public List<String> getDependencias() {
        return dependencias;
    }
}
