package main.model;

import java.util.List;

// ALEJANDRO: Esta clase es lo que devuelve leerSecuaces y leerMisiones
public class ResumenImportacion <T> {       // T es el tipo de los datos leidos (DatosSecuaz o DatosMision)
    private final List<T> datos;            // Datos leidos
    private final List<String> avisos;      // Avisos de lineas invalidas

    public ResumenImportacion(List<T> datos, List<String> avisos) {
        if (datos == null) {
            throw new IllegalArgumentException("datos no puede ser null");
        }
        if (avisos == null) {
            throw new IllegalArgumentException("avisos no puede ser null");
        }
        this.datos = List.copyOf(datos);
        this.avisos = List.copyOf(avisos);
    }

    public List<T> getDatos() {
        return datos;
    }

    public List<String> getAvisos() {
        return avisos;
    }
}