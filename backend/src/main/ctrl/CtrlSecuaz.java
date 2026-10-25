package main.ctrl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import main.model.Secuaz;

public class CtrlSecuaz {
    private final Map<Integer, Secuaz> secuaces = new LinkedHashMap<>();

    public Secuaz get(int id) {
        return secuaces.get(id);
    }

    public boolean exists(int id) {
        return secuaces.containsKey(id);
    }

    public List<Secuaz> all() {
        return List.copyOf(secuaces.values());
    }

    public void insertar(Secuaz secuaz) {
        if (secuaz == null) {
            throw new IllegalArgumentException("El secuaz no puede ser null");
        }
        if (secuaces.containsKey(secuaz.getId())) {
            throw new IllegalArgumentException("El secuaz con id " + secuaz.getId() + " ya existe");
        }
        secuaces.put(secuaz.getId(), secuaz);
    }

    public int siguienteId() {
        return secuaces.size() + 1;
    }
}