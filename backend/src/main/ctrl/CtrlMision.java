package main.ctrl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import main.model.Mision;

public class CtrlMision {
    private final Map<String, Mision> misiones = new LinkedHashMap<>();

    public Mision get(String nombreClave) {
        return misiones.get(nombreClave);
    }

    public boolean exists(String nombreClave) {
        return misiones.containsKey(nombreClave);
    }

    public List<Mision> all() {
        return List.copyOf(misiones.values());
    }

    public void insertar(Mision mision) {
        if (mision == null) {
            throw new IllegalArgumentException("La misión no puede ser null");
        }
        if (misiones.containsKey(mision.getNombreClave())) {
            throw new IllegalArgumentException("La misión " + mision.getNombreClave() + " ya existe");
        }
        misiones.put(mision.getNombreClave(), mision);
    }

    public void eliminar(String nombreClave) {
        if (nombreClave == null) {
            throw new IllegalArgumentException("El nombre clave no puede ser null");
        }
        if (!misiones.containsKey(nombreClave)) {
            throw new IllegalArgumentException("La misión " + nombreClave + " no existe");
        }
        misiones.remove(nombreClave);
    }
}