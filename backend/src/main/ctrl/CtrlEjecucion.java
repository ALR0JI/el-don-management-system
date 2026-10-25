package main.ctrl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import main.model.Ejecucion;

public class CtrlEjecucion {
    private final Map<Integer, Ejecucion> ejecuciones = new LinkedHashMap<>();

    public Ejecucion get(int id) {
        return ejecuciones.get(id);
    }

    public boolean exists(int id) {
        return ejecuciones.containsKey(id);
    }

    public List<Ejecucion> all() {
        return List.copyOf(ejecuciones.values());
    }

    public void insertar(Ejecucion ejecucion) {
        if (ejecucion == null) {
            throw new IllegalArgumentException("La ejecución no puede ser null");
        }
        if (ejecuciones.containsKey(ejecucion.getId())) {
            throw new IllegalArgumentException("La ejecución con id " + ejecucion.getId() + " ya existe");
        }

        ejecuciones.put(ejecucion.getId(), ejecucion);
    }

    public Integer siguienteId() {
        return ejecuciones.size() + 1;
    }
}