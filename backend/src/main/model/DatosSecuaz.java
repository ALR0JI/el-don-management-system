package main.model;

// ALEJANDRO: Actua de forma similar a una tupla
public class DatosSecuaz {
    private final String nombre;
    private final TRol rol;

    public DatosSecuaz(String nombre, TRol rol) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del secuaz no puede ser null ni vacio");
        }
        if (rol == null) {
            throw new IllegalArgumentException("El rol no puede ser null");
        }

        this.nombre = nombre;
        this.rol = rol;
    }

    public String getNombre() {
        return nombre;
    }

    public TRol getRol() {
        return rol;
    }
}